package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.voting.VotingService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.service.VotingRegistrationService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering.ForhandKonvolutterSentraltController;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static java.lang.String.format;
import static java.util.EnumSet.allOf;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.evote.util.MockUtils.setPrivateField;
import static no.valg.eva.admin.common.AreaPath.CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID;
import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.ADVANCE_VOTING_LATE_ARRIVAL_CONFIRM_SEARCH_DIALOG;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_KONVOLUTTER_SENTRALT;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class AdvanceVotingInEnvelopeCentralControllerTest extends BaseFrontendTest {

    private AdvanceVotingInEnvelopeCentralController votingController;
    private MvArea mvArea;
    private MvAreaService mvAreaService;
    private MvElection mvElection;
    private VotingRegistrationService votingRegistrationService;
    private ElectionGroup electionGroup;
    private Municipality municipality;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        votingController = initializeMocks(AdvanceVotingInEnvelopeCentralController.class);
        mvAreaService = getInjectMock(MvAreaService.class);
        votingRegistrationService = getInjectMock(VotingRegistrationService.class);

        PollingPlace pollingPlace = new PollingPlace();
        pollingPlace.setAdvanceVoteInBallotBox(true);

        municipality = Municipality.builder().build();
        municipality.setPk(1L);

        mvArea = mockField("pollingPlaceMvArea", MvArea.class);
        when(mvArea.getPollingPlace()).thenReturn(pollingPlace);
        when(mvArea.getMunicipality()).thenReturn(municipality);
        when(mvArea.getAreaName(any(AreaLevelEnum.class))).thenReturn("areaName");

        electionGroup = new ElectionGroup();
        electionGroup.setPk(1L);

        mvElection = new MvElection();
        mvElection.setPk(1L);
        mvElection.setElectionGroup(electionGroup);
    }

    @Test
    public void getPollingPlaceElectionGeoLevel_returnsValggeografiNivaaKommune() {
        assertThat(votingController.getPollingPlaceElectionGeoLevel()).isEqualTo(ValggeografiNivaa.KOMMUNE);
    }

    @Test(dataProvider = "votingPhases")
    public void testGetVotingType_VerifiesLateArrival(VotingPhase votingPhase) throws NoSuchFieldException, IllegalAccessException {
        mockFieldValue("votingPhase", votingPhase);
        if (VotingPhase.LATE == votingPhase) {
            assertThat(votingController.votingType()).isSameAs(FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER);
        } else {
            assertThat(votingController.votingType()).isSameAs(FORHANDSSTEMME_KONVOLUTTER_SENTRALT);
        }
    }

    @DataProvider
    public Object[][] votingPhases() {
        return allOf(VotingPhase.class).stream()
                .map(votingPhase -> new Object[]{votingPhase})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "registerVotingTestData")
    public void testRegisterVotingInEnvelope_givenVoterAndCategory_verifiesMessage(Voter voter, VotingCategory votingCategory, Voting voting, String expectedMessage) {
        when(votingRegistrationService.registerAdvanceVotingInEnvelope(any(UserData.class), any(PollingPlace.class), any(ElectionGroup.class), any(no.valg.eva.admin.common.configuration.model.Municipality.class), any(Voter.class), any(VotingCategory.class),
                anyBoolean(), any(VotingPhase.class)))
                .thenReturn(voting);

        votingController.setVotingCategory(votingCategory);
        votingController.setPollingPlaceMvArea(mvArea);

        votingController.registerVoting(voter, mvElection, municipality, VotingPhase.ADVANCE);

        assertFacesMessage(SEVERITY_INFO, expectedMessage);
    }

    @DataProvider
    public Object[][] registerVotingTestData() {
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = formattedTime(now);
        String formattedDate = formattedDate(now);

        return EnumSet.allOf(VotingCategory.class).stream()
                .filter(VotingCategory::isAdvanceVotingCategory)
                .map(votingCategory ->
                        new Object[]{voter(), votingCategory, voting(votingCategory),
                                registerVotingMessage(DateUtil.dayOfWeek(now.toLocalDate()), votingCategory, formattedTime, formattedDate)})
                .toArray(Object[][]::new);
    }

    private String registerVotingMessage(int weekDay, VotingCategory votingCategory, String formattedTime, String formattedDate) {

        String messageEnd = FA == votingCategory ?
                "@voting.markOff.advanceForeignEnvelope" :
                format("[%s, %s, null]", "@voting.markOff.votingNumberEnvelope", votingCategory.getId());

        return format("[@voting.markOff.voterMarkedOffAdvance, nameLine, " +
                        "@common.date.weekday[%s].name, %s, %s, %s, null]",
                weekDay, formattedDate, formattedTime, votingCategory.getId())
                + " "
                + messageEnd;
    }

    @Test(dataProvider = "onSelectedVotingCategoryTestData")
    public void testOnSelectedVotingCategory_GivenCategory_VerifiesPollingPlaceSet(VotingCategory votingCategory, VotingPhase votingPhase) {
        when(mvAreaService.findByMunicipalityAndPollingPlaceId(getUserDataMock(),
                mvArea.getMunicipality().getPk(),
                CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID))
                .thenReturn(mvArea);

        votingController.onSelectedVotingCategory(mvArea, votingCategory, votingPhase);

        assertEquals(votingController.getPollingPlaceMvArea(), mvArea);
        assertEquals(votingController.getVotingCategory(), votingCategory);
    }

    @DataProvider
    public Object[][] onSelectedVotingCategoryTestData() {
        return new Object[][]{
                {VotingCategory.FB, VotingPhase.ADVANCE}
        };
    }

    @Test
    public void testGetPageTitleMeta_givenMvArea_verifiesPageTitleMetaInvocation() throws NoSuchFieldException, IllegalAccessException {
        setPrivateField(votingController, "county", mvArea);

        PageTitleMetaBuilder pageTitleMetaBuilder = getInjectMock(PageTitleMetaBuilder.class);
        votingController.getPageTitleMeta();

        verify(pageTitleMetaBuilder, times(1)).area(mvArea);
    }

    @Test
    public void testDeleteAdvanceVoting_givenVoting_verifiesServiceCall() throws NoSuchFieldException, IllegalAccessException {
        Voting voting = voting(VotingCategory.FB);

        setPrivateField(votingController, "voting", voting);

        votingController.deleteAdvanceVoting();

        verify(getInjectMock(VotingService.class), times(1)).delete(getUserDataMock(), voting);
    }

    @Test(dataProvider = "deleteAdvanceVotingTestData")
    public void testDeleteAdvanceVoting_givenVoting_verifiesMessage(VotingCategory votingCategory, String expectedMessage) throws NoSuchFieldException, IllegalAccessException {
        mockVotingController(votingCategory);

        votingController.deleteAdvanceVoting();

        assertFacesMessage(SEVERITY_INFO, expectedMessage);
    }

    private void mockVotingController(VotingCategory votingCategory) throws NoSuchFieldException, IllegalAccessException {
        Voting voting = voting(votingCategory);

        setPrivateField(votingController, "voting", voting);
    }

    @DataProvider
    public Object[][] deleteAdvanceVotingTestData() {

        return EnumSet.allOf(VotingCategory.class).stream()
                .filter(VotingCategory::isAdvanceVotingCategory)
                .map(votingCategory ->
                        new Object[]{votingCategory, deleteAdvanceVotingExpectedMessage(votingCategory)})
                .toArray(Object[][]::new);
    }

    @Test
    public void testDeleteAdvanceVoting_givenVoting_verifiesVotingSetToNull() throws NoSuchFieldException, IllegalAccessException {
        mockVotingController(VotingCategory.FB);

        votingController.deleteAdvanceVoting();

        assertNull(votingController.getVoting());
    }

    private String deleteAdvanceVotingExpectedMessage(VotingCategory votingCategory) {
        if (FA == votingCategory) {
            return "[@voting.requestRemoveAdvanceVotingFA.response, nameLine]";
        } else {
            return String.format("[@voting.requestRemoveAdvanceVoting.response, %s, null, nameLine]", votingCategory.getId());
        }
    }

    @Test
    public void getStemmestedNiva_returnererKommune() throws Exception {
        ForhandKonvolutterSentraltController ctrl = initializeMocks(ForhandKonvolutterSentraltController.class);

        assertThat(ctrl.getStemmestedNiva()).isSameAs(KOMMUNE);
    }

    @Test
    public void getStemmegivningsType_returnererForhandsstemmerKonvolutterSentralt() throws Exception {
        ForhandKonvolutterSentraltController ctrl = initializeMocks(ForhandKonvolutterSentraltController.class);

        assertThat(ctrl.getStemmegivningsType()).isSameAs(FORHANDSSTEMME_KONVOLUTTER_SENTRALT);
    }

    @Test
    public void kontekstKlar_medStemmestedOgAvkryssningsmanntallKjort_verifiserState() throws Exception {
        ForhandKonvolutterSentraltController ctrl = initializeMocks(ForhandKonvolutterSentraltController.class);
        ctrl.setStemmested(stemmested());
        MvArea stemmested = stub_findByMunicipalityAndPollingPlaceId();
        when(stemmested.getMunicipality().isAvkrysningsmanntallKjort()).thenReturn(true);

        ctrl.kontekstKlar();

        assertThat(ctrl.getStemmested()).isSameAs(stemmested);
        assertThat(ctrl.getStatiskeMeldinger()).hasSize(1);
        assertThat(ctrl.getStatiskeMeldinger().get(0).getText()).isEqualTo("@voting.search.forhåndsstemmerStengtPgaAvkrysningsmanntallKjort");
    }

    @Test
    public void manntallsSokVelger_medNullOgAvkryssningsmanntallKjort_sjekkMelding() throws Exception {
        ForhandKonvolutterSentraltController ctrl = initializeMocks(ForhandKonvolutterSentraltController.class);
        MvArea stemmested = new MvAreaBuilder(AREA_PATH_POLLING_PLACE_ENVELOPE).getValue();
        when(stemmested.getMunicipality().isAvkrysningsmanntallKjort()).thenReturn(true);
        ctrl.setStemmested(stemmested);

        ctrl.manntallsSokVelger(null);

        assertThat(ctrl.getStatiskeMeldinger()).hasSize(1);
        assertThat(ctrl.getStatiskeMeldinger().get(0).getText()).isEqualTo("@voting.search.forhåndsstemmerStengtPgaAvkrysningsmanntallKjort");
    }

    @DataProvider
    public Object[][] isStemmetypeDisabled() {
        return new Object[][]{
                {false, false, false},
                {true, false, false},
                {false, true, true},
                {true, true, false},
        };
    }

    @Test
    public void isVisOpprettFiktivVelgerLink_medTomtSokOgIkkeRettIUrne_returnererTrue() throws Exception {
        AdvanceVotingInEnvelopeCentralControllerTest.MyController ctrl = ctrl();
        ctrl.setIngenVelgerFunnet(true);
        ctrl.setForhandsstemmeRettIUrne(false);

        assertThat(ctrl.isVisOpprettFiktivVelgerLink()).isTrue();
    }

    @Test
    public void isVisSlettForhandsstemmeLink_medStemmegivningIkkeRettIUrne_returnererTrue() throws Exception {
        AdvanceVotingInEnvelopeCentralControllerTest.MyController ctrl = ctrl();
        ctrl.setVoting(createMock(Voting.class));
        ctrl.setForhandsstemmeRettIUrne(false);

        assertThat(ctrl.isShowDeleteAdvanceVotingLink()).isTrue();
    }

    private MvArea stemmested() {
        return new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue();
    }

    private MvArea stub_findByMunicipalityAndPollingPlaceId() {
        MvArea stemmested = new MvAreaBuilder(AREA_PATH_POLLING_PLACE_ENVELOPE).getValue();
        when(getInjectMock(MvAreaService.class).findByMunicipalityAndPollingPlaceId(getUserDataMock(), 4444L, CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID))
                .thenReturn(stemmested);
        return stemmested;
    }

    private AdvanceVotingInEnvelopeCentralControllerTest.MyController ctrl() throws Exception {
        return initializeMocks(new MyController());
    }

    private Voting voting(VotingCategory votingCategory) {
        Voting voting = new Voting();
        voting.setPk(2L);
        voting.setCastTimestamp(DateTime.now());
        voting.setVotingCategory(domainVotingCategory(votingCategory));
        voting.setVoter(voter());

        return voting;
    }

    private no.valg.eva.admin.configuration.domain.model.VotingCategory domainVotingCategory(VotingCategory votingCategory) {
        no.valg.eva.admin.configuration.domain.model.VotingCategory domainVotingCategory = new no.valg.eva.admin.configuration.domain.model.VotingCategory();
        domainVotingCategory.setId(votingCategory.getId());

        return domainVotingCategory;
    }

    private Voter voter() {
        return Voter.builder()
                .municipalityId("1")
                .nameLine("nameLine")
                .build();
    }

    private static class MyController extends AdvanceVotingInEnvelopeCentralController {

        private boolean velgerEgenKommune;
        private boolean forhandsstemmeRettIUrne;
        @Getter
        @Setter
        private boolean canRegisterVoting;

        private boolean ingenVelgerFunnet;

        @Override
        String timeString(DateTime dateTime) {
            return "12:12";
        }

        @Override
        public boolean isVelgerEgenKommune(Voter voter) {
            return velgerEgenKommune;
        }

        public void setVelgerEgenKommune(boolean velgerEgenKommune) {
            this.velgerEgenKommune = velgerEgenKommune;
        }

        @Override
        public boolean isForhandsstemmeRettIUrne() {
            return forhandsstemmeRettIUrne;
        }

        public void setForhandsstemmeRettIUrne(boolean forhandsstemmeRettIUrne) {
            this.forhandsstemmeRettIUrne = forhandsstemmeRettIUrne;
        }

        @Override
        public boolean emptyVoterSearchResult() {
            return ingenVelgerFunnet;
        }

        public void setIngenVelgerFunnet(boolean ingenVelgerFunnet) {
            this.ingenVelgerFunnet = ingenVelgerFunnet;
        }

    }

    @Test(dataProvider = "showDialogDataProvider")
    public void testIsShowDialog_VerifiesShowDialog(VotingCategory votingCategory, VotingPhase votingPhase, MvArea mvaArea, boolean isElectronicMarkoffs,
                                                    boolean electronicMarkoffsHasBeenRun, Municipality municipality, boolean dialogShown, boolean expectedOutcome) throws Exception {
        setIsShowDialog(votingCategory, votingPhase, mvaArea, isElectronicMarkoffs, electronicMarkoffsHasBeenRun, municipality, dialogShown);
        assertEquals(votingController.isShowDialog(), expectedOutcome);
    }

    @DataProvider
    public Object[][] showDialogDataProvider() {
        return new Object[][]{
                {FI, VotingPhase.LATE, new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue(), false, false, new Municipality(), false, true},
                {FI, VotingPhase.LATE, new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue(), false, false, null, false, false},
                {FI, VotingPhase.LATE, new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue(), false, false, new Municipality(), true, false},
                {FI, VotingPhase.ADVANCE, new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue(), false, false, new Municipality(), false, false}
        };
    }

    private void setIsShowDialog(VotingCategory votingCategory, VotingPhase votingPhase, MvArea pollingPlace, boolean isElectronicMarkoffs,
                                 boolean electronicMarkoffsHasBeenRun, Municipality municipality, boolean dialogShown)
            throws Exception {
        mockFieldValue("votingCategory", votingCategory);
        mockFieldValue("votingPhase", votingPhase);
        mockFieldValue("pollingPlaceMvArea", pollingPlace);
        mockFieldValue("dialogShown", dialogShown);
        when(pollingPlace.getMunicipality().isElectronicMarkoffs()).thenReturn(isElectronicMarkoffs);
        when(pollingPlace.getMunicipality().isAvkrysningsmanntallKjort()).thenReturn(electronicMarkoffsHasBeenRun);
        when(pollingPlace.getMunicipality()).thenReturn(municipality);

    }

    @Test
    public void getConfirmLateValidationSearchModal_returnsDialog() {
        assertThat(votingController.getConfirmLateValidationSearchModal()).isEqualTo(ADVANCE_VOTING_LATE_ARRIVAL_CONFIRM_SEARCH_DIALOG);
    }

}
