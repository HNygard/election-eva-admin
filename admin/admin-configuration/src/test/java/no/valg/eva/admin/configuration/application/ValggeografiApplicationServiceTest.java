package no.valg.eva.admin.configuration.application;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_47;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1111;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1111_111111;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1111_111111_1113;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1111_111112;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1111_111113;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1112;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_11_11_1113;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_47_11;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_47_12;
import static no.valg.eva.admin.configuration.test.data.MvAreaTestData.MV_AREA_111111_47_13;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111112;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111113;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_47_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_47_12;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_47_13;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.stemmekrets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.service.OmraadehierarkiDomainService;
import no.valg.eva.admin.configuration.domain.service.ValggeografiDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmestedSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Bydel;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.Land;
import no.valg.eva.admin.felles.valggeografi.model.Rode;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valggeografi.model.Stemmested;
import no.valg.eva.admin.felles.valggeografi.model.Valghendelse;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValggeografiApplicationServiceTest extends MockUtilsTestCase {
	private ValggeografiApplicationService service;
	private ValggeografiDomainService domainService;
	private OmraadehierarkiDomainService omraadehierarkiDomainService;
	private UserData userData;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(ValggeografiApplicationService.class);
		domainService = getInjectMock(ValggeografiDomainService.class);
		omraadehierarkiDomainService = getInjectMock(OmraadehierarkiDomainService.class);
		userData = createMock(UserData.class);
	}

	@Test
	public void valghendelse_gittUserData_returnererValghendelse() throws Exception {
		Valghendelse valghendelse = valghendelse();
		when(domainService.valghendelse(operatorValghendelseSti())).thenReturn(valghendelse);
		Valghendelse resultat = service.valghendelse(userData);
		assertThat(resultat).isSameAs(valghendelse);
	}

	@Test
	public void land_gittUserData_returnererLandForValghendelse() throws Exception {
		Land land = land();
		when(domainService.land(operatorValghendelseSti())).thenReturn(land);
		Land resultat = service.land(userData);
		assertThat(resultat).isSameAs(land);
	}

	@Test
	public void fylkeskommuner_gittUserDataOgValghierarkiSti_returnererFylkeskommunerForValghendelse() throws Exception {
		List<Fylkeskommune> fylkeskommuner = fylkeskommuner();
		ValghierarkiSti valghierarkiSti = valghierkiSti();
		when(domainService.fylkeskommuner(operatorValghendelseSti(), valghierarkiSti, operatorValggeografiSti())).thenReturn(fylkeskommuner);
		List<Fylkeskommune> resultat = service.fylkeskommuner(userData, valghierarkiSti, null);
		assertThat(resultat).isSameAs(fylkeskommuner);
	}

	@Test
	public void fylkeskommuner_gittUserDataOgValghierarkiStiOgCountCategory_returnererFylkeskommunerForValghendelse() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierkiSti();
		CountCategory enCountCategory = FO;
		List<MvArea> mvAreas = asList(MV_AREA_111111_47_11, MV_AREA_111111_47_12, MV_AREA_111111_47_13);
		when(userData.operatorValggeografiSti().valghendelseSti()).thenReturn(VALGHENDELSE_STI);
		when(omraadehierarkiDomainService.getCountiesFor(userData, valghierarkiSti.electionPath(), AREA_PATH_111111_47, enCountCategory))
				.thenReturn(mvAreas);
		List<Fylkeskommune> resultat = service.fylkeskommuner(userData, valghierarkiSti, enCountCategory);
		assertThat(resultat).containsExactly(FYLKESKOMMUNE_111111_47_11, FYLKESKOMMUNE_111111_47_12, FYLKESKOMMUNE_111111_47_13);
	}

	@Test
	public void kommunerForValghendelse_gittUserData_returnererKommunerForValghendelse() throws Exception {
		List<Kommune> kommuner = kommuner();
		when(domainService.kommuner(operatorValghendelseSti())).thenReturn(kommuner);
		List<Kommune> resultat = service.kommunerForValghendelse(userData);
		assertThat(resultat).isSameAs(kommuner);
	}

	@Test
	public void kommuner_gittUserDataOgFylkeskommuneStiOgValghierarkiSti_returnererKommunerForFylkeskommune() throws Exception {
		List<Kommune> kommuner = kommuner();
		FylkeskommuneSti fylkeskommuneSti = fylkeskommuneSti();
		ValghierarkiSti valghierarkiSti = valghierkiSti();
		when(domainService.kommuner(fylkeskommuneSti, valghierarkiSti, operatorValggeografiSti())).thenReturn(kommuner);
		List<Kommune> resultat = service.kommuner(userData, fylkeskommuneSti, valghierarkiSti, null);
		assertThat(resultat).isSameAs(kommuner);
	}

	@Test
	public void kommuner_gittUserDataOgFylkeskommuneStiOgValghierarkiStiOgCountCategory_returnererKommunerForFylkeskommune() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierkiSti();
		CountCategory enCountCategory = FO;
		List<MvArea> mvAreas = asList(MV_AREA_111111_11_11_1111, MV_AREA_111111_11_11_1112, MV_AREA_111111_11_11_1113);
		when(omraadehierarkiDomainService.getMunicipalitiesFor(userData, valghierarkiSti.electionPath(), AREA_PATH_111111_11_11, enCountCategory))
				.thenReturn(mvAreas);
		List<Kommune> resultat = service.kommuner(userData, FYLKESKOMMUNE_STI, valghierarkiSti, enCountCategory);
		assertThat(resultat).containsExactly(KOMMUNE_111111_11_11_1111, KOMMUNE_111111_11_11_1112, KOMMUNE_111111_11_11_1113);
	}

	@Test
	public void bydeler_gittUserDataOgKommuneStiOgValghierarkiSti_returnererBydelerForKommune() throws Exception {
		List<Bydel> bydeler = bydeler();
		KommuneSti kommuneSti = kommuneSti();
		ValghierarkiSti valghierarkiSti = valghierkiSti();
		when(domainService.bydeler(kommuneSti, valghierarkiSti, operatorValggeografiSti())).thenReturn(bydeler);
		List<Bydel> resultat = service.bydeler(userData, kommuneSti, valghierarkiSti, null);
		assertThat(resultat).isSameAs(bydeler);
	}

	@Test
	public void bydeler_gittUserDataOgKommuneStiOgValghierarkiStiOgCountCategory_returnererBydelerForKommune() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierkiSti();
		CountCategory enCountCategory = FO;
		List<MvArea> mvAreas = asList(MV_AREA_111111_11_11_1111_111111, MV_AREA_111111_11_11_1111_111112, MV_AREA_111111_11_11_1111_111113);
		when(omraadehierarkiDomainService.getBoroughsFor(userData, enCountCategory, valghierarkiSti.electionPath(), AREA_PATH_111111_11_11_1111))
				.thenReturn(mvAreas);
		List<Bydel> resultat = service.bydeler(userData, KOMMUNE_STI, valghierarkiSti, enCountCategory);
		assertThat(resultat).containsExactly(BYDEL_111111_11_11_1111_111111, BYDEL_111111_11_11_1111_111112, BYDEL_111111_11_11_1111_111113);
	}

	@Test
	public void stemmekrets_gittStemmekretsSti_returnererStemmekrets() throws Exception {
		Stemmekrets stemmekrets = stemmekrets();
		when(domainService.stemmekrets(STEMMEKRETS_STI)).thenReturn(stemmekrets);
		Stemmekrets resultat = service.stemmekrets(STEMMEKRETS_STI);
		assertThat(resultat).isSameAs(stemmekrets);
	}

	@Test
	public void stemmekretser_gittUserDataOgBydelSti_returnererStemmekretserForBydel() throws Exception {
		List<Stemmekrets> stemmekretser = stemmekretser();
		BydelSti bydelSti = bydelSti();
		when(domainService.stemmekretser(bydelSti, operatorValggeografiSti())).thenReturn(stemmekretser);
		List<Stemmekrets> resultat = service.stemmekretser(userData, bydelSti, null, null);
		assertThat(resultat).isSameAs(stemmekretser);
	}

	@Test
	public void stemmekretser_gittUserDataOgBydelStiOgValghierarkiStiOgCountCategory_returnererStemmekretserForBydel() throws Exception {
		CountCategory enCountCategory = FO;
		ValghierarkiSti valghierarkiSti = valghierkiSti();
		List<MvArea> mvAreas = asList(MV_AREA_111111_11_11_1111_111111_1111, MV_AREA_111111_11_11_1111_111111_1112, MV_AREA_111111_11_11_1111_111111_1113);
		when(omraadehierarkiDomainService.getPollingDistrictsFor(userData, enCountCategory, valghierarkiSti.electionPath(), AREA_PATH_111111_11_11_1111_111111))
				.thenReturn(mvAreas);
		List<Stemmekrets> resultat = service.stemmekretser(userData, BYDEL_STI, valghierarkiSti, enCountCategory);
		assertThat(resultat).containsExactly(
				STEMMEKRETS_111111_11_11_1111_111111_1111, STEMMEKRETS_111111_11_11_1111_111111_1112, STEMMEKRETS_111111_11_11_1111_111111_1113);
	}

	@Test
	public void stemmekretser_gittKommuneSti_returnererStemmekretserForKommune() throws Exception {
		List<Stemmekrets> stemmekretser = stemmekretser();
		KommuneSti kommuneSti = kommuneSti();
		when(domainService.stemmekretser(kommuneSti)).thenReturn(stemmekretser);
		List<Stemmekrets> resultat = service.stemmekretser(kommuneSti);
		assertThat(resultat).isSameAs(stemmekretser);
	}

	@Test
	public void stemmesteder_gittUserDataOgStemmekretsSti_returnererStemmestederForStemmekrets() throws Exception {
		List<Stemmested> stemmesteder = stemmesteder();
		StemmekretsSti stemmekretsSti = stemmekretsSti();
		when(domainService.stemmesteder(stemmekretsSti, operatorValggeografiSti())).thenReturn(stemmesteder);
		List<Stemmested> resultat = service.stemmesteder(userData, stemmekretsSti);
		assertThat(resultat).isSameAs(stemmesteder);
	}

	@Test
	public void roder_gittStemmestedStiOgValghierarkiSti_returnererRoderForStemmested() throws Exception {
		List<Rode> roder = roder();
		StemmestedSti stemmestedSti = stemmestedSti();
		when(domainService.roder(stemmestedSti)).thenReturn(roder);
		List<Rode> resultat = service.roder(stemmestedSti);
		assertThat(resultat).isSameAs(roder);
	}

	private ValghendelseSti operatorValghendelseSti() {
		return operatorValggeografiSti().valghendelseSti();
	}

	private ValggeografiSti operatorValggeografiSti() {
		return userData.operatorValggeografiSti();
	}

	private FylkeskommuneSti fylkeskommuneSti() {
		return createMock(FylkeskommuneSti.class);
	}

	private KommuneSti kommuneSti() {
		return createMock(KommuneSti.class);
	}

	private BydelSti bydelSti() {
		return createMock(BydelSti.class);
	}

	private StemmekretsSti stemmekretsSti() {
		return createMock(StemmekretsSti.class);
	}

	private StemmestedSti stemmestedSti() {
		return createMock(StemmestedSti.class);
	}

	private Valghendelse valghendelse() {
		return createMock(Valghendelse.class);
	}

	private Land land() {
		return createMock(Land.class);
	}

	private List<Fylkeskommune> fylkeskommuner() {
		return createListMock();
	}

	private List<Kommune> kommuner() {
		return createListMock();
	}

	private List<Bydel> bydeler() {
		return createListMock();
	}

	private List<Stemmekrets> stemmekretser() {
		return createListMock();
	}

	private List<Stemmested> stemmesteder() {
		return createListMock();
	}

	private List<Rode> roder() {
		return createListMock();
	}

	private ValghierarkiSti valghierkiSti() {
		return createMock(ValghierarkiSti.class);
	}

}
