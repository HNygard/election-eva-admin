package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.evote.service.voting.VotingService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_ORDINAER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ForhandRegistreringControllerTest extends BaseFrontendTest {

	@Test
	public void slettForhandsstemme_medStemmegivningFA_verifiserSlettingOgMelding() throws Exception {
		ForhandRegistreringController ctrl = initializeMocks(new ThisForhandRegistreringController());
		ctrl.setStemmegivning(stemmegivning(velger(true), FA));

		ctrl.slettForhandsstemme();

		verify(getInjectMock(VotingService.class)).delete(eq(getUserDataMock()), anyLong());
		assertFacesMessage(SEVERITY_INFO, "[@voting.requestRemoveAdvanceVotingFA.response, @person.fictitiousVoterNameLine]");
	}

	@Test
	public void slettForhandsstemme_medStemmegivningVO_verifiserSlettingOgMelding() throws Exception {
		ForhandRegistreringController ctrl = initializeMocks(new ThisForhandRegistreringController());
		ctrl.setStemmegivning(stemmegivning(velger(false), VO));

		ctrl.slettForhandsstemme();

		verify(getInjectMock(VotingService.class)).delete(eq(getUserDataMock()), anyLong());
		assertFacesMessage(SEVERITY_INFO, "[@voting.requestRemoveAdvanceVoting.response, VO, 0, Test Testesen]");
	}

	@Test
	public void isForhandsstemmeRettIUrne_medRettIUrne_returnererTrue() throws Exception {
		ForhandRegistreringController ctrl = initializeMocks(new ThisForhandRegistreringController());
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue();
		when(mvArea.getPollingPlace().isAdvanceVoteInBallotBox()).thenReturn(true);
		mockFieldValue("stemmested", mvArea);

		assertThat(ctrl.isForhandsstemmeRettIUrne()).isTrue();
	}

	@Test
	public void registrerStemmegivningUrne_medEgenKommune_verifiserMelding() throws Exception {
		ForhandRegistreringController ctrl = initializeMocks(new ThisForhandRegistreringController());
		ctrl.setVelger(velger(true));
		ctrl.setStemmested(stemmested());
		mockField("valgGruppe", MvElection.class);
		stub_markOffVoterAdvanceVoteInBallotBox(stemmegivning(ctrl.getVelger(), FA));

		ctrl.registrerStemmegivningUrne();

		assertFacesMessage(SEVERITY_INFO,
				"[@voting.markOff.voterMarkedOff, @person.fictitiousVoterNameLine, @common.date.weekday[1].name, 01.01.2017, 12:12, FA, 0]");
	}

	@Test
	public void registrerStemmegivningUrne_medAnnenKommune_verifiserMelding() throws Exception {
		ForhandRegistreringController ctrl = initializeMocks(new ThisForhandRegistreringController(false));
		ctrl.setVelger(velger(false));
		ctrl.setStemmested(stemmested());
		mockField("valgGruppe", MvElection.class);
		stub_markOffVoterAdvanceVoteInBallotBox(stemmegivning(ctrl.getVelger(), FA));

		ctrl.registrerStemmegivningUrne();

		assertFacesMessage(SEVERITY_INFO, "[@voting.markOff.voterMarkedOffAdvance, Test Testesen, @common.date.weekday[1].name, 01.01.2017, 12:12, FA, 0] "
				+ "@voting.markOff.advanceForeignEnvelope");
	}

	@Test
	public void registrerStemmegivningKonvolutt_medFA_verifiserKryss() throws Exception {
		ForhandRegistreringController ctrl = ctrlForsettKryssIManntall(FA);

		ctrl.registrerStemmegivningKonvolutt();

		assertFacesMessage(SEVERITY_INFO, "[@voting.markOff.voterMarkedOffAdvance, null, @common.date.weekday[1].name, "
				+ "01.01.2017, 12:12, FA, 0] @voting.markOff.advanceForeignEnvelope");
	}

	@Test
	public void registrerStemmegivning_medFB_verifiserKryss() throws Exception {
		ForhandRegistreringController ctrl = ctrlForsettKryssIManntall(FB);

		ctrl.registrerStemmegivningKonvolutt();

		assertFacesMessage(SEVERITY_INFO,
				"[@voting.markOff.voterMarkedOffAdvance, null, @common.date.weekday[1].name, 01.01.2017, 12:12, FB, 0] [@voting.markOff.votingNumberEnvelope, FB, 0]");
	}

	private Voting stemmegivning(Voter velger, VotingCategory kategori) {
		Voting stemmegivning = createMock(Voting.class);
		when(stemmegivning.getVoter()).thenReturn(velger);
		when(stemmegivning.getVotingCategory().getId()).thenReturn(kategori.getId());
		when(stemmegivning.getCastTimestamp()).thenReturn(DateTime.parse("2017-01-01T12:12:00"));
		when(stemmegivning.getCastTimeStampAsJavaTime()).thenCallRealMethod();
		return stemmegivning;
	}

	private Voter velger(boolean fictious) {
		Voter velger = createMock(Voter.class);
		when(velger.isFictitious()).thenReturn(fictious);
		when(velger.getNameLine()).thenReturn(fictious ? "fictious" : "Test Testesen");
		return velger;
	}

	private ForhandRegistreringController ctrlForsettKryssIManntall(VotingCategory kategori) throws Exception {
		ForhandRegistreringController ctrl = initializeMocks(new ThisForhandRegistreringController());
		ctrl.setVelger(mockField("velger", Voter.class));
		ctrl.setStemmested(stemmested());
		mockField("valgGruppe", MvElection.class);
		stub_markOffVoterAdvance(stemmegivning(ctrl.getVelger(), kategori));
		return ctrl;
	}

	private void stub_markOffVoterAdvance(Voting stemmegivning) {
		when(getInjectMock(VotingService.class).markOffVoterAdvance(
				eq(getUserDataMock()),
				any(PollingPlace.class),
				any(ElectionGroup.class),
				any(Voter.class),
				anyBoolean(),
                any(),
                any(),
                any(VotingPhase.class)))
				.thenReturn(stemmegivning);
	}

	private void stub_markOffVoterAdvanceVoteInBallotBox(Voting stemmegivning) {
		when(getInjectMock(VotingService.class).markOffVoterAdvanceVoteInBallotBox(
				eq(getUserDataMock()),
				any(PollingPlace.class),
				any(ElectionGroup.class),
				any(Voter.class),
				anyBoolean(), any(VotingPhase.class))).thenReturn(stemmegivning);
	}

	private MvArea stemmested() {
		return new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue();
	}

	private static class ThisForhandRegistreringController extends ForhandRegistreringController {
		private ValggeografiNivaa valggeografiNivaa;
		private StemmegivningsType stemmegivningsType;
		private boolean velgerEgenKommune;

		ThisForhandRegistreringController() {
			this(true);
		}

		ThisForhandRegistreringController(boolean velgerEgenKommune) {
			this(STEMMESTED, FORHANDSSTEMME_ORDINAER, velgerEgenKommune);
		}

		ThisForhandRegistreringController(ValggeografiNivaa valggeografiNivaa, StemmegivningsType stemmegivningsType, boolean velgerEgenKommune) {
			this.valggeografiNivaa = valggeografiNivaa;
			this.stemmegivningsType = stemmegivningsType;
			this.velgerEgenKommune = velgerEgenKommune;
		}

		@Override
		public ValggeografiNivaa getStemmestedNiva() {
			return valggeografiNivaa;
		}

		@Override
		public void kontekstKlar() {
		}

		@Override
		public StemmegivningsType getStemmegivningsType() {
			return stemmegivningsType;
		}

		@Override
		public void registrerStemmegivning() {
		}

		@Override
		public boolean isVelgerEgenKommune() {
			return velgerEgenKommune;
		}

		@Override
		String timeString(DateTime dateTime) {
			return "12:12";
		}
	}

}
