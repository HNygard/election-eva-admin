-- Reference data: the small lookup tables EVA Admin expects to be populated.
--
-- These are code tables, not election data. In the real system they arrive with
-- the Flyway migrations that were withheld (NF-007), so every one of them starts
-- empty here and the application fails on the first lookup that returns null:
--
--   UserDataServiceEjb.findUserMenuMetadata -> NullPointerException
--   because electionRepository.findElectionTypeById("F") found no row.
--
-- Ids come from the release's own constants where it defines them, and are noted
-- per table below. Names are Norwegian terms for the concepts; where the release
-- does not state a name, the name is ours and only affects display.
--
-- Applied by tools/seed-db.sh after 01-base.sql. Idempotent.
-- See docs/not-fixed-yet/NF-010.

BEGIN;

DELETE FROM election_type;
DELETE FROM vote_count_category;
DELETE FROM voting_category;
DELETE FROM marital_status;
DELETE FROM contest_status;
DELETE FROM ballot_status;
DELETE FROM count_qualifier;
DELETE FROM reporting_unit_type;
DELETE FROM party_category;

-- Ids from ElectionType: TYPE_PROPORTIONAL_REPRESENTATION = "F",
-- TYPE_REFERENDUM = "R". "M" for flertallsvalg is inferred by symmetry; nothing
-- in the published source names it.
INSERT INTO election_type (election_type_pk, audit_oplock, election_type_id, election_type_name) VALUES
    (1, 0, 'F', 'Forholdsvalg'),
    (2, 0, 'M', 'Flertallsvalg'),
    (3, 0, 'R', 'Folkeavstemning');

-- Ids are the CountCategory enum: FO, FS, VO, VF, VB, VS, BF.
-- early_voting is true for the forhånds categories (FO, FS, VF).
INSERT INTO vote_count_category (vote_count_category_pk, audit_oplock, early_voting,
                                 vote_count_category_id, mandatory_central_count,
                                 mandatory_count, mandatory_total_count,
                                 vote_count_category_name) VALUES
    (1, 0, true,  'FO', false, false, false, 'Forhåndsstemmer ordinære'),
    (2, 0, true,  'FS', false, false, false, 'Forhåndsstemmer særskilte'),
    (3, 0, false, 'VO', false, false, false, 'Valgtingsstemmer ordinære'),
    (4, 0, true,  'VF', false, false, false, 'Valgtingsstemmer fremmede'),
    (5, 0, false, 'VB', false, false, false, 'Valgtingsstemmer beredskap'),
    (6, 0, false, 'VS', false, false, false, 'Valgtingsstemmer særskilte'),
    (7, 0, false, 'BF', false, false, false, 'Blanke forhåndsstemmer');

INSERT INTO voting_category (voting_category_pk, audit_oplock, early_voting,
                             voting_category_id, voting_category_name) VALUES
    (1, 0, true,  'FO', 'Forhåndsstemme ordinær'),
    (2, 0, true,  'FS', 'Forhåndsstemme særskilt'),
    (3, 0, false, 'VO', 'Valgtingsstemme ordinær'),
    (4, 0, false, 'VS', 'Valgtingsstemme særskilt');

INSERT INTO marital_status (marital_status_pk, audit_oplock, marital_status_id, marital_status_name) VALUES
    (1, 0, 'UG', 'Ugift'),
    (2, 0, 'GI', 'Gift');

INSERT INTO contest_status (contest_status_pk, audit_oplock, contest_status_id, contest_status_name) VALUES
    (1, 0, 1, 'Under arbeid'),
    (2, 0, 2, 'Godkjent');

INSERT INTO ballot_status (ballot_status_pk, audit_oplock, ballot_status_id, ballot_status_name) VALUES
    (1, 0, 1, 'Under arbeid'),
    (2, 0, 2, 'Godkjent');

INSERT INTO count_qualifier (count_qualifier_pk, audit_oplock, count_qualifier_id, count_qualifier_name) VALUES
    (1, 0, 'FORELOPIG', 'Foreløpig telling'),
    (2, 0, 'ENDELIG', 'Endelig telling');

-- election_level follows ElectionLevelEnum, the same numbering as area levels.
INSERT INTO reporting_unit_type (reporting_unit_type_pk, audit_oplock, election_level,
                                 reporting_unit_type_id, reporting_unit_type_name) VALUES
    (1, 0, 0, 1, 'Valgstyret'),
    (2, 0, 2, 2, 'Fylkesvalgstyret'),
    (3, 0, 3, 3, 'Opptellingsvalgstyret');

INSERT INTO party_category (party_category_pk, audit_oplock, party_category_id, party_category_name) VALUES
    (1, 0, 'P', 'Parti'),
    (2, 0, 'G', 'Gruppe');

SELECT setval('election_type_election_type_pk_seq', 3, true);
SELECT setval('vote_count_category_vote_count_category_pk_seq', 7, true);
SELECT setval('voting_category_voting_category_pk_seq', 4, true);
SELECT setval('marital_status_marital_status_pk_seq', 2, true);
SELECT setval('contest_status_contest_status_pk_seq', 2, true);
SELECT setval('ballot_status_ballot_status_pk_seq', 2, true);
SELECT setval('count_qualifier_count_qualifier_pk_seq', 2, true);
SELECT setval('reporting_unit_type_reporting_unit_type_pk_seq', 3, true);
SELECT setval('party_category_party_category_pk_seq', 2, true);

COMMIT;
