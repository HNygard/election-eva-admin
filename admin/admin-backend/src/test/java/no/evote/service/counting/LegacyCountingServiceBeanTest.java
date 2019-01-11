package no.evote.service.counting;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.ContestRelAreaRepository;
import no.valg.eva.admin.test.BaseTakeTimeTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacyCountingServiceBeanTest extends BaseTakeTimeTest {

	@Test(expectedExceptions = EvoteException.class)
	public void testCountOnNotMunicipalityWhenCentralCount() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, false, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountOnMunicipalityWhenPollingDistrictCount() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(true, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(false, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountProtocolOnMunicipalityWhenCentralCount() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(true, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, false, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.POLLING_DISTRICT).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountOnChildPollingDistrict() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(true, false, true, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountProtocolOnParentPollingDistrict() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(false, true, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.POLLING_DISTRICT).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountOnTechnicalPollingDistrictWhenNotTechnicalPollingDistrictCount() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, true);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VF");
		CountingMode countingMode = CountingMode.getCountingMode(true, false, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountOnNonTechnicalPollingDistrictWhenTechnicalPollingDistrictCount() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VF");
		CountingMode countingMode = CountingMode.getCountingMode(true, false, true);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
        Assert.fail();
	}

	@Test
	public void testCountOnTechnicalPollingDistrictWhenTechnicalPollingDistrictCount() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, true);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VF");
		CountingMode countingMode = CountingMode.getCountingMode(true, false, true);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test
	public void testCountProtocolOnParentPollingDistrictWhenCentralPreliminaryCountIsFalse() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(false, true, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(false, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.POLLING_DISTRICT).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountWhenProtocolIsNotReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(0));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountWhenProtocolAndPreliminaryIsNotReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();
		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(false, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountCentralWhenProtocolsAreNotReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean() {
			@Override
			boolean isAllLowerPollingDistrictsDone(final UserData userData, final Municipality municipality, final Contest contest) {
				return false;
			}
		};
		PollingDistrict pollingDistrict = makePollingDistrict(true, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(false, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountWhenFinalIsNotReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();

		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.COUNTY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(0));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCountProtocolChildrenAreNotReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean() {
			@Override
			boolean isAllChildPollingDistrictsDone(final UserData userData, final PollingDistrict pollingDistrict, final Contest contest) {
				return false;
			}
		};

		PollingDistrict pollingDistrict = makePollingDistrict(false, true, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(false, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test
	public void testCountWhenProtocolIsReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();

		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test
	public void testCountWhenProtocolAndPreliminaryIsReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();

		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(false, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.COUNTY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(2));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test
	public void testCountCentralWhenProtocolsAreReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean() {
			@Override
			boolean isAllLowerPollingDistrictsDone(final UserData userData, final Municipality municipality, final Contest contest) {
				return true;
			}
		};

		PollingDistrict pollingDistrict = makePollingDistrict(true, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, false, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test
	public void testCountProtocolChildrenAreReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean() {
			@Override
			boolean isAllChildPollingDistrictsDone(final UserData userData, final PollingDistrict pollingDistrict, final Contest contest) {
				return true;
			}
		};

		PollingDistrict pollingDistrict = makePollingDistrict(false, true, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(false, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test
	public void testCountWhenFinalIsReady() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();

		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.COUNTY).getMvArea();
		MvElection mvElection = makeMvElection(true);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(1));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test
	public void testCountWhenFinalIsNotReadyButPenultimateRecountIsFalse() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();

		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(true);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(false, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.COUNTY).getMvArea();
		MvElection mvElection = makeMvElection(false);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(0));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	@Test
	public void testOkToCountWhenisNotRequiredProtocolCount() {
		LegacyCountingServiceBean countingService = new LegacyCountingServiceBean();

		PollingDistrict pollingDistrict = makePollingDistrict(false, false, false, false);
		Municipality municipality = makeMunicipality(false);
		VoteCountCategory voteCountCategory = makeVoteCountCategory("VO");
		CountingMode countingMode = CountingMode.getCountingMode(true, true, false);
		MvArea reportingUnitMvArea = makeReportingUnit(AreaLevelEnum.MUNICIPALITY).getMvArea();
		MvElection mvElection = makeMvElection(false);

		countingService.setContestRelAreaRepository(getContestRelAreaRepository(0));
		countingService.validateSelectedPollingDistrictAndCategory(null, pollingDistrict, municipality, voteCountCategory, countingMode,
				reportingUnitMvArea, mvElection);
	}

	private MvElection makeMvElection(final boolean penultimateRecount) {
		MvElection mvElection = new MvElection();
		mvElection.setPk(1L);
		Contest contest = mock(Contest.class);
		when(contest.getPenultimateRecount()).thenReturn(penultimateRecount);
		when(contest.isOnBoroughLevel()).thenReturn(false);
		mvElection.setContest(contest);
		return mvElection;
	}

	private Municipality makeMunicipality(final boolean requiredProtocolCount) {
		Municipality municipality = new Municipality();
		municipality.setRequiredProtocolCount(requiredProtocolCount);
		return municipality;
	}

	private PollingDistrict makePollingDistrict(final boolean municipality, final boolean parent, final boolean child, final boolean technicalPollingDistrict) {
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setMunicipality(municipality);
		pollingDistrict.setParentPollingDistrict(parent);
		pollingDistrict.setTechnicalPollingDistrict(technicalPollingDistrict);
		if (child) {
			pollingDistrict.setPollingDistrict(pollingDistrict);
		}
		pollingDistrict.setPk(1L);
		return pollingDistrict;
	}

	private ReportingUnit makeReportingUnit(final AreaLevelEnum areaLevel) {
		ReportingUnit reportingUnit = new ReportingUnit();
		MvArea mvArea = new MvArea();
		mvArea.setPk(1L);
		mvArea.setAreaLevel(areaLevel.getLevel());
		reportingUnit.setMvArea(mvArea);
		return reportingUnit;
	}

	private VoteCountCategory makeVoteCountCategory(String id) {
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(id);
		return voteCountCategory;
	}

	private ContestRelAreaRepository getContestRelAreaRepository(final int count) {
		ContestRelAreaRepository contestRelAreaRepository = mock(ContestRelAreaRepository.class);
		when(contestRelAreaRepository.countCountsOnPollingDistrict(any(Long.class), any(Long.class), any(String.class), any(Long.class))).thenReturn(count);
		return contestRelAreaRepository;
	}
}
