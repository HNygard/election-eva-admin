#!/usr/bin/env bash
#
# Regenerates the parts of the seed that are derived from the release's code.
#
#   docker/postgres/seed/02-access.sql   from the Accesses enum
#
# Everything else in docker/postgres/seed/ is hand written and reviewed; only
# this part is mechanical, and it is mechanical because the enum is the exact
# authority on which accesses exist.
#
# Requires tools/build.sh to have installed the EVA artifacts first.
# See docs/not-fixed-yet/NF-026.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

OUT=docker/postgres/seed/02-access.sql
ROLE_PK="${1:-1}"

"$REPO_ROOT/tools/mvn.sh" -q -f tools/schema-export/pom.xml \
	compile exec:java \
	-Dexec.mainClass=no.valg.eva.tools.AccessDump \
	-Dexec.args="$ROLE_PK" >"$OUT"

echo "Wrote $OUT"
echo "  accesses: $(grep -c 'INSERT INTO access ' "$OUT")"
echo "  grants:   $(grep -c 'INSERT INTO role_access ' "$OUT")"
