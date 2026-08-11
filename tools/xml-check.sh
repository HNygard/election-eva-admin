#!/usr/bin/env bash
#
# Checks that every XML or XHTML file we added or modified is well-formed.
#
# .xhtml is included deliberately: Facelets are XML, and a malformed one fails at
# render time with "Error Parsing ... The string \"--\" is not permitted within
# comments", which is a long way from the edit that caused it.
#
# Exists because the same mistake was made three times: a "--" inside an XML
# comment. It is illegal in XML, and the failure surfaces far from the cause --
# once as a Weld bootstrap error deep in a deployment, once as a WAR that
# silently 404s because PARSE failed. A second of checking beats another
# rebuild-and-deploy cycle.
#
# Only files listed in manifest/changes.tsv are checked. Release files are not
# our business, and if one of them were malformed that would be a finding, not
# something to fix here.

set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

TAB="$(printf '\t')"
FAILURES=0

while IFS= read -r line; do
	case "$line" in '#'* | '') continue ;; esac

	status="$(printf '%s' "$line" | cut -d"$TAB" -f1)"
	path="$(printf '%s' "$line" | cut -d"$TAB" -f2)"

	case "$status" in ADDED | MODIFIED | GENERATED) ;; *) continue ;; esac
	case "$path" in *.xml | *.xhtml) ;; *) continue ;; esac
	[ -f "$path" ] || continue

	if ! python3 -c "import sys,xml.dom.minidom; xml.dom.minidom.parse(sys.argv[1])" "$path" 2>/tmp/xml-check-err; then
		FAILURES=$((FAILURES + 1))
		printf 'FAIL  %s\n' "$path" >&2
		sed 's/^/      /' /tmp/xml-check-err >&2
	fi
done <manifest/changes.tsv

rm -f /tmp/xml-check-err

if [ "$FAILURES" -gt 0 ]; then
	printf '\n%s malformed XML file(s).\n' "$FAILURES" >&2
	exit 1
fi

echo "XML well-formed."
