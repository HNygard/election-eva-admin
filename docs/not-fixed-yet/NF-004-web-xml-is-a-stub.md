# NF-004 — Both web.xml files are 41-byte stubs

Status: FIXED
Milestone: M2

## Symptom

    WFLYUT0027: Failed to parse XML descriptor
      "/content/admin-frontend-2019.22-SNAPSHOT.war/WEB-INF/web.xml" at [2,0]
    Caused by: com.ctc.wstx.exc.WstxEOFException: Unexpected EOF in prolog
     at [row,col {unknown-source}]: [2,0]

Both WARs fail deployment at the PARSE phase.

## What is known

Both descriptors contain exactly one line and no root element:

    admin/admin-frontend/src/main/webapp/WEB-INF/web.xml
    admin/admin-backend/src/main/webapp/WEB-INF/web.xml

        <!-- Generated for building purposes -->

They were stripped for publication. The real ones can be partly inferred from the
root pom, which filters them through maven-war-plugin in both the `secureCookies`
and `insecureCookies` profiles, substituting:

- `${session.cookie.secure}` — `true` or `false` depending on profile
- `${jsf.projectStage}` — `Development` by default, via the `jsf-development` profile

So the reconstructed files must contain those two placeholders, or the profiles
become meaningless.

Further content has to be derived from what the code expects: the frontend is JSF
2 with PrimeFaces and has servlets, filters and listeners under
`no.valg.eva.admin.frontend` and `no.evote.presentation`; the backend WAR carries
EJBs looked up remotely as `ejb:/admin-backend//<Service>!<FQCN>`.

## What the enumeration found

Done — see `docs/findings/2026-08-10-what-web-xml-must-contain.md`.

**Backend:** near-trivial. `origin/attempt` replaced it with an empty
`<web-app version="3.1">` shell and that was enough for the backend to start. The
WAR carries EJBs, which need no servlet declarations.

**Frontend:** genuine reconstruction. Nine components are annotation-configured
and need nothing. Ten are not, and exist only if declared — including five
security filters. `attempt` never touched the frontend descriptor, so it offers
no help here.

Two consequences were split out as their own items:

- **NF-015** — the filter chain order is recorded nowhere but `web.xml`, and five of the filters are security controls. A wrong order fails silently.
- **NF-016** — `faces-config.xml` is missing too; only one thing actually needs it.

Both files are release files: each needs a `MODIFIED` row in
`manifest/changes.tsv` explaining what was reconstructed and from what evidence.

## Done when

Both WARs get past the PARSE phase in WildFly, and a finding records the log lines
that show it.
