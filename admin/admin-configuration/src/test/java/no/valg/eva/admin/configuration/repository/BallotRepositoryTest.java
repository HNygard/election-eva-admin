package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.common.configuration.model.ballot.PartyData;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class BallotRepositoryTest extends AbstractJpaTestBase {

	private BallotRepository ballotRepository;
	
	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		new BallotRepository();
		ballotRepository = new BallotRepository(getEntityManager());
	}

	@Test
	public void partiesForContest_returnsPartyData() {
		Contest contest = new Contest();
		contest.setPk(1L);
		List<PartyData> partyDataList = ballotRepository.partiesForContest(contest);
		assertThat(partyDataList).isNotEmpty();
		for (PartyData partyData : partyDataList) {
			assertThat(partyData.getPartyId()).isNotEmpty();
		}
	}
}
