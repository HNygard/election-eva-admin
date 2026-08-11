# The full test suite passes, unmodified

`tools/build.sh --with-tests`, all 16 modules, 3 minutes 16 seconds:

    [INFO] EVA Administration Common .......................... SUCCESS [ 47.906 s]
    [INFO] EVA Administration Integration ..................... SUCCESS [  6.479 s]
    [INFO] EVA Administration Backend TestingTools ............ SUCCESS [  5.019 s]
    [INFO] EVA Administration Backend Common .................. SUCCESS [  9.250 s]
    [INFO] EVA Administration Configuration ................... SUCCESS [ 11.783 s]
    [INFO] EVA Administration Voting .......................... SUCCESS [  8.013 s]
    [INFO] EVA Administration RBAC ............................ SUCCESS [  7.549 s]
    [INFO] EVA Administration Counting ........................ SUCCESS [ 12.168 s]
    [INFO] EVA Administration Settlement ...................... SUCCESS [  9.028 s]
    [INFO] EVA Administration Valgnatt ........................ SUCCESS [  7.436 s]
    [INFO] EVA Administration JasperServer configuration ...... SUCCESS [  5.445 s]
    [INFO] EVA Administration Backend ......................... SUCCESS [ 21.437 s]
    [INFO] EVA Administration Frontend ........................ SUCCESS [ 37.258 s]
    [INFO] EVA Administrasjon Rapport ......................... SUCCESS [  5.994 s]
    [INFO] BUILD SUCCESS

## Test counts per module

| Module | Tests |
|---|---:|
| admin-common | 2428 |
| admin-integration | 29 |
| admin-backend-testtools | 0 |
| admin-backend-common | 49 |
| admin-configuration | 368 |
| admin-voting | 270 |
| admin-rbac | 103 |
| admin-counting | 568 |
| admin-settlement | 434 |
| admin-valgnatt | 98 |
| admin-report-templates | 1 |
| admin-backend | 557 |
| admin-frontend | 3166 |
| admin-rapport | 12 |
| **Total** | **8083** |

**Failures: 0. Errors: 0. Skipped: 0.**

## What this settles

`added-stuff/README.md` recorded in 2019:

> EvoteProperties is crashing during construction. It requires properties to be
> defined. This will result in failing "mvn clean install". This command should
> not fail in a normal Maven based Java project. Skipping tests for now. Had to
> remove stuff from POM files for scope=tests.

and `origin/attempt` acted on it — `13fba0b`, "Removing test scoped dependencies -
We skip tests since they don't work".

The tests work. They needed one environment variable that the code reads on
line 104 of the class that was throwing, and a `-DskipTests` instead of a
`-Dmaven.test.skip`. Two flags, no file changes, 8083 passing tests.

Worth stating plainly because it changes what this repo is: the published EVA
Admin source is not a broken dump that needs repairing. It builds and its tests
pass exactly as published. What is missing is deployment *configuration* — the
descriptors, the schema and the report templates — not working code.

## Caveat on coverage

This is the default Surefire group only. The root pom excludes
`repository,slow,erasesTestData,resources`, which need a populated PostgreSQL
database. Those are still unrun and remain part of NF-003's tail; they become
reachable once M2 provides a database.

So: 8083 tests pass, and an unknown number of database-backed tests have never
been attempted.
