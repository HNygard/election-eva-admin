package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_ORDINAER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ForhandOrdinaerControllerTest extends BaseFrontendTest {

	@Test
	public void getKontekstVelgerOppsett_verifiserOppsett() throws Exception {
		ForhandOrdinaerController ctrl = initializeMocks(ForhandOrdinaerController.class);

		KontekstvelgerOppsett oppsett = ctrl.getKontekstVelgerOppsett();

		assertThat(oppsett.serialize()).isEqualTo("[hierarki|nivaer|1][geografi|nivaer|6|filter|FORHAND_ORDINAERE]");
	}

	@Test
	public void getStemmestedNiva_returnererStemmested() throws Exception {
		ForhandOrdinaerController ctrl = initializeMocks(ForhandOrdinaerController.class);

		assertThat(ctrl.getStemmestedNiva()).isSameAs(STEMMESTED);

	}

	@Test
	public void getStemmegivningsType_returnererOrdinaere() throws Exception {
		ForhandOrdinaerController ctrl = initializeMocks(ForhandOrdinaerController.class);

		assertThat(ctrl.getStemmegivningsType()).isSameAs(FORHANDSSTEMME_ORDINAER);
	}

	@Test(dataProvider = "registrerStemmegivning")
	public void registrerStemmegivning_medDataProvider_verifisertForventet(
			MvArea stemmested, boolean isForhandsstemmeRettIUrne, boolean isRegistrerStemmegivningUrne, boolean isRegistrerStemmegivningKonvolutt)
			throws Exception {

		ThisForhandOrdinaerController ctrl = initializeMocks(new ThisForhandOrdinaerController());
		stub_isForhandsstemmeRettIUrne(stemmested, isForhandsstemmeRettIUrne);

		ctrl.registrerStemmegivning();

		assertThat(ctrl.isRegistrerStemmegivningUrne()).isSameAs(isRegistrerStemmegivningUrne);
		assertThat(ctrl.isRegistrerStemmegivningKonvolutt()).isSameAs(isRegistrerStemmegivningKonvolutt);
	}

	@DataProvider
	public Object[][] registrerStemmegivning() {
		return new Object[][] {
				{ createMock(MvArea.class), true, true, false },
				{ createMock(MvArea.class), false, false, true }
		};
	}

	@Test(dataProvider = "tittel")
	public void tittel_medDataProvider_verifisertForventet(
			MvArea stemmested, boolean isForhandsstemmeRettIUrne, String forventet)
			throws Exception {

		ThisForhandOrdinaerController ctrl = initializeMocks(new ThisForhandOrdinaerController());
		stub_isForhandsstemmeRettIUrne(stemmested, isForhandsstemmeRettIUrne);

		assertThat(ctrl.getTittel()).isEqualTo(forventet);
	}

	@DataProvider
	public Object[][] tittel() {
		return new Object[][] {
				{ null, false, "" },
				{ createMock(MvArea.class), false, "@voting.searchAdvance.header Stemmested" },
				{ createMock(MvArea.class), true, "@voting.searchAdvanceBallotBox.header Stemmested" }
		};
	}

	private void stub_isForhandsstemmeRettIUrne(MvArea stemmested, boolean isForhandsstemmeRettIUrne) throws Exception {
		mockFieldValue("stemmested", stemmested);
		if (stemmested != null) {
			when(stemmested.getPollingPlace().getName()).thenReturn("Stemmested");
			when(stemmested.getPollingPlace().isAdvanceVoteInBallotBox()).thenReturn(isForhandsstemmeRettIUrne);
		}
	}

	private static class ThisForhandOrdinaerController extends ForhandOrdinaerController {
		private boolean registrerStemmegivningUrne;
		private boolean registrerStemmegivningKonvolutt;

		@Override
		void registrerStemmegivningUrne() {
			registrerStemmegivningUrne = true;
		}

		@Override
        protected void registrerStemmegivningKonvolutt() {
			registrerStemmegivningKonvolutt = true;
		}

		public boolean isRegistrerStemmegivningUrne() {
			return registrerStemmegivningUrne;
		}

		public boolean isRegistrerStemmegivningKonvolutt() {
			return registrerStemmegivningKonvolutt;
		}
	}

}
