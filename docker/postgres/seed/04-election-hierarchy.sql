-- The election hierarchy: election group, election, contest, and the mv_election
-- rows that mirror them (NF-028).
--
-- Without this most screens redirect to the context chooser, because they need an
-- election context and the seed stopped at the event:
--
--   GET /secure/counting/countingOverview.xhtml
--     302 -> /secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|2,3][side|uri|42]
--
-- That redirect is EVA Admin behaving correctly. What was missing is the data
-- behind it.
--
-- Modelled on a Norwegian municipal election: one election group, one election of
-- type F (Forholdsvalg, from ElectionType.TYPE_PROPORTIONAL_REPRESENTATION), and
-- one contest covering the seeded municipality.
--
-- ElectionPath is EEVENT.EG.E.C, so:
--   000000           the election event
--   000000.01        the election group
--   000000.01.01     the election
--   000000.01.01.000301  the contest
--
-- The segment widths are not free. ElectionPath validates against
--   ^(\d{6})(\.(\d{2})(\.(\d{2})(\.(\d{6}))?)?)?$
-- so the group and election segments are two digits and the CONTEST segment is
-- six. A two digit contest is rejected outright:
--   IllegalArgumentException: illegal path <000000.01.01.01>
-- Which is also why mv_election.election_path is varchar(19): 6+1+2+1+2+1+6.
--
-- Election levels follow ElectionLevelEnum, the same numbering as area levels:
-- 0 event, 1 group, 2 election, 3 contest.
--
-- NOT REAL ELECTION DATA. Thresholds and divisors below are plausible Norwegian
-- values, not values taken from any published configuration: the real ones would
-- come from an EML import. Nothing computed from them should be treated as a
-- correct election result.
--
-- Applied by tools/seed-db.sh after 03-reference.sql. Idempotent.

BEGIN;

DELETE FROM operator_role;
DELETE FROM contest_area;
DELETE FROM contest;
DELETE FROM election;
DELETE FROM election_group;
DELETE FROM mv_election;

INSERT INTO election_group (election_group_pk, audit_oplock, advance_vote_in_ballot_box,
                            electronic_markoffs, election_group_id, election_group_name,
                            scanning_permitted,
                            validate_polling_place_electoral_board_and_list_proposal,
                            validate_role_and_list_proposal, election_event_pk)
VALUES (1, 0, false, false, '01', 'Kommunestyre- og fylkestingsvalg',
        false, false, false, 1);

-- settlement_first_divisor 1.4 is Norway's modified Sainte-Lague first divisor.
-- It is stored here as a whole number because the column is numeric(3,0); the
-- real configuration may scale it differently, which is one reason not to trust
-- any settlement computed from this data.
-- max_candidate_name_length and max_candidate_residence_profession_length are
-- NULLABLE columns mapped to primitive int fields (Election.java:77-78). Leaving
-- them NULL is not an option:
--
--   PropertyAccessException: Null value was assigned to a property of primitive
--   type setter of ...Election.maxCandidateNameLength
--
-- so every such column has to carry a value even where the schema permits none.
-- The lengths below match the candidate table's own varchar(50) columns.
INSERT INTO election (election_pk, audit_oplock, area_level,
                      candidate_rank_vote_share_threshold, candidates_in_contest_area,
                      end_date_of_birth, election_id, leveling_seats,
                      leveling_seats_vote_share_threshold, election_name,
                      max_candidate_name_length, max_candidate_residence_profession_length,
                      max_candidates, max_candidates_addition,
                      min_candidates, min_candidates_addition,
                      penultimate_recount, personal, renumber, renumber_limit,
                      settlement_first_divisor, single_area, strikeout, writein,
                      writein_local_override, election_group_pk, election_type_pk)
VALUES (1, 0, 3, 5, false, DATE '2008-12-31', '01', 0, 4, 'Kommunestyrevalg',
        50, 50, 72, 6, 7, 0,
        false, true, false, false, 1, false, true, true, false, 1, 1);

INSERT INTO contest (contest_pk, audit_oplock, contest_id, contest_name,
                     contest_status_pk, election_pk)
VALUES (1, 0, '000301', 'Oslo kommunestyre', 1, 1);

-- Ties the contest to the municipality in the area hierarchy. This is what
-- ContestRelAreaRepository's ltree queries walk:
--   join mv_area a on (text2ltree(a.area_path) <@ text2ltree(ca.area_path) ...)
INSERT INTO contest_area (contest_area_pk, audit_oplock, child_area, parent_area,
                          contest_pk, mv_area_pk)
VALUES (1, 0, false, true, 1, 4);

-- mv_election mirrors the hierarchy the way mv_area mirrors the geography, with
-- each level repeating its ancestors' ids and names.
INSERT INTO mv_election (mv_election_pk, election_event_id, election_event_name,
                         election_level, election_path,
                         election_group_id, election_group_name,
                         election_id, election_name,
                         contest_id, contest_name,
                         area_level, single_area,
                         election_event_pk, election_group_pk, election_pk, contest_pk) VALUES
    (1, '000000', 'Demo valghendelse', 0, '000000',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL),
    (2, '000000', 'Demo valghendelse', 1, '000000.01',
     '01', 'Kommunestyre- og fylkestingsvalg', NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, NULL, NULL),
    (3, '000000', 'Demo valghendelse', 2, '000000.01.01',
     '01', 'Kommunestyre- og fylkestingsvalg', '01', 'Kommunestyrevalg', NULL, NULL, 3, false, 1, 1, 1, NULL),
    (4, '000000', 'Demo valghendelse', 3, '000000.01.01.000301',
     '01', 'Kommunestyre- og fylkestingsvalg', '01', 'Kommunestyrevalg',
     '000301', 'Oslo kommunestyre', 3, false, 1, 1, 1, 1);

-- Two roles for the same operator, at different points in the area hierarchy.
--
-- Role 1 sits at the election event root and suits configuration screens. Role 2
-- sits at the municipality, and exists because many screens require a geography
-- context at a specific level and otherwise bounce to the context chooser.
-- Both point at the contest-level mv_election, which is the deepest context.
INSERT INTO operator_role (operator_role_pk, audit_oplock, mv_area_pk,
                           mv_election_pk, operator_pk, role_pk) VALUES
    (1, 0, 1, 4, 1, 1),
    (2, 0, 4, 4, 1, 1);

SELECT setval('operator_role_operator_role_pk_seq', 2, true);
SELECT setval('election_group_election_group_pk_seq', 1, true);
SELECT setval('election_election_pk_seq', 1, true);
SELECT setval('contest_contest_pk_seq', 1, true);
SELECT setval('contest_area_contest_area_pk_seq', 1, true);
SELECT setval('mv_election_mv_election_pk_seq', 4, true);

COMMIT;
