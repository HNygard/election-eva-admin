# NF-009 — Frontend reaches the backend over remote EJB JNDI

Status: OPEN
Milestone: M3

## Symptom

Not yet observed. Expected once both WARs deploy: `NamingException` wrapped in
`IllegalStateException` from `ServiceLookupUtil`, on every page that needs data.

## What is known

`admin/admin-common/src/main/java/no/valg/eva/admin/util/ServiceLookupUtil.java`
builds names of the form

    ejb:/admin-backend//<SimpleName>!<CanonicalName>

and looks them up through an `InitialContext` configured with
`Context.URL_PKG_PREFIXES` = `org.jboss.ejb.client.naming` and
`jboss.naming.client.ejb.context` = true (lines 27-35, 57-66).

This is the JBoss EJB client API as it existed around WildFly 8. WildFly 17 —
what `added-stuff/jboss/Dockerfile` builds — removed the old `ejb:` naming
provider in favour of `remote+http://`. So the lookup style in the release may
simply not work on the server version we are running.

The two WARs deploy into the same server, which means an alternative exists:
local rather than remote lookup.

## How to approach

First establish whether WildFly 17 still resolves `ejb:` names, before changing
any code. If it does not, the choice is between running an older WildFly that
matches the code and changing the lookup — the first keeps the release
unmodified and is therefore preferable.

Check what the 2019 system actually ran on: `added-stuff/README.md` cites the
system documentation for WildFly, but not which version. The WildFly version is
our choice, not the release's, so it can be moved freely.

## Done when

A frontend page renders data fetched from the backend, with the request path
traced in a finding.
