package no.valg.eva.admin.voting.repository;

import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@Test(groups = TestGroups.REPOSITORY)
public class VotingRejectionRepositoryTest extends AbstractJpaTestBase {

    VotingRejectionRepository votingRejectionRepository;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        votingRejectionRepository = new VotingRejectionRepository(getEntityManager());
    }

    @Test
    public void findByPk_givenPk_returnsVotingRejection() {
        VotingRejection byPk = votingRejectionRepository.findByPk(1L);
        assertThat(byPk.getId()).isEqualTo("F0");
    }

    @Test
    public void findById_givenId_returnsVotingRejection() {
        VotingRejection byId = votingRejectionRepository.findById("F0");
        assertThat(byId.getPk()).isEqualTo(1L);
    }

    @Test(dataProvider = "isEarly")
    public void findByEarly_givenVoting_returnsVotingRejections(boolean isEarly, boolean expected) {
        Voting voting = createMock(Voting.class);
        when(voting.getVotingCategory().isEarlyVoting()).thenReturn(isEarly);
        List<VotingRejection> byEarly = votingRejectionRepository.findByEarly(voting);
        for (VotingRejection votingRejection : byEarly) {
            assertThat(votingRejection.isEarlyVoting()).isEqualTo(expected);
        }

    }

    @DataProvider
    public Object[][] isEarly() {
        return new Object[][]{
                {true, true},
                {false, false}
        };
    }

    @Test
    public void testFindAll() {
        Assert.assertTrue(!votingRejectionRepository.findAll().isEmpty());
    }
}