# NF-010 — No reference data and no authentication configuration

Status: OPEN
Milestone: M3

## Symptom

Not yet observed. A generated schema has 113 empty tables. Login will have no
operator to authenticate, and RBAC will have no roles to grant.

## What is known

Schema generation emits no rows at all (D005). The system needs at least:

- locales and text ids — the UI resolves labels through `LocaleText`/`TextId`
- RBAC rows: `Access`, `Role`, `RoleAccess`, `RoleAreaLevel`, `Operator`, `OperatorRole`
- an election event, and the area hierarchy beneath it (country, county, municipality, borough, polling district, polling place)

`admin/admin-backend/src/test/resources/` holds import fixtures that hint at the
shapes involved: `area-hierarchy-import.txt`, `electoralRoll/`, `INIT_*.TXT`,
`markoff-evoting-import.zip`, and `reporting/initializeDB.sql`. That last file is
worth reading early — it may be a substantial part of the answer.

Authentication: the 2019 system used Buypass certificates
(`no.valg.eva.admin.backend.common.port.adapter.service.buypass`), including CRL
checking. That is not reproducible here, so a local-only authentication path will
be needed, and it must be obvious in the code that it is local-only.

## How to approach

Read `admin/admin-backend/src/test/resources/reporting/initializeDB.sql` first.
Then look at how the area hierarchy import parses its fixtures — using the
application's own import path to load data is far better than hand-writing INSERT
statements, because it exercises the code and cannot drift from the model.

## Done when

An operator can log in and reach a page with real data, and the route used to
create that data is documented and repeatable.
