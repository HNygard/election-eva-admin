package no.valg.eva.admin.common.configuration.model.central;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ContestListProposalDataTest extends MockUtilsTestCase {

	@Test(dataProvider = "isCandidatesInput")
	public void isMinCandidatesInput_withDataProvider_verifyExpected(Integer min, Integer minAddition, boolean expected) throws Exception {
		Election election = election();
		election.setMinCandidates(min);
		election.setMinCandidatesAddition(minAddition);
		ContestListProposalData data = contestListProposalData(election);

		assertThat(data.isMinCandidatesInput()).isEqualTo(expected);
	}

	@Test(dataProvider = "isCandidatesInput")
	public void isMaxCandidatesInput_withDataProvider_verifyExpected(Integer min, Integer minAddition, boolean expected) throws Exception {
		Election election = election();
		election.setMaxCandidates(min);
		election.setMaxCandidatesAddition(minAddition);
		ContestListProposalData data = contestListProposalData(election);

		assertThat(data.isMaxCandidatesInput()).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] isCandidatesInput() {
		return new Object[][] {
				{ null, null, true },
				{ 1, null, false },
				{ null, 1, false }
		};
	}

	@Test
	public void isMaxWriteInInput_withIsWriteinLocalOverride_returnsTrue() throws Exception {
		Election election = election();
		election.setWriteinLocalOverride(true);
		ContestListProposalData data = contestListProposalData(election);

		assertThat(data.isMaxWriteInInput()).isTrue();
	}

	@Test
	public void isHasNoDependentInputs_withDependentInputs_returnsFalse() throws Exception {
		ContestListProposalData data = contestListProposalData(election());

		assertThat(data.isHasNoDependentInputs()).isFalse();
	}

	@Test(dataProvider = "isValid")
	public void isValid_withDataProvider_verifyExpected(
			boolean writein,
			boolean renumberLimit,
			Integer minProposersNewParty,
			Integer minProposersOldParty,
			Integer minCandidates,
			Integer maxCandidates,
			Integer maxWriteIn,
			Integer maxRenumber,
			boolean expected) throws Exception {
		Election election = election();
		election.setWritein(writein);
		election.setRenumberLimit(renumberLimit);
		ContestListProposalData data = new ContestListProposalData(
				election,
				minProposersNewParty,
				minProposersOldParty,
				minCandidates,
				maxCandidates,
				10,
				maxWriteIn,
				maxRenumber);

		assertThat(data.isValid()).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] isValid() {
		return new Object[][] {
				{ false, false, null, null, null, null, null, null, false },
				{ false, false, 1, 1, 1, 1, null, null, true },
				{ true, false, 1, 1, 1, 1, null, null, false },
				{ true, false, 1, 1, 1, 1, 2, null, true },
				{ true, true, 1, 1, 1, 1, 2, null, false },
				{ true, true, 1, 1, 1, 1, 2, 3, true }
		};
	}

	@Test
	public void recalculate_withMinCandidatesAddition_recalculatesMinCandidates() throws Exception {
		Election election = election();
		election.setMinCandidatesAddition(10);
		ContestListProposalData data = contestListProposalData(election);
		data.setNumberOfPositions(10);

		data.recalculate();

		assertThat(data.getMinCandidates()).isEqualTo(20);
	}

	@Test
	public void recalculate_withMinCandidatesConf_recalculatesMinCandidates() throws Exception {
		Election election = election();
		election.setMinCandidates(10);
		ContestListProposalData data = contestListProposalData(election);
		data.setNumberOfPositions(10);

		data.recalculate();

		assertThat(data.getMinCandidates()).isEqualTo(10);
	}

	@Test
	public void recalculate_withMaxCandidatesAddition_recalculatesMaxCandidates() throws Exception {
		Election election = election();
		election.setMaxCandidatesAddition(10);
		ContestListProposalData data = contestListProposalData(election);
		data.setNumberOfPositions(10);

		data.recalculate();

		assertThat(data.getMaxCandidates()).isEqualTo(20);
	}

	@Test
	public void recalculate_withMaxCandidatesConf_recalculatesMaxCandidates() throws Exception {
		Election election = election();
		election.setMaxCandidates(10);
		ContestListProposalData data = contestListProposalData(election);
		data.setNumberOfPositions(10);

		data.recalculate();

		assertThat(data.getMaxCandidates()).isEqualTo(10);
	}

	@Test(dataProvider = "recalculate")
	public void recalculate_withWriteInConfAndDataProvider_verifyExpected(int numberOfPos, int expectedMaxWriteIn) throws Exception {
		Election election = election();
		election.setWritein(true);
		election.setWriteinLocalOverride(false);
		ContestListProposalData data = contestListProposalData(election);
		data.setNumberOfPositions(numberOfPos);

		data.recalculate();

		assertThat(data.getMaxWriteIn()).isEqualTo(expectedMaxWriteIn);
	}

	@DataProvider
	public Object[][] recalculate() {
		return new Object[][] {
				{ 12, 5 },
				{ 19, 5 },
				{ 31, 7 }
		};
	}

	private ContestListProposalData contestListProposalData(Election election) {
		return new ContestListProposalData(election, 0, 0, 0, 0, 10, 0, 0);
	}

	private Election election() {
		return new Election(ELECTION_PATH_ELECTION_GROUP);
	}

}

