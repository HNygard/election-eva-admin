#!/usr/bin/env bash
#
# Verifies the working tree against the manifest and fails if anything is
# undeclared. This is the control that lets us change files in place without
# losing track of what we changed (see docs/decisions/D002-*).
#
# Rules:
#   * A release file whose content still matches manifest/originals.sha is an
#     untouched ORIGINAL and needs no declaration.
#   * A release file whose content differs must be declared MODIFIED.
#   * A release file that is gone must be declared DELETED.
#   * A file that is not in the release must be declared ADDED or GENERATED,
#     or live under a declared ADDED_TREE prefix.
#   * Every declaration needs a non-empty reason, and must refer to something
#     that actually is in that state. Stale rows fail.
#
# Exit code 0 = clean, 1 = drift.

set -uo pipefail
export LC_ALL=C # keep sort/join/comm collation consistent

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

ORIGINALS=manifest/originals.sha
CHANGES=manifest/changes.tsv

TAB="$(printf '\t')"
FAILURES=0
W="$(mktemp -d)"
trap 'rm -rf "$W"' EXIT

fail() {
	FAILURES=$((FAILURES + 1))
	printf 'FAIL  %s\n' "$1" >&2
}

for required in "$ORIGINALS" "$CHANGES" manifest/BASELINE; do
	if [ ! -f "$required" ]; then
		echo "FAIL  missing $required -- run tools/manifest-generate.sh" >&2
		exit 1
	fi
done

# ---------------------------------------------------------------------------
# Baseline: path -> expected blob sha
# ---------------------------------------------------------------------------
grep -v '^#' "$ORIGINALS" | awk -F'\t' 'NF == 2 { print $2 "\t" $1 }' | sort >"$W/baseline.tsv"
cut -d"$TAB" -f1 "$W/baseline.tsv" >"$W/baseline.paths"

# ---------------------------------------------------------------------------
# Declarations from changes.tsv
# ---------------------------------------------------------------------------
declare -A DECLARED=()
TREES=()

lineno=0
while IFS= read -r line || [ -n "$line" ]; do
	lineno=$((lineno + 1))
	case "$line" in '#'* | '') continue ;; esac

	IFS="$TAB" read -r status path milestone reason <<<"$line"

	if [ -z "${path:-}" ] || [ -z "${milestone:-}" ] || [ -z "${reason:-}" ]; then
		fail "$CHANGES:$lineno needs STATUS<TAB>PATH<TAB>MILESTONE<TAB>REASON"
		continue
	fi

	case "$status" in
	ADDED | MODIFIED | DELETED | GENERATED)
		if [ -n "${DECLARED[$path]:-}" ]; then
			fail "$CHANGES:$lineno $path declared twice"
			continue
		fi
		DECLARED["$path"]="$status"
		;;
	ADDED_TREE)
		# A whole directory of our own scaffolding. Never allowed to overlap the
		# release tree -- otherwise it could be used to mask edits to EVA code.
		prefix="${path%/}/"
		if grep -q "^${prefix}" "$W/baseline.paths"; then
			fail "$CHANGES:$lineno ADDED_TREE $path overlaps release files; declare those individually"
			continue
		fi
		TREES+=("$prefix")
		;;
	*)
		fail "$CHANGES:$lineno unknown status '$status'"
		;;
	esac
done <"$CHANGES"

under_declared_tree() {
	local p="$1" prefix
	for prefix in ${TREES[@]+"${TREES[@]}"}; do
		case "$p" in "$prefix"*) return 0 ;; esac
	done
	return 1
}

# ---------------------------------------------------------------------------
# Current tree: everything git tracks or would track (ignored files excluded)
#
# ls-files still lists tracked files that have been deleted from the working
# tree, and hashing a path that is not there would fail and shift every
# subsequent hash against its path. So only paths that actually exist go in;
# the rest fall out as missing further down, which is exactly what they are.
# ---------------------------------------------------------------------------
git -c core.quotePath=false ls-files --cached --others --exclude-standard \
	| sort -u >"$W/listed.paths"
: >"$W/current.paths"
while IFS= read -r p; do
	[ -f "$p" ] && printf '%s\n' "$p" >>"$W/current.paths"
done <"$W/listed.paths"

if [ -s "$W/current.paths" ]; then
	git hash-object --stdin-paths <"$W/current.paths" >"$W/current.hashes"
else
	: >"$W/current.hashes"
fi
paste "$W/current.paths" "$W/current.hashes" >"$W/current.tsv"

# ---------------------------------------------------------------------------
# Set math -- no per-file subprocesses, so this stays fast enough for a hook
# ---------------------------------------------------------------------------
join -t"$TAB" "$W/baseline.tsv" "$W/current.tsv" >"$W/both.tsv" # path, base, current
awk -F'\t' '$2 != $3 { print $1 }' "$W/both.tsv" >"$W/changed.paths"
n_original="$(awk -F'\t' '$2 == $3' "$W/both.tsv" | wc -l)"

comm -23 "$W/baseline.paths" "$W/current.paths" >"$W/missing.paths"
comm -13 "$W/baseline.paths" "$W/current.paths" >"$W/new.paths"

n_modified=0
n_added=0
n_generated=0
n_deleted=0

while IFS= read -r path; do
	case "${DECLARED[$path]:-}" in
	MODIFIED) n_modified=$((n_modified + 1)) ;;
	'') fail "$path differs from the release but is not declared MODIFIED in $CHANGES" ;;
	*) fail "$path differs from the release; declared '${DECLARED[$path]}' but must be MODIFIED" ;;
	esac
done <"$W/changed.paths"

while IFS= read -r path; do
	case "${DECLARED[$path]:-}" in
	ADDED) n_added=$((n_added + 1)) ;;
	GENERATED) n_generated=$((n_generated + 1)) ;;
	'')
		if under_declared_tree "$path"; then
			n_added=$((n_added + 1))
		else
			fail "$path is not part of the release and is not declared ADDED in $CHANGES"
		fi
		;;
	*) fail "$path is new; declared '${DECLARED[$path]}' but must be ADDED or GENERATED" ;;
	esac
done <"$W/new.paths"

while IFS= read -r path; do
	if [ "${DECLARED[$path]:-}" = "DELETED" ]; then
		n_deleted=$((n_deleted + 1))
	else
		fail "$path is a release file but is missing; declare it DELETED or restore it"
	fi
done <"$W/missing.paths"

# ---------------------------------------------------------------------------
# Stale declarations: rows that no longer describe reality
# ---------------------------------------------------------------------------
for path in "${!DECLARED[@]}"; do
	status="${DECLARED[$path]}"
	if [ "$status" = "DELETED" ]; then
		grep -qxF "$path" "$W/current.paths" && fail "$path declared DELETED but is present"
		continue
	fi
	if ! grep -qxF "$path" "$W/current.paths"; then
		fail "$path declared $status but does not exist"
		continue
	fi
	if [ "$status" = "MODIFIED" ] && ! grep -qxF "$path" "$W/changed.paths"; then
		fail "$path declared MODIFIED but is identical to the release; drop the row"
	fi
	if { [ "$status" = "ADDED" ] || [ "$status" = "GENERATED" ]; } && grep -qxF "$path" "$W/baseline.paths"; then
		fail "$path declared $status but is a release file"
	fi
done

printf '%s original / %s modified / %s added / %s generated / %s deleted\n' \
	"$n_original" "$n_modified" "$n_added" "$n_generated" "$n_deleted"

if [ "$FAILURES" -gt 0 ]; then
	printf '\n%s manifest problem(s). Declare the change in %s, or revert it.\n' "$FAILURES" "$CHANGES" >&2
	exit 1
fi

echo "Manifest clean."
