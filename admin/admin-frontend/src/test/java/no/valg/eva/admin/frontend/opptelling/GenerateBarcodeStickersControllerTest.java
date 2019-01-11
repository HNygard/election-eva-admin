package no.valg.eva.admin.frontend.opptelling;

import no.evote.service.SpecialPurposeReportService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.addAll;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_11;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenerateBarcodeStickersControllerTest extends BaseFrontendTest {
	private GenerateBarcodeStickersController ctrl;
	private PageTitleMetaBuilder pageTitleMetaBuilder;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = initializeMocks(GenerateBarcodeStickersController.class);
		ValghierarkiService valghierarkiService = getInjectMock(ValghierarkiService.class);
		ValggeografiService valggeografiService = getInjectMock(ValggeografiService.class);
		pageTitleMetaBuilder = getInjectMock(PageTitleMetaBuilder.class);

		when(valghierarkiService.valg(VALG_STI)).thenReturn(VALG_111111_11_11);
		when(valggeografiService.stemmekrets(STEMMEKRETS_STI)).thenReturn(STEMMEKRETS_111111_11_11_1111_111111_1111);

	}

	@Test
	public void getKontekstVelgerOppsett() {
		assertThat(ctrl.getKontekstVelgerOppsett()).isEqualTo(kontekstvelgerOppsett());
	}

	@Test
	public void getNumberOfStickers_gittCtrl_returnerStandardverdi() {
		assertThat(ctrl.getNumberOfStickers()).isEqualTo(1);
	}

	@Test
	public void setNumberOfStickers_gittNyVerdi_setterNyVerdi() {
		ctrl.setNumberOfStickers(2);
		assertThat(ctrl.getNumberOfStickers()).isEqualTo(2);
	}

	@Test
	public void getVelgValgLinkNavn_gittCtrl_returnererLinkNavn() {
        assertThat(ctrl.getSelectElectionLinkName()).isEqualTo("@common.choose @election_level[2].name");
	}

	@Test
	public void getVelgOpptellingskategoriLinkNavn_gitCtrl_returnererLinkNavn() {
        assertThat(ctrl.getSelectCountCategoryLinkName()).isEqualTo("@common.choose @count.ballot.approve.rejected.category");
	}

	@Test
	public void getVelgStemmekretsLinkNavn_gittCtrl_returnererLinkNavn() {
        assertThat(ctrl.getSelectPollingDistrictLinkName()).isEqualTo("@common.choose @area_level[5].name");
	}

	@Test
	public void getElectionPageTitleMeta_gittCtrl_returnerMetaDataForValg() {
		ctrl.initialized(kontekst());
		PageTitleMetaModel pageTitleMetaModel1 = createMock(PageTitleMetaModel.class);
		PageTitleMetaModel pageTitleMetaModel2 = createMock(PageTitleMetaModel.class);
		PageTitleMetaModel pageTitleMetaModel3 = createMock(PageTitleMetaModel.class);
		PageTitleMetaModel pageTitleMetaModel4 = createMock(PageTitleMetaModel.class);
		when(pageTitleMetaBuilder.fra(VALG_111111_11_11)).thenReturn(arrayList(pageTitleMetaModel1, pageTitleMetaModel2, pageTitleMetaModel3));
		when(pageTitleMetaBuilder.countCategory(FO)).thenReturn(arrayList(pageTitleMetaModel4));
		assertThat(ctrl.getElectionPageTitleMeta()).containsExactly(pageTitleMetaModel1, pageTitleMetaModel2, pageTitleMetaModel3, pageTitleMetaModel4);
	}

	private List<PageTitleMetaModel> arrayList(PageTitleMetaModel... pageTitleMetaModels) {
		ArrayList<PageTitleMetaModel> pageTitleMetaModelList = new ArrayList<>();
		addAll(pageTitleMetaModelList, pageTitleMetaModels);
		return pageTitleMetaModelList;
	}

	@Test
	public void getAreaPageTitleMeta_gittCtrl_returnererMetaDataForStemmekrets() {
		ctrl.initialized(kontekst());
		PageTitleMetaModel pageTitleMetaModel1 = createMock(PageTitleMetaModel.class);
		PageTitleMetaModel pageTitleMetaModel2 = createMock(PageTitleMetaModel.class);
		PageTitleMetaModel pageTitleMetaModel3 = createMock(PageTitleMetaModel.class);
		when(pageTitleMetaBuilder.fra(STEMMEKRETS_111111_11_11_1111_111111_1111)).thenReturn(arrayList(pageTitleMetaModel1, pageTitleMetaModel2, pageTitleMetaModel3));
		assertThat(ctrl.getAreaPageTitleMeta()).containsExactly(pageTitleMetaModel1, pageTitleMetaModel2, pageTitleMetaModel3);
	}

	@Test
	public void redirectTilVelgValg_gittCtrlMedOppsett_redirecterTilKorrektUrl() throws Exception {
		mockFieldValue("contextPickerSetup", kontekstvelgerOppsett());
		ctrl.redirectToSelectElection();
		getServletContainer().verifyRedirect("/secure/kontekstvelger.xhtml?oppsett=[hierarki|nivaer|2][opptellingskategori][geografi|nivaer|4,5|variant|ALT_VELG_BYDEL]");
	}

	@Test
	public void redirectTilVelgOpptellingskategori_gittCtrlMedOppsettOgKontekst_redirecterTilKorrektUrl() throws Exception {
		mockFieldValue("contextPickerSetup", kontekstvelgerOppsett());
		ctrl.initialized(kontekst());
		ctrl.redirectToSelectCountCategory();
		getServletContainer().verifyRedirect(
				"/secure/kontekstvelger.xhtml?oppsett=[hierarki|nivaer|2][opptellingskategori][geografi|nivaer|4,5|variant|ALT_VELG_BYDEL]&kontekst=hierarki|111111.11.11");
	}

	@Test
	public void redirectTilVelgStemmekrets_gittCtrlMedOppsettOgKontekst_redirecterTilKorrektUrl() throws Exception {
		mockFieldValue("contextPickerSetup", kontekstvelgerOppsett());
		ctrl.initialized(kontekst());
		ctrl.redirectToSelectPollingDistrict();
		getServletContainer().verifyRedirect(
				"/secure/kontekstvelger.xhtml?oppsett=[hierarki|nivaer|2][opptellingskategori][geografi|nivaer|4,5|variant|ALT_VELG_BYDEL]&kontekst=hierarki|111111.11.11|countCategory|FO");
	}

	private KontekstvelgerOppsett kontekstvelgerOppsett() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALG));
		oppsett.leggTil(opptellingskategori());
		oppsett.leggTil(geografi(BYDEL, STEMMEKRETS).medVariant(ValggeografiVariant.ALT_VELG_BYDEL));
		return oppsett;
	}

	@Test
	public void generateSticker_withEvoteException_shouldAddErrorMessage() {
		evoteExceptionWhen(SpecialPurposeReportService.class)
				.generateScanningBoxLabel(eq(getUserDataMock()), any(ElectionPath.class), any(CountCategory.class), any(AreaPath.class), anyInt());
		ctrl.initialized(kontekst());

		ctrl.generateSticker();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "EvoteException");
	}

	@Test
	public void generateSticker_withValidData_shouldWriteContentToResponse() throws Exception {
		when(getInjectMock(SpecialPurposeReportService.class)
				.generateScanningBoxLabel(eq(getUserDataMock()), any(ElectionPath.class), any(CountCategory.class), any(AreaPath.class), anyInt()))
				.thenReturn("Hello".getBytes());
		ctrl.initialized(kontekst());

		ctrl.generateSticker();

		verify(getServletContainer().getResponseMock().getOutputStream()).write("Hello".getBytes());
	}

	private Kontekst kontekst() {
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(VALG_STI);
		kontekst.setValggeografiSti(STEMMEKRETS_STI);
		kontekst.setCountCategory(FO);
		return kontekst;
	}

}
