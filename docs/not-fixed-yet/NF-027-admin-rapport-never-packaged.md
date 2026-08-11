# NF-027 — admin-rapport is built but never packaged

Status: FIXED
Milestone: M3

## Symptom

    NoSuchEJBException: EJBCLIENT000079: Unable to discover destination for
    request for EJB StatelessEJBLocator for "/admin-backend/RapportService",
    view is interface no.valg.eva.admin.common.rapport.service.RapportService

Every page that lists reports fails, including `/secure/index.xhtml`, because
`ReportLinksController` needs the service.

## Cause

`RapportApplicationService` lives in `admin-rapport`. That module is in the
reactor and builds fine, but:

- nothing declares a dependency on it — `grep -l admin-rapport admin/*/pom.xml` returns only its own pom
- it is absent from the root pom's `dependencyManagement`
- it is therefore not in `admin-backend.war/WEB-INF/lib`

So `RapportService` is never deployed anywhere, while the frontend looks it up by
a name that requires it to be inside the `admin-backend` module.

This is a gap in the published build configuration, not a missing file: the
implementation ships, the wiring does not. The real build must have had it.

## Resolution

`admin-rapport` added to the root pom's `dependencyManagement` and to
`admin-backend`'s dependencies, following the project's own rule that version
numbers live only in the root pom. The EJB then binds:

    ejb:admin-backend/RapportService!no.valg.eva.admin.common.rapport.service.RapportService

Two release files modified, both poms, both declared.
