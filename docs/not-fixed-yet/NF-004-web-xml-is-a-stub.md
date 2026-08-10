# NF-004 — Both web.xml files are 41-byte stubs

Status: OPEN
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

## How to approach

Enumerate what actually needs declaring before writing any XML: grep the frontend
for `@WebServlet`, `@WebFilter`, `@WebListener` and for classes extending servlet
or filter types, and check which of them are annotation-configured already. A Java
EE 7 `web.xml` may need very little beyond the JSF servlet mapping, the two
filtered properties, and security constraints.

Compare with `git diff master origin/attempt -- '*/WEB-INF/web.xml'` to see what
was enough to make the backend start there.

Both files are release files: each needs a `MODIFIED` row in
`manifest/changes.tsv` explaining what was reconstructed and from what evidence.

## Done when

Both WARs get past the PARSE phase in WildFly, and a finding records the log lines
that show it.
