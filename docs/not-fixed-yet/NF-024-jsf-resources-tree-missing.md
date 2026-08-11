# NF-024 — The whole JSF resources/ tree is missing

Status: PARTIALLY FIXED — components stubbed, styling and scripts absent
Milestone: M3

## Symptom

    <widget:message> Tag Library supports namespace:
    http://java.sun.com/jsf/composite/widget, but no tag was defined for
    name: message

and, visible in the very first page that rendered:

    <link type="text/css" rel="stylesheet" href="RES_NOT_FOUND"/>
    Unable to find resource javascript/dist/widget/Select.js

## Cause

`admin-frontend/src/main/webapp/` contains `secure/`, `templates/`,
`welcome.xhtml`, `favicon.ico`, `robots.txt` and `WEB-INF` — and no `resources/`.
That directory held the JSF composite components, the CSS, the JavaScript and
the images. All of it was stripped.

## What was done

`tools/generate-composite-components.py` recovers what the source does record:
which components exist, which of 9 libraries each belongs to, and what attributes
each is passed. 69 stubs were generated with accurate `cc:interface` blocks and
placeholder implementations that render their own name and their children.

Pages compose and render. `data-eva-stub="widget:dialog"` marks each stub in the
HTML so it is obvious what is placeholder.

## What remains

- **Appearance.** The stubs render structure, not EVA Admin's markup. This is the "make it look the same" work, driven by the manual screenshots (NF-011).
- **CSS.** No stylesheet is present; `RES_NOT_FOUND` appears wherever one is requested.
- **JavaScript.** `javascript/dist/**` is absent, so client behaviour — the select widgets, wait buttons, table column toggles — does nothing.
- **Images.** Including the logo referenced by `welcome.xhtml`.

None of this blocks the application. It blocks it *looking* like itself.
