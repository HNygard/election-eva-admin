package no.valg.eva.admin.counting.repository;

import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class VotingRepositoryTest extends AbstractJpaTestBase {

	/** A parameterless constructor is needed in order for the injection to work in our production code */
	@Test
	public void parameterlessConstructor_always_exists() {
		VotingRepository votingRepository = new VotingRepository();

		assertThat(votingRepository).isNotNull();
	}

	@Test
	public void findApprovedVotingCountByBoroughAndCategoriesAndLateValidation_whenOneVotingExistsForBoroughAndCategories_returnsOne() {
		VotingRepository votingRepository = createVotingRepository();
		String areaPathVestreAker = "200701.47.01.0101.010100";
		MvArea mvAreaVestreAker = findMvAreaByAreaPath(areaPathVestreAker);
		Borough boroughVestreAker = mvAreaVestreAker.getBorough();
		no.valg.eva.admin.common.voting.VotingCategory[] votingCategories = new no.valg.eva.admin.common.voting.VotingCategory[] {
				FU,
				FI,
				FB,
				FE };

		long votingCount = votingRepository.findApprovedVotingCountByBoroughAndCategoriesAndLateValidation(boroughVestreAker, votingCategories, false);

		
		assertThat(votingCount).isEqualTo(1434);
		
	}

	@Test
	public void findApprovedVotingsByPollingDistrictAndCategories_whenOneVotingExistsForPollingDistrictAndCategory_returnsOneVoting() {
		VotingRepository votingRepository = createVotingRepository();
		String areaPathMarienlystSkole = "200701.47.03.0301.030104.0401";
		MvArea mvAreaGrindbakken = findMvAreaByAreaPath(areaPathMarienlystSkole);
		PollingDistrict pollingDistrictGrindbakken = mvAreaGrindbakken.getPollingDistrict();
		no.valg.eva.admin.common.voting.VotingCategory[] votingCategories = new no.valg.eva.admin.common.voting.VotingCategory[] { VF };

		Collection<Voting> votings = votingRepository.findApprovedVotingsByPollingDistrictAndCategories(pollingDistrictGrindbakken, votingCategories);

		assertThat(votings.size()).isEqualTo(1);
		assertThat(votings.iterator().next().isApproved()).isEqualTo(true);
		assertThat(votings.iterator().next().getVotingCategory().getId()).isEqualTo(CountCategory.VF.getId());
	}

	@Test
	void findMarkOffInOtherBoroughs_voterHasVotedInAnotherBorough_countAtLeastOneVf() {
		VotingRepository votingRepository = createVotingRepository();
		String areaPathVestreAkerBorough = "200701.47.03.0301.030107";
		MvArea mvArea = findMvAreaByAreaPath(areaPathVestreAkerBorough);

		Long numberOfVotersWhoHaveVotedInAnotherBorough = votingRepository.findMarkOffInOtherBoroughs(mvArea.getBorough().getPk());

		assertThat(numberOfVotersWhoHaveVotedInAnotherBorough).isGreaterThanOrEqualTo(1L);
	}

	@Test
	public void findVotingCategoryById_whenSearchingForAVotingCategoryById_returnsTheVotingCategory() {
		VotingRepository votingRepository = createVotingRepository();

		VotingCategory votingCategory = votingRepository.findVotingCategoryById(VO.getId());

		assertThat(votingCategory).isNotNull();
		assertThat(votingCategory.getId()).isEqualTo(VO.getId());
		assertThat(votingCategory.isEarlyVoting()).isFalse();
	}

	@Test
	public void findMarkOffForSamlekommuneInContest_noVotings_returns0() {
		VotingRepository votingRepository = new VotingRepository(getEntityManager());

		MvElection mvElection = new MvElection();
		mvElection.setPk(1L);
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(1L);
		mvElection.setElectionEvent(electionEvent);
		Contest contest = new Contest();
		contest.setPk(1L);
		mvElection.setContest(contest);
		assertThat(votingRepository.findMarkOffForSamlekommuneInContest(mvElection, true)).isEqualTo(0);
	}

	private MvArea findMvAreaByAreaPath(String areaPath) {
		GenericTestRepository genericTestRepository = createGenericTestRepository();
		return genericTestRepository.findEntityByProperty(MvArea.class, "areaPath", areaPath);
	}

	private GenericTestRepository createGenericTestRepository() {
		return new GenericTestRepository(getEntityManager());
	}

	private VotingRepository createVotingRepository() {
		return new VotingRepository(getEntityManager());
	}
}
