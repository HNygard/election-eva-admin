package no.valg.eva.admin.configuration.domain.service;

import static java.util.Arrays.asList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.AreaLevelEnum.POLLING_STATION;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGESTS_111111_11_11_1111_111111_1111_1111_1X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGESTS_111111_11_11_1111_111111_1111_111X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGESTS_111111_11_11_1111_111111_111X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGESTS_111111_11_11_1111_11111X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGESTS_111111_11_11_111X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGESTS_111111_11_1X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGEST_111111;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGEST_111111_11;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGEST_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGEST_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.MV_AREA_DIGEST_111111_11_11_1111_111111_1113;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvAreaDigestTestData.mvAreaDigest;
import static no.valg.eva.admin.configuration.test.repository.MvAreaRepositoryMockUtil.findDigestsByPathAndLevel;
import static no.valg.eva.admin.configuration.test.repository.MvAreaRepositoryMockUtil.findFirstDigestByPathAndLevel;
import static no.valg.eva.admin.configuration.test.repository.MvAreaRepositoryMockUtil.singleDigestByPath;
import static no.valg.eva.admin.configuration.test.repository.MvElectionRepositoryMockUtil.matcherValghierarkiStiOgValggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI_111111_11_11_1111_111112;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI_111111_11_11_1111_111113;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_11_11;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_11_12;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.FylkeskommuneStiTestData.FYLKESKOMMUNE_STI_111111_11_13;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1112;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1113;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.LandStiTestData.LAND_STI_111111_11;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmestedStiTestData.STEMMESTED_STI_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111112;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111113;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_11_12;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_11_13;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_NAVN_111111_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_111111_11_11_1111_111111_1111_1111_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_111111_11_11_1111_111111_1111_1111_12;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_111111_11_11_1111_111111_1111_1111_13;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_NAVN_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_111111_11_11_1111_111111_1111_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_111111_11_11_1111_111111_1111_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_NAVN_111111;
import static no.valg.eva.admin.felles.test.valggeografi.model.ValggeografiAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
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

public class ValggeografiDomainServiceTest extends MockUtilsTestCase {
	private ValggeografiDomainService service;
	private MvAreaRepository mvAreaRepository;
	private MvElectionRepository mvElectionRepository;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(ValggeografiDomainService.class);
		mvAreaRepository = getInjectMock(MvAreaRepository.class);
		mvElectionRepository = getInjectMock(MvElectionRepository.class);
	}

	@Test
	public void valghendelse_gittValghendelseSti_returnererValghendelse() throws Exception {
		singleDigestByPath(mvAreaRepository, AREA_PATH_111111, MV_AREA_DIGEST_111111);
		Valghendelse resultat = service.valghendelse(VALGHENDELSE_STI);
		assertThat(resultat).harStiLikMed(VALGHENDELSE_STI);
		assertThat(resultat).harNavnLikMed(VALGHENDELSE_NAVN_111111);
	}

	@Test
	public void land_gittValghendelseSti_returnererLandForValghendelse() throws Exception {
		findFirstDigestByPathAndLevel(mvAreaRepository, AREA_PATH_111111, COUNTRY, MV_AREA_DIGEST_111111_11);
		Land resultat = service.land(VALGHENDELSE_STI);
		assertThat(resultat).harStiLikMed(LAND_STI_111111_11);
		assertThat(resultat).harNavnLikMed(LAND_NAVN_111111_11);
	}

	@Test
	public void fylkeskommuner_gittValghendelseStiOgValgSti_returnerFiltrerteFylkeskommunerForValghendelse() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111, COUNTY, MV_AREA_DIGESTS_111111_11_1X);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALG_STI, FYLKESKOMMUNE_STI_111111_11_11, true);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALG_STI, FYLKESKOMMUNE_STI_111111_11_12, true);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALG_STI, FYLKESKOMMUNE_STI_111111_11_13, true);
		List<Fylkeskommune> resultat = service.fylkeskommuner(VALGHENDELSE_STI, VALG_STI);
		assertThat(resultat).containsExactly(FYLKESKOMMUNE_111111_11_11, FYLKESKOMMUNE_111111_11_12, FYLKESKOMMUNE_111111_11_13);
	}

	@Test
	public void fylkeskommuner_gittValghendelseStiOgValgdistriktSti_returnerFiltrerteFylkeskommunerForValghendelse() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111, COUNTY, MV_AREA_DIGESTS_111111_11_1X);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, FYLKESKOMMUNE_STI_111111_11_11, true);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, FYLKESKOMMUNE_STI_111111_11_12, false);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, FYLKESKOMMUNE_STI_111111_11_13, false);
		List<Fylkeskommune> resultat = service.fylkeskommuner(VALGHENDELSE_STI, VALGDISTRIKT_STI);
		assertThat(resultat).containsExactly(FYLKESKOMMUNE_111111_11_11);
	}

	@Test
	public void fylkeskommuner_gittValghendelseStiOgValghierarkiStiErNull_returnerFylkeskommunerForValghendelse() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111, COUNTY, MV_AREA_DIGESTS_111111_11_1X);
		List<Fylkeskommune> resultat = service.fylkeskommuner(VALGHENDELSE_STI, null);
		assertThat(resultat).containsExactly(FYLKESKOMMUNE_111111_11_11, FYLKESKOMMUNE_111111_11_12, FYLKESKOMMUNE_111111_11_13);
	}

	@Test
	public void fylkeskommuner_gittValghendelseStiOgValghierarkiErNullOgValghendelsesAdmin_returnererFylkeskommunerForValghendelse() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111, COUNTY, MV_AREA_DIGESTS_111111_11_1X);
		List<Fylkeskommune> resultat = service.fylkeskommuner(VALGHENDELSE_STI, null, VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(FYLKESKOMMUNE_111111_11_11, FYLKESKOMMUNE_111111_11_12, FYLKESKOMMUNE_111111_11_13);
	}

	@Test
	public void fylkeskommuner_gittValghendelseStiOgValghierarkiErNullOgFylkeskommuneBruker_returnererFylkeskommunerForValghendelse() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111, COUNTY, MV_AREA_DIGESTS_111111_11_1X);
		List<Fylkeskommune> resultat = service.fylkeskommuner(VALGHENDELSE_STI, null, FYLKESKOMMUNE_STI_111111_11_11);
		assertThat(resultat).containsExactly(FYLKESKOMMUNE_111111_11_11);
	}

	@Test
	public void kommuner_gittValghendelseSti_returnererKommunerForValghendelse() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111, MUNICIPALITY, MV_AREA_DIGESTS_111111_11_11_111X);
		List<Kommune> resultat = service.kommuner(VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(KOMMUNE_111111_11_11_1111, KOMMUNE_111111_11_11_1112, KOMMUNE_111111_11_11_1113);
	}

	@Test
	public void kommuner_gittFylkeskommuneStiOgValghierarkiStiErNull_returnererKommunerForFylkeskommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11, MUNICIPALITY, MV_AREA_DIGESTS_111111_11_11_111X);
		List<Kommune> resultat = service.kommuner(FYLKESKOMMUNE_STI_111111_11_11, null);
		assertThat(resultat).containsExactly(KOMMUNE_111111_11_11_1111, KOMMUNE_111111_11_11_1112, KOMMUNE_111111_11_11_1113);
	}

	@Test
	public void kommuner_gittFylkeskommuneStiOgValgdistriktSti_returnererFiltrerteKommunerForFylkeskommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11, MUNICIPALITY, MV_AREA_DIGESTS_111111_11_11_111X);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, KOMMUNE_STI_111111_11_11_1111, true);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, KOMMUNE_STI_111111_11_11_1112, false);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, KOMMUNE_STI_111111_11_11_1113, false);
		List<Kommune> resultat = service.kommuner(FYLKESKOMMUNE_STI_111111_11_11, VALGDISTRIKT_STI);
		assertThat(resultat).containsExactly(KOMMUNE_111111_11_11_1111);
	}

	@Test
	public void kommuner_gittFylkeskommuneStiOgValggruppeSti_returnererKommunerForFylkeskommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11, MUNICIPALITY, MV_AREA_DIGESTS_111111_11_11_111X);
		List<Kommune> resultat = service.kommuner(FYLKESKOMMUNE_STI_111111_11_11, VALGGRUPPE_STI);
		assertThat(resultat).containsExactly(KOMMUNE_111111_11_11_1111, KOMMUNE_111111_11_11_1112, KOMMUNE_111111_11_11_1113);
	}

	@Test
	public void kommuner_gittFylkeskommuneStiOgValghierarkiStiErNullOgValghendelsesAdmin_returererKommunerForFylkeskommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11, MUNICIPALITY, MV_AREA_DIGESTS_111111_11_11_111X);
		List<Kommune> resultat = service.kommuner(FYLKESKOMMUNE_STI_111111_11_11, null, VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(KOMMUNE_111111_11_11_1111, KOMMUNE_111111_11_11_1112, KOMMUNE_111111_11_11_1113);
	}

	@Test
	public void kommuner_gittFylkeskommuneStiOgValghierarkiStiErNullOgKommuneBruker_returererKommunerForFylkeskommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11, MUNICIPALITY, MV_AREA_DIGESTS_111111_11_11_111X);
		List<Kommune> resultat = service.kommuner(FYLKESKOMMUNE_STI_111111_11_11, null, KOMMUNE_STI);
		assertThat(resultat).containsExactly(KOMMUNE_111111_11_11_1111);
	}

	@Test
	public void bydeler_gittKommuneStiOgValghierarkiErNullOgValghendelsesAdmin_returnererBydelerForKommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111, BOROUGH, MV_AREA_DIGESTS_111111_11_11_1111_11111X);
		List<Bydel> resultat = service.bydeler(KOMMUNE_STI_111111_11_11_1111, null, VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(BYDEL_111111_11_11_1111_111111, BYDEL_111111_11_11_1111_111112, BYDEL_111111_11_11_1111_111113);
	}

	@Test
	public void bydeler_gittKommuneStiOgValghierarkiErNullOgKommuneBruker_returnererBydelerForKommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111, BOROUGH, MV_AREA_DIGESTS_111111_11_11_1111_11111X);
		List<Bydel> resultat = service.bydeler(KOMMUNE_STI_111111_11_11_1111, null, KOMMUNE_STI_111111_11_11_1111);
		assertThat(resultat).containsExactly(BYDEL_111111_11_11_1111_111111, BYDEL_111111_11_11_1111_111112, BYDEL_111111_11_11_1111_111113);
	}

	@Test
	public void bydeler_gittKommuneStiOgValghierarkiErNullOgStemmekretsBruker_returnererKunBydelForStemmekrets() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111, BOROUGH, MV_AREA_DIGESTS_111111_11_11_1111_11111X);
		List<Bydel> resultat = service.bydeler(KOMMUNE_STI_111111_11_11_1111, null, STEMMEKRETS_STI_111111_11_11_1111_111111_1111);
		assertThat(resultat).containsExactly(BYDEL_111111_11_11_1111_111111);
	}

	@Test
	public void bydeler_gittKommuneStiOgValgdistriktStiOgValghendelsesAdmin_returnererFiltrerteBydelerForKommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111, BOROUGH, MV_AREA_DIGESTS_111111_11_11_1111_11111X);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, BYDEL_STI_111111_11_11_1111_111111, true);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, BYDEL_STI_111111_11_11_1111_111112, false);
		matcherValghierarkiStiOgValggeografiSti(mvElectionRepository, VALGDISTRIKT_STI, BYDEL_STI_111111_11_11_1111_111113, false);
		List<Bydel> resultat = service.bydeler(KOMMUNE_STI_111111_11_11_1111, VALGDISTRIKT_STI, VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(BYDEL_111111_11_11_1111_111111);
	}

	@Test
	public void bydeler_gittKommuneStiOgValggruppeStiOgValghendelsesAdmin_returnererBydelerForKommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111, BOROUGH, MV_AREA_DIGESTS_111111_11_11_1111_11111X);
		List<Bydel> resultat = service.bydeler(KOMMUNE_STI_111111_11_11_1111, VALGGRUPPE_STI, VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(BYDEL_111111_11_11_1111_111111, BYDEL_111111_11_11_1111_111112, BYDEL_111111_11_11_1111_111113);
	}

	@Test
	public void stemmekrets_gittStemmekretsSti_returnererStemmekrets() throws Exception {
		singleDigestByPath(mvAreaRepository, AREA_PATH_111111_11_11_1111_111111_1111, MV_AREA_DIGEST_111111_11_11_1111_111111_1111);
		Stemmekrets resultat = service.stemmekrets(STEMMEKRETS_STI);
		assertThat(resultat).isEqualTo(STEMMEKRETS_111111_11_11_1111_111111_1111);
	}

	@Test
	public void stemmekretser_gittKommuneSti_returnererStemmekretserForKommune() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111, POLLING_DISTRICT, MV_AREA_DIGESTS_111111_11_11_1111_111111_111X);
		List<Stemmekrets> resultat = service.stemmekretser(KOMMUNE_STI);
		assertThat(resultat).containsExactly(
				STEMMEKRETS_111111_11_11_1111_111111_1111, STEMMEKRETS_111111_11_11_1111_111111_1112, STEMMEKRETS_111111_11_11_1111_111111_1113);
	}

	@Test
	public void stemmekretser_gittKommuneStiOgPollingDistrictType_returnererFiltrerteStemmekretserForKommune() throws Exception {
		MvAreaDigest mvAreaDigestMunicipalityPollingDistrict =
				mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1111, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1111, PollingDistrictType.MUNICIPALITY);
		List<MvAreaDigest> mvAreaDigests =
				asList(mvAreaDigestMunicipalityPollingDistrict, MV_AREA_DIGEST_111111_11_11_1111_111111_1112, MV_AREA_DIGEST_111111_11_11_1111_111111_1113);
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111, POLLING_DISTRICT, mvAreaDigests);
		List<Stemmekrets> resultat = service.stemmekretser(KOMMUNE_STI, PollingDistrictType.MUNICIPALITY);
		assertThat(resultat).containsExactly(STEMMEKRETS_111111_11_11_1111_111111_1111);
	}

	@Test
	public void stemmekretser_gittBydelStiOgValghendelsesAdmin_returnererStemmekretserForBydel() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111_111111, POLLING_DISTRICT, MV_AREA_DIGESTS_111111_11_11_1111_111111_111X);
		List<Stemmekrets> resultat = service.stemmekretser(BYDEL_STI_111111_11_11_1111_111111, VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(
				STEMMEKRETS_111111_11_11_1111_111111_1111, STEMMEKRETS_111111_11_11_1111_111111_1112, STEMMEKRETS_111111_11_11_1111_111111_1113);
	}

	@Test
	public void stemmekretser_gittBydelStiOgStemmekretsBruker_returnererStemmekrets() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111_111111, POLLING_DISTRICT, MV_AREA_DIGESTS_111111_11_11_1111_111111_111X);
		List<Stemmekrets> resultat = service.stemmekretser(BYDEL_STI_111111_11_11_1111_111111, STEMMEKRETS_STI_111111_11_11_1111_111111_1111);
		assertThat(resultat).containsExactly(STEMMEKRETS_111111_11_11_1111_111111_1111);
	}

	@Test
	public void stemmesteder_gittStemmekretsStiOgValghendelsesAdmin_returnererStemmestederForStemmekrets() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111_111111_1111, POLLING_PLACE, MV_AREA_DIGESTS_111111_11_11_1111_111111_1111_111X);
		List<Stemmested> resultat = service.stemmesteder(STEMMEKRETS_STI_111111_11_11_1111_111111_1111, VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(
				STEMMESTED_111111_11_11_1111_111111_1111_1111, STEMMESTED_111111_11_11_1111_111111_1111_1112, STEMMESTED_111111_11_11_1111_111111_1111_1113);
	}

	@Test
	public void stemmesteder_gittStemmekretsStiOgStemmestedBruker_returnererStemmestederForStemmekrets() throws Exception {
		findDigestsByPathAndLevel(mvAreaRepository, AREA_PATH_111111_11_11_1111_111111_1111, POLLING_PLACE, MV_AREA_DIGESTS_111111_11_11_1111_111111_1111_111X);
		List<Stemmested> resultat = service.stemmesteder(STEMMEKRETS_STI_111111_11_11_1111_111111_1111, STEMMESTED_STI_111111_11_11_1111_111111_1111_1111);
		assertThat(resultat).containsExactly(STEMMESTED_111111_11_11_1111_111111_1111_1111);
	}

	@Test
	public void testRoder() throws Exception {
		findDigestsByPathAndLevel(
				mvAreaRepository, AREA_PATH_111111_11_11_1111_111111_1111_1111, POLLING_STATION, MV_AREA_DIGESTS_111111_11_11_1111_111111_1111_1111_1X);
		List<Rode> resultat = service.roder(STEMMESTED_STI_111111_11_11_1111_111111_1111_1111);
		assertThat(resultat).containsExactly(
				RODE_111111_11_11_1111_111111_1111_1111_11, RODE_111111_11_11_1111_111111_1111_1111_12, RODE_111111_11_11_1111_111111_1111_1111_13);
	}

}
