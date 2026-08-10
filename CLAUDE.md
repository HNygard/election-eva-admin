# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

The published source of **EVA Admin** — the Norwegian election administration
system — release `admin-2019.22.7829`, downloaded from valg.no on 2019-06-08.
Valgdirektoratet publishes it so the election system can be inspected and
evaluated.

It is **not a complete system**. Three modules were withheld: `admin-docker`,
`admin-testing`, `admin-other`. So were several files inside the modules that did
ship. As published, it cannot be built or run.

The work here is getting it running with the smallest possible set of changes,
while keeping every change visible. See `docs/specs/bootstrap-eva-admin.md`.

Note that `readme.md` and `lisens.md` are the release's own files, in Norwegian.

## Start every session here

1. Read `docs/status.md` — it names the current milestone.
2. Open `docs/not-fixed-yet/INDEX.md` and take the topmost `OPEN` item for that milestone.
3. Work it. When you change or add a file, declare it in `manifest/changes.tsv`.
4. Write what you learned to `docs/findings/YYYY-MM-DD-<slug>.md` — including failures, with the verbatim error.
5. Update the item's `Status:` line, run `tools/docs-index.sh`, and commit.

**Commit every step, as you go.** One coherent change per commit — a reconstructed
descriptor and its manifest row, a finding, a decision — not a batch at the end of
a session. The point is that `git log` reads as the account of how this system was
brought up: what was tried, what it cost, in what order. A large commit destroys
that, and so does a commit whose manifest rows were written afterwards to fit.

Say in the message what the change was *for*, and reference the item (`NF-004`) or
decision (`D005`) it serves. A commit that fails the pre-commit hook is telling
you something is undeclared — fix the declaration, never `--no-verify`.

First time in a fresh clone: `tools/install-hooks.sh`.

## Rules

**Every deviation from the release is declared.** `manifest/originals.sha` pins
all 3234 published files by git blob sha. Anything that differs, is missing, or is
new must have a row in `manifest/changes.tsv`:

    STATUS<TAB>PATH<TAB>MILESTONE<TAB>REASON

`tools/manifest-check.sh` enforces this and runs as a pre-commit hook. The reason
must say *why*, not what. Do not weaken the check, and do not use `.gitignore` to
get around it.

**Nothing is installed on the host.** No `java`, no `mvn` — everything goes
through `tools/`, which runs containers. Maven is pinned to JDK 8; `maven:latest`
breaks this build.

**Record what happened, including failures.** A failed attempt with its exact
error is worth more than a summary of a successful one. The next session should
never rediscover a dead end.

**Never merge or cherry-pick `origin/attempt`.** It is a reference for what was
missing, not a source. Read it, re-derive the change, declare it. See
`docs/decisions/D004-attempt-branch-as-reference.md`.

**Evidence before claims.** Do not report something as working without the command
output that shows it. Deployment succeeding is not the same as the application
functioning.

## Commands

    tools/install-hooks.sh          # once per clone
    tools/build.sh                  # seed jcoord, then clean install (tests skipped)
    tools/build.sh --with-tests     # expected to fail -- see NF-002
    tools/mvn.sh <args>             # any maven command in the container
    tools/mvn.sh -pl admin/admin-common test
    tools/mvn.sh -Dtest=EvotePropertiesTest -pl admin/admin-common test   # single test

    tools/manifest-check.sh         # what is original / modified / added
    tools/manifest-generate.sh      # only when the baseline itself moves
    tools/docs-index.sh             # regenerate docs indexes; --check to verify

    docker compose -f docker/postgres/docker-compose.yml up -d   # M2 onwards

The Maven repository lives in the `eva-admin-maven-repo` volume. Removing it is
how you test a build from scratch.

Test groups: the root pom's Surefire config excludes `repository,slow,erasesTestData,resources`
by default. Profiles `slowTests`, `erasingTests` and `allTests` change that; the
non-default groups need a populated database.

## Architecture

Multi-module Maven, `no.valg.eva:admin-all` → `admin` → 14 modules. Two of them
produce WARs that deploy to the same Java EE server:

- **`admin-frontend`** — JSF 2 + PrimeFaces, XHTML under `src/main/webapp/secure/`
- **`admin-backend`** — EJBs, 93 of them `@Stateless`/`@Stateful`

The frontend does **not** call the backend in-process. It looks services up over
remote EJB JNDI, names built in
`admin/admin-common/src/main/java/no/valg/eva/admin/util/ServiceLookupUtil.java:60`:

    ejb:/admin-backend//<SimpleName>!<CanonicalName>

using the old JBoss EJB client API (`org.jboss.ejb.client.naming`). WildFly 17
dropped that naming provider — see `docs/not-fixed-yet/NF-009-*`.

Persistence is Hibernate/JPA on PostgreSQL, 118 entity classes listed explicitly
in `persistence.xml` with `exclude-unlisted-classes`. The schema is **not**
created by code: no `hbm2ddl` anywhere, and the Flyway migrations that built it
were in the withheld `admin-other`.

Two package generations coexist, and the difference matters when reading code:

- `no.evote.*` — the older layer: `model`, `persistence`, `service`, `presentation`. `no.evote.model.views.*` maps to database *views*.
- `no.valg.eva.admin.*` — DDD-layered per bounded context (`configuration`, `counting`, `settlement`, `rbac`, `voting`, `valgnatt`, `rapport`), each with `application` / `domain` / `repository`.

Norwegian domain vocabulary appears in both identifiers and paths, including
non-ASCII ones such as `.../oppgjørsskjema/OppgjørsskjemaDomainService.java`. Key
terms: *valgprotokoll* / *møtebok* (election protocol), *opptelling* (counting),
*oppgjør* (settlement), *manntall* (electoral roll), *stemmegivning* (voting),
*valgnatt* (election night reporting), *valggeografi* (electoral geography).

Reports run on a JasperSoft server outside this codebase; `admin-report-templates`
only uploads templates to it, and the templates themselves are not published.

## Maven conventions the release documents

From `readme.md`, and worth keeping to:

- Version numbers live only in the root pom; sub-poms reference artifacts only.
- Each dependency is documented in the root `dependencyManagement` block, grouped internal/external, sorted alphabetically.
- Anything surprising gets a comment explaining why it is that way.
- `mvn dependency:analyze` warnings are expected to be explained, not ignored.

## Layout

    admin/                  the release source, 14 modules -- treat as read-only unless declared
    admin-maven-repository/ bundled jcoord JAR, not on Maven Central
    added-stuff/            2019-2020 first-attempt notes and WildFly image
    manifest/              what is original, modified and added
    tools/                 dockerised build, checks, git hook
    docker/postgres/       PostgreSQL on the coordinates the release expects
    docs/                  status, decisions, findings, work queue, specs, reference
