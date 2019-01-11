package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.security.UserData;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.test.data.AreaPathTestData;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.melding.Melding;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

public class RegisterVotingInEnvelopeSearchListenerTest extends BaseFrontendTest {

    private final RegisterVotingInEnvelopeController registerVotingInEnvelopeController = new RegisterVotingInEnvelopeController();
    private RegisterVotingInEnvelopeSearchListener registerVotingInEnvelopeSearchListener;

    @BeforeMethod
    public void setUp() throws Exception {
        registerVotingInEnvelopeSearchListener = initializeMocks(RegisterVotingInEnvelopeSearchListener.class);
        registerVotingInEnvelopeSearchListener.addListener(registerVotingInEnvelopeController);
    }

    @Test
    public void testAddListener_givenMockListener_VerifyListenerAdded() {
        assertEquals(registerVotingInEnvelopeSearchListener.getRegisterVotingInEnvelopeController(), registerVotingInEnvelopeController);
        verify(getInjectMock(ManntallsSokWidget.class), times(1)).addListener(registerVotingInEnvelopeSearchListener);
    }

    @Test
    public void testManntallsSokInit_givenRegisterVotingController_verifyEmptySearchResult() {
        registerVotingInEnvelopeSearchListener.manntallsSokInit();
        assertFalse(registerVotingInEnvelopeSearchListener.getRegisterVotingInEnvelopeController().isEmptySearchResult());
    }

    @Test
    public void testManntallsSokInit_givenRegisterVotingController_verifyNoVoter() {
        registerVotingInEnvelopeSearchListener.manntallsSokInit();
        assertNull(registerVotingInEnvelopeSearchListener.getRegisterVotingInEnvelopeController().getVoter());
    }

    @Test(dataProvider = "manntallsSokVelgerTestData")
    public void testManntallsSokVelger_givenVoter_verifyVoterSet(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeController();

        registerVotingInEnvelopeSearchListener.manntallsSokVelger(voter);
        verify(registerVotingInEnvelopeController, times(1)).setVoter(voter);
    }

    @Test(dataProvider = "manntallsSokVelgerTestData")
    public void testManntallsSokVelger_givenVoter_verifySetEmptySearchResult(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeController();

        registerVotingInEnvelopeSearchListener.manntallsSokVelger(voter);
        verify(registerVotingInEnvelopeController, times(1)).setEmptySearchResult(false);
    }

    @Test(dataProvider = "manntallsSokVelgerTestData")
    public void testManntallsSokVelger_givenVoter_verifyNotShowElectoralRollSearchView(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeController();

        registerVotingInEnvelopeSearchListener.manntallsSokVelger(voter);
        int numberOfInvocations = voter != null ? 1 : 0;
        verify(registerVotingInEnvelopeController, times(numberOfInvocations)).setShowElectoralRollSearchView(false);
    }

    @Test(dataProvider = "manntallsSokVelgerTestData")
    public void testManntallsSokVelger_givenVoter_verifyShowRegisterVotingView(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeController();

        registerVotingInEnvelopeSearchListener.manntallsSokVelger(voter);

        int numberOfInvocations = voter != null ? 1 : 0;
        verify(registerVotingInEnvelopeController, times(numberOfInvocations)).setShowRegisterVotingView(true);
    }

    @Test(dataProvider = "manntallsSokVelgerAddMessageTestData")
    public void testManntallsSokVelger_givenElectionDayVoting_verifyAddMessage(Voter voter, StemmegivningsType stemmegivningsType, boolean shouldAddMessage) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeController(stemmegivningsType);

        registerVotingInEnvelopeSearchListener.manntallsSokVelger(voter);

        int numberOfInvocations = shouldAddMessage ? 1 : 0;
        verify(registerVotingInEnvelopeController, times(numberOfInvocations)).addMessage(any(Melding.class));
    }

    @DataProvider
    public static Object[][] manntallsSokVelgerAddMessageTestData() {
        return new Object[][]{
                {new Voter(), StemmegivningsType.FORHANDSSTEMME_ORDINAER, true},
                {new Voter(), StemmegivningsType.VALGTINGSTEMME_ORDINAER, false}
        };
    }

    @Test(dataProvider = "manntallsSokVelgerTestData")
    public void testManntallsSokVelger_givenVoter_verifyOnSelectedVoterClick(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeController();

        registerVotingInEnvelopeSearchListener.manntallsSokVelger(voter);
        verify(registerVotingInEnvelopeController.getSelectedMenuItem().getVotingController(), times(1)).onSelectedVoterClick(registerVotingInEnvelopeController.getElectionGroupAsMvElection());
    }

    @Test(dataProvider = "manntallsSokVelgerTestData")
    public void testManntallsSokVelger_givenVoter_verifySetVotToOtherMunicipalityConfirmDialog(Voter voter) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeController();

        registerVotingInEnvelopeSearchListener.manntallsSokVelger(voter);
        verify(registerVotingInEnvelopeController, times(1)).setVoteToOtherMunicipalityConfirmDialog(false);
    }

    @Test(dataProvider = "manntallsSokVelgerFindVoterForVotingTestData")
    public void testManntallsSokVelger_givenVoter_verifySetCanRegisterVoting(Voter voter, boolean expectsToBeAbleToRegisterVoting, VelgerSomSkalStemme velgerSomSkalStemme) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeControllerForFindVoterForVotingAndInvokeTestClass(voter, velgerSomSkalStemme);

        int numberOfInvocations = expectsToBeAbleToRegisterVoting ? 1 : 0;
        verify(registerVotingInEnvelopeController, times(numberOfInvocations)).setCanRegisterVoting(expectsToBeAbleToRegisterVoting);
    }

    @Test(dataProvider = "manntallsSokVelgerFindVoterForVotingTestData")
    public void testManntallsSokVelger_findVoterForVoting_givenVoter_verifyBuildVoterMessages(Voter voter, boolean expectsToBeAbleToRegisterVoting, VelgerSomSkalStemme velgerSomSkalStemme) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeControllerForFindVoterForVotingAndInvokeTestClass(voter, velgerSomSkalStemme);

        int numberOfInvocations = voter != null ? 1 : 0;
        verify(registerVotingInEnvelopeController, times(numberOfInvocations)).buildVoterMessages(velgerSomSkalStemme);
    }

    @DataProvider
    public static Object[][] manntallsSokVelgerFindVoterForVotingTestData() {

        VelgerSomSkalStemme velgerSomKanStemme = new VelgerSomSkalStemme(Collections.singletonList(new VotingCategory()));
        velgerSomKanStemme.setKanRegistrereStemmegivning(true);

        VelgerSomSkalStemme velgerSomIkkeKanStemme = new VelgerSomSkalStemme(Collections.singletonList(new VotingCategory()));
        velgerSomKanStemme.setKanRegistrereStemmegivning(true);

        return new Object[][]{
                {new Voter(), true, velgerSomKanStemme},
                {null, false, velgerSomIkkeKanStemme}
        };
    }

    private RegisterVotingInEnvelopeController mockRegisterVotingInEnvelopeControllerForFindVoterForVotingAndInvokeTestClass(Voter voter, VelgerSomSkalStemme velgerSomSkalStemme) throws NoSuchFieldException, IllegalAccessException {
        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockRegisterVotingInEnvelopeController();

        when(getInjectMock(VotingService.class).hentVelgerSomSkalStemme(any(UserData.class), any(StemmegivningsType.class), any(ElectionPath.class), any(AreaPath.class), any(Voter.class))).thenReturn(velgerSomSkalStemme);

        registerVotingInEnvelopeSearchListener.manntallsSokVelger(voter);
        return registerVotingInEnvelopeController;
    }

    private RegisterVotingInEnvelopeController mockRegisterVotingInEnvelopeController() throws NoSuchFieldException, IllegalAccessException {
        return mockRegisterVotingInEnvelopeController(StemmegivningsType.FORHANDSSTEMME_ORDINAER);
    }

    @DataProvider
    public static Object[][] manntallsSokVelgerTestData() {
        return new Object[][]{
                {new Voter()},
                {null}
        };
    }

    private RegisterVotingInEnvelopeController mockRegisterVotingInEnvelopeController(StemmegivningsType stemmegivningsType) throws NoSuchFieldException, IllegalAccessException {
        VotingController votingController = createMock(VotingController.class);

        RegisterVotingInEnvelopeMenuItem menuItem = RegisterVotingInEnvelopeMenuItem.builder().build();
        menuItem.setVotingController(votingController);

        Municipality municipality = Municipality.builder().build();
        municipality.setAvkrysningsmanntallKjort(true);

        MvArea mvArea = MvArea.builder()
                .areaPath(AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111.path())
                .municipality(municipality)
                .build();

        RegisterVotingInEnvelopeController registerVotingInEnvelopeController = mockField("registerVotingInEnvelopeController", RegisterVotingInEnvelopeController.class);

        when(votingController.votingType()).thenReturn(stemmegivningsType);
        when(registerVotingInEnvelopeController.getSelectedMenuItem()).thenReturn(menuItem);
        when(registerVotingInEnvelopeController.getSelectedMvArea()).thenReturn(mvArea);

        return registerVotingInEnvelopeController;
    }
}