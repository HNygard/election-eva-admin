package no.evote.service.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.evote.service.configuration.CandidateFileRowConverter;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MaritalStatus;

import org.testng.annotations.Test;


public class CandidateFileRowConverterTest {

	private static final String POSITION = "1";
	private static final String FIRSTNAME = "Ola";
	private static final String MIDDLENAME = "Iver";
	private static final String LASTNAME = "Nordmann";
	private static final String DATE_OF_BIRTH = "01.01.1990";
	private static final String AFFILIATION = "DLF";

	private final CandidateFileRowConverter candidateFileRowConverter = new CandidateFileRowConverter();

	@Test
	public void convertRowsToCandidateList_forMunicipalityElection() {
		List<String[]> rowCandidates = new ArrayList<>();

		String[] rowCandidate = { POSITION, FIRSTNAME, MIDDLENAME, LASTNAME, DATE_OF_BIRTH, AFFILIATION };
		rowCandidates.add(rowCandidate);

		Affiliation affiliation = createAffiliation(AreaLevelEnum.MUNICIPALITY.getLevel());
		int initialDisplayOrder = 22;
		MaritalStatus maritalStatus = new MaritalStatus();
		CandidateFileRowConverter candidateFileRowConverter = new CandidateFileRowConverter();

		List<Candidate> candidates = candidateFileRowConverter.convertRowsToCandidateList(rowCandidates, affiliation, initialDisplayOrder, maritalStatus);

		assertEquals(candidates.get(0).getFirstName(), FIRSTNAME);
		assertEquals(candidates.get(0).getMiddleName(), MIDDLENAME);
		assertEquals(candidates.get(0).getLastName(), LASTNAME);
		assertEquals(candidates.get(0).getDateOfBirth(), DateUtil.parseLocalDate(DATE_OF_BIRTH));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void convertRowsToCandidateList_forFileWithRowWithInvalidData_throwsException() {
		List<String[]> rowCandidates = new ArrayList<>();
		String[] rowCandidate = { POSITION, FIRSTNAME, MIDDLENAME, LASTNAME, DATE_OF_BIRTH };
		rowCandidates.add(rowCandidate);

		Affiliation affiliation = createAffiliation(AreaLevelEnum.MUNICIPALITY.getLevel());
		int initialDisplayOrder = 22;
		MaritalStatus maritalStatus = new MaritalStatus();
		String[] rowCandidate2 = { "1", "FirstName2", "MiddleName2", "Lastname2" };
		rowCandidates.add(rowCandidate2);

		candidateFileRowConverter.convertRowsToCandidateList(rowCandidates, affiliation, initialDisplayOrder, maritalStatus);
	}

	@Test
	public void convertRowsToCandidateList_ifRowIsNull_anEmptyListIsReturned() {
		Affiliation affiliation = createAffiliation(AreaLevelEnum.MUNICIPALITY.getLevel());
		int initialDisplayOrder = 22;
		MaritalStatus maritalStatus = new MaritalStatus();

		List<Candidate> noCandidates = candidateFileRowConverter.convertRowsToCandidateList(null, affiliation, initialDisplayOrder, maritalStatus);
		assertEquals(noCandidates.size(), 0);
	}

	@Test
	public void convertRowsToCandidateList_ifRowIsEmpty_anEmptyListIsReturned() {
		Affiliation affiliation = createAffiliation(AreaLevelEnum.MUNICIPALITY.getLevel());
		int initialDisplayOrder = 22;
		MaritalStatus maritalStatus = new MaritalStatus();

		List<Candidate> noCandidates = candidateFileRowConverter.convertRowsToCandidateList(new ArrayList<String[]>(), affiliation, initialDisplayOrder, maritalStatus);
		assertEquals(noCandidates.size(), 0);
	}

	@Test
	public void convertRowsToCandidateList_forOtherElectionsThanMunicipality_electionBaselineVotesAreIgnored() {
		List<String[]> rowCandidates = new ArrayList<>();
		String[] rowCandidate = { POSITION, FIRSTNAME, LASTNAME, DATE_OF_BIRTH, AFFILIATION };
		rowCandidates.add(rowCandidate);

		Affiliation affiliation = createAffiliation(AreaLevelEnum.MUNICIPALITY.getLevel());
		int initialDisplayOrder = 22;
		MaritalStatus maritalStatus = new MaritalStatus();
		CandidateFileRowConverter candidateFileRowConverter = new CandidateFileRowConverter();
		List<Candidate> candidates = candidateFileRowConverter.convertRowsToCandidateList(rowCandidates, affiliation, initialDisplayOrder, maritalStatus);
		assertFalse(candidates.get(0).isBaselineVotes());
	}

	private Affiliation createAffiliation(final int areaLevel) {
		Affiliation affiliation = new Affiliation();
		Ballot ballot = new Ballot();
		Contest contest = new Contest();
		Election election = new Election();
		election.setAreaLevel(areaLevel);
		contest.setElection(election);
		ballot.setContest(contest);
		affiliation.setBallot(ballot);
		return affiliation;
	}
}

