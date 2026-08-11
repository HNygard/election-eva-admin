#!/usr/bin/env bash
#
# Regenerates docker/postgres/initdb/01-schema-generated.sql from the release's
# own JPA entity classes.
#
# Requires tools/build.sh to have installed the EVA artifacts into the Maven
# volume first -- this reads them from there, it does not rebuild them.
#
# The entity list is derived from the @Entity annotations in the release source,
# NOT from a persistence.xml. No shipped persistence unit covers the whole model:
# admin-backend's test unit lists 117 classes, the union of every shipped unit
# reaches 119, and the source has 120. Reading the source is the only way to get
# all of them, and it cannot drift.
#
# The release is only read, never modified.
#
# See docs/decisions/D005-database-schema-generated-from-this-repo.md

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$REPO_ROOT"

OUTPUT=docker/postgres/initdb/01-schema-generated.sql
CLASS_LIST=tools/schema-export/target/entity-classes.txt

mkdir -p "$(dirname "$OUTPUT")" "$(dirname "$CLASS_LIST")"

# Every @Entity in main source, as fully qualified names.
grep -rl "^@Entity" admin --include=*.java \
	| grep "/src/main/" \
	| while IFS= read -r file; do
		package="$(sed -n 's/^package \(.*\);$/\1/p' "$file" | head -1)"
		class="$(basename "$file" .java)"
		printf '%s.%s\n' "$package" "$class"
	done \
	| sort -u >"$CLASS_LIST"

echo "Entity classes derived from source: $(wc -l <"$CLASS_LIST")"

"$REPO_ROOT/tools/mvn.sh" -q -f tools/schema-export/pom.xml \
	compile exec:java \
	-Dexec.mainClass=no.valg.eva.tools.SchemaExport \
	-Dexec.args="/usr/src/eva-admin/$CLASS_LIST /usr/src/eva-admin/$OUTPUT"

echo
echo "$OUTPUT:"
echo "  tables:       $(grep -ci 'create table' "$OUTPUT" || true)"
echo "  foreign keys: $(grep -ci 'foreign key' "$OUTPUT" || true)"
echo "  sequences:    $(grep -ci 'create sequence' "$OUTPUT" || true)"
