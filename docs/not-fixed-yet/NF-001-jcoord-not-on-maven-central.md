# NF-001 — jcoord 1.0 is not on Maven Central

Status: OPEN
Milestone: M1

## Symptom

`uk.org.mygrid.resources.jcoord:jcoord:1.0` cannot be resolved from any
configured repository, so the build fails during dependency resolution.

## What is known

The release bundles the JAR instead:

    admin-maven-repository/uk/org/mygrid/resources/jcoord/jcoord/1.0/jcoord-1.0.jar

`added-stuff/README.md` records the `install:install-file` invocation used in
2019 to get past this. `tools/seed-local-repo.sh` is that command, made
repeatable and idempotent; `tools/build.sh` runs it first.

## How to approach

Run `tools/seed-local-repo.sh` and confirm the artifact lands in the Maven
volume. Then confirm the build gets past resolution — that is the actual test,
not the install command succeeding.

## Done when

`tools/build.sh` no longer fails on jcoord resolution, starting from an empty
`eva-admin-maven-repo` volume, and a finding records the output.
