package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.frontend.cdi.BeanLookupSingleton;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;
import org.joda.time.LocalDate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

import static no.valg.eva.admin.common.voting.LockType.LOCKED;
import static no.valg.eva.admin.common.voting.LockType.NOT_APPLICABLE;
import static no.valg.eva.admin.common.voting.LockType.UNLOCKED;
import static no.valg.eva.admin.common.voting.Tense.FUTURE;
import static no.valg.eva.admin.common.voting.Tense.PAST;
import static no.valg.eva.admin.common.voting.Tense.PRESENT;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.common.voting.VotingPhase.ADVANCE;
import static no.valg.eva.admin.common.voting.VotingPhase.ELECTION_DAY;
import static no.valg.eva.admin.common.voting.VotingPhase.LATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class RegisterVotingInEnvelopeMenuFactoryTest extends BaseFrontendTest {

    @Test(dataProvider = "buildMenuItemVerifyEnabledTestData")
    public void testBuildMenuItem_GivenTense_VerifiesEnabled(VotingCategoryStatus votingCategoryStatus)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);
        mockGetMessageByProperty(anyString(), "");
        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        assertTrue(menuItem.isEnabled());
    }

    @DataProvider
    public Object[][] buildMenuItemVerifyEnabledTestData() {
        LocalDate localDate = new LocalDate();
        return new Object[][]{
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, FUTURE, UNLOCKED, localDate)}
        };
    }

    @Test(dataProvider = "buildMenuItemOpenForRegistrationTestData")
    public void testBuildMenuItem_GivenTense_VerifiesOpenForRegistration(VotingCategoryStatus votingCategoryStatus, boolean expectsOpenForRegistration)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        mockGetMessageByProperty(anyString(), "");
        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        assertEquals(menuItem.isOpenForRegistration(), expectsOpenForRegistration);
    }

    @DataProvider
    public Object[][] buildMenuItemOpenForRegistrationTestData() {
        return new Object[][]{
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, PRESENT, UNLOCKED, new LocalDate()), true},
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, PAST, UNLOCKED, new LocalDate()), false},
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, FUTURE, UNLOCKED, new LocalDate()), false}
        };
    }


    @Test(dataProvider = "buildMenuItemNotOpenForRegistrationMessageTestData")
    public void testBuildMenuItem_GivenTense_VerifiesNotOpenForRegistrationMessage(VotingCategoryStatus votingCategoryStatus,
                                                                                   boolean electronicMarkoffs,
                                                                                   String notOpenForRegistrationMessageProperty,
                                                                                   String expectedNotOpenForRegistrationMessage)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        mockGetMessageByProperty(notOpenForRegistrationMessageProperty, expectedNotOpenForRegistrationMessage);

        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus, electronicMarkoffs);

        assertEquals(menuItem.getNotOpenForRegistrationMessage(), expectedNotOpenForRegistrationMessage);
    }

    @DataProvider
    public Object[][] buildMenuItemNotOpenForRegistrationMessageTestData() {
        LocalDate localDate = new LocalDate();


        return new Object[][]{
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, PRESENT, UNLOCKED, localDate),
                        true, "", ""},
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, PAST, UNLOCKED, localDate),
                        true, "@voting.registration.expired", "@voting.registration.expired"},
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, FUTURE, UNLOCKED, localDate),
                        true, "@voting.registration.notStarted", String.format("[@voting.registration.notStarted, %s]", DateUtil.getFormattedShortDate(localDate))},
                {new VotingCategoryStatus("messageProperty", FI, ELECTION_DAY, FUTURE, UNLOCKED, localDate),
                        true, "@voting.registration.notStarted", String.format("[@voting.registration.notStarted, %s]", DateUtil.getFormattedShortDate(localDate))},
                
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, FUTURE, UNLOCKED, localDate),
                        false, "@voting.registration.notStarted", String.format("[@voting.registration.notStarted, %s]", DateUtil.getFormattedShortDate(localDate))},
                {new VotingCategoryStatus("messageProperty", FI, ELECTION_DAY, FUTURE, UNLOCKED, localDate),
                        false, "@voting.registration.electionDayLateNotStartedForPaperMun", String.format("[@voting.registration.electionDayLateNotStartedForPaperMun, %s]", DateUtil.getFormattedShortDate(localDate))}
        };
    }

    @Test(dataProvider = "buildMenuItemVerifiesMenuLabelTestData")
    public void testBuildMenuItem_GivenVotingCategory_VerifiesMenuLabel(VotingCategoryStatus votingCategoryStatus, String messageProperty, String expectedMenuLabel)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        mockGetMessageByProperty(messageProperty, expectedMenuLabel);
        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        assertEquals(menuItem.getMenuLabel(), expectedMenuLabel);
    }

    @DataProvider
    public Object[][] buildMenuItemVerifiesMenuLabelTestData() {
        LocalDate localDate = new LocalDate();

        return new Object[][]{
                {new VotingCategoryStatus("messageProperty", FI, ADVANCE, PRESENT, UNLOCKED, localDate), "", "messageProperty"},
                {new VotingCategoryStatus("messageProperty", FI, LATE, PRESENT, UNLOCKED, localDate), "", "messageProperty"},
                {new VotingCategoryStatus("messageProperty", VS, LATE, PRESENT, UNLOCKED, localDate), "", "messageProperty"}
        };
    }

    @Test(dataProvider = "buildMenuItemVerifiesIconCssTestData")
    public void testBuildMenuItem_GivenVotingCategory_VerifiesIconCss(VotingCategoryStatus votingCategoryStatus, String expectedIconCss)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        mockGetMessageByProperty("", "");
        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        assertEquals(menuItem.getIconCss(), expectedIconCss);
    }

    @DataProvider
    public Object[][] buildMenuItemVerifiesIconCssTestData() {
        return new Object[][]{
                {votingCategoryStatus(FI, "", ADVANCE, UNLOCKED, PRESENT), "eva-icon-unlocked"},
                {votingCategoryStatus(FI, "", ADVANCE, LOCKED, PRESENT), "eva-icon-lock"},
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, PRESENT), ""}

        };
    }

    @Test(dataProvider = "buildMenuItemVerifiesBackgroundCssTestData")
    public void testBuildMenuItem_GivenVotingCategory_VerifiesBackgroundCss(VotingCategoryStatus votingCategoryStatus, String expectedBackgroundCss)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        mockGetMessageByProperty("", "");
        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        assertEquals(menuItem.getBackgroundCss(), expectedBackgroundCss);
    }

    @DataProvider
    public Object[][] buildMenuItemVerifiesBackgroundCssTestData() {
        return new Object[][]{
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, PAST), "ui-state-expired"},
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, PRESENT), ""},
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, FUTURE), "ui-state-not-started"}

        };
    }

    @Test(dataProvider = "buildMenuItemVerifiesVotingCategoryTestData")
    public void testBuildMenuItem_GivenVotingCategory_VerifiesVotingCategory(VotingCategoryStatus votingCategoryStatus, VotingCategory expectedVotingCategory)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        mockGetMessageByProperty("", "");
        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        assertEquals(menuItem.getVotingCategory(), expectedVotingCategory);
    }

    @DataProvider
    public Object[][] buildMenuItemVerifiesVotingCategoryTestData() {
        return new Object[][]{
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, PAST), FI},
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, PRESENT), FI},
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, FUTURE), FI}

        };
    }

    @Test(dataProvider = "buildMenuItemVerifiesDataAFTTestData")
    public void testBuildMenuItem_GivenVotingCategory_VerifiesDataAFT(VotingCategoryStatus votingCategoryStatus, String expectedDataAFTId)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        mockGetMessageByProperty("", "");
        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        assertEquals(menuItem.getDataAftId(), expectedDataAFTId);
    }

    @DataProvider
    public Object[][] buildMenuItemVerifiesDataAFTTestData() {
        VotingCategoryStatus votingCategoryStatusWithOutMessageProperty = new VotingCategoryStatus("", VotingCategory.FI, ADVANCE, PRESENT,
                LockType.NOT_APPLICABLE,
                new LocalDate());
        return new Object[][]{
                {votingCategoryStatus(FI, "status", ADVANCE, NOT_APPLICABLE, PAST), "status"},
                {votingCategoryStatusWithOutMessageProperty, ""}

        };
    }

    @Test(dataProvider = "buildMenuItemVerifiesControllerTestData")
    public void testBuildMenuItem_GivenVotingCategory_VerifiesController(VotingCategoryStatus votingCategoryStatus, Class<VotingController> expectedVotingController)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        BeanLookupSingleton beanLookupSingleton = mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());
        mockGetMessageByProperty("", "");

        registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        verify(beanLookupSingleton, times(1)).lookup(expectedVotingController);
    }

    @DataProvider
    public Object[][] buildMenuItemVerifiesControllerTestData() {
        return new Object[][]{
                {votingCategoryStatus(VS, "", ELECTION_DAY, NOT_APPLICABLE, PRESENT), ElectionDayVotingInEnvelopeCentralController.class},
                {votingCategoryStatus(FI, "", LATE, NOT_APPLICABLE, PRESENT), AdvanceVotingInEnvelopeCentralController.class},
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, PRESENT), AdvanceVotingInEnvelopeCentralController.class}
        };
    }

    @Test(dataProvider = "buildMenuItemVerifiesViewTestData")
    public void testBuildMenuItem_GivenVotingCategory_VerifiesView(VotingCategoryStatus votingCategoryStatus, String expectedView)
            throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory = initializeMocks(RegisterVotingInEnvelopeMenuFactory.class);

        mockBeanLookup(new AdvanceVotingInEnvelopeCentralController());
        mockGetMessageByProperty("", "");

        RegisterVotingInEnvelopeMenuItem menuItem = registerVotingInEnvelopeMenuFactory.buildMenuItem(votingCategoryStatus);

        assertEquals(menuItem.getView(), expectedView);
    }

    @DataProvider
    public Object[][] buildMenuItemVerifiesViewTestData() {
        return new Object[][]{
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, PAST), "registerVotesInEnvelopes"},
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, FUTURE), "registerVotesInEnvelopes"},
                {votingCategoryStatus(FI, "", ADVANCE, NOT_APPLICABLE, PRESENT), "advanceVotingInEnvelope"},
                {votingCategoryStatus(FI, "", LATE, NOT_APPLICABLE, PRESENT), "advanceVotingLateArrival"}
        };
    }

    private VotingCategoryStatus votingCategoryStatus(VotingCategory votingCategory, String status, VotingPhase votingPhase, LockType lockType,
                                                      Tense tense) {
        return new VotingCategoryStatus("@voting_category_status[" + status + "].name", votingCategory, votingPhase, tense, lockType,
                new LocalDate());
    }

    private void mockGetMessageByProperty(String messageProperty, String expectedMessage) {
        MessageProvider messageProvider = getInjectMock(MessageProvider.class);
        when(messageProvider.get(messageProperty)).thenReturn(expectedMessage);
    }

    private BeanLookupSingleton mockBeanLookup(VotingController controller) {
        BeanLookupSingleton beanLookupSingleton = getInjectMock(BeanLookupSingleton.class);
        when(beanLookupSingleton.lookup(any())).thenReturn(controller);

        return beanLookupSingleton;
    }
}
