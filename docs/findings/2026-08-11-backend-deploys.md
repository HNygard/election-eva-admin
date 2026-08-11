# The backend deploys

    WFLYSRV0025: WildFly Full 13.0.0.Final (WildFly Core 5.0.0.Final)
    started in 14565ms - Started 2082 of 2270 services
    (315 services are lazy, passive or on-demand)

`WFLYSRV0025`, not `WFLYSRV0026` — started, not "started with errors". Zero
`MSC000001` failures.

    WFLYUT0021: Registered web context: '/admin-backend-2019.22-SNAPSHOT'
    WFLYSRV0010: Deployed "admin-backend-2019.22-SNAPSHOT.war"
    WFLYJCA0001: Bound data source [java:/jdbc/evote]
    WFLYJPA0010: Starting Persistence Unit 'admin-backend-2019.22-SNAPSHOT.war#evotePU'

    $ curl -o /dev/null -w "%{http_code}" http://localhost:8080/admin-backend-2019.22-SNAPSHOT/
    200

190 EJBs bound. Flyway ran at startup and found nothing pending, so
`DatabaseSchemaCheckerBean` passed.

## The route, one failure at a time

Each step below was a separate deploy, and each error named the next problem.

| # | Failure | Fix | Files |
|---|---|---|---|
| 1 | `WFLYUT0027 ... Unexpected EOF in prolog` | empty `web-app` 3.1 shell | 1 modified |
| 2 | `WELD-001408: Unsatisfied dependencies for type ValghierarkiDomainService` | `beans.xml`, `bean-discovery-mode="all"` | 12 added |
| 3 | `WELD-000119 ... Type org.hibernate.Session not found` | `jboss-deployment-structure.xml` | 1 added |
| 4 | `WFLYWELD0037 ... Can't find a persistence unit named 'evotePU'` | generated runtime persistence unit | 1 generated |
| 5 | `IllegalArgumentException: EVOTE_PROPERTIES is not defined` | env var + mount the release's own sample | 0 |
| 6 | `EvoteException: Unable to find log4j.xml on classpath` | copy from the release's test resources | 2 added |
| 7 | `EvoteException: Could not find version properties file /version.properties` | restore the file `git-commit-id-plugin` filters | 1 added |

Service counts across those steps: 300 → 1752 → 2075 → 2076 → **2082, none
failed**.

Total: **1 release file modified, 17 added.** Nothing in the release's Java source
was touched, and the modified file is a descriptor that was published with no
root element.

## Names recovered rather than invented

Nothing here was guessed at where the source could answer it:

| What | Value | Recovered from |
|---|---|---|
| datasource JNDI | `java:/jdbc/evote` | `StatusApplicationService.java:36`, `DatabaseSchemaCheckerBean.java:20` |
| persistence unit | `evotePU` | `@PersistenceContext(unitName = "evotePU")`, 7 call sites |
| database, user | `evote`, `admin`/`admin` | root pom defaults, admin-backend test persistence unit |
| schemas | `admin`, `audit` | `DatabaseSchemaCheckerBean.java:37` `setSchemas("admin", "audit")` |
| Flyway location | `no/valg/eva/admin/database/migrations` | `DatabaseSchemaCheckerBean.java:36` (classpath location is absent, hence 0 pending) |
| `version.properties` | `commitId=${git.commit.id}` | `git-commit-id-plugin` + filtered `src/main/resources` in admin-backend/pom.xml |

## What this says about the published source

Seven distinct blockers, and not one of them was a bug. Every single one was a
**missing deployment artefact**: a descriptor stripped to a comment, config files
that live only under `src/test/resources`, a properties file the build pipeline
generated. The Java code needed no changes at all.

## A finding for M3

EJBs bind under the WAR's name:

    ejb:admin-backend-2019.22-SNAPSHOT/MvAreaService!no.evote.service.configuration.MvAreaService

but `ServiceLookupUtil` builds:

    ejb:/admin-backend//MvAreaService!no.evote.service.configuration.MvAreaService

The module name comes from the WAR filename, so `admin-backend-2019.22-SNAPSHOT.war`
produces the wrong one. Deploying it as `admin-backend.war` should line them up —
a deployment-naming change, not a code change. Raised as NF-019.
