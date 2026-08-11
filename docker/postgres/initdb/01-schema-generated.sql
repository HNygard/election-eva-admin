
    create table admin.election_event_locale (
        election_event_locale_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_event_pk int8 not null,
        locale_pk int8 not null,
        primary key (election_event_locale_pk)
    );

    alter table admin.election_event_locale 
        add constraint UK1qmmb30xm9c8yxle3sch6b5my unique (election_event_pk, locale_pk);

    create table aarsakskode (
        aarsakskode_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        aarsakskode_id varchar(2) not null,
        aarsakskode_name varchar(100),
        primary key (aarsakskode_pk)
    );

    create table access (
        access_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        access_name varchar(50) not null,
        access_path varchar(100) not null,
        primary key (access_pk)
    );

    create table affiliation (
        affiliation_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        approved boolean not null,
        display_order int4,
        show_candidate_profession boolean not null,
        show_candidate_residence boolean not null,
        ballot_pk int8 not null,
        party_pk int8 not null,
        primary key (affiliation_pk)
    );

    create table affiliation_vote_count (
        affiliation_vote_count_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        added_votes int4 not null,
        ballots int4 not null,
        baseline_votes int4 not null,
        early_voting_ballots int4 not null,
        early_voting_modified_ballots int4 not null,
        election_day_ballots int4 not null,
        election_day_modified_ballots int4 not null,
        modified_ballots int4 not null,
        subtracted_votes int4 not null,
        votes int4 not null,
        affiliation_pk int8 not null,
        settlement_pk int8 not null,
        primary key (affiliation_vote_count_pk)
    );

    create table antall_stemmesedler_lagt_til_side (
        antall_stemmesedler_lagt_til_side_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        antall_stemmesedler int4 not null,
        contest_pk int8,
        election_group_pk int8 not null,
        municipality_pk int8 not null,
        primary key (antall_stemmesedler_lagt_til_side_pk)
    );

    create table area_level (
        area_level_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        area_level_id int4 not null,
        area_level_name varchar(50) not null,
        primary key (area_level_pk)
    );

    create table ballot (
        ballot_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        approved boolean not null,
        display_order int4,
        ballot_id varchar(10) not null,
        ballot_status_pk int8 not null,
        contest_pk int8 not null,
        locale_pk int8 not null,
        primary key (ballot_pk)
    );

    create table ballot_count (
        ballot_count_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        modified_ballots int4 not null,
        unmodified_ballots int4 not null,
        ballot_pk int8,
        ballot_rejection_pk int8,
        vote_count_pk int8 not null,
        primary key (ballot_count_pk)
    );

    create table ballot_rejection (
        ballot_rejection_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        early_voting boolean not null,
        ballot_rejection_id varchar(6) not null,
        ballot_rejection_name varchar(50) not null,
        primary key (ballot_rejection_pk)
    );

    create table ballot_status (
        ballot_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        ballot_status_id int4 not null,
        ballot_status_name varchar(255) not null,
        primary key (ballot_status_pk)
    );

    create table batch (
        batch_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        batch_category varchar(255) not null,
        info_text varchar(150),
        message_text varchar(500),
        batch_number int4 not null,
        batch_status_pk int8 not null,
        batch_binary_data_pk int8,
        election_event_pk int8 not null,
        operator_role_pk int8,
        primary key (batch_pk)
    );

    create table batch_status (
        batch_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        batch_status_id int4 not null,
        batch_status_name varchar(50) not null,
        primary key (batch_status_pk)
    );

    create table binary_data (
        binary_data_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        binary_data bytea not null,
        column_name varchar(50) not null,
        file_name varchar(226) not null,
        mime_type varchar(255) not null,
        binary_data_number int4 not null,
        table_name varchar(50) not null,
        election_event_pk int8 not null,
        primary key (binary_data_pk)
    );

    create table borough (
        borough_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        borough_id varchar(6) not null,
        municipality boolean not null,
        borough_name varchar(50) not null,
        municipality_pk int8 not null,
        primary key (borough_pk)
    );

    create table candidate (
        candidate_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        address_line1 varchar(50),
        address_line2 varchar(50),
        address_line3 varchar(50),
        approved boolean not null,
        baseline_votes boolean not null,
        date_of_birth date,
        display_order int4 not null,
        email varchar(129),
        first_name varchar(50) not null,
        candidate_id varchar(11) not null,
        info_text varchar(150),
        last_name varchar(50) not null,
        middle_name varchar(50),
        name_line varchar(152) not null,
        post_town varchar(50),
        postal_code varchar(4),
        profession varchar(50),
        residence varchar(50),
        telephone_number varchar(35),
        affiliation_pk int8,
        ballot_pk int8 not null,
        marital_status_pk int8 not null,
        primary key (candidate_pk)
    );

    create table candidate_audit (
        audit_oplock int4 not null,
        audit_timestamp timestamp not null,
        candidate_pk int4 not null,
        address_line1 varchar(255),
        address_line2 varchar(255),
        address_line3 varchar(255),
        affiliation_pk int4,
        approved boolean,
        audit_operation varchar(1),
        ballot_pk int8,
        baseline_votes boolean,
        candidate_id varchar(11),
        date_of_birth date,
        display_order int4,
        email varchar(129),
        first_name varchar(255),
        info_text varchar(255),
        last_name varchar(255),
        marital_status_pk int4,
        middle_name varchar(255),
        name_line varchar(255),
        post_town varchar(255),
        postal_code varchar(4),
        profession varchar(255),
        residence varchar(255),
        telephone_number varchar(35),
        primary key (audit_oplock, audit_timestamp, candidate_pk)
    );

    create table candidate_rank (
        candidate_rank_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        rank_number int4 not null,
        votes numeric(10, 0) not null,
        affiliation_pk int8 not null,
        candidate_pk int8 not null,
        settlement_pk int8 not null,
        primary key (candidate_rank_pk)
    );

    create table candidate_seat (
        candidate_seat_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        dividend int4 not null,
        divisor numeric(5, 0) not null,
        elected boolean not null,
        quotient numeric(14, 0) not null,
        seat_number int4 not null,
        affiliation_pk int8 not null,
        candidate_pk int8 not null,
        settlement_pk int8 not null,
        primary key (candidate_seat_pk)
    );

    create table candidate_vote (
        candidate_vote_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        renumber_position int4,
        candidate_pk int8 not null,
        cast_vote_pk int8 not null,
        vote_category_pk int8 not null,
        primary key (candidate_vote_pk)
    );

    create table candidate_vote_count (
        candidate_vote_count_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        early_voting_votes numeric(10, 0) not null,
        election_day_votes numeric(10, 0) not null,
        rank_number int4,
        votes numeric(10, 0) not null,
        affiliation_pk int8 not null,
        candidate_pk int8 not null,
        settlement_pk int8 not null,
        vote_category_pk int8 not null,
        primary key (candidate_vote_count_pk)
    );

    create table cast_vote (
        cast_vote_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        cast_vote_id varchar(10) not null,
        type varchar(255) not null,
        ballot_count_pk int8 not null,
        scan_binary_data_pk int8,
        primary key (cast_vote_pk)
    );

    create table cast_vote_batch (
        cast_vote_batch_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        batch_id varchar(255),
        process varchar(255) not null,
        ballot_count_pk int8,
        operator_pk int8 not null,
        primary key (cast_vote_batch_pk)
    );

    create table cast_vote_batch_member (
        cast_vote_batch_member_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        done boolean,
        serial_number int4,
        cast_vote_pk int8,
        cast_vote_batch_pk int8 not null,
        primary key (cast_vote_batch_member_pk)
    );

    create table certificate_revocation_list (
        certificate_revocation_list_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        encoded_crl bytea not null,
        issuer_dn varchar(100) not null,
        next_update timestamp not null,
        updated timestamp not null,
        primary key (certificate_revocation_list_pk)
    );

    create table configuration (
        configuration_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        version_id varchar(10) not null,
        primary key (configuration_pk)
    );

    create table contest (
        contest_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        end_date_of_birth date,
        contest_id varchar(6) not null,
        max_candidates int4,
        max_renumber int4,
        max_votes int4,
        max_write_in int4,
        min_candidates int4,
        min_proposers_new_party int4,
        min_proposers_old_party int4,
        min_votes int4,
        contest_name varchar(100) not null,
        number_of_positions int4,
        penultimate_recount boolean,
        contest_status_pk int8 not null,
        election_pk int8 not null,
        primary key (contest_pk)
    );

    create table contest_area (
        contest_area_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        child_area boolean not null,
        parent_area boolean not null,
        contest_pk int8 not null,
        mv_area_pk int8 not null,
        primary key (contest_area_pk)
    );

    create table contest_rel_area (
        mv_area_pk int8 not null,
        mv_election_pk int8 not null,
        area_level int4,
        area_path varchar(255),
        borough_name varchar(255),
        contest_area_level int4,
        contest_name varchar(255),
        contest_pk int8,
        country_name varchar(255),
        county_name varchar(255),
        election_event_pk int8,
        election_group_name varchar(255),
        election_group_pk int8,
        election_name varchar(255),
        election_path varchar(255),
        election_pk int8,
        municipality_name varchar(255),
        polling_district_name varchar(255),
        primary key (mv_area_pk, mv_election_pk)
    );

    create table contest_report (
        contest_report_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        contest_pk int8 not null,
        reporting_unit_pk int8 not null,
        primary key (contest_report_pk)
    );

    create table contest_report_text (
        contest_report_text_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        contest_report_text varchar(150) not null,
        contest_report_text_name varchar(50) not null,
        contest_report_pk int8 not null,
        primary key (contest_report_text_pk)
    );

    create table contest_status (
        contest_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        contest_status_id int4 not null,
        contest_status_name varchar(50) not null,
        primary key (contest_status_pk)
    );

    create table contest_text (
        contest_text_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        contest_text varchar(150) not null,
        contest_text_name varchar(50) not null,
        contest_pk int8 not null,
        primary key (contest_text_pk)
    );

    create table contest_voting (
        contest_pk int8 not null,
        voting_pk int8 not null,
        approved boolean,
        borough_id varchar(6),
        borough_name varchar(255),
        borough_pk int8,
        cast_timestamp timestamp,
        contest_id varchar(8),
        contest_name varchar(255),
        country_id varchar(2),
        country_name varchar(255),
        country_pk int8,
        county_id varchar(2),
        county_name varchar(255),
        county_pk int8,
        date_of_birth date,
        early_voting boolean,
        election_event_id varchar(8),
        election_event_name varchar(255),
        election_event_pk int8,
        election_group_id varchar(8),
        election_group_name varchar(255),
        election_group_pk int8,
        election_id varchar(8),
        election_name varchar(255),
        election_pk int8,
        late_validation boolean,
        municipality_id varchar(4),
        municipality_name varchar(255),
        municipality_pk int8,
        mv_area_pk int8,
        mv_election_pk int8,
        name_line varchar(255),
        polling_district_id varchar(4),
        polling_district_name varchar(255),
        polling_district_pk int8,
        validation_timestamp timestamp,
        voter_id varchar(11),
        voter_pk int8,
        voting_borough_id varchar(255),
        voting_borough_name varchar(255),
        voting_borough_pk int8,
        voting_category_id varchar(4),
        voting_category_name varchar(255),
        voting_country_id varchar(255),
        voting_country_name varchar(255),
        voting_country_pk int8,
        voting_county_id varchar(255),
        voting_county_name varchar(255),
        voting_county_pk int8,
        voting_municipality_id varchar(255),
        voting_municipality_name varchar(255),
        voting_municipality_pk int8,
        voting_number int4,
        voting_polling_district_id varchar(255),
        voting_polling_district_name varchar(255),
        voting_polling_district_pk int8,
        voting_polling_place_id varchar(255),
        voting_polling_place_name varchar(255),
        voting_polling_place_pk int8,
        voting_rejection_pk int8,
        primary key (contest_pk, voting_pk)
    );

    create table count_qualifier (
        count_qualifier_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        count_qualifier_id varchar(10) not null,
        count_qualifier_name varchar(50) not null,
        primary key (count_qualifier_pk)
    );

    create table country (
        country_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        country_id varchar(2) not null,
        country_name varchar(50) not null,
        election_event_pk int8 not null,
        primary key (country_pk)
    );

    create table county (
        county_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        county_id varchar(2) not null,
        county_name varchar(50) not null,
        country_pk int8 not null,
        county_status_pk int8 not null,
        locale_pk int8 not null,
        primary key (county_pk)
    );

    create table county_local_config_status (
        county_local_config_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        language boolean not null,
        list_proposals boolean not null,
        reporting_unit_fylkesvalgstyre boolean not null,
        scanning boolean not null,
        county_pk int8 not null,
        primary key (county_local_config_status_pk)
    );

    create table county_status (
        county_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        county_status_id int4 not null,
        county_status_name varchar(50) not null,
        primary key (county_status_pk)
    );

    create table election (
        election_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        area_level int4 not null,
        baseline_vote_factor numeric(3, 0),
        candidate_rank_vote_share_threshold numeric(3, 0) not null,
        candidates_in_contest_area boolean not null,
        end_date_of_birth date not null,
        election_id varchar(8) not null,
        leveling_seats int4 not null,
        leveling_seats_vote_share_threshold numeric(3, 0) not null,
        max_candidate_name_length int4,
        max_candidate_residence_profession_length int4,
        max_candidates int4,
        max_candidates_addition int4,
        min_candidates int4,
        min_candidates_addition int4,
        election_name varchar(100) not null,
        penultimate_recount boolean not null,
        personal boolean not null,
        renumber boolean not null,
        renumber_limit boolean not null,
        settlement_first_divisor numeric(3, 0) not null,
        single_area boolean not null,
        strikeout boolean not null,
        valg_type varchar(255),
        writein boolean not null,
        writein_local_override boolean not null,
        election_group_pk int8 not null,
        election_type_pk int8 not null,
        primary key (election_pk)
    );

    create table election_day (
        election_day_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_day_date date not null,
        election_day_end_time time not null,
        election_day_start_time time not null,
        election_event_pk int8 not null,
        primary key (election_day_pk)
    );

    create table election_day_votings (
        contest_pk int8 not null,
        election_day_pk int8 not null,
        mv_area_pk int8 not null,
        voting_category_pk int8 not null,
        votings int4,
        primary key (contest_pk, election_day_pk, mv_area_pk, voting_category_pk)
    );

    create table election_event (
        election_event_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        advance_voting_start_date date,
        demo_election boolean not null,
        early_advance_voting_start_date date,
        election_end_date date,
        election_end_time time,
        electoral_roll_cut_off_date date not null,
        electoral_roll_lines_per_page int4,
        election_event_id varchar(8) not null,
        election_event_name varchar(100) not null,
        theme varchar(125),
        voter_import_dir_name varchar(226),
        voter_import_municipality boolean not null,
        voter_numbers_assigned_date date,
        voting_card_deadline date not null,
        voting_card_electoral_roll_date date not null,
        election_event_status_pk int8 not null,
        locale_pk int8 not null,
        primary key (election_event_pk)
    );

    create table election_event_report (
        election_event_report_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_event_pk int8,
        report_pk int8,
        primary key (election_event_report_pk)
    );

    create table election_event_status (
        election_event_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_event_status_id int4 not null,
        election_event_status_name varchar(50) not null,
        primary key (election_event_status_pk)
    );

    create table election_event_text (
        election_event_text_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_event_text varchar(150) not null,
        election_event_text_name varchar(50) not null,
        election_event_pk int8 not null,
        primary key (election_event_text_pk)
    );

    create table election_group (
        election_group_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        advance_vote_in_ballot_box boolean not null,
        electronic_markoffs boolean not null,
        election_group_id varchar(2) not null,
        election_group_name varchar(100) not null,
        scanning_permitted boolean not null,
        validate_polling_place_electoral_board_and_list_proposal boolean not null,
        validate_role_and_list_proposal boolean not null,
        election_event_pk int8 not null,
        primary key (election_group_pk)
    );

    create table election_level (
        election_level_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_level_id int4 not null,
        election_level_name varchar(50) not null,
        primary key (election_level_pk)
    );

    create table election_seat (
        election_seat_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        dividend int4 not null,
        divisor numeric(5, 0) not null,
        elected boolean not null,
        quotient numeric(14, 0) not null,
        same_quotient_as_next boolean not null,
        same_votes_as_next boolean not null,
        seat_number int4 not null,
        election_settlement_pk int8 not null,
        party_pk int8 not null,
        primary key (election_seat_pk)
    );

    create table election_settlement (
        election_settlement_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        settlement_number int4 not null,
        leveling_seat_settlement_pk int8 not null,
        primary key (election_settlement_pk)
    );

    create table election_text (
        election_text_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_text varchar(150) not null,
        election_text_name varchar(50) not null,
        election_pk int8 not null,
        primary key (election_text_pk)
    );

    create table election_type (
        election_type_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_type_id varchar(8) not null,
        election_type_name varchar(50) not null,
        primary key (election_type_pk)
    );

    create table election_vote_count (
        election_vote_count_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        added_votes int4 not null,
        ballots int4 not null,
        baseline_votes int4 not null,
        contest_seats int4 not null,
        early_voting_ballots int4 not null,
        early_voting_modified_ballots int4 not null,
        election_day_ballots int4 not null,
        election_day_modified_ballots int4 not null,
        eligible_for_leveling_seats boolean not null,
        modified_ballots int4 not null,
        subtracted_votes int4 not null,
        total_votes int4 not null,
        votes int4 not null,
        leveling_seat_settlement_pk int8 not null,
        party_pk int8 not null,
        primary key (election_vote_count_pk)
    );

    create table election_vote_count_category (
        election_vote_count_category_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        central_preliminary_count boolean not null,
        polling_district_count boolean not null,
        special_cover boolean not null,
        count_category_editable boolean not null,
        count_category_enabled boolean not null,
        technical_polling_district_count_configurable boolean not null,
        election_group_pk int8 not null,
        vote_count_category_pk int8 not null,
        primary key (election_vote_count_category_pk)
    );

    create table eligibility (
        mv_area_pk int8 not null,
        mv_election_pk int8 not null,
        borough_id varchar(6),
        borough_name varchar(255),
        borough_pk int8,
        contest_id varchar(8),
        contest_name varchar(255),
        country_id varchar(2),
        country_name varchar(255),
        country_pk int8,
        county_id varchar(2),
        county_name varchar(255),
        county_pk int8,
        election_end_date_of_birth date,
        election_event_id varchar(8),
        election_event_name varchar(255),
        election_event_pk int8,
        election_group_id varchar(8),
        election_group_name varchar(255),
        election_group_pk int8,
        election_id varchar(8),
        election_name varchar(255),
        election_pk int8,
        end_date_of_birth date,
        municipality_id varchar(4),
        municipality_name varchar(255),
        municipality_pk int8,
        polling_district_id varchar(4),
        polling_district_name varchar(255),
        primary key (mv_area_pk, mv_election_pk)
    );

    create table foreign_early_voting (
        voting_pk  bigserial not null,
        v_municipality_id varchar(4),
        v_municipality_name varchar(50),
        cast_timestamp timestamp,
        date_of_birth date,
        election_group_name varchar(100),
        election_group_pk int8,
        municipality_id varchar(4),
        municipality_name varchar(50),
        municipality_pk int4,
        name_line varchar(152),
        voter_id varchar(11),
        primary key (voting_pk)
    );

    create table key_domain (
        key_domain_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        key_domain_id varchar(20) not null,
        key_domain_name varchar(50) not null,
        system_wide_key boolean not null,
        primary key (key_domain_pk)
    );

    create table legacy_polling_district (
        legacy_polling_district_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        legacy_municipality_id varchar(4) not null,
        legacy_polling_district_id varchar(4) not null,
        voter_pk int8,
        primary key (legacy_polling_district_pk)
    );

    create table leveling_seat (
        leveling_seat_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        contest_leveled boolean not null,
        party_leveled boolean not null,
        rank_number int4,
        same_quotient_as_next boolean not null,
        same_votes_as_next boolean not null,
        seat_number int4,
        candidate_seat_pk int8,
        leveling_seat_quotient_pk int8 not null,
        leveling_seat_settlement_pk int8 not null,
        primary key (leveling_seat_pk)
    );

    create table leveling_seat_quotient (
        leveling_seat_quotient_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        contest_seats int4,
        contest_votes int4,
        dividend numeric(19, 2),
        divisor numeric(19, 2),
        party_seats int4,
        party_votes int4,
        quotient numeric(19, 2),
        contest_pk int8 not null,
        leveling_seat_settlement_pk int8 not null,
        party_pk int8 not null,
        primary key (leveling_seat_quotient_pk)
    );

    create table leveling_seat_settlement (
        leveling_seat_settlement_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        election_pk int8 not null,
        primary key (leveling_seat_settlement_pk)
    );

    create table leveling_seat_summary (
        leveling_seat_summary_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        contest_seats int4 not null,
        election_seats int4 not null,
        leveling_seats int4 not null,
        election_settlement_pk int8 not null,
        party_pk int8 not null,
        primary key (leveling_seat_summary_pk)
    );

    create table locale (
        locale_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        locale_id varchar(5) not null,
        locale_name varchar(50) not null,
        primary key (locale_pk)
    );

    create table locale_text (
        locale_text_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        locale_text varchar(32768) not null,
        locale_pk int8 not null,
        text_id_pk int8 not null,
        primary key (locale_text_pk)
    );

    create table manual_contest_voting (
        manual_contest_voting_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        votings int4,
        contest_pk int8 not null,
        election_day_pk int8 not null,
        mv_area_pk int8 not null,
        voting_category_pk int8 not null,
        primary key (manual_contest_voting_pk)
    );

    create table marital_status (
        marital_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        marital_status_id varchar(2) not null,
        marital_status_name varchar(50) not null,
        primary key (marital_status_pk)
    );

    create table municipality (
        municipality_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        avkrysningsmanntall_kjort boolean not null,
        election_card_text varchar(200),
        electronic_markoffs boolean not null,
        handle_election_card_centrally boolean,
        municipality_id varchar(4) not null,
        municipality_name varchar(50) not null,
        required_protocol_count boolean not null,
        technical_polling_districts_allowed boolean not null,
        county_pk int8 not null,
        locale_pk int8 not null,
        municipality_status_pk int8 not null,
        primary key (municipality_pk)
    );

    create table municipality_local_config_status (
        municipality_local_config_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        advance_polling_places boolean not null,
        count_categories boolean not null,
        election_card boolean not null,
        election_polling_places boolean not null,
        electronic_markoffs boolean not null,
        language boolean not null,
        list_proposals boolean not null,
        polling_districts boolean not null,
        polling_stations boolean not null,
        reporting_unit_stemmestyre boolean not null,
        reporting_unit_valgstyre boolean not null,
        scanning boolean not null,
        tech_polling_districts boolean not null,
        municipality_pk int8 not null,
        primary key (municipality_local_config_status_pk)
    );

    create table municipality_opening_hour (
        municipality_opening_hour_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        end_time time not null,
        start_time time not null,
        election_day_pk int8 not null,
        municipality_pk int8,
        primary key (municipality_opening_hour_pk)
    );

    create table municipality_status (
        municipality_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        municipality_status_id int4 not null,
        municipality_status_name varchar(50) not null,
        primary key (municipality_status_pk)
    );

    create table mv_area (
        mv_area_pk  bigserial not null,
        area_level int4 not null,
        area_path varchar(37) not null,
        borough_id varchar(6),
        borough_name varchar(50),
        country_id varchar(2),
        country_name varchar(50),
        county_id varchar(2),
        county_name varchar(50),
        election_event_id varchar(6) not null,
        election_event_name varchar(100) not null,
        municipality_id varchar(4),
        municipality_name varchar(50),
        parent_polling_district boolean,
        polling_district_id varchar(4),
        polling_district_name varchar(50),
        polling_place_id varchar(4),
        polling_place_name varchar(50),
        polling_station_first varchar(2),
        polling_station_last varchar(2),
        borough_pk int8,
        country_pk int8,
        county_pk int8,
        election_event_pk int8 not null,
        municipality_pk int8,
        polling_district_pk int8,
        parent_polling_district_pk int8,
        polling_place_pk int8,
        primary key (mv_area_pk)
    );

    create table mv_election (
        mv_election_pk  bigserial not null,
        area_level int4,
        contest_end_date_of_birth date,
        contest_id varchar(8),
        contest_name varchar(100),
        election_end_date_of_birth date,
        election_event_id varchar(8) not null,
        election_event_name varchar(100) not null,
        election_group_id varchar(8),
        election_group_name varchar(100),
        election_id varchar(8),
        election_level int4 not null,
        election_name varchar(100),
        election_path varchar(19) not null,
        single_area boolean,
        contest_pk int8,
        election_pk int8,
        election_event_pk int8 not null,
        election_group_pk int8,
        primary key (mv_election_pk)
    );

    create table mv_election_reporting_units (
        mv_election_reporting_units_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        mv_election_pk int8 not null,
        reporting_unit_type_pk int8 not null,
        primary key (mv_election_reporting_units_pk)
    );

    create table opening_hours (
        opening_hours_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        end_time time not null,
        start_time time not null,
        election_day_pk int8 not null,
        polling_place_pk int8,
        primary key (opening_hours_pk)
    );

    create table operator (
        operator_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        active boolean not null,
        address_line1 varchar(50),
        address_line2 varchar(50),
        address_line3 varchar(50),
        contact_info_confirmed boolean not null,
        email varchar(129),
        first_name varchar(50) not null,
        operator_id varchar(11) not null,
        info_text varchar(150),
        key_serial_number varchar(19),
        last_name varchar(50) not null,
        middle_name varchar(50),
        name_line varchar(152) not null,
        post_town varchar(50),
        postal_code varchar(4),
        telephone_number varchar(35),
        election_event_pk int8 not null,
        primary key (operator_pk)
    );

    create table operator_role (
        operator_role_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        mv_area_pk int8 not null,
        mv_election_pk int8 not null,
        operator_pk int8 not null,
        role_pk int8 not null,
        primary key (operator_role_pk)
    );

    create table party (
        party_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        approved boolean not null,
        blank boolean not null,
        forenklet_behandling boolean not null,
        party_id varchar(8) not null,
        party_name varchar(50) not null,
        short_code int4,
        election_event_pk int8 not null,
        party_category_pk int8 not null,
        primary key (party_pk)
    );

    create table party_category (
        party_category_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        party_category_id varchar(2) not null,
        party_category_name varchar(50) not null,
        primary key (party_category_pk)
    );

    create table party_contest_area (
        party_contest_area_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        borough_id varchar(6),
        county_id varchar(2),
        municipality_id varchar(4),
        party_pk int8 not null,
        primary key (party_contest_area_pk)
    );

    create table polling_district (
        polling_district_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        polling_district_id varchar(4) not null,
        municipality boolean not null,
        polling_district_name varchar(50) not null,
        parent_polling_district boolean not null,
        technical_polling_district boolean not null,
        borough_pk int8 not null,
        parent_polling_district_pk int8,
        primary key (polling_district_pk)
    );

    create table polling_place (
        polling_place_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        address_line1 varchar(50),
        address_line2 varchar(50),
        address_line3 varchar(50),
        advance_vote_in_ballot_box boolean not null,
        election_day_voting boolean not null,
        gps_coordinates varchar(255),
        polling_place_id varchar(4) not null,
        info_text varchar(150),
        polling_place_name varchar(50) not null,
        post_town varchar(50),
        postal_code varchar(4),
        public_place boolean not null,
        using_polling_stations boolean not null,
        polling_district_pk int8 not null,
        primary key (polling_place_pk)
    );

    create table polling_place_voting (
        early_voting boolean not null,
        polling_place_pk int8 not null,
        area_level int4,
        area_path varchar(255),
        borough_id varchar(6),
        borough_name varchar(255),
        borough_pk int8,
        country_id varchar(2),
        country_name varchar(255),
        country_pk int8,
        county_id varchar(2),
        county_name varchar(255),
        county_pk int8,
        election_event_pk int8,
        municipality_id varchar(4),
        municipality_name varchar(255),
        municipality_pk int8,
        polling_district_id varchar(4),
        polling_district_name varchar(255),
        polling_district_pk int8,
        polling_place_id varchar(4),
        polling_place_name varchar(255),
        primary key (early_voting, polling_place_pk)
    );

    create table polling_station (
        polling_station_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        polling_station_first varchar(2),
        polling_station_id varchar(2) not null,
        polling_station_last varchar(2),
        polling_place_pk int8 not null,
        primary key (polling_station_pk)
    );

    create table proposer (
        proposer_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        address_line1 varchar(50),
        address_line2 varchar(50),
        address_line3 varchar(50),
        approved boolean not null,
        date_of_birth date,
        display_order int4 not null,
        email varchar(129),
        first_name varchar(50) not null,
        proposer_id varchar(11) not null,
        last_name varchar(50) not null,
        middle_name varchar(50),
        name_line varchar(152) not null,
        post_town varchar(50),
        postal_code varchar(4),
        telephone_number varchar(35),
        ballot_pk int8 not null,
        proposer_role_pk int8,
        primary key (proposer_pk)
    );

    create table proposer_role (
        proposer_role_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        proposer_role_id varchar(4) not null,
        proposer_role_name varchar(50) not null,
        single boolean not null,
        primary key (proposer_role_pk)
    );

    create table report (
        report_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        category varchar(255) not null,
        report_id varchar(64) not null,
        access_pk int8,
        primary key (report_pk)
    );

    create table report_count_category (
        report_count_category_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        central_preliminary_count boolean not null,
        polling_district_count boolean not null,
        special_cover boolean not null,
        technical_polling_district_count boolean,
        election_group_pk int8 not null,
        vote_count_category_pk int8 not null,
        municipality_pk int8 not null,
        primary key (report_count_category_pk)
    );

    create table reporting_unit (
        reporting_unit_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        address_line1 varchar(50),
        address_line2 varchar(50),
        address_line3 varchar(50),
        email varchar(129),
        name_line varchar(50) not null,
        post_town varchar(50),
        postal_code varchar(4),
        telephone_number varchar(35),
        mv_area_pk int8 not null,
        mv_election_pk int8 not null,
        reporting_unit_type_pk int8 not null,
        primary key (reporting_unit_pk)
    );

    create table reporting_unit_type (
        reporting_unit_type_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        area_level int4,
        election_level int4 not null,
        reporting_unit_type_id int4 not null,
        reporting_unit_type_name varchar(50) not null,
        primary key (reporting_unit_type_pk)
    );

    create table responsibility (
        responsibility_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        responsibility_id varchar(4) not null,
        responsibility_name varchar(50) not null,
        primary key (responsibility_pk)
    );

    create table responsible_officer (
        responsible_officer_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        address_line1 varchar(50),
        address_line2 varchar(50),
        address_line3 varchar(50),
        display_order int4 not null,
        email varchar(129),
        first_name varchar(50) not null,
        last_name varchar(50) not null,
        middle_name varchar(50),
        name_line varchar(152) not null,
        post_town varchar(50),
        postal_code varchar(4),
        telephone_number varchar(35),
        reporting_unit_pk int8 not null,
        responsibility_pk int8 not null,
        primary key (responsible_officer_pk)
    );

    create table role (
        role_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        active boolean not null,
        check_candidate_conflicts boolean not null,
        election_level varchar(255),
        role_id varchar(255) not null,
        mutually_exclusive boolean not null,
        role_name varchar(50) not null,
        security_level int4 not null,
        is_user_support boolean not null,
        election_event_pk int8 not null,
        primary key (role_pk)
    );

    create table role_access (
        role_access_pk int8 not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        access_pk int8 not null,
        role_pk  bigserial not null,
        primary key (role_pk, access_pk)
    );

    create table role_area_level (
        role_area_level_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        polling_place_type varchar(255),
        area_level_pk int8 not null,
        role_pk int8 not null,
        primary key (role_area_level_pk)
    );

    create table role_include (
        role_include_pk int8 not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        included_role_pk int8 not null,
        role_pk  bigserial not null,
        primary key (role_pk, included_role_pk)
    );

    create table scanning_config (
        scanning_config_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        collaboration boolean,
        collaboration_participants varchar(150),
        collaboration_responsible varchar(50),
        responsible_email varchar(129),
        responsible_full_name varchar(152),
        responsible_telephone_number varchar(35),
        is_scanning boolean not null,
        vendor varchar(20),
        county_pk int8,
        municipality_pk int8,
        primary key (scanning_config_pk)
    );

    create table settlement (
        settlement_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        final_settlement boolean not null,
        settlement_number int4 not null,
        contest_pk int8 not null,
        primary key (settlement_pk)
    );

    create table settlement_number (
        settlement_number_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        last_settlement_number int4 not null,
        contest_pk int8 not null,
        primary key (settlement_number_pk)
    );

    create table signing_key (
        signing_key_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        key_encrypted_passphrase varchar(100),
        key_binary_data_pk int8,
        election_event_pk int8 not null,
        key_domain_pk int8 not null,
        primary key (signing_key_pk)
    );

    create table spes_reg_type (
        spes_reg_type_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        spes_reg_type_id char(1) not null,
        spes_reg_type_name varchar(50),
        primary key (spes_reg_type_pk)
    );

    create table statuskode (
        statuskode_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        statuskode_id char(1) not null,
        statuskode_name varchar(50),
        primary key (statuskode_pk)
    );

    create table text_id (
        text_id_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        info_text varchar(150),
        text_id varchar(100) not null,
        election_event_pk int8,
        primary key (text_id_pk)
    );

    create table valgnattrapport (
        valgnattrapport_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        json_content varchar(255),
        report_type varchar(30),
        status varchar(8) not null,
        contest_pk int8,
        election_pk int8,
        municipality_pk int8,
        mv_area_pk int8,
        primary key (valgnattrapport_pk)
    );

    create table vote_category (
        vote_category_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        vote_category_id varchar(12) not null,
        vote_category_name varchar(50) not null,
        primary key (vote_category_pk)
    );

    create table vote_count (
        vote_count_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        approved_ballots int4 not null,
        ballots_for_other_contests int4,
        emergency_special_covers int4,
        foreign_special_covers int4,
        vote_count_id varchar(10) not null,
        info_text varchar(150),
        manual_count boolean not null,
        modified_ballots_processed boolean not null,
        rejected_ballots int4,
        rejected_ballots_processed boolean not null,
        special_covers int4,
        technical_votings int4,
        contest_report_pk int8 not null,
        count_qualifier_pk int8 not null,
        mv_area_pk int8 not null,
        polling_district_pk int8,
        vote_count_category_pk int8 not null,
        vote_count_status_pk int8 not null,
        primary key (vote_count_pk)
    );

    create table vote_count_category (
        vote_count_category_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        early_voting boolean not null,
        vote_count_category_id varchar(4) not null,
        mandatory_central_count boolean not null,
        mandatory_count boolean not null,
        mandatory_total_count boolean not null,
        vote_count_category_name varchar(50) not null,
        primary key (vote_count_category_pk)
    );

    create table vote_count_status (
        vote_count_status_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        vote_count_status_id int4 not null,
        vote_count_status_name varchar(50) not null,
        primary key (vote_count_status_pk)
    );

    create table voter (
        voter_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        aarsakskode varchar(2),
        additional_information varchar(200),
        address_line1 varchar(50),
        address_line2 varchar(50),
        address_line3 varchar(50),
        approval_request varchar(150),
        approved boolean not null,
        borough_id varchar(6) not null,
        country_id varchar(2) not null,
        county_id varchar(2) not null,
        date_of_birth date,
        date_time_submitted timestamp not null,
        electoral_roll_line int4,
        electoral_roll_page int4,
        eligible boolean not null,
        email varchar(129),
        endringstype char(1),
        fictitious boolean not null,
        first_name varchar(50) not null,
        voter_id varchar(11) not null,
        import_batch_number int4,
        last_name varchar(50) not null,
        mailing_address_line1 varchar(50),
        mailing_address_line2 varchar(50),
        mailing_address_line3 varchar(50),
        mailing_address_specified boolean,
        mailing_country_code varchar(255),
        middle_name varchar(50),
        municipality_id varchar(4) not null,
        name_line varchar(152) not null,
        voter_number int8,
        polling_district_id varchar(4) not null,
        post_town varchar(50),
        postal_code varchar(4),
        reg_dato date,
        spes_reg_type char(1),
        statuskode char(1),
        telephone_number varchar(35),
        temporary_credentials_count int4 not null,
        temporary_credentials_timestamp timestamp,
        voting_card_returned boolean not null,
        election_event_pk int8 not null,
        mv_area_pk int8,
        polling_station_pk int8,
        temporary_credentials_polling_place_pk int8,
        primary key (voter_pk)
    );

    create table voter_audit (
        audit_oplock int4 not null,
        audit_timestamp timestamp not null,
        voter_pk int8 not null,
        aarsakskode varchar(2),
        address_line1 varchar(255),
        address_line2 varchar(255),
        address_line3 varchar(255),
        approval_request varchar(255),
        approved boolean,
        audit_operation char(1),
        audit_operator varchar(255),
        audit_user varchar(255),
        borough_id varchar(6),
        country_id varchar(2),
        county_id varchar(2),
        date_of_birth date,
        date_time_submitted timestamp,
        election_event_pk int8,
        eligible boolean,
        email varchar(129),
        endringstype char(1),
        first_name varchar(255),
        import_batch_number int4,
        last_name varchar(255),
        mailing_address_line1 varchar(255),
        mailing_address_line2 varchar(255),
        mailing_address_line3 varchar(255),
        mailing_address_specified boolean,
        mailing_country_code varchar(255),
        middle_name varchar(255),
        municipality_id varchar(4),
        name_line varchar(255),
        polling_district_id varchar(4),
        post_town varchar(255),
        postal_code varchar(4),
        reg_dato date,
        spes_reg_type char(1),
        statuskode char(1),
        telephone_number varchar(35),
        voter_id varchar(11),
        voter_number int8,
        primary key (audit_oplock, audit_timestamp, voter_pk)
    );

    create table voter_import_batch (
        voter_import_batch_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        last_import_batch_number int4 not null,
        last_import_end timestamp not null,
        last_import_records_delete int4 not null,
        last_import_records_insert int4 not null,
        last_import_records_skip int4 not null,
        last_import_records_total int4 not null,
        last_import_records_update int4 not null,
        last_import_start timestamp not null,
        election_event_pk int8 not null,
        primary key (voter_import_batch_pk)
    );

    create table voting (
        voting_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        approved boolean not null,
        ballot_box_id varchar(4),
        cast_timestamp timestamp not null,
        late_validation boolean not null,
        phase varchar(255),
        received_timestamp timestamp,
        removal_request varchar(150),
        validation_timestamp timestamp,
        voting_number int4,
        election_group_pk int8 not null,
        mv_area_pk int8 not null,
        operator_pk int8 not null,
        polling_place_pk int8,
        suggested_voting_rejection_pk int8,
        voter_pk int8 not null,
        voting_category_pk int8 not null,
        voting_rejection_pk int8,
        primary key (voting_pk)
    );

    create table voting_category (
        voting_category_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        early_voting boolean not null,
        voting_category_id varchar(4) not null,
        voting_category_name varchar(50) not null,
        primary key (voting_category_pk)
    );

    create table voting_rejection (
        voting_rejection_pk  bigserial not null,
        audit_operation varchar(1),
        audit_operator varchar(255),
        audit_oplock int4 not null,
        audit_timestamp timestamp,
        early_voting boolean not null,
        voting_rejection_id varchar(8) not null,
        voting_rejection_name varchar(50) not null,
        suggested_voting_rejection_name varchar(50) not null,
        primary key (voting_rejection_pk)
    );

    alter table aarsakskode 
        add constraint UK13bqljopepoynwid5l8iujcxe unique (aarsakskode_id);

    alter table access 
        add constraint UKslafuyylhlt5xqvtbxf23sjv3 unique (access_path);

    alter table affiliation 
        add constraint UKjulwk9dttsfu3qe3cyhnvscor unique (ballot_pk, display_order);

    alter table affiliation 
        add constraint UKb2eq4qmbtsyrplfujqlwte2l1 unique (ballot_pk, party_pk);

    alter table affiliation_vote_count 
        add constraint UKpccqxhbdkmifiv0frjl8vhwym unique (settlement_pk, affiliation_pk);

    alter table antall_stemmesedler_lagt_til_side 
        add constraint UKifmrfmb1p96guqbysrxle0ody unique (municipality_pk, election_group_pk, contest_pk);

    alter table area_level 
        add constraint UK_ni7keyf8xfr3g6mi9hy0urlau unique (area_level_id);

    alter table ballot 
        add constraint UKhhniaw5my0wiie3u4x56fv94p unique (contest_pk, ballot_id);

    alter table ballot 
        add constraint UK7xmxxfyxtnlu85ol5lhgugp79 unique (contest_pk, display_order);

    alter table ballot_count 
        add constraint UKt8ycph7ds9d1x5xo6jco80hbo unique (vote_count_pk, ballot_pk, ballot_rejection_pk);

    alter table ballot_rejection 
        add constraint UKckupk2unscan170g0gq490tnp unique (ballot_rejection_id);

    alter table ballot_status 
        add constraint UK5qdxwebvfccrg70st9s670y5x unique (ballot_status_id);

    alter table batch 
        add constraint UKdeq5wq9xpnbatbxnq23yv6amx unique (election_event_pk, batch_category, batch_number);

    alter table batch_status 
        add constraint UK_3l705us4hmcl75stobxr6gdk unique (batch_status_id);

    alter table binary_data 
        add constraint UK14awt9yegf1s5lth01lgbibhe unique (election_event_pk, binary_data_number);

    alter table borough 
        add constraint UKrq75br1g84tg421dmloevnvu5 unique (municipality_pk, borough_id);

    alter table candidate 
        add constraint UKl83oqolvfkqf20kl0ig6prybo unique (ballot_pk, display_order);

    alter table candidate 
        add constraint UKpjktl6wyqrcgu25shna0wbklw unique (ballot_pk, candidate_id);

    alter table candidate_rank 
        add constraint UKguq7abikmyg29hmpkc3goy9dq unique (settlement_pk, candidate_pk);

    alter table candidate_rank 
        add constraint UKagyyyx8vxqu3vmoncquxq09sf unique (settlement_pk, affiliation_pk, rank_number);

    alter table candidate_seat 
        add constraint UKq2oxj1kk16gyra71ac4xd6r3q unique (settlement_pk, candidate_pk);

    alter table candidate_seat 
        add constraint UKdpdywm19ej1ib5dk2uayl1tpu unique (settlement_pk, seat_number);

    alter table candidate_vote 
        add constraint UKhjiq70nwq8eld18d032ku5cyk unique (cast_vote_pk, candidate_pk, vote_category_pk);

    alter table candidate_vote_count 
        add constraint UKjay2k1ad7nv6cl9fdgotwv4sh unique (settlement_pk, candidate_pk, vote_category_pk, rank_number);

    alter table cast_vote 
        add constraint UKfn53gbvsfnk9afa92x4o4npmp unique (ballot_count_pk, cast_vote_id);

    alter table certificate_revocation_list 
        add constraint UKf9ikucnp9e8qyfhoa75wyirbb unique (issuer_dn);

    alter table configuration 
        add constraint UK_l3tm8bdn47ywx478mgukiqn7l unique (version_id);

    alter table contest 
        add constraint UK61o71b1ouwwfshi9obeynx0st unique (election_pk, contest_id);

    alter table contest_area 
        add constraint UKq69mkhgu7bavohwb9vubtbbqi unique (contest_pk, mv_area_pk);

    alter table contest_report 
        add constraint UK5m7rt2tqqmyepckq832vsjxyq unique (reporting_unit_pk, contest_pk);

    alter table contest_report_text 
        add constraint UK5vbw4f8228uycr01hemroab9y unique (contest_report_pk, contest_report_text_name);

    alter table contest_status 
        add constraint UK_dq9i47cvv67edltx1fk0nkdx9 unique (contest_status_id);

    alter table contest_text 
        add constraint UKdfkwpj2ql8516el66s7sama4s unique (contest_pk, contest_text_name);

    alter table count_qualifier 
        add constraint UKoyp3rmd9efh6oytqxqduc8qrv unique (count_qualifier_id);

    alter table country 
        add constraint UKhecgxo8f2oe0iiamsg5cfjx6o unique (election_event_pk, country_id);

    alter table county 
        add constraint UKb2kd7wet598xr6doy60j9ny7m unique (country_pk, county_id);

    alter table county_local_config_status 
        add constraint UKm4if6q032m9iqf8qa9xyq9u9m unique (county_pk);

    alter table county_status 
        add constraint UK_mh5ms4i7mcthvnu97qwyqkqup unique (county_status_id);

    alter table election 
        add constraint UKmbej39b5yrx6nw825wvs4g6q6 unique (election_group_pk, election_id);

    alter table election_day 
        add constraint UKtjp1f4fj06v3wx7udvs89iamf unique (election_event_pk, election_day_date);

    alter table election_event 
        add constraint UK8di5m5xi1359ag0b0hlstvi4 unique (election_event_id);

    alter table election_event_status 
        add constraint UKt9s6p9q2ebao9j30eal3tj61h unique (election_event_status_id);

    alter table election_event_text 
        add constraint UKqanq629rtfwvbhep5uh7lbpsb unique (election_event_pk, election_event_text_name);

    alter table election_group 
        add constraint UKapnyfvcksdxjvxkx75y6h7o1f unique (election_event_pk, election_group_id);

    alter table election_level 
        add constraint UK_g0yudiufdcpc4pg6nbx7t06hr unique (election_level_id);

    alter table election_seat 
        add constraint UKaksnmedgcbvsp3mo9vqtdna8f unique (election_settlement_pk, seat_number);

    alter table election_settlement 
        add constraint UKk8a6aikx5no165olwmh0hb5sk unique (leveling_seat_settlement_pk, settlement_number);

    alter table election_text 
        add constraint UKpqu0ox3yklccdswmbt4nnaq2k unique (election_pk, election_text_name);

    alter table election_type 
        add constraint UK23mn3h3damky3bm5xybo890ww unique (election_type_id);

    alter table election_vote_count 
        add constraint UKkxoc8pesabunyoch3pv43ytw7 unique (leveling_seat_settlement_pk, party_pk);

    alter table election_vote_count_category 
        add constraint UK393ih2tb4fgsmn54fh46cch6j unique (election_group_pk, vote_count_category_pk);

    alter table key_domain 
        add constraint UK_sh1vnwqjofl1ukudembd0orr9 unique (key_domain_id);

    alter table legacy_polling_district 
        add constraint UK6c29o3m7adh61jegokkqftjom unique (voter_pk, legacy_municipality_id, legacy_polling_district_id);

    alter table leveling_seat 
        add constraint UKn38w08o0h2efg2mhp041u6fsj unique (leveling_seat_settlement_pk, rank_number);

    alter table leveling_seat 
        add constraint UKsv39u551v52750cud2j8ycxxn unique (leveling_seat_quotient_pk);

    alter table leveling_seat_quotient 
        add constraint UKilwdj0wso0e2tbp0dp3owee0y unique (leveling_seat_settlement_pk, contest_pk, party_pk);

    alter table leveling_seat_settlement 
        add constraint UKto1iqrvo2eafrd52c2ptgp9go unique (election_pk);

    alter table leveling_seat_summary 
        add constraint UKgv4swl2j3wx37odvbqiskrxgj unique (election_settlement_pk, party_pk);

    alter table locale 
        add constraint UK2e3mk1g5lvkptoeipu83to3lu unique (locale_id);

    alter table locale_text 
        add constraint UKajp0j7ceprewegy7n9hs40ga6 unique (locale_pk, text_id_pk);

    alter table manual_contest_voting 
        add constraint UKlfbiuc2hi2vprsue8owmphyoo unique (contest_pk, mv_area_pk, voting_category_pk, election_day_pk);

    alter table marital_status 
        add constraint UKroujcex2ppwu57w62d1f5175e unique (marital_status_id);

    alter table municipality 
        add constraint UKql7k4lhpoovq4ckaa4lgawj1h unique (county_pk, municipality_id);

    alter table municipality_local_config_status 
        add constraint UK4xv956l6qiddu294kw75x06lx unique (municipality_pk);

    alter table municipality_opening_hour 
        add constraint UKfxxxabouo67v5sbtr1w82sruf unique (municipality_pk, election_day_pk, start_time);

    alter table municipality_status 
        add constraint UK_neskirye31i573fhyyx658h3e unique (municipality_status_id);

    alter table mv_area 
        add constraint UKausg71clflxjqnr8ctjvkd8fa unique (election_event_pk, country_pk, county_pk, municipality_pk, borough_pk, polling_district_pk, polling_place_pk);

    alter table mv_election 
        add constraint UKfug78ih41ws668jjmgpxu6urb unique (election_event_pk, election_group_pk, election_pk, contest_pk);

    alter table mv_election_reporting_units 
        add constraint UK3kd3u3doj5poily8hf0d1rnrk unique (mv_election_pk, reporting_unit_type_pk);

    alter table opening_hours 
        add constraint UKb787mg1e6pphju1jhh2j0umg0 unique (polling_place_pk, election_day_pk, start_time);

    alter table operator 
        add constraint UK6ptcvn7bfcjg1eot1uy22g5m2 unique (election_event_pk, operator_id);

    alter table operator_role 
        add constraint UK54vv6hlf3jfjpi3aqc7cj9np unique (operator_pk, role_pk, mv_election_pk, mv_area_pk);

    alter table party 
        add constraint UK5dh0ik2d40uuei9rnl1h16ftx unique (election_event_pk, short_code);

    alter table party 
        add constraint UKjko0nkjo4rtbf4t6970hw9g17 unique (election_event_pk, party_id);

    alter table party_category 
        add constraint UKt9fedcclt480waakeny76hyhv unique (party_category_id);

    alter table polling_district 
        add constraint UK6l0hhpuwg8mpaqy31yv9o4iog unique (borough_pk, polling_district_id);

    alter table polling_district 
        add constraint UKkgnr2kqgrfltemj2c13sg3a9w unique (borough_pk, municipality);

    alter table polling_place 
        add constraint UK73ur6noxcgbj24t0vt9toebrm unique (polling_district_pk, polling_place_id);

    alter table polling_place 
        add constraint UKr6o2md50hwpj9uw7ar7nx3o2a unique (polling_district_pk, election_day_voting);

    alter table polling_station 
        add constraint UKfd402c4b2ajai5mi04uuep4ie unique (polling_station_pk, polling_station_id);

    alter table proposer 
        add constraint UKrqgqediejaxoa6g63ctoxxiph unique (ballot_pk, proposer_id);

    alter table proposer 
        add constraint UKmr8q82qkm1y1ljjqjm47ngjrr unique (ballot_pk, display_order);

    alter table proposer_role 
        add constraint UKepymcw9d7veh4xdrr4cyq9gmy unique (proposer_role_id);

    alter table report_count_category 
        add constraint UKivwh7dlrt4lurid8qh9x5aqut unique (election_group_pk, municipality_pk, vote_count_category_pk);

    alter table reporting_unit 
        add constraint UKpn73264kto870w7hnmxocqr4j unique (mv_election_pk, mv_area_pk);

    alter table reporting_unit_type 
        add constraint UK_2clkj90sf0j2n5whxve0khov2 unique (reporting_unit_type_id);

    alter table responsibility 
        add constraint UK7o770cepw77xcodga4eeuqpd6 unique (responsibility_id);

    alter table role 
        add constraint UKrrbeobpwdmngvwi58t91sbuo unique (election_event_pk, role_id);

    alter table role_area_level 
        add constraint UK1gobrnjih52qu5jxar3n6kvtv unique (role_pk, area_level_pk);

    alter table settlement 
        add constraint UKpppwy6myt7r7l62bfqsjup5nv unique (contest_pk, final_settlement);

    alter table settlement 
        add constraint UKn3xk7iqvustuttcfurxj4iaue unique (contest_pk, settlement_number);

    alter table settlement_number 
        add constraint UK_f0mwiadtm7ai1htp6qvf7qcws unique (contest_pk);

    alter table signing_key 
        add constraint UKrqjdo4icx4ohwxur918h88wsf unique (election_event_pk, key_domain_pk);

    alter table spes_reg_type 
        add constraint UKhsb6r9746gd9qvdt90d0183m3 unique (spes_reg_type_id);

    alter table statuskode 
        add constraint UKt3vb97xj9rj8nl1g9hia318kx unique (statuskode_id);

    alter table text_id 
        add constraint UKd51pa1xp6f1fbyoh76iuwuoic unique (election_event_pk, text_id);

    alter table vote_category 
        add constraint UKbawunnpkun27140ayksgvv4fa unique (vote_category_id);

    alter table vote_count 
        add constraint UKl1kgad34fmpglnlc3e36mqes5 unique (contest_report_pk, polling_district_pk, vote_count_category_pk, count_qualifier_pk, vote_count_id);

    alter table vote_count_category 
        add constraint UK7fhm60dua6i2jskvh772qq075 unique (vote_count_category_id);

    alter table vote_count_status 
        add constraint UK_bbuhx4psu6pw3k1wf08d91ybp unique (vote_count_status_id);

    alter table voter 
        add constraint UKaxkueijctsglufer88ue8wa7o unique (voter_number);

    alter table voter 
        add constraint UKnunrg4965ilq8polmgpyi3dc3 unique (election_event_pk, voter_id);

    alter table voter_import_batch 
        add constraint UKqebra3kxti0bbtxtxl8dv3noj unique (election_event_pk);

    alter table voting 
        add constraint UK8dmdv5n2lrq8sgmwuvuyqqrmn unique (voter_pk, election_group_pk, approved);

    alter table voting 
        add constraint UKk924toro1ffyxo1vwda9g06rt unique (voter_pk, election_group_pk, voting_category_pk, voting_number);

    alter table voting_category 
        add constraint UKkvgfw97g20gs07j0aweqf39de unique (voting_category_id);

    alter table voting_rejection 
        add constraint UK3j64hy1yeh8546v6e8tk2krvh unique (voting_rejection_id);

    alter table admin.election_event_locale 
        add constraint FKl4yhtfxh3822kx6ae05hy3no2 
        foreign key (election_event_pk) 
        references election_event;

    alter table admin.election_event_locale 
        add constraint FKluolmmdr2ofcrpt9o6cq8gbv 
        foreign key (locale_pk) 
        references locale;

    alter table affiliation 
        add constraint FKlurk8fwyh6qdyjywiad5vf23s 
        foreign key (ballot_pk) 
        references ballot;

    alter table affiliation 
        add constraint FKi5us2jd9eo1fk3xwqj0ka7gt2 
        foreign key (party_pk) 
        references party;

    alter table affiliation_vote_count 
        add constraint FK26l1kq62n49rlhj2l52mccebr 
        foreign key (affiliation_pk) 
        references affiliation;

    alter table affiliation_vote_count 
        add constraint FK2gbo7korgr901aqelovgwuau2 
        foreign key (settlement_pk) 
        references settlement;

    alter table antall_stemmesedler_lagt_til_side 
        add constraint FKcx8uamgdf67vy84nhrwd2rm72 
        foreign key (contest_pk) 
        references contest;

    alter table antall_stemmesedler_lagt_til_side 
        add constraint FKe8hspq3bpflrapphcrt67ya3v 
        foreign key (election_group_pk) 
        references election_group;

    alter table antall_stemmesedler_lagt_til_side 
        add constraint FKkqbtnmr9a1wd10pj5orcwn7aa 
        foreign key (municipality_pk) 
        references municipality;

    alter table ballot 
        add constraint FK9oo0rjnnx7r2wjw06apu225hw 
        foreign key (ballot_status_pk) 
        references ballot_status;

    alter table ballot 
        add constraint FKi709296o2p88xnbn4bbhcd1tx 
        foreign key (contest_pk) 
        references contest;

    alter table ballot 
        add constraint FK6k4crhr1mw5pk9kaqxherbjdy 
        foreign key (locale_pk) 
        references locale;

    alter table ballot_count 
        add constraint FKa2jdyfv9yqy8bdgx2k6cwo4at 
        foreign key (ballot_pk) 
        references ballot;

    alter table ballot_count 
        add constraint FKbrauasmduiiru42gjn5k50mt9 
        foreign key (ballot_rejection_pk) 
        references ballot_rejection;

    alter table ballot_count 
        add constraint FKatx2dmnwr2sq82egrycl3vajs 
        foreign key (vote_count_pk) 
        references vote_count;

    alter table batch 
        add constraint FKixkc3mhtmabjix28nu7o2umq0 
        foreign key (batch_status_pk) 
        references batch_status;

    alter table batch 
        add constraint FKmsrp8evug2g0vunrx14k1r53 
        foreign key (batch_binary_data_pk) 
        references binary_data;

    alter table batch 
        add constraint FKlm29687k5l4ok2ii230lj21wq 
        foreign key (election_event_pk) 
        references election_event;

    alter table batch 
        add constraint FKjm4roch5s2cww5ydlochndkhu 
        foreign key (operator_role_pk) 
        references operator_role;

    alter table binary_data 
        add constraint FK6efnlb9cqn2h7e14xk4ey4a9s 
        foreign key (election_event_pk) 
        references election_event;

    alter table borough 
        add constraint FKbughggu21u45e947vmnve41xc 
        foreign key (municipality_pk) 
        references municipality;

    alter table candidate 
        add constraint FKrf145gd2jsddj11q94bh62gab 
        foreign key (affiliation_pk) 
        references affiliation;

    alter table candidate 
        add constraint FK8pu6s5j1qy15uilkfhrtaqip9 
        foreign key (ballot_pk) 
        references ballot;

    alter table candidate 
        add constraint FK25u0ta0hm8s7m86s590ve9nc1 
        foreign key (marital_status_pk) 
        references marital_status;

    alter table candidate_rank 
        add constraint FKh1oy7t178j5qeuqorkvpqvikf 
        foreign key (affiliation_pk) 
        references affiliation;

    alter table candidate_rank 
        add constraint FK4m0h6jceeahth2h8iad79qbmm 
        foreign key (candidate_pk) 
        references candidate;

    alter table candidate_rank 
        add constraint FKo2u71r1w7o9getwkh3igndxui 
        foreign key (settlement_pk) 
        references settlement;

    alter table candidate_seat 
        add constraint FK1xp61i837sexd88r9sa5fl6mn 
        foreign key (affiliation_pk) 
        references affiliation;

    alter table candidate_seat 
        add constraint FK9wotd4m74oaiw37tsvu4fvwcw 
        foreign key (candidate_pk) 
        references candidate;

    alter table candidate_seat 
        add constraint FK5t61vaw6gaird66ao97a0bh37 
        foreign key (settlement_pk) 
        references settlement;

    alter table candidate_vote 
        add constraint FKiyeh6k3ap979d1eb6rpo9rrkw 
        foreign key (candidate_pk) 
        references candidate;

    alter table candidate_vote 
        add constraint FKhs21u7hn4137ymmj41dxru563 
        foreign key (cast_vote_pk) 
        references cast_vote;

    alter table candidate_vote 
        add constraint FK498ewuj2cjrss4uxkdmxgujuj 
        foreign key (vote_category_pk) 
        references vote_category;

    alter table candidate_vote_count 
        add constraint FKei15j5mh0oxk8e4b3p7cvb68q 
        foreign key (affiliation_pk) 
        references affiliation;

    alter table candidate_vote_count 
        add constraint FKl8bwbjeqepog42bh3pjltcm03 
        foreign key (candidate_pk) 
        references candidate;

    alter table candidate_vote_count 
        add constraint FKpkec9j33xaie85fg04pe1n9dt 
        foreign key (settlement_pk) 
        references settlement;

    alter table candidate_vote_count 
        add constraint FKl37ppwmf7o9h4fy73xpj26op3 
        foreign key (vote_category_pk) 
        references vote_category;

    alter table cast_vote 
        add constraint FK2ldi0swxacnwo2aax1jp2v6bk 
        foreign key (ballot_count_pk) 
        references ballot_count;

    alter table cast_vote 
        add constraint FK1ob2mtjpvc3va95faksedtx4v 
        foreign key (scan_binary_data_pk) 
        references binary_data;

    alter table cast_vote_batch 
        add constraint FK51ni69b3qv03u879apwmierof 
        foreign key (ballot_count_pk) 
        references ballot_count;

    alter table cast_vote_batch 
        add constraint FK23im1f14ri5ubhb0tdladssr3 
        foreign key (operator_pk) 
        references operator;

    alter table cast_vote_batch_member 
        add constraint FKpyhpha0e8xwylcc4dgsjp3ku7 
        foreign key (cast_vote_pk) 
        references cast_vote;

    alter table cast_vote_batch_member 
        add constraint FKr79w21a2m9d4622e0mfx2oc3m 
        foreign key (cast_vote_batch_pk) 
        references cast_vote_batch;

    alter table contest 
        add constraint FKgu4eibogmbjrr8tqyd5vn6s5j 
        foreign key (contest_status_pk) 
        references contest_status;

    alter table contest 
        add constraint FKt0mosqa1f1m6468cjpi0rm1gx 
        foreign key (election_pk) 
        references election;

    alter table contest_area 
        add constraint FKmvh05avqdiir1twlnj2kppu6k 
        foreign key (contest_pk) 
        references contest;

    alter table contest_area 
        add constraint FKt9yu6j7wyqg097i9g9ax9871n 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table contest_report 
        add constraint FKjnxwt6cj2aaiywijjh2r2gx3k 
        foreign key (contest_pk) 
        references contest;

    alter table contest_report 
        add constraint FKpa727hy7m1a69ri8g0b4etcbc 
        foreign key (reporting_unit_pk) 
        references reporting_unit;

    alter table contest_report_text 
        add constraint FKg3jfw1ndoknsboc85h5bcexp0 
        foreign key (contest_report_pk) 
        references contest_report;

    alter table contest_text 
        add constraint FKjbngq1j4jjdumjf051vsdvkyh 
        foreign key (contest_pk) 
        references contest;

    alter table country 
        add constraint FKf76mvfl5id91jl52fsn19d25b 
        foreign key (election_event_pk) 
        references election_event;

    alter table county 
        add constraint FK16q5w4a8rdl7lyxw2bmxniqfv 
        foreign key (country_pk) 
        references country;

    alter table county 
        add constraint FKnc3m48votigoc8oxntn76ftvi 
        foreign key (county_status_pk) 
        references county_status;

    alter table county 
        add constraint FKdidwbdjndlyjuy16tk79fjj38 
        foreign key (locale_pk) 
        references locale;

    alter table county_local_config_status 
        add constraint FKsor5p1j9wo9wcf2tnjt2duis1 
        foreign key (county_pk) 
        references county;

    alter table election 
        add constraint FKfwrt2ovjdgtnl7pf5s8j8bvqe 
        foreign key (election_group_pk) 
        references election_group;

    alter table election 
        add constraint FKcjm5gxfcv8kqqbbcw53ef39a2 
        foreign key (election_type_pk) 
        references election_type;

    alter table election_day 
        add constraint FKa4j7xy0ifr3w2ogh4aws74sp 
        foreign key (election_event_pk) 
        references election_event;

    alter table election_day_votings 
        add constraint FKqetsltxtjpaf6th2txa3tu9gb 
        foreign key (contest_pk) 
        references contest;

    alter table election_day_votings 
        add constraint FK2123ucljuppuh5galrighl7kj 
        foreign key (election_day_pk) 
        references election_day;

    alter table election_day_votings 
        add constraint FKnnqxn0niv1uv0gr6hq0lwtt2y 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table election_day_votings 
        add constraint FKkt0qjotynh0psvyugrh30aymb 
        foreign key (voting_category_pk) 
        references voting_category;

    alter table election_event 
        add constraint FKievivk6jd0c92u4rks00d9roi 
        foreign key (election_event_status_pk) 
        references election_event_status;

    alter table election_event 
        add constraint FKlro6wrndafageckuc8g4sj0dr 
        foreign key (locale_pk) 
        references locale;

    alter table election_event_report 
        add constraint FKrnxs21td7i77uhnqubtnflv3y 
        foreign key (election_event_pk) 
        references election_event;

    alter table election_event_report 
        add constraint FK19q556kc4gkco0ofbq0lw8fhf 
        foreign key (report_pk) 
        references report;

    alter table election_event_text 
        add constraint FK6wceywfqeqnixequ0lk0bhfph 
        foreign key (election_event_pk) 
        references election_event;

    alter table election_group 
        add constraint FKgde41qkbw9dv8ug540t60io79 
        foreign key (election_event_pk) 
        references election_event;

    alter table election_seat 
        add constraint FKqoa66lg692x0ijofty8aammf5 
        foreign key (election_settlement_pk) 
        references election_settlement;

    alter table election_seat 
        add constraint FK33g6ks4ummn839b4mwtif94sf 
        foreign key (party_pk) 
        references party;

    alter table election_settlement 
        add constraint FKm1l4oqxant53x93c0kujq6odt 
        foreign key (leveling_seat_settlement_pk) 
        references leveling_seat_settlement;

    alter table election_text 
        add constraint FK29aecf13yi5bcbdm897464i2h 
        foreign key (election_pk) 
        references election;

    alter table election_vote_count 
        add constraint FK59vbhh7n3jbk376pa5h1slh0b 
        foreign key (leveling_seat_settlement_pk) 
        references leveling_seat_settlement;

    alter table election_vote_count 
        add constraint FKpars0csquirjg0e37wgvdnnui 
        foreign key (party_pk) 
        references party;

    alter table election_vote_count_category 
        add constraint FKb2j9hx4kh5l4ybooh5p353osq 
        foreign key (election_group_pk) 
        references election_group;

    alter table election_vote_count_category 
        add constraint FK757stf50a529auxrtc79ryvxh 
        foreign key (vote_count_category_pk) 
        references vote_count_category;

    alter table legacy_polling_district 
        add constraint FKhetynlxu7gqupifu7k0xdguqw 
        foreign key (voter_pk) 
        references voter;

    alter table leveling_seat 
        add constraint FK4n2etiwiux6q1vsklh8u1ordg 
        foreign key (candidate_seat_pk) 
        references candidate_seat;

    alter table leveling_seat 
        add constraint FKtkp1f7wnc2128nkoovqm1hfwn 
        foreign key (leveling_seat_quotient_pk) 
        references leveling_seat_quotient;

    alter table leveling_seat 
        add constraint FKfjjps59luu1omgaqvjgn7goyo 
        foreign key (leveling_seat_settlement_pk) 
        references leveling_seat_settlement;

    alter table leveling_seat_quotient 
        add constraint FK4pm14r3x6lgvx629cgfa56xki 
        foreign key (contest_pk) 
        references contest;

    alter table leveling_seat_quotient 
        add constraint FKsmdk8oyk00g8l2r02gbwe57r0 
        foreign key (leveling_seat_settlement_pk) 
        references leveling_seat_settlement;

    alter table leveling_seat_quotient 
        add constraint FKg5lndmvjv0avbsyf3smcea9xg 
        foreign key (party_pk) 
        references party;

    alter table leveling_seat_settlement 
        add constraint FKcubyco2bd4uawqr0ah5b31j8u 
        foreign key (election_pk) 
        references election;

    alter table leveling_seat_summary 
        add constraint FKgr36km0wh58ektlqch90c4377 
        foreign key (election_settlement_pk) 
        references election_settlement;

    alter table leveling_seat_summary 
        add constraint FKrrusmeybap79x2p5au6ptsbpd 
        foreign key (party_pk) 
        references party;

    alter table locale_text 
        add constraint FKid3pb0e7e8l98ac54es6i9pl7 
        foreign key (locale_pk) 
        references locale;

    alter table locale_text 
        add constraint FKsr63h15eatgxlb1mciqeu7oxe 
        foreign key (text_id_pk) 
        references text_id;

    alter table manual_contest_voting 
        add constraint FKdema7cdy92q5hjxxs8xs8jnv5 
        foreign key (contest_pk) 
        references contest;

    alter table manual_contest_voting 
        add constraint FKjv1f2u1yrov2nx5256apw2p0l 
        foreign key (election_day_pk) 
        references election_day;

    alter table manual_contest_voting 
        add constraint FK2hu9ib6vo8n4x8sbk2nur9ajy 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table manual_contest_voting 
        add constraint FK1ytubbnk5vdbwyin42mlt45y5 
        foreign key (voting_category_pk) 
        references voting_category;

    alter table municipality 
        add constraint FKbgau7lg7i6njrea7jjixw48nq 
        foreign key (county_pk) 
        references county;

    alter table municipality 
        add constraint FKbnxb99i2eodnmvxwx455pqyk1 
        foreign key (locale_pk) 
        references locale;

    alter table municipality 
        add constraint FKk6c654j99nrcke9fhytnrv6p3 
        foreign key (municipality_status_pk) 
        references municipality_status;

    alter table municipality_local_config_status 
        add constraint FKhewymwskyp9vp90ky3aeq92br 
        foreign key (municipality_pk) 
        references municipality;

    alter table municipality_opening_hour 
        add constraint FKm52eyebjt29enjd0bqvc7eqvl 
        foreign key (election_day_pk) 
        references election_day;

    alter table municipality_opening_hour 
        add constraint FKrsmukicuscg16nj1kw31rvila 
        foreign key (municipality_pk) 
        references municipality;

    alter table mv_area 
        add constraint FKsrmy0ws5f7yr2scsxqbjkv78y 
        foreign key (borough_pk) 
        references borough;

    alter table mv_area 
        add constraint FKk0dg1h778ws8xt0ct5ipc0hpn 
        foreign key (country_pk) 
        references country;

    alter table mv_area 
        add constraint FKhu87ud4mf6lnbwyfs28vlsbq 
        foreign key (county_pk) 
        references county;

    alter table mv_area 
        add constraint FKkdrpem6dxojfu2qmrwqf7ogcc 
        foreign key (election_event_pk) 
        references election_event;

    alter table mv_area 
        add constraint FK23lm8wkg7069hk3xnawygm7kp 
        foreign key (municipality_pk) 
        references municipality;

    alter table mv_area 
        add constraint FKcl6ebkdf4jkymuk6e9ukefqdl 
        foreign key (polling_district_pk) 
        references polling_district;

    alter table mv_area 
        add constraint FKreaam4gxjxihweoxvpkaounfr 
        foreign key (parent_polling_district_pk) 
        references polling_district;

    alter table mv_area 
        add constraint FKsiqtntpyyr0gb2yncoqmh37g 
        foreign key (polling_place_pk) 
        references polling_place;

    alter table mv_election 
        add constraint FK325kg4sulxdarl9vxm38ibstq 
        foreign key (contest_pk) 
        references contest;

    alter table mv_election 
        add constraint FKmgbf10lefudo93h9dg0s4pkna 
        foreign key (election_pk) 
        references election;

    alter table mv_election 
        add constraint FKhfwyhv899rb6fo4ce1f3pwn0r 
        foreign key (election_event_pk) 
        references election_event;

    alter table mv_election 
        add constraint FK12mdrlph1g9tlihavwn78xj7h 
        foreign key (election_group_pk) 
        references election_group;

    alter table mv_election_reporting_units 
        add constraint FKqfj3nmci7k68ke2p4fbmelb2s 
        foreign key (mv_election_pk) 
        references mv_election;

    alter table mv_election_reporting_units 
        add constraint FKlqhfa9b77el1uavtbwl2jf7yc 
        foreign key (reporting_unit_type_pk) 
        references reporting_unit_type;

    alter table opening_hours 
        add constraint FKftoqjxdcagpu52yintyx64tv0 
        foreign key (election_day_pk) 
        references election_day;

    alter table opening_hours 
        add constraint FKjsu68md3thgorb06ajvhamk55 
        foreign key (polling_place_pk) 
        references polling_place;

    alter table operator 
        add constraint FK6n5693dbbh6tpi4r6scbk7bb1 
        foreign key (election_event_pk) 
        references election_event;

    alter table operator_role 
        add constraint FKptaud3q9s523u8p8xvgxtam8e 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table operator_role 
        add constraint FK947xctlt23xulb5w7c8eif46b 
        foreign key (mv_election_pk) 
        references mv_election;

    alter table operator_role 
        add constraint FKhac13g1ieh010ecw1yl26x5wn 
        foreign key (operator_pk) 
        references operator;

    alter table operator_role 
        add constraint FKhpoledbpuy265nq9rymf6f01e 
        foreign key (role_pk) 
        references role;

    alter table party 
        add constraint FKas4xg0p032f2vjrptwytrnugn 
        foreign key (election_event_pk) 
        references election_event;

    alter table party 
        add constraint FKj02223jrrvyk7o65kvhyk38g1 
        foreign key (party_category_pk) 
        references party_category;

    alter table party_contest_area 
        add constraint FKi173twvo9ksp8fdxj56fufbh2 
        foreign key (party_pk) 
        references party;

    alter table polling_district 
        add constraint FKfgx6dm03d7uvf6168qu223a8j 
        foreign key (borough_pk) 
        references borough;

    alter table polling_district 
        add constraint FK859itishxjq5lragywr0d3qhi 
        foreign key (parent_polling_district_pk) 
        references polling_district;

    alter table polling_place 
        add constraint FKrfx3qqijn1quqxyil736fea32 
        foreign key (polling_district_pk) 
        references polling_district;

    alter table polling_station 
        add constraint FKhcih4dyiko9veu98wtlxm1cfl 
        foreign key (polling_place_pk) 
        references polling_place;

    alter table proposer 
        add constraint FKd6ns17xhlhtht855rjf0t8s9a 
        foreign key (ballot_pk) 
        references ballot;

    alter table proposer 
        add constraint FK4nm3bhoxep3t1hdnsdpueoqfa 
        foreign key (proposer_role_pk) 
        references proposer_role;

    alter table report 
        add constraint FK9k62bqcxbrl0wwwpbk5x4xka9 
        foreign key (access_pk) 
        references access;

    alter table report_count_category 
        add constraint FKax5y35i9ju7gxweb39gcfji79 
        foreign key (election_group_pk) 
        references election_group;

    alter table report_count_category 
        add constraint FKipmmioisa7gdv9guq243njbhm 
        foreign key (vote_count_category_pk) 
        references vote_count_category;

    alter table report_count_category 
        add constraint FK2rck3xq4vwbiqar4nxo2cjkt4 
        foreign key (municipality_pk) 
        references municipality;

    alter table reporting_unit 
        add constraint FKi9n06l3xoac89tmd9gpfmuj90 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table reporting_unit 
        add constraint FKaue1tj06965ot98jrlbk3vlbp 
        foreign key (mv_election_pk) 
        references mv_election;

    alter table reporting_unit 
        add constraint FKgt9usct72fnqqp1pfm1dl6i96 
        foreign key (reporting_unit_type_pk) 
        references reporting_unit_type;

    alter table responsible_officer 
        add constraint FKrrrqeg8quuurod7gg7sdxyo0 
        foreign key (reporting_unit_pk) 
        references reporting_unit;

    alter table responsible_officer 
        add constraint FK4fbklbi2pmudnqrosmufqhi15 
        foreign key (responsibility_pk) 
        references responsibility;

    alter table role 
        add constraint FK9ojuyptgnqm8kihs7lxfav8p 
        foreign key (election_event_pk) 
        references election_event;

    alter table role_access 
        add constraint FKpdkiy1slpd9rdej8nlj63ev17 
        foreign key (access_pk) 
        references access;

    alter table role_access 
        add constraint FKij2et5tjljxtvw0n6am7cl8oj 
        foreign key (role_pk) 
        references role;

    alter table role_area_level 
        add constraint FK6l6sb27t6bcce0carqi6sebmp 
        foreign key (area_level_pk) 
        references area_level;

    alter table role_area_level 
        add constraint FKjwd02pkgy40h51js9uh6bwy00 
        foreign key (role_pk) 
        references role;

    alter table role_include 
        add constraint FKl02l6x89y9643joa11qwfi0p1 
        foreign key (included_role_pk) 
        references role;

    alter table role_include 
        add constraint FKptnl5v5l2d2oky14rtia9yf8 
        foreign key (role_pk) 
        references role;

    alter table scanning_config 
        add constraint FKdxu9a3gbk5xs0n82u9v4yydmp 
        foreign key (county_pk) 
        references county;

    alter table scanning_config 
        add constraint FKd6loi3xlo73e690jml13rboc9 
        foreign key (municipality_pk) 
        references municipality;

    alter table settlement 
        add constraint FK6ykpur9r5ffol25m0vy9kya9x 
        foreign key (contest_pk) 
        references contest;

    alter table settlement_number 
        add constraint FKlq3670dm7nqp8b6u2ew1bn2rr 
        foreign key (contest_pk) 
        references contest;

    alter table signing_key 
        add constraint FKb6x3jjghfgif4g7khtqlouawe 
        foreign key (key_binary_data_pk) 
        references binary_data;

    alter table signing_key 
        add constraint FKrg57u3l1qu6v1q9nvslrv3wjp 
        foreign key (election_event_pk) 
        references election_event;

    alter table signing_key 
        add constraint FKt973pi6dqmdhh4sxkw4uvnfmv 
        foreign key (key_domain_pk) 
        references key_domain;

    alter table text_id 
        add constraint FKdljklhiv450iffqc6lcxaua2 
        foreign key (election_event_pk) 
        references election_event;

    alter table valgnattrapport 
        add constraint FK5yjui4k4aw50x2pv424rxcqob 
        foreign key (contest_pk) 
        references contest;

    alter table valgnattrapport 
        add constraint FKng29mbcy1r52427uircphl02m 
        foreign key (election_pk) 
        references election;

    alter table valgnattrapport 
        add constraint FKfuggj4yo3l21p2kh7p2bifqc1 
        foreign key (municipality_pk) 
        references municipality;

    alter table valgnattrapport 
        add constraint FKlpmnclj54j6cw89ajip9wk2f7 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table vote_count 
        add constraint FK35t93kvpshh1bpdjaxoakuo5y 
        foreign key (contest_report_pk) 
        references contest_report;

    alter table vote_count 
        add constraint FKdj2006et2bkyqym1e3bhoa2wo 
        foreign key (count_qualifier_pk) 
        references count_qualifier;

    alter table vote_count 
        add constraint FKci2exk9oicdsky6ew858uwbxp 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table vote_count 
        add constraint FKbch4mvqlwffk3f7gr35ky91qe 
        foreign key (polling_district_pk) 
        references polling_district;

    alter table vote_count 
        add constraint FK36vbnbe51jml4cukqlqubrawf 
        foreign key (vote_count_category_pk) 
        references vote_count_category;

    alter table vote_count 
        add constraint FKqpgst7jaa6h2a3jdsn6yp7jx3 
        foreign key (vote_count_status_pk) 
        references vote_count_status;

    alter table voter 
        add constraint FKtjfc7soexq4qpq1y3u8wmq34c 
        foreign key (election_event_pk) 
        references election_event;

    alter table voter 
        add constraint FKsc5omoc6spmaowlnvdwxguh6q 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table voter 
        add constraint FKeomnh2nlw1ifl6w44jsajtj4j 
        foreign key (polling_station_pk) 
        references polling_station;

    alter table voter 
        add constraint FKbyx3o231i7va3sq0oh590u0mc 
        foreign key (temporary_credentials_polling_place_pk) 
        references polling_place;

    alter table voter_import_batch 
        add constraint FKtqr0v9okun0d19cjyei6qi81t 
        foreign key (election_event_pk) 
        references election_event;

    alter table voting 
        add constraint FK8c1xpaibelhjg1tlu3sdcw5qb 
        foreign key (election_group_pk) 
        references election_group;

    alter table voting 
        add constraint FKtk0s5v1cdwh4xccatarxddhn3 
        foreign key (mv_area_pk) 
        references mv_area;

    alter table voting 
        add constraint FK62wa194rh26i0ps96tla2yhtk 
        foreign key (operator_pk) 
        references operator;

    alter table voting 
        add constraint FK7q382rjp2p9g72slbbbednv4e 
        foreign key (polling_place_pk) 
        references polling_place;

    alter table voting 
        add constraint FKpu3ohi4shb6nikyfv1xjarb8n 
        foreign key (suggested_voting_rejection_pk) 
        references voting_rejection;

    alter table voting 
        add constraint FKlinaxf90514vdkrds00orj2b8 
        foreign key (voter_pk) 
        references voter;

    alter table voting 
        add constraint FKb35fb909ob06rurdn4ntvtl0w 
        foreign key (voting_category_pk) 
        references voting_category;

    alter table voting 
        add constraint FKc528l93oi66a92wam5yi9idyq 
        foreign key (voting_rejection_pk) 
        references voting_rejection;
