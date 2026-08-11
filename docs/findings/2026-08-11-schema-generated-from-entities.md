# Schema generated from the release's entities, and what it revealed

`tools/schema-export/` now produces `docker/postgres/initdb/01-schema-generated.sql`
from the release's own entity classes, and PostgreSQL accepts it:

    $ docker exec eva-admin-postgres psql -U admin -d evote -tAc \
        "select table_schema, count(*) from information_schema.tables
         where table_schema in ('admin','public') group by table_schema"
    admin|113

    foreign keys: 176

All 113 land in the `admin` schema, which is what `00-schema.sql`'s `search_path`
is for — the entities are inconsistent about qualifying it.

## Cross-check against the sibling repo

D005 said an independent derivation should agree. It does, exactly:

| | tables | foreign keys |
|---|---:|---:|
| ours, from this repo's entities | 113 | 176 |
| `~/git/eva-admin-spring-boot-hacktogether` | 113 | 176 |

and the table name sets are identical — `diff` is empty. Two independent routes,
different Hibernate bootstraps, same model.

## What the first attempt got wrong, and why it matters

The generator initially read the entity list from the release's own
`admin/admin-backend/src/test/resources/META-INF/persistence.xml`, which seemed
the most authoritative source available. It produced **110** tables, and the
three missing ones led somewhere useful:

    certificate_revocation_list
    legacy_polling_district
    valgnattrapport

Counting properly:

| Source of the entity list | Classes |
|---|---:|
| `admin-backend` test persistence unit | 117 |
| union of *every* shipped `persistence.xml` | 119 |
| `@Entity` in the release main source | **120** |

`CertificateRevocationList` appears only in `admin-backend-common`'s test unit,
`Valgnattrapport` only in `admin-valgnatt`'s, and `LegacyPollingDistrict` in none
of them at all — even though `admin-configuration` has a
`LegacyPollingDistrictRepository` and `admin-frontend` a producer for it.

**No persistence unit in the release covers the whole model.** The per-module test
units each cover the subset their own tests need. The runtime unit that covered
all 120 was in the withheld configuration.

This is a direct correction to NF-006: a reconstructed runtime `persistence.xml`
must be built from the `@Entity` annotations, not copied from the admin-backend
test unit. Copying it would silently omit three entities, and the failure would
appear much later as a missing table at query time.

The generator now derives its list from `^@Entity` in main source, which is the
only complete answer available.

## Implementation notes

`Persistence.generateSchema` was the obvious API and is unusable here: it builds
a SessionFactory, and a SessionFactory demands a JDBC connection even when only
writing a script.

    [PersistenceUnit: evaSchemaExportPU] Unable to build Hibernate
    SessionFactory: The application must supply JDBC connections

Hibernate's own `SchemaExport` over `MetadataSources` needs no connection.
`hibernate.temp.use_jdbc_metadata_defaults=false` stops it reaching for one to
infer defaults.

The tool is a standalone Maven project, not a reactor module, so `admin/pom.xml`
stays untouched. It pins `hibernate-core 5.0.10.Final` to match the release: a
different Hibernate would generate a different schema, which would defeat the
purpose.

## Limits, unchanged from D005

Still no seed data, still no sequences, and the `no.evote.model.views.*` entities
are emitted as ordinary tables rather than the views they are in the real system.
The schema is good enough to deploy against; it is not production-shaped.
