package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.evote.security.UserData;
import no.evote.util.MockUtils;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.configuration.application.ElectionMapper;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ElectionEventDomainServiceMockTest extends MockUtilsTestCase {
	private static final String AN_ELECTION_EVENT_ID = "150001";
	private static final long AN_ELECTION_EVENT_PK = 2;
	private static final String ROOT_ELECTION_EVENT_ID = "000000";
	private static final long ROOT_ELECTION_EVENT_PK = 1L;

	@Test
	public void create_whenCopyVotings_manualContestVotingsAreNotCopied() throws Exception {
		ElectionEventDomainService electionEventServiceBean = initializeMocks(ElectionEventDomainService.class);
		mockField("refreshResourceBundlesEvent", Event.class);

		UserData userData = createMock(UserData.class);
		ElectionEvent electionEventTo = getElectionEventMock(AN_ELECTION_EVENT_ID, AN_ELECTION_EVENT_PK);
		when(getInjectMock(ElectionEventRepository.class).create(userData, electionEventTo)).thenReturn(electionEventTo);
		ElectionEvent electionEventFrom = getElectionEventMock(ROOT_ELECTION_EVENT_ID, ROOT_ELECTION_EVENT_PK);

		electionEventServiceBean.create(userData, electionEventTo, false, VotingHierarchy.VOTING, CountingHierarchy.NONE,
				electionEventFrom, new HashSet<>());

		verify(getInjectMock(ElectionEventRepository.class), never()).copyManualVoting(any(), any());
	}

	@Test
	public void create_whenCopyCountings_manualContestVotingsAreCopied() throws Exception {
		ElectionEventDomainService electionEventServiceBean = initializeMocks(ElectionEventDomainService.class);
		mockField("refreshResourceBundlesEvent", Event.class);

		UserData userData = createMock(UserData.class);
		ElectionEvent electionEventTo = getElectionEventMock(AN_ELECTION_EVENT_ID, AN_ELECTION_EVENT_PK);
		when(getInjectMock(ElectionEventRepository.class).create(userData, electionEventTo)).thenReturn(electionEventTo);
		ElectionEvent electionEventFrom = getElectionEventMock(ROOT_ELECTION_EVENT_ID, ROOT_ELECTION_EVENT_PK);

		electionEventServiceBean.create(userData, electionEventTo, false, VotingHierarchy.VOTING, CountingHierarchy.COUNTING,
				electionEventFrom, new HashSet<>());

		verify(getInjectMock(ElectionEventRepository.class), times(1)).copyManualVoting(any(), any());
	}

	@Test
	public void findElectionDaysByElectionEvent_withPk_returnsSortedResult() throws Exception {
		ElectionEventDomainService bean = initializeMocks(ElectionEventDomainService.class);
		MockUtils.setPrivateField(bean, "electionMapper", new ElectionMapper(null, null), true);
		List<no.valg.eva.admin.configuration.domain.model.ElectionDay> stubbed = stub_findElectionDaysByElectionEvent();

		List<ElectionDay> result = bean.findElectionDaysByElectionEvent(new ElectionEvent());

		assertThat(result).hasSize(3);
		assertThat(result.get(0).getDate()).isEqualTo(stubbed.get(1).getDate());
		assertThat(result.get(1).getDate()).isEqualTo(stubbed.get(2).getDate());
		assertThat(result.get(2).getDate()).isEqualTo(stubbed.get(0).getDate());
	}

	private ElectionEvent getElectionEventMock(String electionEventId, long electionEventPk) {
		ElectionEvent electionEvent = createMock(ElectionEvent.class);
		when(electionEvent.getId()).thenReturn(electionEventId);
		when(electionEvent.getPk()).thenReturn(electionEventPk);
		return electionEvent;
	}

	private List<no.valg.eva.admin.configuration.domain.model.ElectionDay> stub_findElectionDaysByElectionEvent() {
		List<no.valg.eva.admin.configuration.domain.model.ElectionDay> result = new ArrayList<>();
		result.add(electionDay("04012016"));
		result.add(electionDay("01012016"));
		result.add(electionDay("03012016"));
		when(getInjectMock(ElectionEventRepository.class).findElectionDaysByElectionEvent(any())).thenReturn(result);
		return result;
	}

	private no.valg.eva.admin.configuration.domain.model.ElectionDay electionDay(String date) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("ddMMyyyy");
		no.valg.eva.admin.configuration.domain.model.ElectionDay result = new no.valg.eva.admin.configuration.domain.model.ElectionDay();
		result.setDate(fmt.parseLocalDate(date));
		result.setStartTime(new LocalTime());
		result.setEndTime(new LocalTime());
		return result;
	}

}

