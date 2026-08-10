# Baseline survey of the released EVA Admin source

What is in this repo, what is missing, and what the host can run. Nothing was
built — this is the survey the rest of the setup rests on.

## The tree is pristine

`git diff --stat 629c4ea HEAD` before this work: only `readme.md` and the three
`added-stuff/` files. 3234 release files, all byte-identical to the 2019 drop.
That is why a checksum baseline is worth anchoring now (D002).

Branch layout: `master` carries the release plus notes. `origin/attempt`
(`dc1e8e1`) carries a working-backend attempt, 342 files changed, +2266/-301.
Thirteen dependabot branches exist and are not relevant.

## Host toolchain

    $ which java mvn docker
    /usr/bin/docker
    /bin/bash: line 1: java: command not found
    /bin/bash: line 1: mvn: command not found

Docker 29.1.3. No JDK, no Maven — hence D003.

## Missing modules

`readme.md` names three, and all three are absent: `admin-docker`, `admin-testing`,
`admin-other`. Between them they held the docker setup, the integration and
functional tests, and the tooling — including, judging by the configured
`flyway-maven-plugin`, the database migrations.

## Missing files inside the modules that do ship

**Both `web.xml` files are one-line stubs.**

    $ cat admin/admin-frontend/src/main/webapp/WEB-INF/web.xml
    <!-- Generated for building purposes -->

41 bytes, no root element. This is the direct cause of the `WstxEOFException:
Unexpected EOF in prolog` recorded in `added-stuff/README.md`. The root pom
filters these files through maven-war-plugin substituting `${session.cookie.secure}`
and `${jsf.projectStage}`, so the originals contained those placeholders. NF-004.

**No `beans.xml` anywhere.** NF-005.

**No runtime `persistence.xml`.** All nine are under `src/test/resources`. NF-006.

**No report templates.** `admin/admin-report-templates/` has 8 files, all code.
Its `ReportConfig.xml` references 32 `.jrxml` paths; `find` for `*.jrxml` or
`*.jasper` returns nothing. NF-012 — and this is what stands between us and a
valgprotokoll.

**No SQL migrations.** The only `.sql` in the repo is
`admin/admin-backend/src/test/resources/reporting/initializeDB.sql`. NF-007.

## The schema is not created by code

No `hbm2ddl` and no `javax.persistence.schema-generation` property anywhere. The
test persistence unit connects to a database it expects to already exist:

    jdbc:postgresql://${testDatabaseHost}:${testDatabasePort}/${testDatabaseName}
    user=admin  password=admin  dialect=PostgreSQL9Dialect

with root-pom defaults `localhost` / `15432` / `evote`. `docker/postgres/` is
configured to match those exactly, so the release's own settings work unmodified.

118 entity classes are listed explicitly with `<exclude-unlisted-classes>true</exclude-unlisted-classes>`,
which is what makes generating the schema from this repo practical (D005).

## Architecture, as far as the source shows it

Two WARs on a Java EE server. The frontend looks the backend up remotely:

    ejb:/admin-backend//<SimpleName>!<CanonicalName>

built in `admin/admin-common/src/main/java/no/valg/eva/admin/util/ServiceLookupUtil.java:60`,
through an `InitialContext` using `org.jboss.ejb.client.naming` — the old JBoss
EJB client API, which WildFly 17 no longer provides. NF-009.

93 classes are `@Stateless`/`@Stateful`. Package layout splits into legacy
`no.evote.*` and DDD-layered `no.valg.eva.admin.*`
(`application`/`domain`/`repository` per bounded context: configuration, counting,
settlement, rbac, voting, valgnatt, rapport).

## Known-bad from the earlier attempt, reproduced here

From `added-stuff/README.md`, still true and worth not rediscovering:

- `maven:latest` (JDK 11) fails with `NullPointerException` at `SystemUtils.isJavaVersionAtLeast` in Surefire 2.20.1. Use JDK 8.
- `jcoord:1.0` is not on Maven Central; the JAR ships in `admin-maven-repository/`.
- `EVOTE_PROPERTIES is not defined` aborts static initialisation of `EvoteProperties` and takes unrelated tests down with it.

## Sibling repositories

- `~/git/eva-admin-spring-boot-hacktogether` — Spring Boot harness over copies of these entities; generated a PostgreSQL schema of 113 tables and 176 foreign keys, with zero seed rows and zero sequences. Cross-check for NF-007.
- `~/git/valgprotokoller` — parsers for published valgprotokoll documents. Useful for knowing what the output should contain (NF-012).
