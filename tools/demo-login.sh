#!/usr/bin/env bash
#
# Logs in as the seeded demo operator and fetches a page, driving EVA Admin's
# real authentication end to end.
#
#   tools/demo-login.sh                       # fetch /secure/index.xhtml
#   tools/demo-login.sh secure/rbac/roles.xhtml
#
# Four steps, because that is what the application requires: hit the secure area
# to get a session, post the tmp login form, load the role chooser, then post the
# role selection as a JSF form so the access cache is built.
#
# Prints the HTTP status and size, and leaves the body in the file it names, so
# it can be used to check any screen quickly.

set -euo pipefail

BASE="${BASE:-http://localhost:8080}"
USER_ID="${USER_ID:-01017012345}"
SEC_LEVEL="${SEC_LEVEL:-3}"
PAGE="${1:-secure/index.xhtml}"

JAR="$(mktemp)"
BODY="$(mktemp --suffix=.html)"
trap 'rm -f "$JAR"' EXIT

curl -s -o /dev/null -c "$JAR" --max-time 30 "$BASE/secure/index.xhtml"

curl -s -o /dev/null -b "$JAR" -c "$JAR" --max-time 30 \
	-d "username=$USER_ID&secLevel=$SEC_LEVEL" \
	"$BASE/tmpLogin?scanning=false"

ROLE_PAGE="$(mktemp)"
curl -s -b "$JAR" -c "$JAR" -o "$ROLE_PAGE" --max-time 30 "$BASE/secure/selectRole.xhtml"

# `|| true` because grep exits non-zero on no match and pipefail would abort
# the script before the friendlier diagnostic below can run.
VIEWSTATE="$(grep -oE 'name="javax.faces.ViewState"[^>]*value="[^"]*"' "$ROLE_PAGE" \
	| head -1 | sed 's/.*value="//; s/"$//' || true)"
# The role row is a JSF commandLink; its id is generated, so read it back rather
# than hard-coding j_idt numbers that change whenever the page does.
# ROLE_INDEX picks which role to select, 1-based, in the order the chooser
# lists them. The demo operator holds two: 1 at the election event root, 2 at the
# municipality. Screens that need a geography context only resolve under the
# latter.
ROLE_INDEX="${ROLE_INDEX:-1}"
ROLE_ID="$(grep -oE 'id="fwMainContentForm:[^"]*:selectRole"' "$ROLE_PAGE" \
	| sed -n "${ROLE_INDEX}p" | sed 's/id="//; s/"$//' || true)"
rm -f "$ROLE_PAGE"

if [ -z "$ROLE_ID" ]; then
	echo "No selectable role on /secure/selectRole.xhtml -- is the database seeded?" >&2
	echo "Run tools/seed-db.sh" >&2
	exit 1
fi

curl -s -o /dev/null -b "$JAR" -c "$JAR" --max-time 30 \
	--data-urlencode "fwMainContentForm=fwMainContentForm" \
	--data-urlencode "$ROLE_ID=$ROLE_ID" \
	--data-urlencode "javax.faces.ViewState=$VIEWSTATE" \
	"$BASE/secure/selectRole.xhtml"

STATUS="$(curl -s -b "$JAR" -c "$JAR" -o "$BODY" -w '%{http_code} %{size_download}' \
	--max-time 60 "$BASE/$PAGE")"

echo "$PAGE -> HTTP ${STATUS% *}, ${STATUS#* } bytes"
echo "body: $BODY"
