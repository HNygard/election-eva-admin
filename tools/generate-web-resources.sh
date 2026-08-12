#!/usr/bin/env bash
#
# Generates placeholder CSS and JavaScript for the resources/ tree the release
# does not ship (NF-024).
#
# Every h:outputScript and h:outputStylesheet in the pages names a resource that
# is not there, so the browser console fills with
#
#   Unable to find resource javascript/dist/primefaces/SelectOneMenu.js
#   <link rel="stylesheet" href="RES_NOT_FOUND"/>
#
# and JSF renders those messages into the page itself.
#
# The names are recoverable -- they are written at every use site. The CONTENT is
# not: EVA Admin's stylesheets and its App.js component framework were in the
# withheld directory. Each file generated here is a valid, empty placeholder, so
# the resource resolves and the page stops complaining.
#
# THIS DOES NOT RESTORE BEHAVIOUR. Anything the real scripts did -- the select
# widgets, wait buttons, session timeout, dialog handling -- still does nothing.
# The point is a page that renders cleanly enough to read, not a working client.
#
# The stylesheet carries a little layout so the menu is legible; that styling is
# ours and looks nothing like EVA Admin.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

WEBAPP=admin/admin-frontend/src/main/webapp
RES="$WEBAPP/resources"

# Every resource name referenced by the pages, with its library.
mapfile -t SCRIPTS < <(grep -rhoE '<h:outputScript[^>]*name="[^"]*"' "$WEBAPP" --include=*.xhtml \
	| grep -oE 'name="[^"]*"' | sed 's/name="//; s/"$//' | sort -u)

count=0
for name in "${SCRIPTS[@]}"; do
	target="$RES/$name"
	mkdir -p "$(dirname "$target")"
	cat >"$target" <<EOF
/* GENERATED PLACEHOLDER by tools/generate-web-resources.sh.
 *
 * $name was referenced by EVA Admin's pages but is not in the
 * published release: the whole resources/ directory was withheld (NF-024).
 *
 * This file exists only so the resource resolves and JSF stops rendering
 * "Unable to find resource" into the page. It restores no behaviour.
 */
EOF
	count=$((count + 1))
done

mkdir -p "$RES/css"
cat >"$RES/css/all.css" <<'EOF'
/* GENERATED PLACEHOLDER by tools/generate-web-resources.sh.
 *
 * EVA Admin's stylesheet was in the withheld resources/ directory (NF-024), so
 * this is not its CSS and the application will not look like itself.
 *
 * The few rules below exist so the reconstructed navigation is readable and so
 * placeholder components are visibly placeholders. Replace this wholesale when
 * working from the manual screenshots.
 */

body { font-family: system-ui, sans-serif; margin: 0; color: #222; }
.container, .container-fluid { max-width: 1100px; margin: 0 auto; padding: 0 16px; }

/* Reconstructed navigation (resources/widget/menu.xhtml) */
.eva-menu ul { list-style: none; padding-left: 16px; }
.eva-menu-level-1 > li { margin: 10px 0; }
.eva-menu-header { margin: 18px 0 6px; font-size: 1.05rem; }
.eva-menu a { text-decoration: none; }
.eva-menu a:hover { text-decoration: underline; }
.menu-item-disabled { color: #999; }

/* Anything still standing in for a withheld component announces itself. */
[data-eva-stub] {
    border-left: 3px solid #d9b310;
    padding-left: 8px;
    margin: 4px 0;
}
[data-eva-stub]::before {
    content: "placeholder: " attr(data-eva-stub);
    display: block;
    font-size: 11px;
    color: #8a6d00;
}
EOF
count=$((count + 1))

echo "Wrote $count placeholder resources under $RES"
echo "These resolve the resource lookups. They restore no behaviour (NF-024)."
