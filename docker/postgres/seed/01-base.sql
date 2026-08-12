-- Test data: one election event, an area hierarchy, one role, one operator.
--
-- The release ships no reference data at all (NF-010), so all 113 tables start
-- empty and EVA Admin rejects every login. This is the demo dataset.
--
-- NOT A REAL ELECTION SETUP, and not a reconstruction of one. Real election
-- events are built through EVA Admin's own import functions, which cannot be
-- reached until somebody can log in. This file exists to break that circle and
-- to give the screens something to show.
--
-- The election event id is '000000' on purpose. ElectionPath.ROOT_ELECTION_EVENT_ID
-- is "000000", and AccessServiceBean skips signing the access cache for it:
--
--   // Don't try to sign data for the admin event, since it doesn't have any key.
--
-- Any other id sends the login through signDataWithCurrentElectionEventCertificate,
-- which needs election-event key material that does not exist here.
--
-- Path formats are the release's own, from AreaPath and ElectionPath:
--   AreaPath      EEVENT.CO.CT.MUNI    000000.47.03.0301
-- with a six character election event id.
--
-- Applied by tools/seed-db.sh, after 00-views.sql. Idempotent.
-- See docs/not-fixed-yet/NF-010.

BEGIN;

-- Reset. TRUNCATE ... CASCADE rather than an ordered list of DELETEs: the seed
-- spans four files and their tables reference each other across file boundaries
-- (contest_area in 04 points at mv_area here, election_group in 04 at
-- election_event here), so any hand-maintained order breaks the moment the seed
-- grows. CASCADE works the graph out for itself.
--
-- Safe because this database holds nothing but seed data. It would not be safe
-- against a real EVA Admin database, which is one more reason this file is for a
-- local reconstruction only.
TRUNCATE TABLE
    operator_role, operator, role_access, role, access,
    contest_area, contest, election, election_group, mv_election,
    mv_area, polling_station, polling_place, polling_district, borough,
    municipality, county, country,
    municipality_status, county_status,
    election_event, election_event_status, area_level, locale,
    election_type, vote_count_category, voting_category, marital_status,
    contest_status, ballot_status, count_qualifier, reporting_unit_type,
    party_category
CASCADE;

-- The id is 'nb-NO' with a HYPHEN, not the underscore Java uses for bundle
-- names. TmpLoginFilter:130 calls translationService.findLocaleById("nb-NO"),
-- and with the underscore form the lookup returns null, UserData has no locale,
-- and every page dies with a NullPointerException at
-- TranslationProvider.getByElectionEvent:48.
INSERT INTO locale (locale_pk, audit_oplock, locale_id, locale_name) VALUES
    (1, 0, 'nb-NO', 'Bokmal'),
    (2, 0, 'nn-NO', 'Nynorsk');

-- Levels are AreaLevelEnum: ROOT 0, COUNTRY 1, COUNTY 2, MUNICIPALITY 3,
-- BOROUGH 4, POLLING_DISTRICT 5, POLLING_PLACE 6, POLLING_STATION 7.
INSERT INTO area_level (area_level_pk, audit_oplock, area_level_id, area_level_name) VALUES
    (1, 0, 0, 'Valghendelse'),
    (2, 0, 1, 'Land'),
    (3, 0, 2, 'Fylke'),
    (4, 0, 3, 'Kommune'),
    (5, 0, 4, 'Bydel'),
    (6, 0, 5, 'Krets'),
    (7, 0, 6, 'Stemmested'),
    (8, 0, 7, 'Stemmekrets');

INSERT INTO election_event_status (election_event_status_pk, audit_oplock,
                                   election_event_status_id, election_event_status_name) VALUES
    (1, 0, 1, 'Under arbeid'),
    (2, 0, 2, 'Klargjort'),
    (3, 0, 3, 'Avsluttet');

INSERT INTO county_status (county_status_pk, audit_oplock, county_status_id, county_status_name)
VALUES (1, 0, 1, 'Under arbeid');

INSERT INTO municipality_status (municipality_status_pk, audit_oplock,
                                 municipality_status_id, municipality_status_name)
VALUES (1, 0, 1, 'Under arbeid');

INSERT INTO election_event (election_event_pk, audit_oplock, demo_election,
                            electoral_roll_cut_off_date, election_event_id,
                            election_event_name, voter_import_municipality,
                            voting_card_deadline, voting_card_electoral_roll_date,
                            election_event_status_pk, locale_pk)
VALUES (1, 0, true, DATE '2026-07-01', '000000',
        'Demo valghendelse', false,
        DATE '2026-08-01', DATE '2026-07-01', 1, 1);

-- Area hierarchy: Norway / Oslo county / Oslo municipality.
INSERT INTO country (country_pk, audit_oplock, country_id, country_name, election_event_pk)
VALUES (1, 0, '47', 'Norge', 1);

INSERT INTO county (county_pk, audit_oplock, county_id, county_name,
                    country_pk, county_status_pk, locale_pk)
VALUES (1, 0, '03', 'Oslo', 1, 1, 1);

INSERT INTO municipality (municipality_pk, audit_oplock, avkrysningsmanntall_kjort,
                          electronic_markoffs, municipality_id, municipality_name,
                          required_protocol_count, technical_polling_districts_allowed,
                          county_pk, locale_pk, municipality_status_pk)
VALUES (1, 0, false, false, '0301', 'Oslo', false, false, 1, 1, 1);

-- mv_area is a materialised view in the real system; here it is an ordinary
-- table (D005), so the rows have to be written explicitly. Each level repeats
-- the ids and names of its ancestors, which is what makes it a view worth having.
INSERT INTO mv_area (mv_area_pk, area_level, area_path, election_event_id, election_event_name,
                     country_id, country_name, county_id, county_name,
                     municipality_id, municipality_name, election_event_pk) VALUES
    (1, 0, '000000',              '000000', 'Demo valghendelse', NULL, NULL,   NULL, NULL,   NULL,   NULL,   1),
    (2, 1, '000000.47',           '000000', 'Demo valghendelse', '47', 'Norge', NULL, NULL,  NULL,   NULL,   1),
    (3, 2, '000000.47.03',        '000000', 'Demo valghendelse', '47', 'Norge', '03', 'Oslo', NULL,  NULL,   1),
    (4, 3, '000000.47.03.0301',   '000000', 'Demo valghendelse', '47', 'Norge', '03', 'Oslo', '0301', 'Oslo', 1);

-- The election hierarchy and the operator_role rows that reference it are built
-- in 04-election-hierarchy.sql, because operator_role points at an mv_election
-- and the rows have to exist first.

-- security_level 3 matches the default the tmp login form offers.
INSERT INTO role (role_pk, audit_oplock, active, check_candidate_conflicts,
                  role_id, mutually_exclusive, role_name, security_level,
                  is_user_support, election_event_pk)
VALUES (1, 0, true, false, 'ELECTION_EVENT_ADMIN', false,
        'Valghendelsesadministrator', 3, false, 1);

-- operator_id is the uid the tmp login screen asks for as "Bruker ID".
INSERT INTO operator (operator_pk, audit_oplock, active, contact_info_confirmed,
                      first_name, operator_id, last_name, name_line,
                      election_event_pk)
VALUES (1, 0, true, true, 'Demo', '01017012345', 'Operator', 'Demo Operator', 1);


-- bigserial sequences must be moved past the hand-assigned ids, or the first
-- insert the application makes will collide.
SELECT setval('locale_locale_pk_seq', 2, true);
SELECT setval('area_level_area_level_pk_seq', 8, true);
SELECT setval('election_event_status_election_event_status_pk_seq', 3, true);
SELECT setval('county_status_county_status_pk_seq', 1, true);
SELECT setval('municipality_status_municipality_status_pk_seq', 1, true);
SELECT setval('election_event_election_event_pk_seq', 1, true);
SELECT setval('country_country_pk_seq', 1, true);
SELECT setval('county_county_pk_seq', 1, true);
SELECT setval('municipality_municipality_pk_seq', 1, true);
SELECT setval('mv_area_mv_area_pk_seq', 4, true);
SELECT setval('role_role_pk_seq', 1, true);
SELECT setval('operator_operator_pk_seq', 1, true);

COMMIT;
