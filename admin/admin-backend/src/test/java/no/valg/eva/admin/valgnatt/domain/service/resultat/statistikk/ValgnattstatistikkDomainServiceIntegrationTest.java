package no.valg.eva.admin.valgnatt.domain.service.resultat.statistikk;

import static org.assertj.core.api.Assertions.assertThat;

import no.evote.service.backendmock.BackendContainer;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.ElectoralRollCountReport;
import no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata.ValgnattElectoralRollDomainService;
import no.valg.eva.admin.voting.domain.model.Stemmegivningsstatistikk;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ValgnattstatistikkDomainServiceIntegrationTest extends AbstractJpaTestBase {

	private static final String HALDEN = "0101";
	private static final String MOSS = "0104";
	private static final String HVALER = "0111";
	private static final String SOR_ODAL = "0419";
	private static final String[] SA = {
			"\"valg\":\"Sametingsvalg\",\"valgtype\":\"SA\"" };
	private static final String[] BY = {
			"\"valg\":\"Bydelsvalg\",\"valgtype\":\"BY\"",
			"\"fylkesnummer\":\"03\",\"fylke\":\"Oslo\"",
			"\"kommunenummer\":\"0301\",\"kommune\":\"Oslo\"",
			"\"kretsnummer\":\"0705\",\"kretsnavn\":\"Grindbakken skole\"",
			"\"bydelsnummer\":\"030107\",\"bydelsnavn\":\"Vestre Aker\"" };
	private static final String[] KO = {
			"\"valg\":\"Kommunestyrevalg\",\"valgtype\":\"KO\"",
			"\"fylkesnummer\":\"01\",\"fylke\":\"Østfold\"",
			"\"kommunenummer\":\"0101\",\"kommune\":\"Halden\"",
			"\"kretsnummer\":\"0000\",\"kretsnavn\":\"Hele kommunen\"",
			"\"bydelsnummer\":\"010100\",\"bydelsnavn\":\"Hele kommunen\"" };
	private static final String[] FY = {
			"\"valg\":\"Fylkestingsvalg\",\"valgtype\":\"FY\"",
			"\"fylkesnummer\":\"01\",\"fylke\":\"Østfold\"",
			"\"kommunenummer\":\"0101\",\"kommune\":\"Halden\"",
			"\"kretsnummer\":\"0000\",\"kretsnavn\":\"Hele kommunen\"",
			"\"bydelsnummer\":\"010100\",\"bydelsnavn\":\"Hele kommunen\"" };
	private MvElectionRepository mvElectionRepository;
	private MvAreaRepository mvAreaRepository;
	private ValgnattstatistikkDomainService valgnattstatistikkDomainService;
	private ValgnattElectoralRollDomainService valgnattElectoralRollDomainService;

	@BeforeMethod
	public void init() {
		BackendContainer backend = new BackendContainer(getEntityManager());
		backend.initServices();

		mvElectionRepository = backend.getMvElectionRepository();
		mvAreaRepository = backend.getMvAreaRepository();
		valgnattstatistikkDomainService = backend.getValgnattstatistikkDomainService();
		valgnattElectoralRollDomainService = backend.getValgnattElectoralRollDomainService();
	}

	@Test(dataProvider = "kommuneKretsStemmetallGodkjenteVtsg")
	public void stemmegivningsstatistikk_kommune_godkjenteVtsg(String kommunenummer, String kretsnummer, int resultat) {
		Stemmegivningsstatistikk stemmegivningsstatistikk = getStemmegivningsstatistikk(kommunenummer, kretsnummer);

		assertThat(stemmegivningsstatistikk.getGodkjenteVtsg()).isEqualTo(resultat);
	}

	@DataProvider
	public Object[][] kommuneKretsStemmetallGodkjenteVtsg() {
		return new Object[][] {
			{ HALDEN, "0000", 471 }, // Xim, Sentralt fordelt på krets
			{ MOSS, "0001", 25 }, // Papirmanntall
			{ HVALER, "0000", 2 }, // Xim, Sentalt samlet
			{ SOR_ODAL, "8002", 2 }, // Xim, Tellekretser
		};
	}

	private Stemmegivningsstatistikk getStemmegivningsstatistikk(String kommunenummer, String kretsnummer) {
		ElectionPath electionPath = ElectionPath.from("200701", "01", "02", "00" + kommunenummer);
		MvElection valgdistrikt = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		AreaPath areaPath = AreaPath.from("200701.47." + kommunenummer.substring(0, 2) + "." + kommunenummer + "." + kommunenummer + "00." + kretsnummer);
		MvArea stemmekretsMva = mvAreaRepository.findSingleByPath(areaPath);
		return valgnattstatistikkDomainService.stemmegivningsstatistikk(valgdistrikt, stemmekretsMva);
	}

	@Test(dataProvider = "kommuneKretsStemmetallForkastedeVtsg")
	public void stemmegivningsstatistikk_kommune_forkastedeVtsg(String kommunenummer, String kretsnummer, int resultat) {
		Stemmegivningsstatistikk stemmegivningsstatistikk = getStemmegivningsstatistikk(kommunenummer, kretsnummer);
		
		assertThat(stemmegivningsstatistikk.getForkastedeVtsg()).isEqualTo(resultat);
	}
	
	@DataProvider
	public Object[][] kommuneKretsStemmetallForkastedeVtsg() {
		return new Object[][] {
			{ HVALER, "0000", 2 }, // Har 2 VA for krets 0000
			{ SOR_ODAL, "8002", 0 } // Har 1 VA for krets 0000 
		};
	}

	@Test(dataProvider = "valghendelse")
	public void findVotersAndReportingAreas_valghendelse_korrektStemmekretsgeografi(String electionEventId, String electionGroupId, String electionId,
			String[] forventetResultat) {
		MvElection valg = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from(electionEventId, electionGroupId, electionId).tilValghierarkiSti());

		ElectoralRollCountReport votersAndReportingAreas = valgnattElectoralRollDomainService.findVotersAndReportingAreas(valg);

		String json = votersAndReportingAreas.toJson();
		for (String resultat : forventetResultat) {
			assertThat(json).contains(resultat);
		}
	}

	@DataProvider
	public Object[][] valghendelse() {
		return new Object[][] {
				{ "200901", "02", "02", SA },
				{ "200701", "01", "03", BY },
				{ "200701", "01", "02", KO },
				{ "200701", "01", "01", FY }
		};
	}

}
