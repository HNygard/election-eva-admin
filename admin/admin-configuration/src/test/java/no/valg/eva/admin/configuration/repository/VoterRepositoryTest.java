package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.PersistenceUnitUtil;

import no.evote.model.views.VoterAudit;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
/* DEV-NOTE: Merk at det er egne testklasser for manntallsnummer/side/linje-tesing */
public class VoterRepositoryTest extends AbstractJpaTestBase {

	private static final long ELECTION_EVENT_PK = 1L;
	private static final String VOTER_ID = "1234567890";
	private static final int ANTALL_VELGERE_I_HALDEN_KRETS_0001_I_TESTDATA = 5010;

	private VoterRepository voterRepository;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		voterRepository = new VoterRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
	}

	@Test
	public void voterOfId_givenVoterId_returnsVoter() {
		Voter voter = getDefaultVoter();
		genericTestRepository.createEntity(voter);

		assertThat(voterRepository.voterOfId(VOTER_ID, ELECTION_EVENT_PK)).isNotNull();
	}

	@Test
	public void getVotersWithoutPollingDistricts_withVotersWithoutPollingDistricts_returnsVoters() {
		Voter voter = getDefaultVoter();
		genericTestRepository.createEntity(voter);

		List<String[]> voters = voterRepository.getVotersWithoutPollingDistricts(ELECTION_EVENT_PK);

		assertThat(voters).hasSize(1);
	}

	@Test
	public void findByElectionEventAreaAndId_withValidPath_returnsOneVoter() throws Exception {
		Voter voter = getDefaultVoter();
		genericTestRepository.createEntity(voter);
		AreaPath areaPath = AreaPath.from("000000.47.01");

		List<Voter> result = voterRepository.findByElectionEventAreaAndId(ELECTION_EVENT_PK, areaPath, VOTER_ID);

		assertThat(result).hasSize(1);

	}

	@Test(dataProvider = "antalTreffPrOmraadesti")
	public void findByOmraadesti_gittEnOmraadesti_returnerAntallManntallsoppfoeringerIKretsen(AreaPath omraadesti, Integer forventetAntallVelgereIKrets) {
		List<Voter> result = voterRepository.findByOmraadesti(omraadesti);
		assertThat(result).hasSize(forventetAntallVelgereIKrets);
	}

	@DataProvider
	private Object[][] antalTreffPrOmraadesti() {
		return new Object[][] {
				{ omraadesti("200701.47.01.0101.010100.0001"), ANTALL_VELGERE_I_HALDEN_KRETS_0001_I_TESTDATA},
				{ omraadesti("200701.47.01.0101.010100.0008"), 0 }
		};
	}

	private AreaPath omraadesti(String omraadesti) {
		return new AreaPath(omraadesti);
	}

	private Voter getDefaultVoter() {
		Voter voter = new Voter();
		voter.setId(VOTER_ID);
		voter.setBoroughId("0001");
		voter.setCountryId("47");
		voter.setCountyId("01");
		voter.setMunicipalityId(AreaPath.OSLO_MUNICIPALITY_ID);
		voter.setPollingDistrictId("0101");
		voter.setDateTimeSubmitted(DateTime.now().toDate());
		voter.setElectionEvent(genericTestRepository.findEntityByProperty(ElectionEvent.class, "pk", ELECTION_EVENT_PK));
		voter.setFirstName("Ola");
		voter.setLastName("Nordmann");
		voter.setNameLine("Ola Nordmann");
		return voter;
	}
	
	@Test
	public void findVotersForValgkortgrunnlag_forEnValghendelse_henterUtAlleVelgereMedEagerLoading() {
		PersistenceUnitUtil puUtil = getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
		ElectionEvent valghendelse2007 = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");
		
		List<Voter> velgere = voterRepository.findVotersForValgkortgrunnlag(valghendelse2007);
		
		assertThat(velgere.size()).isEqualTo(22896);

		Voter velger = velgere.get(0);
		assertThat(puUtil.isLoaded(velger, "mvArea")).isTrue();
		
		MvArea mvArea = velger.getMvArea();
		assertThat(puUtil.isLoaded(mvArea, "pollingDistrict")).isTrue();

		PollingDistrict stemmekrets = mvArea.getPollingDistrict();
		assertThat(puUtil.isLoaded(stemmekrets, "pollingPlaces")).isTrue();
	}

	@Test
	public void deleteVoters_alleVelgereSlettes() {
		Voter voter = getDefaultVoter();
		genericTestRepository.createEntity(voter);
		List<Voter> voters = voterRepository.findVotersByElectionEvent(ELECTION_EVENT_PK);
		assertThat(voters).hasSize(1);
		voterRepository.deleteVoters("000000", "000000");

		voters = voterRepository.findVotersByElectionEvent(ELECTION_EVENT_PK);
		assertThat(voters).isEmpty();
	}

	@Test
	public void deleteAuditVoters_historikkSlettes() {
		assertThat(genericTestRepository.findEntitiesByProperty(VoterAudit.class, "voterId", VOTER_ID)).isEmpty();

		Voter voter = getDefaultVoter();
		genericTestRepository.createEntity(voter);
		assertThat(genericTestRepository.findEntitiesByProperty(VoterAudit.class, "voterId", VOTER_ID)).hasSize(1);

		voterRepository.deleteVoters("000000", "000000");
		assertThat(genericTestRepository.findEntitiesByProperty(VoterAudit.class, "voterId", VOTER_ID)).hasSize(2);

		voterRepository.deleteAuditVoters("000000");
		assertThat(genericTestRepository.findEntitiesByProperty(VoterAudit.class, "voterId", VOTER_ID)).isEmpty();
	}
}
