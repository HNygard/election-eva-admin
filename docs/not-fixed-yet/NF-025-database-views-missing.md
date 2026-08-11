# NF-025 — Views are missing from the generated schema

Status: PARTIALLY FIXED — role_include_all reconstructed
Milestone: M3

## Symptom

    ERROR: relation "role_include_all" does not exist
    WFLYEJB0034: EJB Invocation failed on component RoleService for method
    getAccumulatedSecLevelFor

## Cause

Predicted by D005 and now confirmed. The schema is generated from JPA entity
classes, so a database view that no entity maps is invisible to that process. The
application still uses such views from native SQL.

This is distinct from the other view problem D005 records: entities under
`no.evote.model.views.*` map to views in the real system and were emitted as
ordinary tables. Those exist but will never populate. `role_include_all` is the
opposite — nothing maps it, so it did not exist at all.

## What was done

`docker/postgres/seed/00-views.sql` reconstructs `role_include_all` as the
transitive closure of `role_include`, derived from its only two use sites
(`Role.java:84`, `RoleRepository.java:58`). Both read it as
`(role_pk, included_role_pk)` and expect all reachable roles, not just direct
ones — otherwise `role_include` itself would have served.

One assumption is recorded in the file: the closure is reflexive, so a role
includes itself. Without that, `getAccumulatedSecLevelFor` returns NULL for a
role that includes no other, instead of that role's own security level.

## What remains

Other views may surface the same way, one error at a time. A systematic sweep of
native SQL for relations absent from the generated schema would find them ahead
of time; it has not been done.

The `no.evote.model.views.*` tables that exist but never populate are the larger
and quieter problem, and are still untouched.
