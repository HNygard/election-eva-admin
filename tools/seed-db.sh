#!/usr/bin/env bash
#
# Applies the seed data in docker/postgres/seed/ to the running database.
#
# Separate from docker/postgres/initdb/ on purpose: initdb only runs against an
# empty database, and this needs re-running as the seed grows. Each file is
# idempotent.
#
# See docs/not-fixed-yet/NF-010.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

for f in docker/postgres/seed/*.sql; do
	[ -e "$f" ] || continue
	echo "Applying $f"
	docker exec -i eva-admin-postgres psql -v ON_ERROR_STOP=1 -U admin -d evote <"$f"
done

echo
echo "Row counts:"
docker exec eva-admin-postgres psql -U admin -d evote -tAc \
	"select 'operator=' || (select count(*) from operator)
       || ' role=' || (select count(*) from role)
       || ' operator_role=' || (select count(*) from operator_role)
       || ' election_event=' || (select count(*) from election_event)"
