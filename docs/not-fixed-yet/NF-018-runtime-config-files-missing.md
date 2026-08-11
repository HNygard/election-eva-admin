# NF-018 — Runtime config files exist only under src/test/resources

Status: FIXED
Milestone: M2

## Symptom

Two startup failures in a row, each from a file the application expects on the
runtime classpath:

    EvoteException: Unable to find log4j.xml on classpath
        no.evote.util.Log4jUtil.configureFromClasspathConfig:44

    EvoteException: Could not find version properties file /version.properties
        no.evote.util.CommitIdProducer.init

Each took down a `@Startup @Singleton` and everything depending on it.

## Cause

`log4j.xml` ships four times in the release, all under `src/test/resources`.
Nothing puts one in `src/main/resources`.

`version.properties` is a build artefact. `admin-backend/pom.xml` runs
`git-commit-id-plugin`'s `revision` goal and marks `src/main/resources` as
`filtering=true`, so the release plainly had a `version.properties` there
containing the plugin's substitution. It was stripped along with the other
build-time material.

## Resolution

- `log4j.xml` and `log4j.dtd` copied verbatim from the release's own admin-backend test resources.
- `version.properties` restored with `commitId=${git.commit.id}`, which Maven filtering resolves. Verified in the built WAR:

      commitId=04683e01bca747420337125e884f82bc14076808

All three declared ADDED. None invents a value: each restores something the
release's own build configuration shows was there.
