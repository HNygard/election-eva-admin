package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.util.Calendar.DAY_OF_WEEK;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class VotingControllerTest extends BaseFrontendTest {

    private VotingControllerMock votingControllerMock;

    @BeforeMethod
    public void beforeTest() throws IllegalAccessException, InstantiationException, NoSuchFieldException, java.lang.reflect.InvocationTargetException {
        votingControllerMock = initializeMocks(VotingControllerMock.class);
    }

    @Test(dataProvider = "onSelectedVotingCategoryTestData")
    public void testOnSelectedVotingCategory_givenVotingCategory_verifiesVotingCategorySelected(VotingCategory votingCategory, VotingPhase votingPhase, MvArea mvArea) {
        votingControllerMock.onSelectedVotingCategory(MvArea.builder().build(), votingCategory, votingPhase);
        assertEquals(votingControllerMock.getVotingCategory(), votingCategory);
        assertEquals(votingControllerMock.getPollingPlaceMvArea(), mvArea);
        assertEquals(votingControllerMock.getCounty(), mvArea);
    }

    @DataProvider
    public Object[][] onSelectedVotingCategoryTestData() {
        MvArea mvArea = MvArea.builder().build();
        return new Object[][]{
                {VotingCategory.FI, VotingPhase.ADVANCE, mvArea},
                {VotingCategory.FI, VotingPhase.EARLY, mvArea},
                {VotingCategory.FU, VotingPhase.EARLY, mvArea},
                {VotingCategory.FB, VotingPhase.EARLY, mvArea},
                {VotingCategory.FE, VotingPhase.EARLY, mvArea},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, mvArea},
                {VotingCategory.VF, VotingPhase.ELECTION_DAY, mvArea},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, mvArea},
                {VotingCategory.FI, VotingPhase.LATE, mvArea}
        };
    }

    @Test(dataProvider = "buildVotingMessageTestData")
    public void testBuildVotingMessage(Voter voter, Voting voting, String message, String expectedMessage) {
        assertEquals(votingControllerMock.buildVotingMessage(voter, voting, message), expectedMessage);
    }

    @DataProvider
    public Object[][] buildVotingMessageTestData() {
        LocalDateTime nowJavaTime = LocalDateTime.now();
        DateTime nowJodaTime = DateTime.now();

        String formattedTime = formattedTime(nowJavaTime);
        String formattedDate = formattedDate(nowJavaTime);
        int dayOfWeek = Calendar.getInstance().get(DAY_OF_WEEK);

        VotingCategory votingCategory = VotingCategory.FI;
        return new Object[][]{
                {voter(true), voting(nowJodaTime, votingCategory), "message", messagePropertyString("@person.fictitiousVoterNameLine", formattedTime, formattedDate, dayOfWeek, votingCategory)},
                {voter(false), voting(nowJodaTime, votingCategory), "message", messagePropertyString(null, formattedTime, formattedDate, dayOfWeek, votingCategory)}
        };
    }

    private Voter voter(boolean isFictitious) {
        return Voter.builder()
                .fictitious(isFictitious)
                .build();
    }

    private String messagePropertyString(String voterName, String formattedTime, String formattedDate, int dayOfWeek, VotingCategory votingCategory) {
        return String.format("[message, %s, @common.date.weekday[%s].name, %s, %s, %s, null]", voterName, dayOfWeek, formattedDate, formattedTime, votingCategory.getId());
    }

    private Voting voting(DateTime now, VotingCategory votingCategory) {
        Voting voting = new Voting();
        voting.setCastTimestamp(now);
        voting.setVotingCategory(domainVotingCategory(votingCategory));

        return voting;
    }

    private no.valg.eva.admin.configuration.domain.model.VotingCategory domainVotingCategory(VotingCategory votingCategoryEnum) {
        no.valg.eva.admin.configuration.domain.model.VotingCategory votingCategory = new no.valg.eva.admin.configuration.domain.model.VotingCategory();
        votingCategory.setId(votingCategoryEnum.getId());

        return votingCategory;
    }

    @Test
    public void testGetCountyList_givenNull_verifiesServiceCall() {
        when(getInjectMock(ValggeografiService.class).kommunerForValghendelse(getUserDataMock())).thenReturn(Collections.emptyList());
        List<Kommune> countyList = votingControllerMock.getCountyList();
        assertEquals(countyList, Collections.emptyList());
        verify(getInjectMock(ValggeografiService.class)).kommunerForValghendelse(getUserDataMock());
    }

    @Test
    public void testGetCountyList_givenExistingList_verifiesNoServiceCall() throws NoSuchFieldException, IllegalAccessException {
        mockFieldValue("countyList", Collections.emptyList());
        List<Kommune> countyList = votingControllerMock.getCountyList();
        assertEquals(countyList, Collections.emptyList());
        verifyZeroInteractions(getInjectMock(ValggeografiService.class));
    }

    @Test(dataProvider = "isVelgerEgenKommuneTestData")
    public void testIsVelgerEgenKommune_givenVoter_verifiesIsVoterInMunicipality(String voterMunicipalityId, String pollingPlaceMunicipalityId, boolean expectingEqual) throws NoSuchFieldException, IllegalAccessException {
        Voter voter = createMock(Voter.class);

        when(voter.getMunicipalityId()).thenReturn(voterMunicipalityId);
        MvArea pollingPlaceMvArea = mockField("pollingPlaceMvArea", MvArea.class);
        when(pollingPlaceMvArea.getMunicipalityId()).thenReturn(pollingPlaceMunicipalityId);

        assertEquals(votingControllerMock.isVelgerEgenKommune(voter), expectingEqual);
    }

    @DataProvider
    public Object[][] isVelgerEgenKommuneTestData() {
        return new Object[][]{
                {"1234", "1234", true},
                {"1234", "1235", false},
        };
    }

    @Test(dataProvider = "getManntallsnummerTestData")
    public void testGetManntallsnummer_givenManntallsnummer_verifiesResult(Manntallsnummer manntallsnummer, String expectedResult) {
        when(getInjectMock(ManntallsSokWidget.class).getManntallsnummerObject()).thenReturn(manntallsnummer);

        assertEquals(votingControllerMock.getManntallsnummer(), expectedResult);
    }

    @DataProvider
    public Object[][] getManntallsnummerTestData() {
        Manntallsnummer manntallsnummer = new Manntallsnummer(234234234L, 8);
        return new Object[][]{
                {manntallsnummer, "0234234234 84"},
                {null, null}
        };
    }

    @Test
    public void testGetPageTitleMeta_givenMvArea_verifyPageTitleMetaBuilderInvocation() throws NoSuchFieldException, IllegalAccessException {
        MvArea pollingPlaceMvArea = getPrivateField("pollingPlaceMvArea", MvArea.class);
        votingControllerMock.getPageTitleMeta();
        PageTitleMetaBuilder pageTitleMetaBuilder = getPrivateField("pageTitleMetaBuilder", PageTitleMetaBuilder.class);
        verify(pageTitleMetaBuilder, times(1)).area(pollingPlaceMvArea);
    }

    @Test(dataProvider = "isForhandsStemmerRettIUrneTestData")
    public void testIsForhandsStemmerRettIUrne(boolean isAdvanceVotingInBallotBox, boolean expectedResult) throws NoSuchFieldException, IllegalAccessException {
        PollingPlace pollingPlace = createMock(PollingPlace.class);
        when(pollingPlace.isAdvanceVoteInBallotBox()).thenReturn(isAdvanceVotingInBallotBox);
        MvArea mvArea = createMock(MvArea.class);
        
        mockFieldValue("pollingPlaceMvArea", mvArea);
        when(mvArea.getPollingPlace()).thenReturn(pollingPlace);
        
        assertEquals(votingControllerMock.isForhandsstemmeRettIUrne(), expectedResult);
    }

    @DataProvider
    public Object[][] isForhandsStemmerRettIUrneTestData() {
        return new Object[][]{
                {true, true},
                {false, false},
        };
    }

    @Test(dataProvider = "emptyVoterSearchResultTestData")
    public void testEmptyVoterSearchResult_givenFoundInSearch_verifiesResult(boolean voterFoundInSearch, boolean expectedResult) throws NoSuchFieldException, IllegalAccessException {
        mockFieldValue("voterFoundInSearch", voterFoundInSearch);
        assertEquals(votingControllerMock.emptyVoterSearchResult(), expectedResult);
    }

    @DataProvider
    public Object[][] emptyVoterSearchResultTestData() {
        return new Object[][]{
                {true, false},
                {false, true},
        };
    }

    @Test
    public void testOnSelectedVoterClick() {
        MvElection mvElection = createMock(MvElection.class);
        votingControllerMock.onSelectedVoterClick(mvElection);
        assertEquals(votingControllerMock.getElectionGroupAsMvElection(), mvElection);
    }

    @Test(dataProvider = "getSelectedVotingCategoryIdTestData")
    public void testGetSelectedVotingCategoryId(VotingCategory votingCategory, String expectedId) throws NoSuchFieldException, IllegalAccessException {
        mockFieldValue("votingCategory", votingCategory);
        String selectedVotingCategoryId = votingControllerMock.getSelectedVotingCategoryId();
        assertEquals(selectedVotingCategoryId, expectedId);
    }

    @DataProvider
    public Object[][] getSelectedVotingCategoryIdTestData() {
        return new Object[][]{
                {VotingCategory.FI, "FI"},
                {null, ""},
        };
    }

    @Test(dataProvider = "votingHeaderMessagePropertyTestData")
    public void testGetVotingHeader_VerifiesHeaderMessageProperty(VotingCategory votingCategory, VotingPhase votingPhase) {
        votingControllerMock.setVotingCategory(votingCategory);
        votingControllerMock.setVotingPhase(votingPhase);
        assertEquals(votingControllerMock.getVotingHeader(), format("[@voting.envelope.searchVoter.header, @voting_category[%s_%s].name]", votingPhase.name(), votingCategory.getId()));
    }

    @DataProvider
    public Object[][] votingHeaderMessagePropertyTestData() {
        return new Object[][]{
                {FI, VotingPhase.EARLY},
                {FI, VotingPhase.ADVANCE},
                {VO, VotingPhase.ELECTION_DAY},
                {FI, VotingPhase.LATE},
        };
    }
}