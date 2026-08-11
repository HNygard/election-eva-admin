# D009 — Deploy on WildFly 13, not 17

Date: 2026-08-11

## Decision

EVA Admin is deployed on **WildFly 13.0.0.Final**. `docker/wildfly/` builds it.
The WildFly 17 image in `added-stuff/jboss/` is left as the historical artefact
it is.

## Why

The release targets **Java EE 7** — the root pom depends on `javaee-api` 7.0.
WildFly 13 is the last EE7 full server; 14 onwards are EE8. Running EE7 code on
an EE7 server removes a whole class of question that would otherwise contaminate
every later failure.

The decisive part is how the frontend reaches the backend. It does not inject
EJBs; it looks them up remotely, through `ServiceLookupUtil` (`admin-common`):

    ejb:/admin-backend//<SimpleName>!<CanonicalName>

with `Context.URL_PKG_PREFIXES` = `org.jboss.ejb.client.naming` and
`jboss.naming.client.ejb.context` = true. That is the jboss-ejb-client 2.x style.
WildFly 14 replaced it with wildfly-naming-client, where those properties no
longer mean the same thing.

This is not one call site. **73 service producers** in `admin-frontend` route
through `ServiceInvocationHandler`, which calls `ServiceLookupUtil`, plus
converters that call it directly. On WildFly 17 all of them would need the lookup
changed — 73+ modifications to release source, to work around a server version we
chose ourselves.

Picking a server that matches the code costs nothing and modifies nothing. NF-009
exists precisely because of this mismatch; this decision is the cheapest way to
close it.

## Evidence it is available

    200  https://download.jboss.org/wildfly/13.0.0.Final/wildfly-13.0.0.Final.tar.gz

## What we give up

WildFly 13 is from 2018 and unmaintained. It will contain known CVEs. That is
acceptable and in fact appropriate here: the goal is to observe how the 2019
system behaved, on infrastructure of its own era, on a local machine with no
exposure. It would be a bad basis for anything else.

If M4 finds that current EVA Admin runs on something newer, that is a finding
about the system's evolution, not a reason to move this reconstruction.

## Alternatives rejected

- **WildFly 17** (`added-stuff/jboss/`). Would require changing the lookup in 73+ producers, exactly the kind of source modification this project exists to avoid.
- **WildFly 11.** Also EE7 and also viable. 13 chosen as the latest EE7 release, so we get its fixes without leaving the target platform.
