# NF-022 — The i18n message bundle is missing, so no page can render

Status: PARTIALLY FIXED — bundle generated, values are placeholders
Milestone: M3

## Symptom

    MissingResourceException: Can't find bundle for base name
    no.valg.eva.admin.common.Messages, locale nb_NO

thrown from `TranslationProvider.postConstruct`, which fails `MessageProvider`,
which fails every JSF view. HTTP 500 on every page.

## Cause

`admin-common/src/main/resources-unfiltered/` does not exist. Several poms refer
to it — `admin-common` includes it as a resource directory, and `admin-frontend`
and `admin-backend-common` copy `**/*.properties` out of it — so the release
plainly had one and it was stripped, like `log4j.xml` and `version.properties`
before it.

The build says so out loud, and this was visible in the very first build log:

    skip non existing resourceDirectory .../admin-common/src/main/resources-unfiltered

Bundles do survive under `src/test/resources-unfiltered` in three modules, but
they are fixtures: 3 keys, 3 keys and 22 keys, including `@test1 bokmål`.

## What was done

`tools/generate-message-bundle.sh` writes `Messages_nb_NO.properties` and
`Messages_nn_NO.properties` with **1386 keys recovered from the source** — every
`@key` referenced by the XHTML pages and the Java code. Pages now render.

The **keys are recovered; the values are not**. The real Norwegian text was never
published, and per election event the text lives in the database
(`TranslationProvider` consults `LocaleText` first and this bundle second). Each
value is therefore a placeholder derived from its own key:

    @area.borough.create.header      Create header
    @access.konfig.grunnlagsdata     Konfig grunnlagsdata

That is deliberate: a placeholder that echoes its key makes it obvious on screen
which labels are still unknown, which is exactly what the screenshot comparison
in M3 needs in order to fill them in.

## What remains

- Real wording, from the manuals and archived valgmedarbeiderportalen screenshots (NF-011). This is the "make it look the same" step.
- The 22 genuine strings in `admin-voting`'s test fixture are not merged, because they are test data and are not UTF-8 encoded. Worth harvesting when the real wording work starts.
- Nothing here should be mistaken for EVA Admin's own wording, in a screenshot or anywhere else.
