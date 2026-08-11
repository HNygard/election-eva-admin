-- Creates the "admin" schema and makes it the default for the admin user.
--
-- Most EVA Admin entities carry no explicit schema and rely on the connecting
-- user's search_path, but at least one pins it: the generated DDL emits
-- "create table admin.election_event_locale". Both styles therefore have to land
-- in the same place, which means search_path must put "admin" first.
--
-- Runs before 01-schema-generated.sql (filename order).

CREATE SCHEMA IF NOT EXISTS admin AUTHORIZATION admin;

ALTER ROLE admin IN DATABASE evote SET search_path TO admin, public;

-- DatabaseSchemaCheckerBean.java:37 configures Flyway with
-- setSchemas("admin", "audit"), so the real system has an audit schema too.
-- Nothing generated targets it -- the audit entities (no.evote.model.views.*)
-- come out unqualified -- but Flyway expects it to exist at startup.
CREATE SCHEMA IF NOT EXISTS audit AUTHORIZATION admin;
