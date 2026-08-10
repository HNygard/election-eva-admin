# D003 — Docker-only toolchain, pinned to JDK 8

Date: 2026-08-10

## Decision

No JDK, Maven, WildFly or PostgreSQL is installed on the host. Everything runs in
containers through `tools/`:

- `tools/mvn.sh` — Maven in `maven:3-jdk-8`, with a named volume for `~/.m2`
- `tools/seed-local-repo.sh` — installs the bundled `jcoord` JAR into that volume
- `tools/build.sh` — seed, then build
- `docker/postgres/` — PostgreSQL on the coordinates the release expects

Maven runs as the invoking user (`--user`, `-Duser.home=/var/maven`), so `target/`
directories in the bind mount are not left root-owned.

## Why

The host has neither `java` nor `mvn`, and this is a 2019 Java EE codebase — the
JDK it wants is not the JDK anyone would install today. Containers make the
version explicit and disposable.

JDK 8 specifically: the poms set `maven.compiler.source/target` to 1.8, and
Surefire 2.20.1 dies with a `NullPointerException` in `SystemUtils.isJavaVersionAtLeast`
under JDK 11. That was already recorded in `added-stuff/README.md` and is
reproduced in `docs/findings/2026-08-10-baseline-survey.md`. Using `maven:latest`
reintroduces it.

## Alternatives rejected

- **Install JDK 8 + Maven on the host.** Ties the work to one machine and to whatever else is installed there.
- **`maven:latest`.** Known to break this build.
- **Run as root in the container.** Simpler, but salts the repo with root-owned build output that then needs sudo to clean.
