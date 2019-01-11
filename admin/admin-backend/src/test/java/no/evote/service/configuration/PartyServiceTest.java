package no.evote.service.configuration;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.List;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.model.party.Partikategori;
import no.valg.eva.admin.configuration.application.party.PartyMapper;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.REPOSITORY })
public class PartyServiceTest extends ListProposalBaseTest {

	private static final int SHORT_CODE = 9998;
	private static final int SHORT_CODE_2 = 7777;
	private static final int EXPECTED_NO_OF_ERRORS = 1;
	private static final int NEXT_NOT_LOCAL_PARTY_NUMBER_FROM_PARTY_NUMBER_TXT = 1801;
	private static final int NEXT_LOCAL_PARTY_NUMBER_FROM_PARTY_NUMBER_TXT = 5601;
	private static final long STORTING_PARTY_CAT_PK = 1L;
	private static final long LANDSDEKKENDE_PARTY_CAT_PK = 2L;
	private static final long LOCAL_PARTY_CAT_PK = 3L;

	private PartyServiceBean partyService;
	private PartyRepository partyRepository;
	private PartyCategoryRepository partyCategoryRepository;
	private PartyMapper partyMapper;
	private GenericTestRepository genericTestRepository;

	@Override
	@BeforeMethod(alwaysRun = true)
	public void init() {
		super.init();
		partyService = backend.getPartyServiceBean();
		partyRepository = backend.getPartyRepository();
		partyCategoryRepository = backend.getPartyCategoryRepository();
		partyMapper = backend.getPartyMapper();
		genericTestRepository = backend.getGenericTestRepository();
	}

	@DataProvider(name = "partyCatPartyNumber")
	public Object[][] getPartyCatPartyNumber() {
		return new Object[][]{
				{LANDSDEKKENDE_PARTY_CAT_PK, NEXT_NOT_LOCAL_PARTY_NUMBER_FROM_PARTY_NUMBER_TXT},
				{LOCAL_PARTY_CAT_PK, NEXT_LOCAL_PARTY_NUMBER_FROM_PARTY_NUMBER_TXT},
				{STORTING_PARTY_CAT_PK, NEXT_NOT_LOCAL_PARTY_NUMBER_FROM_PARTY_NUMBER_TXT}
		};
	}
	
	@Test(dataProvider = "partyCatPartyNumber")
	public void create_shortCodeNotSet_shortCodeIsGenerated(long partyCatPk, int expected) {
		Party party = new Party();
		party.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(partyCatPk));
		party.setId("ANYID");
		party.setTranslatedPartyName("AnyName");
		party.setElectionEvent(genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701"));

		party = partyService.create(rbacTestFixture.getUserData(), party);
		
		assertThat(party.getShortCode()).isEqualTo(expected);
	}

	@Test
	public void update_oppdaterer() {
		Party party = new Party();
		party.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(1L));
		party.setShortCode(SHORT_CODE_2);
		party.setElectionEvent(getElectionEvent());
		party.setTranslatedPartyName("AnyName");

		party.setId("IDTWO");
		party = partyService.create(rbacTestFixture.getUserData(), party);

		Parti parti = partyMapper.toParti(rbacTestFixture.getUserData(), party);
		parti.setOversattNavn("AnyName");

		party = partyService.update(rbacTestFixture.getUserData(), parti);
		Assert.assertEquals("IDTWO", party.getId());
		party = partyService.update(rbacTestFixture.getUserData(), parti);
		Assert.assertEquals("@party[IDTWO].name", party.getName());
	}

	@Test
	public void delete_medEksisterendeParti_sletterPartiet() {
		Party party = new Party();
		party.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(1L));
		party.setShortCode(SHORT_CODE_2);
		party.setElectionEvent(getElectionEvent());
		party.setTranslatedPartyName("AnyName");

		party.setId("IDTWO");
		party = partyService.create(rbacTestFixture.getUserData(), party);

		partyService.delete(rbacTestFixture.getUserData(), party);
	}
	
	private Parti getParti() {
		Parti parti = new Parti(Partikategori.STORTING, "ID");
		parti.setOversattNavn("Navn");
		return parti;
	}

	@Test
	public void testValidateParty() {
		Parti parti = getParti();

		Assert.assertEquals(partyService.validateParty(rbacTestFixture.getUserData(), parti).size(), 0);

		Assert.assertEquals(partyService.validateParty(rbacTestFixture.getUserData(), parti).size(), 0);

		Parti idDuplicateParti = new Parti(Partikategori.STORTING, "DUPID");
		idDuplicateParti.setOversattNavn("EtNavn");

		Assert.assertEquals(partyService.validateParty(rbacTestFixture.getUserData(), idDuplicateParti).size(), 0);

		Party createdParty = partyService.create(rbacTestFixture.getUserData(),
				partyMapper.toParty(idDuplicateParti, rbacTestFixture.getUserData().electionEvent()));

		Parti idDuplicateParti2 = new Parti(Partikategori.STORTING, "DUPID");

		Assert.assertEquals(partyService.validateParty(rbacTestFixture.getUserData(), idDuplicateParti2).size(), EXPECTED_NO_OF_ERRORS);

		idDuplicateParti.setPartyPk(createdParty.getPk());
		partyService.delete(rbacTestFixture.getUserData(), partyMapper.toParty(idDuplicateParti, rbacTestFixture.getUserData().electionEvent()));
	}

	@Test
	public void testValidatePartyDelete() {
		Assert.assertTrue(partyService.validatePartyForDelete(rbacTestFixture.getUserData(), getTestParty2()).size() == 0);
	}

	@Test
	public void testFindAllInEvent() {
		Assert.assertFalse(partyRepository.findAllPartiesInEvent(rbacTestFixture.getUserData().getElectionEventPk()).isEmpty());

		Assert.assertFalse(partyRepository.findAllPartiesInEvent(getElectionEvent().getPk()).isEmpty());
	}

	@Test
	public void testFindAllPartiesButNotBlank() {
		List<Party> partyListNoBlank = partyRepository.findAllButNotBlank(getElectionEvent().getPk());
		Assert.assertFalse(partyListNoBlank.isEmpty());

		for (Party party : partyListNoBlank) {
			Assert.assertFalse(party.getId().equalsIgnoreCase(EvoteConstants.PARTY_ID_BLANK));
		}
	}

	@Test
	public void testFindPartyByShortCodeAndEvent() {
		Party party = partyRepository.findPartyByShortCodeAndEvent(0, getElectionEvent().getPk());
		Assert.assertTrue(party == null);

		party = partyRepository.findPartyByShortCodeAndEvent(null, getElectionEvent().getPk());
		Assert.assertTrue(party == null);

		party = partyRepository.findPartyByShortCodeAndEvent(SHORT_CODE, getElectionEvent().getPk());
		Assert.assertTrue(party != null);
	}
}
