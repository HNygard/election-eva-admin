-- Database views the schema generator cannot produce.
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
