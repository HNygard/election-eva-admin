package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.MaritalStatus;

import org.apache.commons.lang3.StringUtils;

/**
 * Converts rows from import file into candidates.
 */
public class CandidateFileRowConverter {
	public static final int NO_OF_REQUIRED_ROWS = 1;
	private static final String EMPTY = "";
	private static final int FIRST_NAME = 1;
	private static final int MIDDLE_NAME = 2;
	private static final int LAST_NAME = 3;
	private static final int DATE_OF_BIRTH = 4;

	/**
	 * Converts rows to candidates
	 * 
	 * @param rowCandidates
	 *            list of rows from file
	 * @param affiliation
	 *            partitilhørighet
	 * @param initialDisplayOrder
	 *            displayOrder for first imported row
	 * @param maritalStatus
	 *            maritalStatus to set on all candidates
	 * @return list of Candidate
	 */
	public List<Candidate> convertRowsToCandidateList(final List<String[]> rowCandidates, final Affiliation affiliation, final int initialDisplayOrder,
			final MaritalStatus maritalStatus) {
		int currentRow = 1;
		try {
			List<Candidate> candidateList = new ArrayList<>();
			if (rowCandidates != null && rowCandidates.size() >= NO_OF_REQUIRED_ROWS) {
				int displayOrder = initialDisplayOrder;

				for (String[] rowData : rowCandidates) {
					if (rowData != null && !isRowDataEmpty(rowData)) {
						Candidate newCandidate = createNewCandidate(affiliation, maritalStatus);

						newCandidate.setDisplayOrder(displayOrder);
						newCandidate.setFirstName(rowData[FIRST_NAME]);
						newCandidate.setMiddleName(rowData[MIDDLE_NAME]);
						newCandidate.setLastName(rowData[LAST_NAME]);
						newCandidate.setDateOfBirth(DateUtil.parseLocalDate(rowData[DATE_OF_BIRTH]));
						candidateList.add(newCandidate);
						displayOrder++;
						currentRow++;
					}
				}
			}
			return candidateList;
		} catch (ArrayIndexOutOfBoundsException aix) {
			throw new IllegalArgumentException("Invalid candidate in row " + currentRow);
		}
	}

	/**
	 * If empty cells in an excel document is read as a row
	 */
	private boolean isRowDataEmpty(final String[] rowData) {
		for (String c : rowData) {
			if (!StringUtils.isEmpty(c)) {
				return false;
			}
		}
		return true;
	}

	public Candidate createNewCandidate(final Affiliation affiliation, final MaritalStatus maritalStatus) {
		Candidate newCandidate = new Candidate();
		newCandidate.setMaritalStatus(maritalStatus);
		newCandidate.setId(EMPTY);
		newCandidate.setFirstName(EMPTY);
		newCandidate.setLastName(EMPTY);
		newCandidate.setNameLine(EMPTY);
		newCandidate.setAffiliation(affiliation);
		newCandidate.setBallot(affiliation.getBallot());
		newCandidate.setBaselineVotes(false);

		return newCandidate;
	}

}
