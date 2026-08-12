-- The lower half of the area hierarchy: borough, polling district, polling place,
-- polling station, and the mv_area rows that mirror them.
--
-- 01-base.sql stops at the municipality, which leaves screens that need a deeper
-- context either bouncing to the chooser or failing outright:
--
--   KontekstAvhengigController.init() -> NullPointerException
--   MvAreaController.init()           -> NullPointerException
--
-- AreaLevelEnum runs ROOT 0, COUNTRY 1, COUNTY 2, MUNICIPALITY 3, BOROUGH 4,
-- POLLING_DISTRICT 5, POLLING_PLACE 6, POLLING_STATION 7, and AreaPath appends a
-- segment per level:
--
--   000000.47.03.0301.000001.0001.0001.01
--
-- NOT REAL GEOGRAPHY. One borough, one district, one place, one station under the
-- seeded municipality. Enough for the context chooser to have something to
-- choose and for context-dependent screens to resolve; nothing more.
--
-- Applied by tools/seed-db.sh after 04. Idempotent: 01's TRUNCATE ... CASCADE
-- clears these too.

BEGIN;

INSERT INTO borough (borough_pk, audit_oplock, borough_id, municipality,
                     borough_name, municipality_pk)
VALUES (1, 0, '000001', true, 'Sentrum', 1);

INSERT INTO polling_district (polling_district_pk, audit_oplock, polling_district_id,
                              municipality, polling_district_name,
                              parent_polling_district, technical_polling_district,
                              borough_pk)
VALUES (1, 0, '0001', true, 'Sentrum krets', false, false, 1);

INSERT INTO polling_place (polling_place_pk, audit_oplock, advance_vote_in_ballot_box,
                           election_day_voting, polling_place_id, polling_place_name,
                           public_place, using_polling_stations, polling_district_pk)
VALUES (1, 0, false, true, '0001', 'Sentrum skole', true, true, 1);

INSERT INTO polling_station (polling_station_pk, audit_oplock, polling_station_id,
                             polling_place_pk)
VALUES (1, 0, '01', 1);

-- mv_area rows for the four new levels. Each repeats every ancestor's id and
-- name, which is the whole point of the view.
INSERT INTO mv_area (mv_area_pk, area_level, area_path, election_event_id, election_event_name,
                     country_id, country_name, county_id, county_name,
                     municipality_id, municipality_name,
                     borough_id, borough_name,
                     polling_district_id, polling_district_name, parent_polling_district,
                     polling_place_id, polling_place_name,
                     polling_station_first, polling_station_last,
                     election_event_pk) VALUES
    (5, 4, '000000.47.03.0301.000001', '000000', 'Demo valghendelse',
     '47', 'Norge', '03', 'Oslo', '0301', 'Oslo', '000001', 'Sentrum',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1),
    (6, 5, '000000.47.03.0301.000001.0001', '000000', 'Demo valghendelse',
     '47', 'Norge', '03', 'Oslo', '0301', 'Oslo', '000001', 'Sentrum',
     '0001', 'Sentrum krets', false, NULL, NULL, NULL, NULL, 1),
    (7, 6, '000000.47.03.0301.000001.0001.0001', '000000', 'Demo valghendelse',
     '47', 'Norge', '03', 'Oslo', '0301', 'Oslo', '000001', 'Sentrum',
     '0001', 'Sentrum krets', false, '0001', 'Sentrum skole', NULL, NULL, 1),
    (8, 7, '000000.47.03.0301.000001.0001.0001.01', '000000', 'Demo valghendelse',
     '47', 'Norge', '03', 'Oslo', '0301', 'Oslo', '000001', 'Sentrum',
     '0001', 'Sentrum krets', false, '0001', 'Sentrum skole', '01', '01', 1);

SELECT setval('borough_borough_pk_seq', 1, true);
SELECT setval('polling_district_polling_district_pk_seq', 1, true);
SELECT setval('polling_place_polling_place_pk_seq', 1, true);
SELECT setval('polling_station_polling_station_pk_seq', 1, true);
SELECT setval('mv_area_mv_area_pk_seq', 8, true);

COMMIT;
