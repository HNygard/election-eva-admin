# What the stripped web.xml files must contain

Static analysis only — no build or deployment involved. Establishes what has to be
reconstructed for NF-004, and turned up two things that cannot be reconstructed
at all.

## The backend needs almost nothing

`origin/attempt` replaced only the backend descriptor, with an empty shell:

    $ git show origin/attempt:admin/admin-backend/src/main/webapp/WEB-INF/web.xml
    <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
             http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
             version="3.1">
    </web-app>

and that was enough for "EVA Admin backend starts up!" (`dc1e8e1`). The backend
WAR carries EJBs, which need no servlet declarations. Its `src/main/webapp` holds
only `index.html` and the descriptor.

Note the frontend descriptor on that branch is **still the 41-byte stub** — so
the frontend was never deployed there. Nothing about the frontend can be learned
from `attempt`.

## The frontend needs real content

Annotation-configured already, so no declaration needed — 9 components:

    ResourceNotFoundServlet, DisableCachingFilter, WSDLRewriter,
    ReportContentDownloadServlet, TmpLoginServlet, FrontendStatusServlet,
    LogoutServlet, FrontendStatusServletOld, SystemDateTimeServlet

**Not annotated, therefore invisible unless declared in web.xml** — 10 components:

    no/evote/lifecycle/LifecycleFilter
    no/evote/service/security/PageAccessFilter
    no/evote/service/security/SelectRoleFilter
    no/evote/service/security/SessionListener          (HttpSessionListener)
    no/valg/eva/admin/frontend/filter/IEModeFilter
    no/valg/eva/admin/frontend/filter/WelcomePageFilter
    no/valg/eva/admin/frontend/filter/ForceLocaleFilter
    no/valg/eva/admin/frontend/security/SessionHijackingDetector
    no/valg/eva/admin/frontend/security/TmpLoginFilter
    no/valg/eva/admin/frontend/security/OidcFilter

Plus the two properties the root pom filters into the file, without which the
`secureCookies`/`insecureCookies` and `jsf-development` profiles do nothing:
`${session.cookie.secure}` and `${jsf.projectStage}`.

## First thing that cannot be reconstructed: filter chain order

A filter chain executes in `<filter-mapping>` order, and that order exists
**only** in `web.xml`. Nothing in the source records it.

Five of the ten undeclared filters are security controls: `PageAccessFilter`,
`SelectRoleFilter`, `SessionHijackingDetector`, `TmpLoginFilter`, `OidcFilter`.
Their relative order decides whether authentication runs before authorisation,
whether session-hijacking detection sees a request before or after a role is
chosen, and whether any of them can be bypassed.

So a reconstructed chain is a guess, and a wrong guess is a security hole that
looks like a working application. Any ordering we choose has to be stated as an
assumption, not presented as recovered fact. Raised as NF-015.

## Second thing that is missing: faces-config.xml

    $ find admin -name faces-config.xml
    (nothing)

Mostly this does not matter, because JSF artefacts here are annotation-registered:
12 `@FacesConverter`, 1 `@FacesValidator`, 1 `@FacesComponent`, and beans are CDI
`@Named` (273 classes) rather than `@ManagedBean` (0).

One exception. `no/valg/eva/admin/frontend/faces/MultiPageMessagesSupport.java:34`
implements `PhaseListener`, and a PhaseListener can only be registered through
`faces-config.xml`. Without it the class is dead code and messages do not survive
across redirects. Raised as NF-016.

## Consequence for the reconstruction

The backend descriptor is near-certain: the empty shell is defensible and already
demonstrated to work. The frontend descriptor is a genuine reconstruction with
security-relevant guesswork in it, and should be treated with matching suspicion.
