package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.service.VotingRegistrationService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.VALGTINGSTEMME_KONVOLUTTER_SENTRALT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ValgtingKonvolutterSentraltControllerTest extends BaseFrontendTest {

	@Test
	public void getStemmestedNiva_returnererKommune() throws Exception {
		ValgtingKonvolutterSentraltController ctrl = initializeMocks(ValgtingKonvolutterSentraltController.class);

		assertThat(ctrl.getStemmestedNiva()).isSameAs(KOMMUNE);
	}

	@Test
	public void getStemmegivningsType_returnererValgtingKonvSentralt() throws Exception {
		ValgtingKonvolutterSentraltController ctrl = initializeMocks(ValgtingKonvolutterSentraltController.class);

		assertThat(ctrl.getStemmegivningsType()).isSameAs(VALGTINGSTEMME_KONVOLUTTER_SENTRALT);
	}

	@Test
	public void registrerStemmegivning_medVelger_avgirSentralStemme() throws Exception {
		ValgtingKonvolutterSentraltController ctrl = initializeMocks(new ThisValgtingKonvolutterSentraltController());
		mockField("valgGruppe", MvElection.class);
		mockFieldValue("velger", velger());
        mockFieldValue("stemmetype", "VO");
        mockField("stemmested", MvArea.class);
		stub_registerVoteCentrally();

		ctrl.registrerStemmegivning();

		assertFacesMessage(SEVERITY_INFO, "[@voting.markOff.registerVoteCentrally[VS], Test Testesen, @common.date.weekday[1].name, 01.01.2017, 12:12, VS, 1]");
	}

	private void stub_registerVoteCentrally() {
		when(getInjectMock(VotingRegistrationService.class).registerElectionDayVotingInEnvelopeCentrally(eq(getUserDataMock()), any(ElectionGroup.class), any(Municipality.class), any(Voter.class), 
				any(no.valg.eva.admin.common.voting.VotingCategory.class), any(VotingPhase.class)))
				.thenReturn(stemmegivning());
	}

	private Voting stemmegivning() {
		Voting stemmegivning = new Voting();
		stemmegivning.setCastTimestamp(DateTime.parse("2017-01-01T12:12:00"));
		stemmegivning.setVotingNumber(1);
		VotingCategory vc = new VotingCategory();
		vc.setId(VS.getId());
		stemmegivning.setVotingCategory(vc);
		return stemmegivning;
	}

	private Voter velger() {
		Voter velger = new Voter();
		velger.setFictitious(false);
		velger.setNameLine("Test Testesen");
		return velger;
	}

	private static class ThisValgtingKonvolutterSentraltController extends ValgtingKonvolutterSentraltController {
		@Override
		String timeString(DateTime dateTime) {
			return "12:12";
		}
	}

}
