# Both WARs deployed, and the application speaks

    WFLYSRV0025: WildFly Full 13.0.0.Final started in 19749ms
                 Started 2366 of 2555 services

    WFLYUT0021: Registered web context: '/admin-frontend'
    WFLYUT0021: Registered web context: '/admin-backend'
    WFLYSRV0010: Deployed "admin-frontend.war"
    WFLYSRV0010: Deployed "admin-backend.war"

Zero `MSC000001` failures.

    HTTP 200  /admin-frontend/
    HTTP 200  /admin-frontend/welcome.xhtml
    HTTP 200  /admin-frontend/status
    HTTP 200  /admin-frontend/secure/index.xhtml

And the body of `welcome.xhtml`:

    System passphrase has not been entered, unable to continue.

That is not an error. It is EVA Admin's own logic, from
`admin/admin-frontend/src/main/java/no/evote/lifecycle/LifecycleFilter.java:53`,
refusing to serve anything until an operator has entered the system passphrase.
The reconstruction has reached the point where the application's real behaviour,
rather than the container's, decides what happens.

## Incidental confirmation that the reconstruction is right

Two independent signals, neither of which was arranged:

**The war-plugin placeholders resolved.** The root pom filters `web.xml` in three
profiles, substituting `${session.cookie.secure}` and `${jsf.projectStage}`. In
the built WAR:

    <param-value>Development</param-value>
    <secure>false</secure>

If the original file had not contained exactly those two placeholders, that
filtering configuration would be pointless. It is not — so it did.

**LifecycleFilter behaved as its position predicts.** It was placed outermost on
the assumption that request lifecycle wraps everything. The passphrase gate
firing on every URL, including `/status` and `/secure/index.xhtml`, is what an
outermost global gate looks like. That assumption is now evidence-backed rather
than merely plausible.

Both are corroboration, not proof. The filter *order* among the security filters
is still partly assumed, and NF-015 stays open.

## Cost so far

| | count |
|---|---:|
| release files modified | 3 |
| files added | 88 |
| release Java source changed | **0** |

The three modified files are the two `web.xml` descriptors, published with no
root element, and `readme.md`, which the repo owner edited before this work
began. Of the 88 added, most are our own tooling and documentation; 17 are
deployment artefacts inside `admin/`.

## Next

The passphrase gate needs an operator, a system password, and signing keys —
none of which exist in a schema generated from entity classes with no seed data.
That is NF-010, and it is now the thing standing between here and a rendered
page.
