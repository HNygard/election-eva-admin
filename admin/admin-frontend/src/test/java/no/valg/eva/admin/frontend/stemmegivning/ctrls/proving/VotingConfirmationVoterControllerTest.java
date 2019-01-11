package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import no.evote.service.configuration.VoterService;
import no.evote.service.voting.VotingRejectionService;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static no.valg.eva.admin.common.AreaPath.from;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.common.ctrls.RedirectInfo.REDIRECT_INFO_SESSION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VotingConfirmationVoterControllerTest extends BaseFrontendTest {

	private static final String THIS_MUNICIPALITY_AREA_PATH = "123456.47.01.0101.010100.0001.0101";
	private static final String OTHER_MUNICIPALITY_AREA_PATH = "123456.47.02.0202.020200.0001.0101";
	
	@Test
	public void getStemmestedNiva_returnsMunicipality() throws Exception {
		VotingConfirmationVoterController ctrl = ctrl();

		assertThat(ctrl.getStemmestedNiva()).isSameAs(KOMMUNE);
	}

	@Test
	public void kontekstKlar_withRedirectInfo_initializesWithVoter() throws Exception {
		RedirectInfo redirectInfo = new RedirectInfo("1", "", "");
		ThisVotingConfirmationVoterController ctrl = ctrl(redirectInfo);
        stub_findByElectionEventAndId(singletonList(voter()));

		assertThat(ctrl.isManntallsSokVelgerInvocation()).isTrue();
	}

	@Test(dataProvider = "manntallsSokVelgerTestData")
	public void manntallsSokVelger_givenVotings_doesAsExpected(List<Voting> votings, FacesMessage.Severity expectedMessageSeverity, String expectedMessage,
																	  boolean expectedCanApproveVotings, boolean expectedVoterIsNull) throws Exception {
		ThisVotingConfirmationVoterController ctrl = ctrl();
		Voter voter = voter();
		stub_getVotingsByElectionGroupAndVoter(votings);

		MvArea pollingPlace = mockField("stemmested", MvArea.class);
		when(pollingPlace.getMunicipality().isElectronicMarkoffs()).thenReturn(false);
		when(pollingPlace.areaPath()).thenReturn(from(THIS_MUNICIPALITY_AREA_PATH));

		ctrl.manntallsSokVelger(voter);
		
		if (expectedMessageSeverity != null) {
			assertFacesMessage(expectedMessageSeverity, expectedMessage);
		}
		assertThat(ctrl.getVelger() == null).isEqualTo(expectedVoterIsNull);
		assertThat(ctrl.isCanApproveVotings(createMock(Voting.class))).isEqualTo(expectedCanApproveVotings);
	}
	
	@DataProvider
	private Object[][] manntallsSokVelgerTestData() {
		return new Object[][] {
			{ votings(), SEVERITY_WARN, "@voting.approveBallot.acceptedVoting", false, false },
			{ votingsAlsoFromOtherMunicipality(true), SEVERITY_WARN, "@voting.approveBallot.acceptedVoting", false, false },
			{ votingsAlsoFromOtherMunicipality(false), null, null, false, false },
			{ emptyList(), SEVERITY_INFO, "[@voting.verifyBallot.noBallots, Test Testesen]", false, true },
		};
	}
	
	@Test
	public void prepareRedirect_addsInfoToSession() throws Exception {
		ThisVotingConfirmationVoterController ctrl = ctrl();
		RedirectInfo redirectInfo = mockField("redirectInfo", RedirectInfo.class);
		when(redirectInfo.getUrl()).thenReturn("/my/url");

		assertThat(ctrl.prepareRedirect()).isEqualTo("/my/url?faces-redirect=true");
		verify(getServletContainer().getHttpSessionMock()).setAttribute(REDIRECT_INFO_SESSION_KEY, redirectInfo);
	}

	@Test(dataProvider = "isCanApproveVotingsTestData")
	public void isCanApproveVotings(boolean isElectronicMarkoffs, Voting stemmegivning, boolean forventet)
			throws Exception {
		ThisVotingConfirmationVoterController ctrl = ctrl();
		mockFieldValue("canApproveVoting", true);

		MvArea pollingPlace = mockField("stemmested", MvArea.class);
		when(pollingPlace.getMunicipality().isElectronicMarkoffs()).thenReturn(isElectronicMarkoffs);
		when(pollingPlace.areaPath()).thenReturn(from(THIS_MUNICIPALITY_AREA_PATH));

		assertThat(ctrl.isCanApproveVotings(stemmegivning)).isEqualTo(forventet);
	}
	
	@DataProvider
	public Object[][] isCanApproveVotingsTestData() {
		return new Object[][] {
				{ true, null, true },
				{ false, voting(false, false), false },
				{ false, voting(true, true), false },
				{ false, voting(true, false), true }
		};
	}

	@Test
	public void approveVoting_withVoting_approvesVoting() throws Exception {
		ThisVotingConfirmationVoterController ctrl = ctrl();
		stub_getVotingsByElectionGroupAndVoter(votings());
		mockField("velger", Voter.class);
		mockField("valgGruppe", MvElection.class);

		ctrl.approveVoting(createMock(Voting.class));

		assertFacesMessage(SEVERITY_INFO, "@voting.approveBallot.votingApproved");
		assertThat(ctrl.isCanApproveVotings(createMock(Voting.class))).isFalse();
	}

	@Test
	public void setVotingToRejection_withVoting_fetchesRejectionReasons() throws Exception {
		ThisVotingConfirmationVoterController ctrl = ctrl();
		Voting voting = createMock(Voting.class);

		ctrl.setVotingToRejection(voting);

		verify(getInjectMock(VotingRejectionService.class)).findByEarly(getUserDataMock(), voting);
	}

	@Test
	public void rejectVoting_withVoting_verifyRejection() throws Exception {
		ThisVotingConfirmationVoterController ctrl = ctrl();
		Voting voting = mockField("voting", Voting.class);
		when(voting.getVotingRejection().getName()).thenReturn("Ugyldig");
		stub_getVotingsByElectionGroupAndVoter(votings());
		mockField("velger", Voter.class);
		mockField("valgGruppe", MvElection.class);
		mockFieldValue("votingRejections", new ArrayList<>());

		ctrl.rejectVoting();

        verify(voting).setVotingRejection(any());
		verify(voting).setValidationTimestamp(any(DateTime.class));
		verify(getInjectMock(VotingService.class)).update(getUserDataMock(), voting);
		assertFacesMessage(SEVERITY_INFO, "[@voting.approveBallot.votingRejected, Ugyldig]");
	}

	@Test
	public void cancelRejection_withVoting_verifyCancellation() throws Exception {
		ThisVotingConfirmationVoterController ctrl = ctrl();
		Voting voting = mockField("voting", Voting.class);
		stub_getVotingsByElectionGroupAndVoter(votings());
		mockFieldValue("velger", voter());

		ctrl.cancelRejection(voting);

		verify(voting).setVotingRejection(null);
		verify(voting).setValidationTimestamp(null);
		verify(getInjectMock(VotingService.class)).update(getUserDataMock(), voting);
		assertFacesMessage(SEVERITY_INFO, "[@voting.approveBallot.undoRejectionResponse, Test Testesen]");
	}

	private ThisVotingConfirmationVoterController ctrl() throws Exception {
		return ctrl(null);
	}

	private ThisVotingConfirmationVoterController ctrl(RedirectInfo redirectInfo) throws Exception {
		ThisVotingConfirmationVoterController controller = initializeMocks(new ThisVotingConfirmationVoterController(redirectInfo));
		Kontekst context = new Kontekst();
		context.setValghierarkiSti(ValghierarkiSti.fra(ELECTION_PATH_ELECTION_GROUP));
		context.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));
        stub_findByElectionEventAndId(singletonList(voter()));
		controller.initialized(context);
		return controller;
	}

	private void stub_findByElectionEventAndId(List<Voter> liste) {
		when(getInjectMock(VoterService.class).findByElectionEventAndId(
				eq(getUserDataMock()),
				anyString(),
				anyLong())).thenReturn(liste);
	}

	private void stub_getVotingsByElectionGroupAndVoter(List<Voting> liste) {
		when(getInjectMock(VotingService.class).getVotingsByElectionGroupAndVoter(
				eq(getUserDataMock()),
				anyLong(),
				anyLong())).thenReturn(liste);
	}

	private Voter voter() {
		Voter voter = createMock(Voter.class);
		when(voter.getNameLine()).thenReturn("Test Testesen");
		return voter;
	}

	private List<Voting> votings() {
		return asList(
				voting(false, null, 1, true),
				voting(true, null, 1, true),
				voting(false, createMock(VotingRejection.class), 1, true));
	}

	private List<Voting> votingsAlsoFromOtherMunicipality(boolean approved) {
		return asList(
			voting(false, null, 1, true),
			voting(approved, null, 1, false),
			voting(false, createMock(VotingRejection.class), 1, true));
	}

	private Voting voting(boolean eligible, VotingRejection votingRejection, Integer votingNumber, boolean fromSameMunicipality) {
		Voting voting = createMock(Voting.class);
		when(voting.isApproved()).thenReturn(eligible);
		when(voting.getVotingRejection()).thenReturn(votingRejection);
		when(voting.getVotingNumber()).thenReturn(votingNumber);
		if (fromSameMunicipality) {
			when(voting.getMvArea().getAreaPath()).thenReturn(THIS_MUNICIPALITY_AREA_PATH);
		} else {
			when(voting.getMvArea().getAreaPath()).thenReturn(OTHER_MUNICIPALITY_AREA_PATH);
		}
		return voting;
	}

	private Voting voting(boolean earlyVoting, boolean lateValidation) {
		Voting voting = createMock(Voting.class);
		when(voting.getVotingCategory().isEarlyVoting()).thenReturn(earlyVoting);
		when(voting.isLateValidation()).thenReturn(lateValidation);
		return voting;
	}

	private static class ThisVotingConfirmationVoterController extends VotingConfirmationVoterController {

		private RedirectInfo redirectInfoThis;
		private boolean manntallsSokVelgerInvocation;

		ThisVotingConfirmationVoterController(RedirectInfo redirectInfoThis) {
			this.redirectInfoThis = redirectInfoThis;
		}

		boolean isManntallsSokVelgerInvocation() {
			return manntallsSokVelgerInvocation;
		}

		@Override
		protected RedirectInfo getAndRemoveRedirectInfo() {
			return redirectInfoThis;
		}

		@Override
		public void manntallsSokVelger(Voter voter) {
			manntallsSokVelgerInvocation = true;
			super.manntallsSokVelger(voter);
		}
	}

}
