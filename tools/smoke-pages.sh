#!/usr/bin/env bash
#
# Logs in as the demo operator, then fetches every page the menu links to and
# reports the HTTP status of each.
#
#   tools/smoke-pages.sh            # every menu link
#   tools/smoke-pages.sh 10         # the first 10, for a quick check
#
# This is the measure of how much of EVA Admin is actually demoable. A page that
# returns 200 renders; it does not follow that it is correct, or that it shows
# the right data. Treat this as a floor, not a verdict.
#
# Requires a running stack and a seeded database.

set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

BASE="${BASE:-http://localhost:8080}"
USER_ID="${USER_ID:-01017012345}"
LIMIT="${1:-0}"

JAR="$(mktemp)"
INDEX="$(mktemp)"
trap 'rm -f "$JAR" "$INDEX"' EXIT

# Log in and select the role, exactly as tools/demo-login.sh does.
curl -s -o /dev/null -c "$JAR" --max-time 30 "$BASE/secure/index.xhtml"
curl -s -o /dev/null -b "$JAR" -c "$JAR" --max-time 30 \
	-d "username=$USER_ID&secLevel=3" "$BASE/tmpLogin?scanning=false"

ROLE_PAGE="$(mktemp)"
curl -s -b "$JAR" -c "$JAR" -o "$ROLE_PAGE" --max-time 30 "$BASE/secure/selectRole.xhtml"
VIEWSTATE="$(grep -oE 'name="javax.faces.ViewState"[^>]*value="[^"]*"' "$ROLE_PAGE" \
	| head -1 | sed 's/.*value="//; s/"$//' || true)"
# ROLE_INDEX picks which role to select, 1-based, in the order the chooser
# lists them. The demo operator holds two: 1 at the election event root, 2 at the
# municipality. Screens that need a geography context only resolve under the
# latter.
ROLE_INDEX="${ROLE_INDEX:-1}"
ROLE_ID="$(grep -oE 'id="fwMainContentForm:[^"]*:selectRole"' "$ROLE_PAGE" \
	| sed -n "${ROLE_INDEX}p" | sed 's/id="//; s/"$//' || true)"
rm -f "$ROLE_PAGE"

if [ -z "$ROLE_ID" ]; then
	echo "No selectable role -- run tools/seed-db.sh" >&2
	exit 1
fi

curl -s -o /dev/null -b "$JAR" -c "$JAR" --max-time 30 \
	--data-urlencode "fwMainContentForm=fwMainContentForm" \
	--data-urlencode "$ROLE_ID=$ROLE_ID" \
	--data-urlencode "javax.faces.ViewState=$VIEWSTATE" \
	"$BASE/secure/selectRole.xhtml"

curl -s -b "$JAR" -c "$JAR" -o "$INDEX" --max-time 60 "$BASE/secure/index.xhtml"

PAGES="$(grep -oE 'href="/secure/[^"]*\.xhtml"' "$INDEX" \
	| sed 's/href="//; s/"$//' | sort -u)"
[ "$LIMIT" -gt 0 ] 2>/dev/null && PAGES="$(printf '%s\n' "$PAGES" | head -"$LIMIT")"

total=0
ok=0
declare -a failures=()

while IFS= read -r page; do
	[ -n "$page" ] || continue
	total=$((total + 1))
	# -b without -c on purpose: the jar is READ, never written back. Letting a
	# page rewrite the session cookie meant one screen that resets state made
	# every screen after it look broken.
	code="$(curl -s -o /dev/null -w '%{http_code}' -b "$JAR" \
		--max-time 60 "$BASE$page")"
	if [ "$code" = "200" ]; then
		ok=$((ok + 1))
		printf '  200  %s\n' "$page"
	else
		failures+=("$code $page")
		printf '  %s  %s\n' "$code" "$page"
	fi
done <<<"$PAGES"

echo
printf '%s of %s pages return 200\n' "$ok" "$total"

if [ "${#failures[@]}" -gt 0 ]; then
	echo
	echo "Not rendering:"
	printf '  %s\n' "${failures[@]}"
fi
