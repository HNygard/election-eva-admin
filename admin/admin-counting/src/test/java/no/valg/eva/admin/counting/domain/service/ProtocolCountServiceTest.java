package no.valg.eva.admin.counting.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.joda.time.LocalDate;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProtocolCountServiceTest extends MockUtilsTestCase {

	private ProtocolCountService protocolCountService;
	@Mock
	private ManualContestVotingRepository manualContestVotingRepository;
	@Mock
	private VotingRepository votingRepository;
	@Mock
	private ReportCountCategoryRepository reportCountCategoryRepository;
	@Mock
	private ReportingUnitRepository reportingUnitRepository;
	@Mock
	private UserData userData;

	private MvArea mvArea;

	@BeforeSuite
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void setUp() {
		protocolCountService = new ProtocolCountService(reportingUnitRepository, reportCountCategoryRepository, votingRepository,
				manualContestVotingRepository);
		Municipality municipality = MunicipalityMockups.municipality(false);
		mvArea = MvAreaMockups.pollingDistrictMvArea(municipality);
		Mockito.reset(manualContestVotingRepository);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void xim() {
		Contest contest = createMock(Contest.class);
		when(contest.getElection().getElectionGroup().getElectionEvent().getElectionDays()).thenReturn(electionDays());
		List<DailyMarkOffCount> dailyMarkOffCounts = dailyMarkOffCounts();
		protocolCountService.createManualXiMs(userData, dailyMarkOffCounts, contest, mvArea);
		verify(manualContestVotingRepository, Mockito.times(1)).createMany(any(UserData.class), anyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void ximNoElectionDays() {
		Contest contest = createMock(Contest.class);
        when(contest.getElection().getElectionGroup().getElectionEvent().getElectionDays()).thenReturn(new HashSet<>());
		List<DailyMarkOffCount> dailyMarkOffCounts = dailyMarkOffCounts();
		protocolCountService.createManualXiMs(userData, dailyMarkOffCounts, contest, mvArea);
		verify(manualContestVotingRepository, Mockito.times(1)).createMany(any(UserData.class), anyList());
	}

	private List<DailyMarkOffCount> dailyMarkOffCounts() {
		List<DailyMarkOffCount> markOffList = new ArrayList<>();
		markOffList.add(dailyMarkOffCount());
		return markOffList;
	}

	private DailyMarkOffCount dailyMarkOffCount() {
		DailyMarkOffCount markOffCount = new DailyMarkOffCount(new LocalDate());
		markOffCount.setMarkOffCount(1);
		return markOffCount;
	}

	private Set<ElectionDay> electionDays() {
		Set<ElectionDay> days = new HashSet<>();
		days.add(day(new LocalDate()));
		days.add(day(new LocalDate().minusDays(1)));
		return days;
	}

	private ElectionDay day(LocalDate date) {
		ElectionDay day = new ElectionDay();
		day.setDate(date);
		return day;
	}

}
