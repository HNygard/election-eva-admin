package no.valg.eva.admin.settlement.application;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.settlement.model.LevelingSeatSettlementSummary;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.settlement.application.mapper.LevelingSeatMapper;
import no.valg.eva.admin.settlement.domain.LevelingSeatSettlementDomainService;
import no.valg.eva.admin.settlement.domain.model.LevelingSeat;
import no.valg.eva.admin.settlement.repository.LevelingSeatSettlementRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.valg.eva.admin.common.settlement.model.LevelingSeatSettlementSummary.Status.DONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class LevelingSeatSettlementApplicationServiceTest extends MockUtilsTestCase {
	private static final ElectionPath ELECTION_EVENT_PATH = ElectionPath.from("111111");

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@leveling_seats.error.settlement_not_done")
	public void distributeLevelingSeats_givenElectionNotReady_throwsException() throws Exception {
		LevelingSeatSettlementApplicationService service = initializeMocks(LevelingSeatSettlementApplicationService.class);
		Election election = election();
		MvElection mvElection = mvElection(election);

		when(getInjectMock(MvElectionRepository.class).findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION)).thenReturn(singletonList(mvElection));
		when(getInjectMock(LevelingSeatSettlementRepository.class).areAllSettlementsInElectionFinished(election)).thenReturn(false);

		service.distributeLevelingSeats(userData());
	}

	@Test
	public void distributeLevelingSeats_givenElectionReady_distributesLevelingSeatsAndReturnsResult() throws Exception {
		LevelingSeatSettlementApplicationService service = initializeMocks(LevelingSeatSettlementApplicationService.class);
		LevelingSeatSettlementRepository levelingSeatSettlementRepository = getInjectMock(LevelingSeatSettlementRepository.class);
		UserData userData = userData();
		Election election = election();
		MvElection mvElection = mvElection(election);
		List<LevelingSeat> levelingSeatEntities = createListMock();
		List<no.valg.eva.admin.common.settlement.model.LevelingSeat> levelingSeatDtos = createListMock();

		when(getInjectMock(MvElectionRepository.class).findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION)).thenReturn(singletonList(mvElection));
		when(levelingSeatSettlementRepository.areAllSettlementsInElectionFinished(election)).thenReturn(true);
		when(levelingSeatSettlementRepository.findLevelingSeatsByElection(election)).thenReturn(levelingSeatEntities);
		when(getInjectMock(LevelingSeatMapper.class).levelingSeats(levelingSeatEntities)).thenReturn(levelingSeatDtos);

		LevelingSeatSettlementSummary levelingSeatSettlementSummary = service.distributeLevelingSeats(userData);
		verify(getInjectMock(LevelingSeatSettlementDomainService.class)).distributeLevelingSeats(userData, election);
		assertThat(levelingSeatSettlementSummary).isEqualToComparingFieldByField(new LevelingSeatSettlementSummary(DONE, levelingSeatDtos));
	}

	private Election election() {
		return election(true);
	}

	private Election election(boolean hasLevelingSeats) {
		Election election = createMock(Election.class);
		when(election.hasLevelingSeats()).thenReturn(hasLevelingSeats);
		return election;
	}

	private MvElection mvElection(Election election) {
		MvElection mvElection = createMock(MvElection.class);
		when(mvElection.getElection()).thenReturn(election);
		return mvElection;
	}

	private MvElection mvElection() {
		Election election = election();
		MvElection mvElection = createMock(MvElection.class);
		when(mvElection.getElection()).thenReturn(election);
		return mvElection;
	}

	public UserData userData() {
		UserData userData = createMock(UserData.class);
		when(userData.getOperatorElectionPath()).thenReturn(ELECTION_EVENT_PATH);
		return userData;
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@leveling_seats.error.missing_election")
	public void levelingSeatSettlementSummary_givenNoElectionWithLevelingSeats_throwsException() throws Exception {
		LevelingSeatSettlementApplicationService service = initializeMocks(LevelingSeatSettlementApplicationService.class);
		MvElection mvElection = mvElection(election(false));

		when(getInjectMock(MvElectionRepository.class).findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION)).thenReturn(singletonList(mvElection));

		service.levelingSeatSettlementSummary(userData());
	}

	@Test
	public void levelingSeatSettlementSummary_withNotDoneAndAllSettlementsFinished_returnsReadyState() throws Exception {
		LevelingSeatSettlementApplicationService service = initializeMocks(LevelingSeatSettlementApplicationService.class);
		stub_findLevelingSeatsByElection(new ArrayList<>());
		stub_areAllSettlementsInElectionFinished(true);
		MvElection mvElection = mvElection();

		when(getInjectMock(MvElectionRepository.class).findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION)).thenReturn(singletonList(mvElection));

		LevelingSeatSettlementSummary summary = service.levelingSeatSettlementSummary(userData());

		assertThat(summary.getStatus()).isSameAs(LevelingSeatSettlementSummary.Status.READY);
		assertThat(summary.getLevelingSeats()).isNull();
	}

	@Test
	public void levelingSeatSettlementSummary_withNotDoneAndNotAllSettlementsFinished_returnsNotReadyState() throws Exception {
		LevelingSeatSettlementApplicationService service = initializeMocks(LevelingSeatSettlementApplicationService.class);
		stub_findLevelingSeatsByElection(new ArrayList<>());
		stub_areAllSettlementsInElectionFinished(false);
		MvElection mvElection = mvElection();

		when(getInjectMock(MvElectionRepository.class).findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION)).thenReturn(singletonList(mvElection));

		LevelingSeatSettlementSummary summary = service.levelingSeatSettlementSummary(userData());

		assertThat(summary.getStatus()).isSameAs(LevelingSeatSettlementSummary.Status.NOT_READY);
		assertThat(summary.getLevelingSeats()).isNull();
	}

	private void stub_findLevelingSeatsByElection(List<LevelingSeat> list) {
		when(getInjectMock(LevelingSeatSettlementRepository.class).findLevelingSeatsByElection(any(Election.class))).thenReturn(list);
	}

	private void stub_areAllSettlementsInElectionFinished(boolean response) {
		when(getInjectMock(LevelingSeatSettlementRepository.class).areAllSettlementsInElectionFinished(any(Election.class))).thenReturn(response);
	}
}

