package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.ProvingSamletRedirectInfo;
import org.primefaces.event.SelectEvent;
import org.testng.annotations.Test;

public class ProvingSamletControllerTest extends BaseFrontendTest {

	@Test
	public void getKontekstVelgerOppsett_verifiserOppsett() throws Exception {
		ProvingSamletController ctrl = initializeMocks(new ThisProvingSamletController());

		KontekstvelgerOppsett oppsett = ctrl.getKontekstVelgerOppsett();

		assertThat(oppsett.serialize()).isEqualTo("[hierarki|nivaer|1][geografi|nivaer|3]");
	}

	@Test
	public void initialized_medRedirectInfo_verifiserInitialisering() throws Exception {
		ThisProvingSamletController ctrl = initializeMocks(new ThisProvingSamletController(createMock(ProvingSamletRedirectInfo.class)));
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(ValghierarkiSti.fra(ELECTION_PATH_ELECTION_GROUP));
		kontekst.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));
        mockFieldValue("pageURL", "/min/side.xhtml?a=b");

		ctrl.initialized(kontekst);

		assertThat(ctrl.getDenneSidenURL()).isEqualTo("/min/side.xhtml?a=b");
		assertThat(ctrl.getValgGruppe()).isNotNull();
		assertThat(ctrl.getKommune()).isNotNull();
		assertThat(ctrl.isFindVotingsCalled()).isTrue();
	}

	@Test
	public void isVisStemmestedListe_medForhandOgIkkeRettIUrne_returnererTrue() throws Exception {
		ThisProvingSamletController ctrl = initializeMocks(new ThisProvingSamletController());
		ctrl.setValgting(false);
		ElectionGroup valgGruppe = mockField("valgGruppe", ElectionGroup.class);
		when(valgGruppe.isAdvanceVoteInBallotBox()).thenReturn(false);

		assertThat(ctrl.isVisStemmestedListe()).isTrue();
	}

	private static class ThisProvingSamletController extends ProvingSamletController {

		private RedirectInfo redirectInfo;
		private boolean findVotingsCalled;
		private boolean valgting;

        ThisProvingSamletController() {
		}

        ThisProvingSamletController(RedirectInfo redirectInfo) {
			this.redirectInfo = redirectInfo;
		}

        boolean isFindVotingsCalled() {
			return findVotingsCalled;
		}

		@Override
		protected RedirectInfo getAndRemoveRedirectInfo() {
			return redirectInfo == null ? super.getAndRemoveRedirectInfo() : redirectInfo;
		}

		@Override
		public boolean isValgting() {
			return valgting;
		}

        void setValgting(boolean valgting) {
			this.valgting = valgting;
		}

		@Override
		public void findVotings() {
			findVotingsCalled = true;
		}

		@Override
		public void selectVoterInNegativeVotingList(SelectEvent event) {

		}

		@Override
		public void updateVotingsApproved() {

		}

	}

}
