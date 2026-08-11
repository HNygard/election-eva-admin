-- The ltree extension, and the views the schema generator cannot produce.
--
-- EVA Admin uses PostgreSQL's ltree for hierarchy containment. Area and election
-- paths are dotted strings ("000000.47.03.0301") and native queries ask ltree
-- whether one contains another:
--
--   join mv_election e on (text2ltree(e.election_path) @> text2ltree(ca.election_path))
--   join mv_area a on (text2ltree(a.area_path) <@ text2ltree(ca.area_path) ...)
--   -- ContestRelAreaRepository.java:44-47
--
-- JPA maps those columns as String, so schema generation emits varchar and never
-- mentions ltree. Without the extension every such query fails with
--
--   ERROR: function text2ltree(character varying) does not exist
--
-- This is a second, quieter instance of the D005 limitation: not a missing view
-- this time, but a missing extension that only native SQL reveals.

CREATE EXTENSION IF NOT EXISTS ltree;

-- Views the schema generator cannot produce.
--
-- D005 warned about this: the schema is generated from JPA entity classes, and a
-- view that no entity maps is invisible to that process. The application still
-- uses them, from native SQL, and Postgres answers:
--
--   ERROR: relation "role_include_all" does not exist
--   WFLYEJB0034: EJB Invocation failed on component RoleService for method
--   getAccumulatedSecLevelFor
--
-- Applied by tools/seed-db.sh, before the seed data (filename order).
-- See docs/not-fixed-yet/NF-025.

-- role_include_all: the transitive closure of the role hierarchy.
--
-- Reconstructed from its two use sites, which are the only description of it
-- that survives:
--
--   Role.java:84            SELECT r.* FROM role r JOIN role_include_all ri
--                           ON r.role_pk = ri.included_role_pk WHERE ri.role_pk = ?1
--   RoleRepository.java:58  SELECT MAX(r.security_level) FROM role r JOIN
--                           role_include_all ri ON r.role_pk = ri.included_role_pk
--                           WHERE ri.role_pk = :rolePk
--
-- Both read it as (role_pk, included_role_pk) and expect every role reachable
-- from role_pk, not just the directly included ones -- otherwise role_include
-- itself would have served and the view would have no reason to exist.
--
-- ASSUMPTION, and it matters: the closure below is reflexive, so a role includes
-- itself. Without that, getAccumulatedSecLevelFor returns NULL for any role that
-- includes no other, rather than that role's own security level, and a
-- "accumulated security level for this role" that ignores the role itself would
-- be a strange thing to compute. Reflexive is the reading consistent with both
-- call sites, but the original definition was not published.

CREATE OR REPLACE VIEW role_include_all AS
WITH RECURSIVE closure(role_pk, included_role_pk) AS (
    SELECT r.role_pk, r.role_pk
    FROM role r
  UNION
    SELECT c.role_pk, ri.included_role_pk
    FROM closure c
    JOIN role_include ri ON ri.role_pk = c.included_role_pk
)
SELECT role_pk, included_role_pk
FROM closure;


-- role_access_all: every access reachable from a role, following role includes.
--
-- Use sites (Role.java:93, Role.java:97):
--   SELECT a.* FROM access a JOIN role_access_all ra1
--   ON a.access_pk = ra1.access_pk WHERE ra1.role_pk = ?1
--
-- Read as (role_pk, access_pk). Built by composing role_include_all with
-- role_access, which is the only definition consistent with the name and the
-- queries.

CREATE OR REPLACE VIEW role_access_all AS
SELECT ria.role_pk,
       ra.access_pk
FROM role_include_all ria
JOIN role_access ra ON ra.role_pk = ria.included_role_pk;


-- role_access_all_active: the same, minus accesses inherited from disabled roles.
--
-- The name of the method that queries it says what "active" means:
--   AccessRepository.getIncludedAccessesNoDisabledRoles(role)   (AccessRepository:41)
--   GetAccessesSql                                              (GetAccessesSql:16)
--
-- ASSUMPTION: "no disabled roles" filters the INCLUDED role, not the querying
-- role. Filtering the querying role would make the view return nothing for a
-- disabled role, which the caller could check far more cheaply itself; filtering
-- the included ones is what stops a disabled role leaking its accesses into an
-- active role that includes it. That is the reading the method name supports,
-- but the original definition was not published.

CREATE OR REPLACE VIEW role_access_all_active AS
SELECT ria.role_pk,
       ra.access_pk
FROM role_include_all ria
JOIN role included_role ON included_role.role_pk = ria.included_role_pk
                       AND included_role.active
JOIN role_access ra ON ra.role_pk = ria.included_role_pk;
