-- Minimal seed: one election event, one role, one operator who can log in.
--
-- The release ships no reference data at all (NF-010) and the schema is
-- generated from entity classes, so every one of the 113 tables starts empty.
-- EVA Admin authenticates the tmp login correctly and then rejects the user:
--
--   TmpLoginFilter: "01017012345 is not a valid operator in the system!"
--
-- This is the smallest graph that gets past that. It is NOT a realistic election
-- setup and is not meant to be: the intended way to build one is EVA Admin's own
-- import functions, which cannot be reached until somebody can log in. This file
-- exists to break that circle and nothing more.
--
-- Path formats are the release's own, from AreaPath and ElectionPath:
--   AreaPath      EEVENT.CO.CT.MUNI    e.g. 202601.47.01
--   ElectionPath  EEVENT.EG.E.C        e.g. 202601.11.01
-- with a six character election event id.
--
-- Applied by tools/seed-db.sh. Idempotent: it deletes its own rows first.
-- See docs/not-fixed-yet/NF-010.

BEGIN;

DELETE FROM operator_role;
DELETE FROM operator;
DELETE FROM role;
DELETE FROM mv_election;
DELETE FROM mv_area;
DELETE FROM election_event;
DELETE FROM election_event_status;
DELETE FROM locale;

INSERT INTO locale (locale_pk, audit_oplock, locale_id, locale_name)
VALUES (1, 0, 'nb-NO', 'Bokmal');
-- The id is 'nb-NO' with a HYPHEN, not the underscore Java uses for bundle
-- names. TmpLoginFilter:130 calls translationService.findLocaleById("nb-NO"),
-- and with the underscore form the lookup returns null, UserData has no locale,
-- and every page dies with a NullPointerException at
-- TranslationProvider.getByElectionEvent:48.

INSERT INTO election_event_status (election_event_status_pk, audit_oplock,
                                   election_event_status_id, election_event_status_name)
VALUES (1, 0, 1, 'Under arbeid');

INSERT INTO election_event (election_event_pk, audit_oplock, demo_election,
                            electoral_roll_cut_off_date, election_event_id,
                            election_event_name, voter_import_municipality,
                            voting_card_deadline, voting_card_electoral_roll_date,
                            election_event_status_pk, locale_pk)
VALUES (1, 0, true, DATE '2026-07-01', '202601',
        'Rekonstruksjon 2026', false,
        DATE '2026-08-01', DATE '2026-07-01', 1, 1);

-- Election event level: area_level 0, the root of the area hierarchy.
INSERT INTO mv_area (mv_area_pk, area_level, area_path, election_event_id,
                     election_event_name, election_event_pk)
VALUES (1, 0, '202601', '202601', 'Rekonstruksjon 2026', 1);

INSERT INTO mv_election (mv_election_pk, election_event_id, election_event_name,
                         election_level, election_path, election_event_pk)
VALUES (1, '202601', 'Rekonstruksjon 2026', 0, '202601', 1);

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
VALUES (1, 0, true, true, 'Test', '01017012345', 'Operator',
        'Test Operator', 1);

INSERT INTO operator_role (operator_role_pk, audit_oplock, mv_area_pk,
                           mv_election_pk, operator_pk, role_pk)
VALUES (1, 0, 1, 1, 1, 1);

-- bigserial sequences must be moved past the hand-assigned ids, or the first
-- insert the application makes will collide.
SELECT setval('locale_locale_pk_seq', 1, true);
SELECT setval('election_event_status_election_event_status_pk_seq', 1, true);
SELECT setval('election_event_election_event_pk_seq', 1, true);
SELECT setval('mv_area_mv_area_pk_seq', 1, true);
SELECT setval('mv_election_mv_election_pk_seq', 1, true);
SELECT setval('role_role_pk_seq', 1, true);
SELECT setval('operator_operator_pk_seq', 1, true);
SELECT setval('operator_role_operator_role_pk_seq', 1, true);

COMMIT;
