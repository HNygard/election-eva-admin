package no.valg.eva.admin.counting.domain.service.votecount;

import no.valg.eva.admin.common.counting.model.AbstractCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.event.TellingEndrerStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VoteCountStatusendringTriggerTest {

	private Event<TellingEndrerStatus> tellingEndrerStatusEvent;
	private VoteCountStatusendringTrigger trigger;

	@DataProvider
	public static Object[][] statusSkalRapporteresTilMedia() {
		return new Object[][] {
				{ CountStatus.APPROVED },
				{ CountStatus.TO_SETTLEMENT },
				{ CountStatus.REVOKED }
		};
	}

	@DataProvider
	public static Object[][] statusSkalIkkeRapporteresTilMedia() {
		return new Object[][] {
				{ CountStatus.NEW },
				{ CountStatus.SAVED }
		};
	}

	@DataProvider
	public static Object[][] categorySkalRapporteresTilMedia() {
		return new Object[][] {
				{ CountCategory.VO },
				{ CountCategory.FO }
		};
	}

	@DataProvider
	public static Object[][] categorySkalIkkeRapporteresTilMedia() {
		return new Object[][] {
				{ CountCategory.VF },
				{ CountCategory.VS },
				{ CountCategory.VB },
				{ CountCategory.FS },
				{ CountCategory.BF }
		};
	}

	@BeforeMethod
	public void setUp() {
		// noinspection unchecked
		tellingEndrerStatusEvent = mock(Event.class);
		trigger = new VoteCountStatusendringTrigger(tellingEndrerStatusEvent);
	}

	@Test(dataProvider = "statusSkalRapporteresTilMedia")
    public void fireEventForStatusendring_countStatus_eventFired(CountStatus countStatus) {
		AbstractCount count = preliminaryCount(countStatus, CountCategory.VO);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent).fire(any(TellingEndrerStatus.class));
	}

	private AbstractCount preliminaryCount(CountStatus status, CountCategory category) {
		PreliminaryCount preliminaryCount = new PreliminaryCount("", null, category, "", "", true);
		preliminaryCount.setStatus(status);
		return preliminaryCount;
	}

	@Test(dataProvider = "statusSkalIkkeRapporteresTilMedia")
    public void fireEventForStatusendring_countStatus_eventNotFired(CountStatus countStatus) {
		AbstractCount count = preliminaryCount(countStatus, CountCategory.VO);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent, never()).fire(any(TellingEndrerStatus.class));
	}

	@Test(dataProvider = "categorySkalRapporteresTilMedia")
    public void fireEventForStatusendring_countCategory_eventFired(CountCategory countCategory) {
		AbstractCount count = preliminaryCount(CountStatus.APPROVED, countCategory);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent).fire(any(TellingEndrerStatus.class));
	}

	@Test(dataProvider = "categorySkalIkkeRapporteresTilMedia")
    public void fireEventForStatusendring_countCategory_eventNotFired(CountCategory countCategory) {
		AbstractCount count = preliminaryCount(CountStatus.APPROVED, countCategory);
		MvElection mvElectionMock = mock(MvElection.class);
		Contest contestMock = mock(Contest.class);
		when(mvElectionMock.getSingleArea()).thenReturn(true);
		when(mvElectionMock.getContest()).thenReturn(contestMock);
		when(contestMock.isOnBoroughLevel()).thenReturn(false);

		trigger.fireEventForStatusendring(count, mock(MvArea.class), mvElectionMock, CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent, never()).fire(any(TellingEndrerStatus.class));
	}

	@Test(dataProvider = "categorySkalIkkeRapporteresTilMedia")
    public void fireEventForStatusendring_countCategoryOgFinalCount_eventFired(CountCategory countCategory) {
		AbstractCount count = finalCount(CountStatus.APPROVED, countCategory);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent).fire(any(TellingEndrerStatus.class));
	}

	@Test
	public void fireEventForStatusendring_bydelsvalg_eventNotFired() {
		MvElection fakeMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(fakeMvElection.getContest().isOnBoroughLevel()).thenReturn(true);
		AbstractCount count = finalCount(CountStatus.APPROVED, CountCategory.FO);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), fakeMvElection, CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent, never()).fire(any(TellingEndrerStatus.class));
	}
	
	private AbstractCount finalCount(CountStatus status, CountCategory countCategory) {
		FinalCount finalCount = new FinalCount("", null, countCategory, "", "", true, null);
		finalCount.setStatus(status);
		return finalCount;
	}

	@Test
    public void fireEventForStatusendring_preliminaryCountQualifier_eventFired() {
		AbstractCount count = preliminaryCount(CountStatus.APPROVED, CountCategory.VO);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent).fire(any(TellingEndrerStatus.class));
	}

	@Test
    public void fireEventForStatusendring_finalCountQualifier_eventFired() {
		AbstractCount count = finalCount(CountStatus.APPROVED, CountCategory.VO);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent).fire(any(TellingEndrerStatus.class));
	}

	@Test
    public void fireEventForStatusendring_finalCountQualifierOgApprovedToApproved_eventNotFired() {
		AbstractCount count = finalCount(CountStatus.APPROVED, CountCategory.VO);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.APPROVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent, never()).fire(any(TellingEndrerStatus.class));
	}

	@Test
    public void fireEventForStatusendring_protocolCountQualifier_eventNotFired() {
		AbstractCount count = protocolCount();
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.SAVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent, never()).fire(any(TellingEndrerStatus.class));
	}

	private AbstractCount protocolCount() {
		return new ProtocolCount("", null, "", "", true);
	}

	@Test
    public void fireEventForStatusendring_oldCountStatusFromApprovedToSettlement_eventNotFired() {
		AbstractCount count = finalCount(CountStatus.TO_SETTLEMENT, CountCategory.VO);
		trigger.fireEventForStatusendring(count, mock(MvArea.class), mock(MvElection.class, RETURNS_DEEP_STUBS), CountStatus.APPROVED, VALGSTYRET);
		verify(tellingEndrerStatusEvent, never()).fire(any(TellingEndrerStatus.class));
	}
}
