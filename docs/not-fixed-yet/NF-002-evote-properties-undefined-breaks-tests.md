# NF-002 — Tests abort because EVOTE_PROPERTIES is not defined

Status: FIXED
Milestone: M1

## Symptom

    java.lang.IllegalArgumentException: EVOTE_PROPERTIES is not defined
        at no.evote.util.EvoteProperties.readProperties(EvoteProperties.java:92)
        at no.evote.util.EvoteProperties.<clinit>(EvoteProperties.java:84)

and the follow-on damage, because a failed static initialiser poisons every
later use of the class:

    java.lang.NoClassDefFoundError: Could not initialize class no.evote.util.EvoteProperties
    java.lang.NoClassDefFoundError: Could not initialize class no.valg.eva.admin.util.IOUtil

## What is known

`admin/admin-common/src/main/java/no/evote/util/EvoteProperties.java` reads the
property file path from the `EVOTE_PROPERTIES` environment variable, falling back
to the system property of the same name (lines 104-106), and throws from a static
initialiser when neither is set.

A sample file ships at `admin/admin-backend/src/test/resources/evote.properties`,
and `EvotePropertiesTest` sets the system property to a relative path
(`../admin-backend/src/test/resources/evote.properties`) — which only works from
one working directory.

Tests are currently skipped entirely (`tools/build.sh` passes `-Dmaven.test.skip`).
That is a workaround, not a fix.

## How to approach

Surefire already has a `systemPropertyVariables` block in the root pom. Passing
`EVOTE_PROPERTIES` there would fix it without touching source, but the root pom is
a release file, so it needs a declared `MODIFIED` row and a reason. Check whether
`-DEVOTE_PROPERTIES=...` on the command line reaches the forked JVM first — if it
does, no file changes at all.

Note the class reads it via `System.getenv` first: the environment variable route
works through `tools/mvn.sh` by adding `--env`, which changes no files whatsoever.
Try that before editing anything.

## Done when

`tools/build.sh --with-tests` gets past class initialisation, and whatever tests
then fail do so for their own reasons — each recorded.

## Resolution

The environment-variable route works and changes nothing in the release.

`EvoteProperties.loadPropertyFilePath()` reads `System.getenv` first
(`EvoteProperties.java:104`), and a Surefire fork inherits the Maven process
environment. So passing `--env EVOTE_PROPERTIES=...` in `tools/mvn.sh`, pointing
at the sample file the release ships for its own tests, is enough:

    EVOTE_PROPERTIES=/usr/src/eva-admin/admin/admin-backend/src/test/resources/evote.properties

Result in `admin-common`, previously the worst-affected module:

    [INFO] Tests run: 2428, Failures: 0, Errors: 0, Skipped: 0
    [INFO] BUILD SUCCESS

No pom edit, no `systemPropertyVariables` entry, no source change. The 2019 notes
in `added-stuff/README.md` concluded this required removing test-scoped
dependencies from the poms; it did not.
