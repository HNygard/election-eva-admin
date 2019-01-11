package no.valg.eva.admin.voting.repository;

import org.joda.time.LocalDate;

/**
 * Populates SQL string for voting statistics
 */

public class VotingStatisticsSql {

	public static final String VOTING_CATEGORY_ID = "voting_category_id_";
	public static final String MUNICIPALITY_PK = "municipality_pk";
	public static final String POLLING_PLACE_PK = "polling_place_pk";
	public static final String RECEIVED_TIMESTAMP_START = "received_timestamp_start";
	public static final String RECEIVED_TIMESTAMP_END = "received_timestamp_end";
	public static final String VOTING_NUMBER_START = "voting_number_start";
	public static final String VOTING_NUMBER_END = "voting_number_end";
	public static final String ELECTION_GROUP_PK = "election_group_pk";
	private final String sql;

	/**
	 * Generates SQL statement based on parameters. Adds filtering on dates if date parameters differ from null and on late_validation if flag is set. Adds an
	 * array of voting category parameters.
	 * @param votingCategories
	 *            array of String representing voting categories in SQL statement: ... in ( ... )
	 * @param startDate
	 *            of registration
	 * @param endDate
	 *            of registration
	 * @param includeLateValidation
	 *            flag telling whether to only include rows where late_validation is false
	 * @param includeLateAdvanceVotings
	 *            flag telling whether to in addition also include advance votings where late_validation is true
	 */
	public VotingStatisticsSql(final String[] votingCategories, final LocalDate startDate, final LocalDate endDate, final boolean includeLateValidation,
			final boolean includeLateAdvanceVotings) {

		StringBuilder searchString = new StringBuilder(
				"select vc.voting_category_id,(cv.validation_timestamp is not null) proevet, count(*) antall, cv.late_validation ").append(
				"from voting cv join mv_area a on (a.polling_place_pk = cv.polling_place_pk and a.area_level = 6) ").append(
				"join voting_category vc on (vc.voting_category_pk = cv.voting_category_pk");

		if (votingCategories.length == 0) {
			searchString.append(") ");
			if (includeLateAdvanceVotings) {
				searchString.append("and (vc.voting_category_id in ('FB', 'FE', 'FI', 'FU') and cv.late_validation is true) ");
			}
		} else {
			searchString.append(" and (vc.voting_category_id in (").append(voteCategoryParams(votingCategories)).append(") ");
			if (includeLateAdvanceVotings) {
				searchString.append("or (vc.voting_category_id in ('FB', 'FE', 'FI', 'FU') and cv.late_validation is true)");
			}
			searchString.append(")) ");
		}

		searchString.append("where a.municipality_pk = :").append(MUNICIPALITY_PK).append(" and (:").append(POLLING_PLACE_PK)
				.append(" = 0 or a.polling_place_pk = :").append(POLLING_PLACE_PK).append(") ");

		if (startDate != null && endDate != null) {
			searchString.append("and (cv.received_timestamp between :").append(RECEIVED_TIMESTAMP_START).append(" and :").append(RECEIVED_TIMESTAMP_END)
					.append(") ");
		}

		if (!includeLateValidation && !includeLateAdvanceVotings) {
			searchString.append("and cv.late_validation IS FALSE ");
		}

		searchString.append("and (:").append(VOTING_NUMBER_START).append(" = 0 or cv.voting_number between :").append(VOTING_NUMBER_START).append(" and :")
				.append(VOTING_NUMBER_END).append(") ").append("and cv.election_group_pk = :").append(ELECTION_GROUP_PK)
				.append(" group by vc.voting_category_id, proevet, cv.late_validation order by vc.voting_category_id, proevet;");

		sql = searchString.toString();
	}

	private String voteCategoryParams(final String[] items) {
		StringBuilder paramList = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			paramList.append(":").append(VOTING_CATEGORY_ID).append(i);
			if (i < items.length - 1) {
				paramList.append(",");
			}
		}
		return paramList.toString();
	}

	public String getSql() {
		return sql;
	}
}
