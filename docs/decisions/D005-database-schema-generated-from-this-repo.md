# D005 — Generate the database schema from this repo's entity classes

Date: 2026-08-10

## Decision

The PostgreSQL schema is produced by a tool in this repo that runs Hibernate's
schema generation over the release's own JPA entity classes, emitting
`docker/postgres/initdb/01-schema-generated.sql` (declared `GENERATED` in the
manifest). It is not copied from anywhere.

The tool reuses the entity list from the release's own
`admin/admin-backend/src/test/resources/META-INF/persistence.xml` rather than
restating 118 class names, so it cannot drift from the code it describes.

## Why

EVA Admin does not create its schema in code. There is no `hbm2ddl` or
`javax.persistence.schema-generation` property anywhere in the release, and
`persistence.xml` expects a database that already exists. The real schema came
from Flyway migrations — `flyway-maven-plugin` is configured in the root pom —
but those migrations lived in `admin-other`/`admin-docker` and are not published.

So the schema has to be reconstructed. Deriving it from the entity classes in
*this* repo means it always matches the code we are actually running, and a
future change to an entity produces a visible diff in the generated file.

`~/git/eva-admin-spring-boot-hacktogether` already did this once via Spring Boot
(113 tables, 176 foreign keys) and stays useful as a cross-check: if our output
diverges from it substantially, one of the two is wrong.

## Known limits of a generated schema

These are consequences of the approach, not bugs to be surprised by later:

- **No reference data.** Zero seed rows: no roles, no locales, no election event types. Anything that reads reference data will find an empty table.
- **Views become tables.** The `no.evote.model.views.*` entities map to database views in the real system. Schema generation emits them as ordinary tables, so they will exist but never populate.
- **No sequences.** Generation uses `bigserial`, which is not necessarily how production allocates keys.

## Alternatives rejected

- **Copy `create-postgresql.sql` from the sibling repo.** A checked-in artefact derived from a different checkout, with no way to regenerate it here when entities change.
- **`hbm2ddl.auto` at deploy time.** No SQL file to inspect, and it would hide the view problem plus any partial failure behind a successful-looking deployment.
