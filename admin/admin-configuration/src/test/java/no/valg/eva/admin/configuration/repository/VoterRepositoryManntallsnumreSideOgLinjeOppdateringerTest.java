package no.valg.eva.admin.configuration.repository;

import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_ENDRING;
import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_TILGANG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.RepositoryBackedRBACTestFixture;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.RepositoryTestDataFactory;
import no.valg.eva.admin.test.TestGroups;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
/* Tester relatert til verifikasjon av manntallsnummerhåndtering etter initiell import av manntall
   (dvs. i fasen hvor vi får inn manntallsoppdateringer) */
public class VoterRepositoryManntallsnumreSideOgLinjeOppdateringerTest extends AbstractJpaTestBase {

	private static final String VELGER_ID = "08023311705";
	private static final String ELECTION_EVENT_ID = "200701";
	private static final String STI_KRETS_HALDEN = "200701.47.01.0101.010100.0000";
	private static final int ANTALL_SIDER_INITIELT_MANNTALL_HALDEN = 32;
	private static final String STI_KRETS_HORTEN = "200701.47.07.0701.070100.0000";
	private static final int ANTALL_SIDER_INITIELT_MANNTALL_HORTEN = 0; // Tomt manntall i Horten

	private ElectionEventRepository electionEventRepository;
	private MvAreaRepository mvAreaRepository;
	private VoterRepository voterRepository;
	private RBACTestFixture rbacTestFixture;
	private ElectionEvent electionEvent;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		electionEventRepository = new ElectionEventRepository(getEntityManager());
		electionEvent = electionEventRepository.findById(ELECTION_EVENT_ID);
		mvAreaRepository = new MvAreaRepository(getEntityManager());
		voterRepository = new VoterRepository(getEntityManager());
		rbacTestFixture = new RepositoryBackedRBACTestFixture(getEntityManager());
		rbacTestFixture.init();
	}
	
	@Test
	public void insert_etterAtManntallsnumreErGenerert_ikkeGodkjentVelgerFaarIkkeManntallsnummer() {
		simulereAtManntallsnumreErGenerert();

		Voter ikkeGodkjentVelger = opprettOgLagreVelger(VELGER_ID, false);

		assertNull(ikkeGodkjentVelger.getNumber());
	}

	@Test
	public void insert_etterAtManntallsnumreErGenerert_godkjentVelgerFaarManntallsnummer() {
		simulereAtManntallsnumreErGenerert();

		Voter godkjentVelger = opprettOgLagreVelger(VELGER_ID, true);

		assertNotNull(godkjentVelger.getNumber());
	}

	@Test
	public void update_etterAtManntallsnumreErGenerert_ikkeGodkjentVelgerFaarIkkeManntallsnummer() {
		simulereAtManntallsnumreErGenerert();
		Voter ikkeGodkjentVelger = opprettOgLagreVelger(VELGER_ID, false);
		ikkeGodkjentVelger.setApproved(false);
		ikkeGodkjentVelger.setEndringstype(ENDRINGSTYPE_ENDRING);

		Voter fortsattIkkeGodkjentVelger = voterRepository.update(rbacTestFixture.getUserData(), ikkeGodkjentVelger);

		assertNull(fortsattIkkeGodkjentVelger.getNumber());
	}

	@Test
	public void update_etterAtManntallsnumreErGenerert_godkjentVelgerFaarManntallsnummer() {
		simulereAtManntallsnumreErGenerert();
		Voter ikkeGodkjentVelger = opprettOgLagreVelger(VELGER_ID, false);
		ikkeGodkjentVelger.setApproved(true);
		ikkeGodkjentVelger.setEndringstype(ENDRINGSTYPE_ENDRING);

		Voter godkjentVelger = voterRepository.update(rbacTestFixture.getUserData(), ikkeGodkjentVelger);

		assertNotNull(godkjentVelger.getNumber());
	}

	@Test
	public void update_etterAtManntallsnumreErGenerert_godkjentVelgerFaarOgsaaSideOgLinjePaaSisteSide() {
		voterRepository.genererManntallsnumre(electionEvent.getPk());
		Voter ikkeGodkjentVelger = opprettOgLagreVelger(VELGER_ID, false);
		ikkeGodkjentVelger.setApproved(true);
		ikkeGodkjentVelger.setEndringstype(ENDRINGSTYPE_ENDRING);

		Voter godkjentVelger = voterRepository.update(rbacTestFixture.getUserData(), ikkeGodkjentVelger);

		assertThat(godkjentVelger.getElectoralRollPage()).isEqualTo(ANTALL_SIDER_INITIELT_MANNTALL_HALDEN + 1); // Siste side
		assertThat(godkjentVelger.getElectoralRollLine()).isEqualTo(1); // Første endring etter generering av manntall
	}

	@Test
	public void update_etterAtManntallsnumreErGenerert_vedFlyttingTilNyKommuneBeholdesManntallsnummer() {
		// Del 1: Lagre en velger som får initielt manntallsnummer
		simulereAtManntallsnumreErGenerert();
		Voter godkjentVelger = opprettOgLagreVelger(VELGER_ID, true);
		Long initieltManntallsnummer = godkjentVelger.getNumber();

		// Del 2: Oppdatere velgeren og flytte til ny kommune
		oppdaterGeografi(godkjentVelger, kretsIHorten());
		godkjentVelger = voterRepository.update(rbacTestFixture.getUserData(), godkjentVelger);
		Long manntallsnummerEtterFlyttingTilNyKommune = godkjentVelger.getNumber();

		assertThat(manntallsnummerEtterFlyttingTilNyKommune).isEqualTo(initieltManntallsnummer);
	}
	
	@Test
	public void update_etterAtManntallsnumreErGenerert_vedFlyttingTilNyKommuneFaarManSideOgLinjePaaSisteSide() {
		// Del 1: Lagre en velger som får side og linje
		voterRepository.genererManntallsnumre(electionEvent.getPk());
		Voter godkjentVelger = opprettOgLagreVelger(VELGER_ID, true);

		// Del 2: Oppdatere velgeren og flytte til ny kommune
		oppdaterGeografi(godkjentVelger, kretsIHorten());
		godkjentVelger = voterRepository.update(rbacTestFixture.getUserData(), godkjentVelger);

		assertThat(godkjentVelger.getElectoralRollPage()).isEqualTo(ANTALL_SIDER_INITIELT_MANNTALL_HORTEN + 1); // Siste side
		assertThat(godkjentVelger.getElectoralRollLine()).isEqualTo(1); // Første endring etter generering av manntall
	}
	
	private void simulereAtManntallsnumreErGenerert() {
		electionEvent.setVoterNumbersAssignedDate(LocalDate.now());
		electionEventRepository.update(rbacTestFixture.getUserData(), electionEvent);
	}

	private Voter opprettOgLagreVelger(String velgerId, boolean godkjent) {
		Voter velger = lagNyVelger(velgerId, godkjent);
		return voterRepository.create(rbacTestFixture.getUserData(), velger);
	}

	private Voter lagNyVelger(String velgerId, boolean godkjent) {
		Voter velger = new Voter();
		MvArea omraade = kretsIHalden();
		oppdaterGeografi(velger, omraade);
		velger.setElectionEvent(get2007ElectionEvent());
		velger.setId(velgerId);
		velger.setFirstName("Benny");
		velger.setLastName("Bennersen");
		velger.setNameLine("Bennersen Benny");
		velger.setEligible(true);
		velger.setDateTimeSubmitted(DateTime.now().toDate());
		velger.setAarsakskode("02");
		velger.setRegDato(LocalDate.now());
		velger.setSpesRegType('0');
		velger.setAddressLine1("Trettebakken");
		velger.setPostalCode("0755");
		velger.setApproved(godkjent);
		velger.setEndringstype(ENDRINGSTYPE_TILGANG);
		return velger;
	}

	private MvArea kretsIHalden() {
		return mvAreaRepository.findSingleByPath(AreaPath.from(STI_KRETS_HALDEN));
	}

	private void oppdaterGeografi(Voter velger, MvArea nyttOmraade) {
		velger.setCountryId(nyttOmraade.getCountry().getId());
		velger.setCountyId(nyttOmraade.getCounty().getId());
		velger.setMunicipalityId(nyttOmraade.getMunicipality().getId());
		velger.setBoroughId(nyttOmraade.getBorough().getId());
		velger.setPollingDistrictId(nyttOmraade.getPollingDistrict().getId());
		velger.setMvArea(nyttOmraade);
	}

	private MvArea kretsIHorten() {
		return mvAreaRepository.findSingleByPath(AreaPath.from(STI_KRETS_HORTEN));
	}

	private ElectionEvent get2007ElectionEvent() {
		return electionEventRepository.findById(RepositoryTestDataFactory.VALGHENDELSE_ID_2007);
	}
}
