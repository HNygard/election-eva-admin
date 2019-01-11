package no.valg.eva.admin.voting.repository;

import static org.testng.AssertJUnit.assertEquals;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

/**
 * Test cases for VotingStatisticsSql
 */

public class VotingStatisticsSqlTest {

	@Test
	public void testSqlWithNoVotingCategoryParameters() {

		String expectedSql = "select vc.voting_category_id,(cv.validation_timestamp is not null) proevet, count(*) antall, "
				+ "cv.late_validation from voting cv join mv_area a on (a.polling_place_pk = cv.polling_place_pk and a.area_level = 6) "
				+ "join voting_category vc on (vc.voting_category_pk = cv.voting_category_pk) "
				+ "where a.municipality_pk = :municipality_pk and (:polling_place_pk = 0 or a.polling_place_pk = :polling_place_pk) "
				+ "and (:voting_number_start = 0 or cv.voting_number between :voting_number_start and :voting_number_end) "
				+ "and cv.election_group_pk = :election_group_pk group by vc.voting_category_id, proevet, cv.late_validation order by vc.voting_category_id, proevet;";

		assertEquals(expectedSql, new VotingStatisticsSql(new String[] {}, null, null, true, false).getSql());
	}

	@Test
	public void testSqlWithNoVotingCategoryParametersAndLateValidatedAdvanceVotes() {

		String expectedSql = "select vc.voting_category_id,(cv.validation_timestamp is not null) proevet, count(*) antall, "
				+ "cv.late_validation from voting cv join mv_area a on (a.polling_place_pk = cv.polling_place_pk and a.area_level = 6) "
				+ "join voting_category vc on (vc.voting_category_pk = cv.voting_category_pk) and (vc.voting_category_id in ('FB', 'FE', 'FI', 'FU') "
				+ "and cv.late_validation is true) "
				+ "where a.municipality_pk = :municipality_pk and (:polling_place_pk = 0 or a.polling_place_pk = :polling_place_pk) "
				+ "and (:voting_number_start = 0 or cv.voting_number between :voting_number_start and :voting_number_end) "
				+ "and cv.election_group_pk = :election_group_pk group by vc.voting_category_id, proevet, cv.late_validation order by vc.voting_category_id, proevet;";

		assertEquals(expectedSql, new VotingStatisticsSql(new String[] {}, null, null, false, true).getSql());
	}

	@Test
	public void testSqlWithTwoVotingCategoryParameters() {

		String expectedSql = "select vc.voting_category_id,(cv.validation_timestamp is not null) proevet, count(*) antall, "
				+ "cv.late_validation from voting cv join mv_area a on (a.polling_place_pk = cv.polling_place_pk and a.area_level = 6) "
				+ "join voting_category vc on (vc.voting_category_pk = cv.voting_category_pk and (vc.voting_category_id in "
				+ "(:voting_category_id_0,:voting_category_id_1) )) "
				+ "where a.municipality_pk = :municipality_pk and (:polling_place_pk = 0 or a.polling_place_pk = :polling_place_pk) "
				+ "and (:voting_number_start = 0 or cv.voting_number between :voting_number_start and :voting_number_end) "
				+ "and cv.election_group_pk = :election_group_pk group by vc.voting_category_id, proevet, cv.late_validation order by vc.voting_category_id, proevet;";

		assertEquals(expectedSql, new VotingStatisticsSql(new String[] { "FI", "FU" }, null, null, true, false).getSql());
	}

	@Test
	public void testSqlWithDatesAndOneVotingCategoryParameter() {

		String expectedSql = "select vc.voting_category_id,(cv.validation_timestamp is not null) proevet, count(*) antall, "
				+ "cv.late_validation from voting cv join mv_area a on (a.polling_place_pk = cv.polling_place_pk and a.area_level = 6) "
				+ "join voting_category vc on (vc.voting_category_pk = cv.voting_category_pk and (vc.voting_category_id in (:voting_category_id_0) )) "
				+ "where a.municipality_pk = :municipality_pk and (:polling_place_pk = 0 or a.polling_place_pk = :polling_place_pk) "
				+ "and (cv.received_timestamp between :received_timestamp_start and :received_timestamp_end) "
				+ "and (:voting_number_start = 0 or cv.voting_number between :voting_number_start and :voting_number_end) "
				+ "and cv.election_group_pk = :election_group_pk group by vc.voting_category_id, proevet, cv.late_validation order by vc.voting_category_id, proevet;";

		assertEquals(expectedSql, new VotingStatisticsSql(new String[] { "FI" }, LocalDate.now(), LocalDate.now(), true, false).getSql());
	}

	@Test
	public void testSqlWithValidationLateFlagDatesAndOneVotingCategoryParameter() {

		String expectedSql = "select vc.voting_category_id,(cv.validation_timestamp is not null) proevet, count(*) antall, "
				+ "cv.late_validation from voting cv join mv_area a on (a.polling_place_pk = cv.polling_place_pk and a.area_level = 6) "
				+ "join voting_category vc on (vc.voting_category_pk = cv.voting_category_pk and (vc.voting_category_id in (:voting_category_id_0) )) "
				+ "where a.municipality_pk = :municipality_pk and (:polling_place_pk = 0 or a.polling_place_pk = :polling_place_pk) "
				+ "and (cv.received_timestamp between :received_timestamp_start and :received_timestamp_end) "
				+ "and cv.late_validation IS FALSE "
				+ "and (:voting_number_start = 0 or cv.voting_number between :voting_number_start and :voting_number_end) "
				+ "and cv.election_group_pk = :election_group_pk group by vc.voting_category_id, proevet, cv.late_validation order by vc.voting_category_id, proevet;";

		assertEquals(expectedSql, new VotingStatisticsSql(new String[] { "FI" }, LocalDate.now(), LocalDate.now(), false, false).getSql());
	}

}
