# NF-016 — faces-config.xml is missing, so the PhaseListener is unregistered

Status: OPEN
Milestone: M2

## Symptom

    $ find admin -name faces-config.xml
    (nothing)

## What is known

This turned out to be much smaller than feared. JSF artefacts in this codebase
are registered by annotation, not by descriptor:

| Kind | Count |
|---|---|
| `@FacesConverter` | 12 |
| `@FacesValidator` | 1 |
| `@FacesComponent` | 1 |
| `@ManagedBean` | 0 |
| `@Named` (CDI) | 273 |

So beans, converters and validators all resolve without a `faces-config.xml`.

One thing does not. `admin/admin-frontend/src/main/java/no/valg/eva/admin/frontend/faces/MultiPageMessagesSupport.java:34`
implements `javax.faces.event.PhaseListener`, and phase listeners can only be
registered through `faces-config.xml`:

    <lifecycle>
        <phase-listener>no.valg.eva.admin.frontend.faces.MultiPageMessagesSupport</phase-listener>
    </lifecycle>

Without it the class never runs. Its job is carrying faces messages across a
redirect, so the visible symptom is validation or confirmation messages silently
disappearing after any post-redirect-get — which looks like an application bug,
not a missing file.

## How to approach

Add the descriptor to `admin-frontend` as a new file, declared `ADDED`, with the
phase listener registered and nothing else. Resist filling it with anything the
annotations already handle.

Check first whether any XHTML references navigation rules or resource bundles by
name, since those would also need declaring — grep the pages for `<f:loadBundle`
and check how `no.valg.eva.admin.frontend.i18n` resolves bundles.

## Done when

Messages survive a redirect in the running application, and the descriptor
contains only what is genuinely required.
