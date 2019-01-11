package no.valg.eva.admin.configuration.repository.party;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.valg.eva.admin.common.configuration.model.party.Partikategori;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class PartyRepositoryTest extends AbstractJpaTestBase {

	private static final int EXPECTED_NO_OF_PARTIES = 5;
	private static final int NO_OF_STORTING_AND_LANDSDEKKENDE = 17;
	private static final int EXPECTED_PARTIES = 4;

	private PartyRepository partyRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		partyRepository = new PartyRepository(getEntityManager());
	}

	/**
	 * election event 200701, contest 01 Østfold, party_contest_area knytter to lokale partier til Østfold. Skal finne 1 Stortingsparti, 2 landsdekkende partier
	 * og 2 lokale partier
	 */
	@Test
	public void getPartyWithoutAffiliationList_affiliationsAndLocalPartiesInArea_returnsPartiesInAreaWithoutAffiliations() {
		Contest contest = new Contest();
		contest.setPk(1L);
		contest.setContestAreaSet(makeContestAreas("01"));
		assertThat(partyRepository.getPartyWithoutAffiliationList(contest)).hasSize(EXPECTED_NO_OF_PARTIES);
	}

	/**
	 * election event 200701, contest 02, finne 17 Stortingspartier/landsdekkende partier uten listeforslag (affiliation)
	 */
	@Test
	public void getPartyWithoutAffiliationList_noAffiliationsAndNoLocalPartiesInArea_returnsAllStortingLandsdekkendeAndLocalPartiesInArea() {
		Contest contest = new Contest();
		contest.setPk(2L);
		contest.setContestAreaSet(makeContestAreas("02"));

		List<Party> partyWithoutAffiliationList = partyRepository.getPartyWithoutAffiliationList(contest);

		assertThat(partyWithoutAffiliationList).hasSize(NO_OF_STORTING_AND_LANDSDEKKENDE);
		assertThat(localParties(partyWithoutAffiliationList)).hasSize(0);
	}

	/**
	 * election event 200701, contest 02, finne 17 Stortingspartier/landsdekkende partier uten listeforslag (affiliation)
	 */
	@Test
	public void getPartyWithoutAffiliationList_contestOnMunicipalityWithOneLocalParty_returnsStortingLandsdekkendeAndOneLocalParty() {
		Contest contest = new Contest();
		contest.setPk(1L);
		contest.setContestAreaSet(makeContestAreas("01.0101"));

		List<Party> partyWithoutAffiliationList = partyRepository.getPartyWithoutAffiliationList(contest);

		assertThat(partyWithoutAffiliationList).hasSize(EXPECTED_PARTIES);
		assertThat(localParties(partyWithoutAffiliationList)).hasSize(1);
	}
	
	private List<Party> localParties(List<Party> partyWithoutAffiliationList) {
		return partyWithoutAffiliationList.stream().filter(party -> party.getPartyCategory().getId().equals(Partikategori.LOKALT.getId()))
				.collect(Collectors.toList());
	}

	private Set<ContestArea> makeContestAreas(String areaSubpath) {
		Set<ContestArea> contestAreas = new HashSet<>();
		contestAreas.add(makeContestArea(areaSubpath));
		return contestAreas;
	}

	private ContestArea makeContestArea(String areaSubpath) {
		ContestArea contestArea = new ContestArea();
		contestArea.setMvArea(makeArea(areaSubpath));
		return contestArea;
	}

	private MvArea makeArea(String areaSubpath) {
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath("200701.47." + areaSubpath);
		return mvArea;
	}

}
