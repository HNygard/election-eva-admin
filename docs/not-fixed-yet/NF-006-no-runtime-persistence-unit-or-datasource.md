# NF-006 — No runtime persistence unit or datasource

Status: OPEN
Milestone: M2

## Symptom

There is no `src/main/resources/META-INF/persistence.xml` in any module. All nine
`persistence.xml` files in the repo are under `src/test/resources`, so the
deployed application has no persistence unit at all.

## What is known

The test persistence unit (`admin/admin-backend/src/test/resources/META-INF/persistence.xml`)
shows exactly what the runtime one must look like:

- unit name `evotePU`, provider `org.hibernate.jpa.HibernatePersistenceProvider`
- 118 explicitly listed entity classes with `<exclude-unlisted-classes>true</exclude-unlisted-classes>`
- `hibernate.dialect` = `org.hibernate.dialect.PostgreSQL9Dialect`
- direct JDBC connection to `jdbc:postgresql://${testDatabaseHost}:${testDatabasePort}/${testDatabaseName}` as `admin`/`admin`

In a container the connection should come from a JTA datasource instead of direct
JDBC properties, which means a WildFly datasource definition plus the PostgreSQL
JDBC driver as a module or deployment. `docker/postgres/` already provides a
database on the coordinates the release expects.

## How to approach

Find out first whether the deployed code injects an `EntityManager` and under what
unit name — grep for `@PersistenceContext`. That determines the unit name the
runtime file must use.

Then decide where the file belongs (which module's WAR or JAR) rather than
guessing, since the entity classes are spread across `admin-configuration`,
`admin-counting`, `admin-settlement`, `admin-rbac`, `admin-voting`, `admin-valgnatt`
and `admin-common`.

## Done when

The backend deploys with a working `EntityManager`, proven by a query against the
running database, not by absence of errors.
