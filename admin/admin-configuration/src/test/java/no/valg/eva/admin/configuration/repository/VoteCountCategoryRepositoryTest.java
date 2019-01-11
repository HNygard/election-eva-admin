package no.valg.eva.admin.configuration.repository;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;


@Test(groups = TestGroups.REPOSITORY)
public class VoteCountCategoryRepositoryTest extends AbstractJpaTestBase {

	private VoteCountCategoryRepository voteCountCategoryRepository;

	private GenericTestRepository genericTestRepository;
	private MunicipalityRepository municipalityRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		voteCountCategoryRepository = new VoteCountCategoryRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
		municipalityRepository = new MunicipalityRepository(getEntityManager());
	}

	@Test
	public void findByContest_kommunestyrevalgIHalden_shouldReturnVOandFO() {
		Contest contest = findSingleContest("000101", "200701");

		List<VoteCountCategory> voteCountCategoryList = voteCountCategoryRepository.findByContest(contest.getPk());

		assertThat(voteCountCategoryList.size()).isEqualTo(2);
		assertThat(voteCountCategoryList).contains(voteCountCategoryRepository.findById(CountCategory.VO.getId()));
		assertThat(voteCountCategoryList).contains(voteCountCategoryRepository.findById(CountCategory.FO.getId()));
		assertThat(voteCountCategoryList).doesNotContain(voteCountCategoryRepository.findById(CountCategory.BF.getId()));
	}

	@Test
	public void categoriesForContest_kommunestyrevalgIHalden_shouldReturnVOandFO() {
		Contest contest = findSingleContest("000101", "200701");

		List<VoteCountCategory> voteCountCategoryList = voteCountCategoryRepository.categoriesForContest(contest);

		assertThat(voteCountCategoryList.size()).isEqualTo(2);
		assertThat(voteCountCategoryList).contains(voteCountCategoryRepository.findById(CountCategory.VO.getId()));
		assertThat(voteCountCategoryList).contains(voteCountCategoryRepository.findById(CountCategory.FO.getId()));
		assertThat(voteCountCategoryList).doesNotContain(voteCountCategoryRepository.findById(CountCategory.BF.getId()));
	}

	@Test
	public void categoriesForContest_contestNull_shouldReturnEmptyList() {
		Contest contest = null;

		List<VoteCountCategory> voteCountCategoryList = voteCountCategoryRepository.categoriesForContest(contest);

		assertThat(voteCountCategoryList).isEmpty();
	}

	@Test
	public void findByContest_bydelsvalg_shouldIncludeBF() {
		Contest contest = findSingleContest("030101", "200701");
		
		List<VoteCountCategory> voteCountCategoryList = voteCountCategoryRepository.findByContest(contest.getPk());

		assertThat(voteCountCategoryList).containsExactly(
				voteCountCategoryRepository.findById(VO.getId()),
				voteCountCategoryRepository.findById(VS.getId()),
				voteCountCategoryRepository.findById(VB.getId()),
				voteCountCategoryRepository.findById(FO.getId()),
				voteCountCategoryRepository.findById(FS.getId()),
				voteCountCategoryRepository.findById(BF.getId())
		);
	}

	@Test
	public void findByMunicipality_forHaldenAndKommunestyre_shouldReturnVOandFO() {

		// skrevet om til ikke å bruke pk-er. Neste skritt bør være at den setter opp egne data..

		List<ElectionGroup> electionGroups = genericTestRepository.findEntitiesByProperty(ElectionGroup.class, "id", "01");
		assertThat(electionGroups).isNotEmpty();

		// velger første election group resultat
		ElectionGroup electionGroup = electionGroups.get(0);

		String municipalityIdForHalden = "0101";
		Municipality municipality = municipalityRepository.municipalityByElectionEventAndId(electionGroup.getElectionEvent().getPk(), municipalityIdForHalden);

		List<VoteCountCategory> voteCountCategoryList = voteCountCategoryRepository.findByMunicipality(municipality.getPk(), electionGroup.getPk(), true);

		assertThat(voteCountCategoryList.size()).isEqualTo(2);
		assertThat(voteCountCategoryList).contains(voteCountCategoryRepository.findById(CountCategory.VO.getId()));
		assertThat(voteCountCategoryList).contains(voteCountCategoryRepository.findById(CountCategory.FO.getId()));
		assertThat(voteCountCategoryList).doesNotContain(voteCountCategoryRepository.findById(CountCategory.BF.getId()));
	}

	@Test
	public void findByMunicipality_forOslo_shouldIncludeBF() {

		// find pk for an instance of Oslo - municipalityId 0301 - fails if not present in testdata set
		List<Municipality> osloInstances = genericTestRepository.findEntitiesByProperty(Municipality.class, "id", AreaPath.OSLO_MUNICIPALITY_ID);
		assertThat(osloInstances).isNotEmpty();

		Municipality municipality = osloInstances.stream()
				.filter(mun -> mun.getCounty().getCountry().getElectionEvent().getId().equals("000000"))
				.findFirst()
				.orElse(null);
		Long municipalityPkForOslo = Objects.requireNonNull(municipality).getPk();

		ElectionEvent electionEvent = municipality.getCounty().getCountry().getElectionEvent();

		ElectionGroup electionGroup = new ElectionGroup("01", "test", electionEvent);
		ElectionGroup savedElectionGroup = genericTestRepository.createEntity(electionGroup);
		Long electionGroupPk = savedElectionGroup.getPk();

		List<VoteCountCategory> voteCountCategories = voteCountCategoryRepository.findByMunicipality(municipalityPkForOslo, electionGroupPk, true);
		Set<CountCategory> countCategories = new HashSet<>();
		for (VoteCountCategory voteCountCategory : voteCountCategories) {
			countCategories.add(CountCategory.fromId(voteCountCategory.getId()));
		}

		assertThat(countCategories).contains(CountCategory.BF);
	}

	private Contest findSingleContest(String contestId, String electionEventId) {
		List<Contest> contests = genericTestRepository.findEntitiesByProperty(Contest.class, "id", contestId);
		return contests.stream().reduce(null,
				(contestReduced, contestCandidate) -> contestCandidate.getElection().getElectionGroup().getElectionEvent().getId().equals(electionEventId)
						? contestCandidate : contestReduced);
	}

}
