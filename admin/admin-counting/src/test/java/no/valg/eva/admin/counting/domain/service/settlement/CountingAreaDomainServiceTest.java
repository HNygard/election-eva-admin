package no.valg.eva.admin.counting.domain.service.settlement;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CountingAreaDomainServiceTest {

	private static final long ORDINARY_AREA_PK = 1L;
	private static final long CHILD_AREA_PK = 2L;
	private static final long PARENT_AREA_PK = 3L;
	private static final long ELECTION_GROUP_PK = 11L;
	private static final long A_PK = 1L;
	private static final long ANOTHER_PK = 2L;
	private static final long YET_ANOTHER_PK = 3L;

	@Test
    public void countingMvArea_givenCountyContestAndCountCategory_givesMvAreasForMunicipalitiesWithThisCategoryConfigured() {

		MvAreaRepository mvAreaRepository = mock(MvAreaRepository.class);
		VoteCountService voteCountService = mock(VoteCountService.class);
		VoteCountCategoryRepository voteCountCategoryRepository = mock(VoteCountCategoryRepository.class);

		CountingAreaDomainService countingAreaDomainService = new CountingAreaDomainService(mvAreaRepository, voteCountService, voteCountCategoryRepository);

		MvElection countyContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(countyContestMvElection.getContest().isOnCountyLevel()).thenReturn(true);
		AreaPath contestAreaPath = AreaPath.from("111111.11.11");
		when(countyContestMvElection.contestMvArea().getAreaPath()).thenReturn(contestAreaPath.path());
		when(countyContestMvElection.getElectionGroup().getPk()).thenReturn(ELECTION_GROUP_PK);
		MvArea municipalityMvArea1 = municipalityMvArea(A_PK, "111111.11.11.1111");
		MvArea municipalityMvArea2 = municipalityMvArea(ANOTHER_PK, "111111.11.11.2222");
		MvArea municipalityMvArea3 = municipalityMvArea(YET_ANOTHER_PK, "111111.11.11.3333");
		when(mvAreaRepository.findByPathAndLevel(contestAreaPath, MUNICIPALITY))
				.thenReturn(asList(municipalityMvArea1, municipalityMvArea2, municipalityMvArea3));
		when(voteCountCategoryRepository.findByMunicipality(A_PK, ELECTION_GROUP_PK, false))
				.thenReturn(asList(mockVoteCountCategory(VO), mockVoteCountCategory(VF), mockVoteCountCategory(VS)));
		when(voteCountCategoryRepository.findByMunicipality(ANOTHER_PK, ELECTION_GROUP_PK, false))
				.thenReturn(asList(mockVoteCountCategory(VO), mockVoteCountCategory(VB), mockVoteCountCategory(VS)));
		when(voteCountCategoryRepository.findByMunicipality(YET_ANOTHER_PK, ELECTION_GROUP_PK, false))
				.thenReturn(asList(mockVoteCountCategory(VO), mockVoteCountCategory(VF), mockVoteCountCategory(VS)));
		when(voteCountService.countingMode(any(CountCategory.class), any(Municipality.class), any(MvElection.class))).thenReturn(CENTRAL);

		AreaPath areaPath1 = AreaPath.from("111111.11.11.1111.111100.0000");
		MvArea fakeMvArea1 = mock(MvArea.class);
		when(fakeMvArea1.getAreaPath()).thenReturn(areaPath1.path());
		when(mvAreaRepository.findSingleByPath(areaPath1)).thenReturn(fakeMvArea1);
		AreaPath areaPath3 = AreaPath.from("111111.11.11.3333.333300.0000");
		MvArea fakeMvArea3 = mock(MvArea.class);
		when(fakeMvArea3.getAreaPath()).thenReturn(areaPath3.path());
		when(mvAreaRepository.findSingleByPath(areaPath3)).thenReturn(fakeMvArea3);

		List<MvArea> mvAreas = countingAreaDomainService.countingMvAreas(countyContestMvElection, VF);

		assertThat(mvAreas.size()).isEqualTo(2);
		assertThat(mvAreas.get(0).getAreaPath()).isEqualTo(areaPath1.path());
		assertThat(mvAreas.get(1).getAreaPath()).isEqualTo(areaPath3.path());
	}

	@Test(dataProvider = "countCategoryAndIncludeChildAreaAndTwoAreas")
	public void countingMvArea_gittIkkeSingleAreaContestOgTestData_girKorrekteOmråder(
            CountCategory countCategory, boolean includeChildArea, boolean twoAreas) {

		MvAreaRepository mvAreaRepository = mock(MvAreaRepository.class);
		VoteCountService voteCountService = mock(VoteCountService.class);
		VoteCountCategoryRepository voteCountCategoryRepository = mock(VoteCountCategoryRepository.class);

		CountingAreaDomainService countingAreaDomainService = new CountingAreaDomainService(mvAreaRepository, voteCountService, voteCountCategoryRepository);

		MvElection countyContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(countyContestMvElection.getContest().isOnCountyLevel()).thenReturn(false);
		when(countyContestMvElection.getContest().isSingleArea()).thenReturn(false);
		AreaPath contestAreaPath = AreaPath.from("111111.11.11");
		when(countyContestMvElection.contestMvArea().getAreaPath()).thenReturn(contestAreaPath.path());

        // a sami child area is filtered out because it is counted on the parent area
        // a sami parent is filtered out because it has only FO and FS categories
        MvArea municipalitySamiOrdinaryMvArea = municipalityMvArea(ORDINARY_AREA_PK, "111111.11.11.1111");
		MvArea municipalitySamiChildMvArea = municipalityMvArea(CHILD_AREA_PK, "111111.11.11.2222");
		MvArea municipalitySamiParentMvArea = municipalityMvArea(PARENT_AREA_PK, "111111.11.11.3333");

		Municipality municipalitySamiOrdinary = mock(Municipality.class);
		when(municipalitySamiOrdinaryMvArea.getMunicipality()).thenReturn(municipalitySamiOrdinary);
		Municipality municipalitySamiParent = mock(Municipality.class);
		when(municipalitySamiParentMvArea.getMunicipality()).thenReturn(municipalitySamiParent);

		Set<ContestArea> contestAreaSet;
		if (includeChildArea) {
			contestAreaSet = makeContestAreaSet(municipalitySamiOrdinaryMvArea, municipalitySamiChildMvArea, municipalitySamiParentMvArea);
		} else {
			contestAreaSet = makeContestAreaSet(municipalitySamiOrdinaryMvArea, municipalitySamiParentMvArea);
		}
		when(countyContestMvElection.getContest().getContestAreaSet()).thenReturn(contestAreaSet);
		
        // countingMode for VF is null for samlekommune (sami parent area)
		when(voteCountService.countingMode(eq(FO), eq(municipalitySamiParent), any(MvElection.class))).thenReturn(CENTRAL);
		when(voteCountService.countingMode(eq(FO), eq(municipalitySamiOrdinary), any(MvElection.class))).thenReturn(CENTRAL);
		when(voteCountService.countingMode(eq(CountCategory.VF), eq(municipalitySamiOrdinary), any(MvElection.class))).thenReturn(CENTRAL);

		String pollingDistrictSamiAreaPath = "111111.11.11.1111.111100.0000";
		AreaPath areaPath1 = AreaPath.from(pollingDistrictSamiAreaPath);
		MvArea pollingDistrictSamiOrdinaryArea = mock(MvArea.class);
		when(pollingDistrictSamiOrdinaryArea.getAreaPath()).thenReturn(areaPath1.path());
		when(mvAreaRepository.findSingleByPath(areaPath1)).thenReturn(pollingDistrictSamiOrdinaryArea);

		String pollingDistrictSamiParentAreaPath = "111111.11.11.3333.333300.0000";
		AreaPath areaPath2 = AreaPath.from(pollingDistrictSamiParentAreaPath);
		MvArea pollingDistrictSamiParentArea = mock(MvArea.class);
		when(pollingDistrictSamiParentArea.getAreaPath()).thenReturn(areaPath2.path());
		when(mvAreaRepository.findSingleByPath(areaPath2)).thenReturn(pollingDistrictSamiParentArea);

		List<MvArea> mvAreas = countingAreaDomainService.countingMvAreas(countyContestMvElection, countCategory);

		assertThat(mvAreas.size()).isEqualTo(twoAreas ? 2 : 1);
		if (twoAreas) {
			assertThat(mvAreas.get(0).getAreaPath()).isEqualTo(pollingDistrictSamiAreaPath);
			assertThat(mvAreas.get(1).getAreaPath()).isEqualTo(pollingDistrictSamiParentAreaPath);
		} else {
			assertThat(mvAreas.get(0).getAreaPath()).isEqualTo(pollingDistrictSamiAreaPath);
		}
		verify(mvAreaRepository, never()).findByPathAndLevel(any(AreaPath.class), any(AreaLevelEnum.class));
	}

	@DataProvider
	public Object[][] countCategoryAndIncludeChildAreaAndTwoAreas() {
		return new Object[][]{
				{FO, true, true},
				{FO, false, false},
				{VF, true, false},
				{VF, false, false}
		};
	}

	private Set<ContestArea> makeContestAreaSet(MvArea ordinaryMvArea, MvArea childMvArea, MvArea parentMvArea) {
		Set<ContestArea> contestAreas = new HashSet<>();
		contestAreas.add(makeContestArea(ordinaryMvArea, ORDINARY_AREA_PK, false, false));
		contestAreas.add(makeContestArea(childMvArea, CHILD_AREA_PK, true, false));
		contestAreas.add(makeContestArea(parentMvArea, PARENT_AREA_PK, false, true));
		return contestAreas;
	}

	private Set<ContestArea> makeContestAreaSet(MvArea ordinaryMvArea, MvArea parentMvArea) {
		Set<ContestArea> contestAreas = new HashSet<>();
		contestAreas.add(makeContestArea(ordinaryMvArea, ORDINARY_AREA_PK, false, false));
		contestAreas.add(makeContestArea(parentMvArea, PARENT_AREA_PK, false, true));
		return contestAreas;
	}

	private ContestArea makeContestArea(MvArea mvArea, long pk, boolean childArea, boolean parentArea) {
		ContestArea contestArea = new ContestArea();
		contestArea.setPk(pk);
		contestArea.setChildArea(childArea);
		contestArea.setParentArea(parentArea);
		contestArea.setMvArea(mvArea);
		return contestArea;
	}

	private VoteCountCategory mockVoteCountCategory(CountCategory countCategory) {
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(countCategory.getId());
		return voteCountCategory;
	}

	private MvArea municipalityMvArea(long municipalityPk, String areaPath) {
		MvArea municipalityMvArea = mock(MvArea.class, RETURNS_DEEP_STUBS);
		when(municipalityMvArea.getMunicipality().getPk()).thenReturn(municipalityPk);
		when(municipalityMvArea.getAreaPath()).thenReturn(areaPath);
		return municipalityMvArea;
	}
}
