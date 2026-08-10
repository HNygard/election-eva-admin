# First successful build of the released source

M1's build works. Three attempts, two real problems, and one lesson about
trusting exit codes.

## Attempt 1 — network

Failed after 23 minutes in `admin-common`:

    Could not transfer artifact javax.inject:javax.inject:pom:1 from/to central
    (https://repo.maven.apache.org/maven2): transfer failed for
    https://repo.maven.apache.org/maven2/javax/inject/javax.inject/1/javax.inject-1.pom:
    Connection reset

Not a code problem. Downloads were running at 2-8 kB/s because the machine was
being restarted. Retrying was the whole fix — Maven keeps what it already
fetched.

## Attempt 2 — the real problem: -Dmaven.test.skip breaks the reactor

    Failed to execute goal on project admin-integration: Could not resolve
    dependencies for project no.valg.eva:admin-integration:jar:2019.22-SNAPSHOT:
    Could not find artifact no.valg.eva:admin-common:jar:tests:2019.22-SNAPSHOT

This codebase depends on its own test-jars, heavily: **13 modules consume
`<type>test-jar</type>` dependencies, 11 produce them.** `admin-common`'s test-jar
alone is required by 11 other modules.

`-Dmaven.test.skip` skips test *compilation*, so those test-jars are never built
and the reactor cannot resolve them. `-DskipTests` compiles tests and merely
declines to run them, which is what was wanted all along.

This is what `origin/attempt` worked around in `13fba0b`, "Removing test scoped
dependencies - We skip tests since they don't work" — deleting test-scoped
dependencies from 13 poms. The flag does the same job and **changes no files at
all**. First concrete case of D004 paying off: reading `attempt` for the symptom,
then finding a better fix than it did.

## Attempt 3 — success

    [INFO] EVA Administration Top-level parent ................ SUCCESS [  0.913 s]
    [INFO] EVA Administration Parent .......................... SUCCESS [  0.303 s]
    [INFO] EVA Administration Common .......................... SUCCESS [ 25.712 s]
    [INFO] EVA Administration Integration ..................... SUCCESS [  3.096 s]
    [INFO] EVA Administration Backend TestingTools ............ SUCCESS [  7.744 s]
    [INFO] EVA Administration Backend Common .................. SUCCESS [  9.578 s]
    [INFO] EVA Administration Configuration ................... SUCCESS [  8.956 s]
    [INFO] EVA Administration Voting .......................... SUCCESS [  2.915 s]
    [INFO] EVA Administration RBAC ............................ SUCCESS [  2.889 s]
    [INFO] EVA Administration Counting ........................ SUCCESS [  6.694 s]
    [INFO] EVA Administration Settlement ...................... SUCCESS [  4.395 s]
    [INFO] EVA Administration Valgnatt ........................ SUCCESS [  2.942 s]
    [INFO] EVA Administration JasperServer configuration ...... SUCCESS [01:07 min]
    [INFO] EVA Administration Backend ......................... SUCCESS [ 19.176 s]
    [INFO] EVA Administration Frontend ........................ SUCCESS [ 22.600 s]
    [INFO] EVA Administrasjon Rapport ......................... SUCCESS [  1.756 s]
    [INFO] BUILD SUCCESS
    [INFO] Total time:  03:07 min

Artifacts:

    35M  admin/admin-backend/target/admin-backend-2019.22-SNAPSHOT.war
    30M  admin/admin-frontend/target/admin-frontend-2019.22-SNAPSHOT.war

plus 14 module JARs and 9 test-jars. Three minutes with a warm Maven volume.

**The released source compiles without a single modification.** Everything that
was missing so far has been build *configuration*, not code.

## Lesson: the exit code lied

Attempt 2 was reported as exit 0 while `BUILD FAILURE` sat in the log. The
harness reported the status of the wrong process in the pipeline, and `build.sh`
runs two Maven invocations — the jcoord seed and the build itself. Grepping for
`BUILD SUCCESS` found the seed's summary and looked like a pass.

Two things follow, both now habit:

- Check the **last** reactor summary, not the first match.
- Verify artifacts on disk. `find admin -name '*.war'` returning nothing is not a successful build, whatever the log says.

## Still open

Tests are compiled but not run (NF-002, NF-003). The `-DskipTests` flag is a
deliberate M1 boundary, not a fix.
