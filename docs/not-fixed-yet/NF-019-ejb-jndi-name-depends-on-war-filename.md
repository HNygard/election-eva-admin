# NF-019 — EJB JNDI names do not match what the frontend looks up

Status: OPEN
Milestone: M3

## Symptom

Not yet hit — the frontend is not deployed. But the names already visibly
disagree.

WildFly binds, at backend startup:

    ejb:admin-backend-2019.22-SNAPSHOT/MvAreaService!no.evote.service.configuration.MvAreaService

`ServiceLookupUtil.remoteJndiNameForService` builds
(`admin/admin-common/src/main/java/no/valg/eva/admin/util/ServiceLookupUtil.java:60`):

    ejb:/admin-backend//MvAreaService!no.evote.service.configuration.MvAreaService

## Cause

The EJB module name is the deployment filename without its extension. The build
produces `admin-backend-2019.22-SNAPSHOT.war`, because the root pom sets
`finalName` to `${project.artifactId}-${project.version}`, so the module is
`admin-backend-2019.22-SNAPSHOT` and not `admin-backend`.

All 190 bound EJBs are affected, and 73 frontend producers route through this
lookup.

## How to approach

Deploy the WAR under the name the code expects, rather than changing the code.
`tools/wildfly.sh` already copies the WAR into place, so renaming it to
`admin-backend.war` on the way in costs nothing and modifies no release file.

Confirm afterwards that the bound names actually change — read them out of the
server log rather than assuming.

If that is not sufficient, the next option is `jboss-web.xml` or an
`application.xml` module name, still without touching source.

Note the leading slash and doubled slash in the expected name (`ejb:/admin-backend//`)
is the old jboss-ejb-client 2.x form for a deployment with no EAR. D009 picked
WildFly 13 precisely so that form is still understood.

## Done when

A lookup from the frontend resolves against the backend, evidenced by a page that
renders backend data.
