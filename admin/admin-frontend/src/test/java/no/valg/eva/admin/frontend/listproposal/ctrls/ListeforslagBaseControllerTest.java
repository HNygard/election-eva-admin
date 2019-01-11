package no.valg.eva.admin.frontend.listproposal.ctrls;

import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.APPROVED_CONFIGURATION;
import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.test.ObjectAssert.assertThat;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ListeforslagBaseControllerTest {

    @DataProvider
    public static Object[][] forTestIsCentralOrLocalConfiguration() {
        return new Object[][]{
                {CENTRAL_CONFIGURATION, false},
                {LOCAL_CONFIGURATION, false},
                {APPROVED_CONFIGURATION, true}
        };
    }

    @DataProvider
    public static Object[][] forTestIsNotCorrectElectionType() {
        return new Object[][]{
                {EvoteConstants.ELECTION_TYPE_REFERENDUM, false},
                {EvoteConstants.ELECTION_TYPE_CALCULATED, true}
        };
    }

    @DataProvider
    public static Object[][] forTestIsContestNotConfigured() {
        return new Object[][]{
                {getContest(null, null, null, null), true},
                {getContest(1, null, null, null), true},
                {getContest(null, 1, null, null), true},
                {getContest(null, null, 1, null), true},
                {getContest(null, null, null, 1), true},
                {getContest(1, 1, 1, 1), false},
        };
    }

    private static Contest getContest(Integer maxCandidates, Integer minCandidates, Integer minProposersNewParty, Integer minProposersOldParty) {
        Contest contest = new Contest();


        contest.setMaxCandidates(maxCandidates);
        contest.setMinCandidates(minCandidates);
        contest.setMinProposersNewParty(minProposersNewParty);
        contest.setMinProposersOldParty(minProposersOldParty);

        return contest;
    }

    @Test(dataProvider = "forTestIsCentralOrLocalConfiguration")
    public void testIsCentralOrLocalConfiguration_withElectionStatus_verifiesConfiguration(ElectionEventStatusEnum electionEventStatusEnum, boolean expectedResult) {
        ElectionEventStatus electionEventStatus = new ElectionEventStatus();
        electionEventStatus.setId(electionEventStatusEnum.id());

        ListeforslagBaseController listeforslagBaseController = new ListeforslagBaseController();

        assertThat(listeforslagBaseController.isElectionEventLocked(electionEventStatus)).isEqualTo(expectedResult);
    }

    @Test(dataProvider = "forTestIsNotCorrectElectionType")
    public void testIsNotCorrectElectionType_Given_ElectionTypeIds_verifiesElectionType(String electionTypeId, boolean expectedResult) {
        ElectionType electionType = new ElectionType();
        electionType.setId(electionTypeId);

        ListeforslagBaseController listeforslagBaseController = new ListeforslagBaseController();
        assertThat(listeforslagBaseController.isElectionTypeCalculated(electionType)).isEqualTo(expectedResult);
    }

    @Test(dataProvider = "forTestIsContestNotConfigured")
    public void testIsContestNotConfigured_GivenContest_verifiesConfigurationStatus(Contest contest, boolean expectedResult) {
        ListeforslagBaseController listeforslagBaseController = new ListeforslagBaseController();


        assertThat(listeforslagBaseController.isContestNotConfigured(contest)).isEqualTo(expectedResult);
    }
}