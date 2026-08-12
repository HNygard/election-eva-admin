#!/usr/bin/env bash
#
# For every screen that does not render, reports WHY: the exception and the first
# EVA Admin frame in its stack.
#
#   tools/diagnose-pages.sh                 # every failing screen
#   tools/diagnose-pages.sh > report.txt
#
# tools/smoke-pages.sh counts what fails. This says what to fix. The pair is the
# loop: diagnose, fix a cause, re-run smoke, watch the number.
#
# Each page is fetched in its own freshly authenticated session, and the server
# log is read only for the lines that page produced. That matters: correlating
# by timestamp gives the wrong stack when several screens fail in the same
# second, which is exactly what happens here.
#
# ROLE_INDEX picks which role to hold (1 = election event root, 2 = municipality).

set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

BASE="${BASE:-http://localhost:8080}"
USER_ID="${USER_ID:-01017012345}"
ROLE_INDEX="${ROLE_INDEX:-2}"
CONTAINER="${CONTAINER:-eva-admin-wildfly}"

log_lines() { docker logs "$CONTAINER" 2>&1 | wc -l; }

login() {
	local jar="$1"
	curl -s -o /dev/null -c "$jar" --max-time 30 "$BASE/secure/index.xhtml"
	curl -s -o /dev/null -b "$jar" -c "$jar" --max-time 30 \
		-d "username=$USER_ID&secLevel=3" "$BASE/tmpLogin?scanning=false"

	local page viewstate role_id
	page="$(mktemp)"
	curl -s -b "$jar" -c "$jar" -o "$page" --max-time 30 "$BASE/secure/selectRole.xhtml"
	viewstate="$(grep -oE 'name="javax.faces.ViewState"[^>]*value="[^"]*"' "$page" \
		| head -1 | sed 's/.*value="//; s/"$//' || true)"
	role_id="$(grep -oE 'id="fwMainContentForm:[^"]*:selectRole"' "$page" \
		| sed -n "${ROLE_INDEX}p" | sed 's/id="//; s/"$//' || true)"
	rm -f "$page"

	[ -n "$role_id" ] || return 1

	curl -s -o /dev/null -b "$jar" -c "$jar" --max-time 30 \
		--data-urlencode "fwMainContentForm=fwMainContentForm" \
		--data-urlencode "$role_id=$role_id" \
		--data-urlencode "javax.faces.ViewState=$viewstate" \
		"$BASE/secure/selectRole.xhtml"
}

JAR="$(mktemp)"
INDEX="$(mktemp)"
trap 'rm -f "$JAR" "$INDEX"' EXIT

login "$JAR" || {
	echo "Could not select a role -- run tools/seed-db.sh" >&2
	exit 1
}
curl -s -b "$JAR" -o "$INDEX" --max-time 60 "$BASE/secure/index.xhtml"

PAGES="$(grep -oE 'href="/secure/[^"]*\.xhtml"' "$INDEX" | sed 's/href="//; s/"$//' | sort -u)"

redirects=0
while IFS= read -r page; do
	[ -n "$page" ] || continue

	jar="$(mktemp)"
	login "$jar" >/dev/null 2>&1

	before="$(log_lines)"
	code="$(curl -s -o /dev/null -w '%{http_code}' -b "$jar" --max-time 60 "$BASE$page")"

	if [ "$code" = "200" ]; then
		rm -f "$jar"
		continue
	fi

	if [ "$code" = "302" ]; then
		target="$(curl -s -o /dev/null -w '%{redirect_url}' -b "$jar" --max-time 60 "$BASE$page")"
		case "$target" in
		*kontekstvelger*)
			redirects=$((redirects + 1))
			rm -f "$jar"
			continue
			;;
		esac
		printf '%s  %s\n    redirects to %s\n\n' "$code" "$page" "$target"
		rm -f "$jar"
		continue
	fi

	# The server logs the failure asynchronously; reading immediately after the
	# response returns nothing at all, which reads as "no cause" when there is
	# one.
	sleep 1
	new="$(docker logs "$CONTAINER" 2>&1 | tail -n +$((before + 1)))"
	cause="$(printf '%s' "$new" | grep -oE 'Caused by: [a-zA-Z.]+(Exception|Error)[^	]*' \
		| tail -1 | cut -c1-140)"
	frame="$(printf '%s' "$new" | grep -oE 'at no\.valg\.eva\.admin\.[a-zA-Z0-9_.$]+\([A-Za-z]+\.java:[0-9]+\)' \
		| head -1)"

	printf '%s  %s\n' "$code" "$page"
	[ -n "$cause" ] && printf '    %s\n' "$cause"
	[ -n "$frame" ] && printf '    %s\n' "$frame"
	[ -z "$cause$frame" ] && printf '    (nothing logged for this request)\n'
	printf '\n'

	rm -f "$jar"
done <<<"$PAGES"

printf -- '---\n%s screens redirect to the context chooser (correct behaviour, not listed)\n' "$redirects"
