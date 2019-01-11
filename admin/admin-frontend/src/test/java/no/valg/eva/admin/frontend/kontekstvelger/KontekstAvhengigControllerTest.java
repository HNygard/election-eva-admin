package no.valg.eva.admin.frontend.kontekstvelger;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.security.PageAccess;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1112;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI_111111_11_11_111112;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.side;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant.ALT_VELG_BYDEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class KontekstAvhengigControllerTest extends BaseFrontendTest {

	@Test
	public void init_withoutRedirectConfig_createsRedirectConfig() throws Exception {
		KontekstAvhengigController ctrl = ctrl();

		ctrl.init();

		assertThat(ctrl.getKontekstVelgerOppsett().getElementer()).hasSize(0); // Wash away
		Kontekst data = ((Ctrl) ctrl).getInitialized();
		assertThat(data.kommuneSti().areaPath()).isEqualTo(AREA_PATH_MUNICIPALITY);
	}

	@Test
	public void init_withPickerRequired_redirectsToPicker() throws Exception {
		KontekstvelgerOppsett setup = getKontekstVelgerOppsett();
		setup.leggTil(hierarki(VALG));
		KontekstAvhengigController ctrl = ctrl(setup);
		when(getInjectMock(PageAccess.class).getId(anyString())).thenReturn("/my/uri");

		ctrl.init();

		getServletContainer()
				.verifyRedirect("/secure/kontekstvelger.xhtml?oppsett=[hierarki|nivaer|2][side|uri|/my/uri]");
	}

	@Test
	public void init_withCtxParamAndValidElectionAndArea_verifyInit() throws Exception {
		KontekstvelgerOppsett setup = getKontekstVelgerOppsett();
		setup.leggTil(hierarki(VALGHENDELSE));
		KontekstAvhengigController ctrl = ctrl(setup);
		when(getInjectMock(UserDataController.class).getUserData().operatorValghierarkiSti()).thenReturn(VALGHENDELSE_STI);
		when(getInjectMock(UserDataController.class).getUserData().operatorValggeografiSti()).thenReturn(ValghendelseStiTestData.VALGHENDELSE_STI);
		Kontekst data = new Kontekst();
		data.setValghierarkiSti(ValghierarkiSti.fra(ELECTION_PATH_ELECTION_EVENT));
		data.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));
		getServletContainer().setRequestParameter(KONTEKST.toString(), data.serialize());

		ctrl.init();

		assertThat(((Ctrl) ctrl).getInitialized()).isNotNull();
	}

	@Test
	public void init_gittBrukerKnyttetTilValgdistriktOgValgAvOpptellingskategoriOgValgOgStemmekrets_girKorrektInit() throws Exception {
		KontekstvelgerOppsett setup = getKontekstVelgerOppsett();
		setup.leggTil(opptellingskategori());
		setup.leggTil(hierarki(VALG));
		setup.leggTil(geografi(STEMMEKRETS));
		KontekstAvhengigController ctrl = ctrl(setup);
		when(getInjectMock(UserDataController.class).getUserData().operatorValghierarkiSti()).thenReturn(VALGDISTRIKT_STI);
		when(getInjectMock(UserDataController.class).getUserData().operatorValggeografiSti()).thenReturn(ValghendelseStiTestData.VALGHENDELSE_STI);
		Kontekst data = new Kontekst();
		data.setCountCategory(VF);
		data.setValghierarkiSti(VALG_STI);
		data.setValggeografiSti(STEMMEKRETS_STI);
		getServletContainer().setRequestParameter(KONTEKST.toString(), data.serialize());

		ctrl.init();

		assertThat(((Ctrl) ctrl).getInitialized()).isNotNull();
	}

	@Test(dataProvider = "mismatchMellomUserDataOgKontekst")
	public void init_gittMismatchMellomUserDataOgKontekst_redirecterTilKontekstvelger(
			ValghierarkiSti operatorValghierarkiSti, ValggeografiSti operatorValggeografiSti) throws Exception {
		KontekstvelgerOppsett setup = getKontekstVelgerOppsett();
		setup.leggTil(hierarki(VALGDISTRIKT));
		setup.leggTil(geografi(KOMMUNE));
		setup.leggTil(side("/my/uri"));
		KontekstAvhengigController ctrl = ctrl(setup);
		when(getInjectMock(UserDataController.class).getUserData().operatorValghierarkiSti()).thenReturn(operatorValghierarkiSti);
		when(getInjectMock(UserDataController.class).getUserData().operatorValggeografiSti()).thenReturn(operatorValggeografiSti);
		Kontekst data = new Kontekst();
		data.setValghierarkiSti(VALGDISTRIKT_STI);
		data.setValggeografiSti(KOMMUNE_STI);
		getServletContainer().setRequestParameter(KONTEKST.toString(), data.serialize());
		
		ctrl.init();

		getServletContainer()
				.verifyRedirect("/secure/kontekstvelger.xhtml?oppsett=[hierarki|nivaer|3][geografi|nivaer|3][side|uri|/my/uri]");
	}

	@DataProvider
	public Object[][] mismatchMellomUserDataOgKontekst() {
		return new Object[][] {
				{ VALGHENDELSE_STI, KOMMUNE_STI_111111_11_11_1112 },
				{ VALGDISTRIKT_STI_111111_11_11_111112, KOMMUNE_STI }
		};
	}

	@Test
	public void init_withCtxParamAndInvalidElectionAndValidArea_redirectsToPicker() throws Exception {
		KontekstvelgerOppsett setup = getKontekstVelgerOppsett();
		setup.leggTil(hierarki(VALG));
		KontekstAvhengigController ctrl = ctrl(setup);
		when(getInjectMock(UserDataController.class).getUserData().getOperatorAreaPath()).thenReturn(AREA_PATH_ROOT);
		Kontekst data = new Kontekst();
		data.setValghierarkiSti(ValghierarkiSti.fra(ELECTION_PATH_ELECTION_EVENT));
		data.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));
		getServletContainer().setRequestParameter(KONTEKST.toString(), data.serialize());
		when(getInjectMock(PageAccess.class).getId(anyString())).thenReturn("/my/uri");

		ctrl.init();

		getServletContainer()
				.verifyRedirect("/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|3][hierarki|nivaer|2][side|uri|/my/uri]");
	}

	@Test
	public void init_gittOppsettMedOpptellingskategoriOgValghierarkiOgValggeografiOgKontekstMedOpptellingskategori_redirectMedKontekst() throws Exception {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(opptellingskategori());
		oppsett.leggTil(hierarki(VALG));
		oppsett.leggTil(geografi(BYDEL, STEMMEKRETS).medVariant(ALT_VELG_BYDEL));
		Kontekst kontekst = new Kontekst();
		kontekst.setCountCategory(FO);
		KontekstAvhengigController ctrl = ctrl(oppsett);
		getServletContainer().setRequestParameter(KONTEKST.toString(), kontekst.serialize());
		when(getInjectMock(PageAccess.class).getId(anyString())).thenReturn("/my/uri");

		ctrl.init();

		getServletContainer().verifyRedirect(
				"/secure/kontekstvelger.xhtml?oppsett=[opptellingskategori][hierarki|nivaer|2][geografi|nivaer|4,5|variant|ALT_VELG_BYDEL][side|uri|/my/uri]"
						+ "&kontekst=countCategory|FO");
	}

	private KontekstAvhengigController ctrl() throws Exception {
		return ctrl(getKontekstVelgerOppsett());
	}

	private KontekstAvhengigController ctrl(KontekstvelgerOppsett setup) throws Exception {
		KontekstAvhengigController result = initializeMocks(new Ctrl(setup));
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_EVENT);
		setUserDataArea(AREA_PATH_MUNICIPALITY);
		getServletContainer().setRequestURI("/my/uri");
		return result;
	}

	private KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(KontekstvelgerElement.geografi(KOMMUNE));
		return setup;
	}

	private class Ctrl extends KontekstAvhengigController {

		private KontekstvelgerOppsett setup;
		private Kontekst initialized;

		Ctrl(KontekstvelgerOppsett setup) {
			this.setup = setup;
		}

		@Override
		public KontekstvelgerOppsett getKontekstVelgerOppsett() {
			return setup;
		}

		@Override
		public void initialized(Kontekst data) {
			this.initialized = data;
		}

		public Kontekst getInitialized() {
			return initialized;
		}
	}

}
