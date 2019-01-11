package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.evote.service.voting.VotingService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.VALGTINGSTEMME_ORDINAER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ValgtingOrdinaerControllerTest extends BaseFrontendTest {

	@Test
	public void getStemmegivningsType_returnererValgtingstemmeOrdinaer() throws Exception {
		ValgtingOrdinaerController ctrl = initializeMocks(ValgtingOrdinaerController.class);

		assertThat(ctrl.getStemmegivningsType()).isSameAs(VALGTINGSTEMME_ORDINAER);
	}

	@Test
	public void getStemmestedNiva_returnererStemmested() throws Exception {
		ValgtingOrdinaerController ctrl = initializeMocks(ValgtingOrdinaerController.class);

		assertThat(ctrl.getStemmestedNiva()).isSameAs(STEMMESTED);
	}

	@Test
	public void getKontekstVelgerOppsett_verifiserOppsett() throws Exception {
		ValgtingOrdinaerController ctrl = initializeMocks(ValgtingOrdinaerController.class);

		KontekstvelgerOppsett oppsett = ctrl.getKontekstVelgerOppsett();

		assertThat(oppsett.serialize()).isEqualTo("[hierarki|nivaer|1][geografi|nivaer|6|filter|VALGTING_ORDINAERE]");
	}

	@Test
	public void registrerStemmegivning_utenElektroniskManntall_returnererFeilmelding() throws Exception {
		ValgtingOrdinaerController ctrl = initializeMocks(ValgtingOrdinaerController.class);
		mockFieldValue("stemmested", stemmested(false));

		ctrl.registrerStemmegivning();

		assertFacesMessage(SEVERITY_ERROR, "@voting.markOff.noElectronicVotes");
	}

	@Test
	public void registrerStemmegivning_medElektroniskManntall_registrererValgtingStemme() throws Exception {
		ValgtingOrdinaerController ctrl = initializeMocks(new ThisValgtingOrdinaerController());
		mockFieldValue("stemmested", stemmested(true));
		mockFieldValue("velger", velger());
		mockField("valgGruppe", MvElection.class);
		stub_markOffVoter();

		ctrl.registrerStemmegivning();

		assertFacesMessage(SEVERITY_INFO, "[@voting.markOff.voterMarkedOff, Test Testesen, @common.date.weekday[1].name, 01.01.2017, 12:12, VS, 1]");
	}

	@Test
    public void getTittel_with_should() {

	}

	private Voter velger() {
		Voter velger = new Voter();
		velger.setFictitious(false);
		velger.setNameLine("Test Testesen");
		velger.setMunicipalityId(AREA_PATH_MUNICIPALITY.getLeafId());
		velger.setPollingDistrictId(AREA_PATH_POLLING_DISTRICT.getLeafId());
		return velger;
	}

	private MvArea stemmested(boolean elektroniskManntall) {
		MvArea stemmested = new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue();
		when(stemmested.getMunicipality().isElectronicMarkoffs()).thenReturn(elektroniskManntall);
		return stemmested;
	}

	private void stub_markOffVoter() {
		Voting stemmegivning = stemmegivning();
		when(getInjectMock(VotingService.class).markOffVoter(
				eq(getUserDataMock()),
				any(PollingPlace.class),
				any(ElectionGroup.class),
				any(Voter.class),
				anyBoolean(), any(VotingPhase.class))).thenReturn(stemmegivning);
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

	private static class ThisValgtingOrdinaerController extends ValgtingOrdinaerController {
		@Override
		String timeString(DateTime dateTime) {
			return "12:12";
		}
	}

}
