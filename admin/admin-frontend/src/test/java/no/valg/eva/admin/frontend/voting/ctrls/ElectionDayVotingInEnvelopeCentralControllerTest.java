package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.service.VotingRegistrationService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.VALGTINGSTEMME_KONVOLUTTER_SENTRALT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ElectionDayVotingInEnvelopeCentralControllerTest extends BaseFrontendTest {

    private ElectionDayVotingInEnvelopeCentralController controller;
    private VotingRegistrationService votingRegistrationService;
    private Municipality municipality;
    private MvElection mvElection;
    private MvArea mvArea;

    @BeforeMethod
    public void setup() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
        controller = initializeMocks(ElectionDayVotingInEnvelopeCentralController.class);

        votingRegistrationService = getInjectMock(VotingRegistrationService.class);
        municipality = createMock(Municipality.class);
        mvElection = createMock(MvElection.class);
        mvArea = createMock(MvArea.class);
    }

    @Test
    public void testGetPollingPlaceLevel_expectingCounty() {
        assertThat(controller.getPollingPlaceElectionGeoLevel()).isSameAs(KOMMUNE);
    }

    @Test
    public void testGetVotingType_expectingElectionDayVotesInEnvelopesCentral() {
        assertThat(controller.votingType()).isSameAs(VALGTINGSTEMME_KONVOLUTTER_SENTRALT);
    }

    @Test
    public void testRegisterVoting_givenVoterRegisteringVoteCentrally_verifiesFacesMessage() throws NoSuchFieldException, IllegalAccessException {
        Voter voter = voterMock();

        mockFieldValue("votingCategory", VS);
        mockFieldValue("pollingPlaceMvArea", mvArea);

        Voting voting = voting(voter);
        when(votingRegistrationService.registerElectionDayVotingInEnvelopeCentrally(
                any(UserData.class),
                any(ElectionGroup.class),
                any(Municipality.class),
                any(Voter.class),
                any(no.valg.eva.admin.common.voting.VotingCategory.class),
                any(VotingPhase.class)))
                .thenReturn(voting);

        controller.registerVoting(voter, mvElection, municipality, VotingPhase.ELECTION_DAY);
        assertFacesMessage(SEVERITY_INFO, "[@voting.markOff.registerVoteCentrally[VS], Test Testesen, @common.date.weekday[1].name, 01.01.2017, 12:12, VS, 1]");
    }

    @Test
    public void testSetVoting_givenVoting_verifiesSetter() {
        Voting voting = voting(null);
        controller.setVoting(voting);
        assertEquals(controller.getVoting(), voting);
    }

    private Voting voting(Voter voter) {
        VotingCategory votingCategory = new VotingCategory();
        votingCategory.setId(VS.getId());

        DateTime dateTime = DateTime.parse("2017-01-01T12:12:00");

        Voting voting = new Voting();
        voting.setPk(1L);
        voting.setCastTimestamp(dateTime);
        voting.setVotingCategory(votingCategory);
        voting.setVoter(voter);
        voting.setVotingNumber(1);

        return voting;
    }

    private Voter voterMock() {
        Voter voter = new Voter();
        voter.setFictitious(false);
        voter.setNameLine("Test Testesen");
        return voter;
    }
}
