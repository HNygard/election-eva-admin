package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.ConfirmationCategoryStatus;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class ConfirmVotingMenuItemFactoryTest extends BaseFrontendTest {

    private ConfirmVotingMenuItemFactory confirmVotingMenuItemFactory;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        confirmVotingMenuItemFactory = initializeMocks(ConfirmVotingMenuItemFactory.class);
    }

    @Test(dataProvider = "buildMenuItemsBackgroundCssTestData")
    public void testBuildMenuItemsBackgroundCss_GivenCategory_VerifiesBackgroundCss(ConfirmationCategoryStatus confirmationCategoryStatus, String expectedBackgroundCss) {
        List<ConfirmVotingMenuItem> menuItems = confirmVotingMenuItemFactory.buildMenuItems(singletonList(confirmationCategoryStatus));
        assertEquals(menuItems.size(), 1);
        assertEquals(menuItems.get(0).getBackgroundCss(), expectedBackgroundCss);
    }

    @DataProvider
    public Object[][] buildMenuItemsBackgroundCssTestData() {
        return new Object[][]{
                {createVotingConfirmationCategory(Tense.PRESENT), ""},
                {createVotingConfirmationCategory(Tense.PRESENT, true), "ui-state-needs-verification"},
                {createVotingConfirmationCategory(Tense.FUTURE), "ui-state-not-started"},
                {createVotingConfirmationCategory(Tense.PAST), "ui-state-expired"}
        };
    }

    @Test(dataProvider = "buildMenuItemsIconCssTestData")
    public void testBuildMenuItemsIconCss_GivenCategory_VerifiesIconCss(ConfirmationCategoryStatus confirmationCategoryStatus, String expectedIconCss) {
        List<ConfirmVotingMenuItem> menuItems = confirmVotingMenuItemFactory.buildMenuItems(singletonList(confirmationCategoryStatus));
        assertEquals(menuItems.size(), 1);
        assertEquals(menuItems.get(0).getIconCss(), expectedIconCss);
    }

    @DataProvider
    public Object[][] buildMenuItemsIconCssTestData() {
        return new Object[][]{
                {createVotingConfirmationCategory(Tense.PRESENT, LockType.LOCKED), null},
                {createVotingConfirmationCategory(Tense.FUTURE, LockType.UNLOCKED), null},
                {createVotingConfirmationCategory(Tense.PAST, LockType.NOT_APPLICABLE), null}
        };
    }

    @Test(dataProvider = "buildMenuItemsVotingCategoryTestData")
    public void testBuildMenuItemsMenuLabel_GivenCategory_verifiesCategory(ConfirmationCategoryStatus confirmationCategoryStatus) {
        List<ConfirmVotingMenuItem> menuItems = confirmVotingMenuItemFactory.buildMenuItems(singletonList(confirmationCategoryStatus));
        assertEquals(menuItems.size(), 1);
        assertEquals(menuItems.get(0).getVotingCategory(), confirmationCategoryStatus.getVotingCategory());
    }

    @DataProvider
    public Object[][] buildMenuItemsVotingCategoryTestData() {
        return new Object[][]{
                {createVotingConfirmationCategory(VotingCategory.VO, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.VF, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.VS, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.VB, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.FI, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.FU, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.FA, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.FB, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.FE, Tense.PRESENT, LockType.LOCKED)},
                {createVotingConfirmationCategory(VotingCategory.FI, Tense.PRESENT, LockType.LOCKED, VotingPhase.LATE)},
        };
    }

    @Test(dataProvider = "buildMenuItemsMenuLabelTestData")
    public void testBuildMenuItemsMenuLabel_GivenCategory_verifiesMenuLabel(ConfirmationCategoryStatus confirmationCategoryStatus, String expectedMenuLabel) {
        List<ConfirmVotingMenuItem> menuItems = confirmVotingMenuItemFactory.buildMenuItems(singletonList(confirmationCategoryStatus));
        assertEquals(menuItems.size(), 1);
        assertEquals(menuItems.get(0).getMenuLabel(), expectedMenuLabel);
    }

    @DataProvider
    public Object[][] buildMenuItemsMenuLabelTestData() {
        return new Object[][]{
                {createVotingConfirmationCategory(VotingCategory.FI, Tense.PRESENT, LockType.LOCKED, VotingPhase.EARLY), "messageProperty"},
                {createVotingConfirmationCategory(VotingCategory.FI, Tense.PRESENT, LockType.LOCKED, VotingPhase.LATE), "messageProperty"},
        };
    }

    @Test(dataProvider = "buildMenuItemsDataAftIdTestData")
    public void testBuildMenuItemsMenuLabel_GivenCategory_verifiesDataAftId(ConfirmationCategoryStatus confirmationCategoryStatus, String expectedDataAftId) {
        List<ConfirmVotingMenuItem> menuItems = confirmVotingMenuItemFactory.buildMenuItems(singletonList(confirmationCategoryStatus));
        assertEquals(menuItems.size(), 1);
        assertEquals(menuItems.get(0).getDataAftId(), expectedDataAftId);
    }

    @DataProvider
    public Object[][] buildMenuItemsDataAftIdTestData() {
        return new Object[][]{
                {createVotingConfirmationCategory(VotingCategory.FI, Tense.PRESENT, LockType.UNLOCKED, VotingPhase.ADVANCE,
                        "@voting.here.and.there[ADVANCE_FI].and.there", new LocalDate(), new LocalDate().plusDays(1), false), "ADVANCE_FI"},
        };
    }

    @Test(dataProvider = "buildMenuItemsCategoryOpenTestData")
    public void testBuildMenuItemsMenuLabel_GivenCategory_verifiesCategoryOpen(ConfirmationCategoryStatus confirmationCategoryStatus, boolean expectsCategoryOpen) {
        List<ConfirmVotingMenuItem> menuItems = confirmVotingMenuItemFactory.buildMenuItems(singletonList(confirmationCategoryStatus));
        assertEquals(menuItems.size(), 1);
        assertEquals(menuItems.get(0).isCategoryOpen(), expectsCategoryOpen);
    }

    @DataProvider
    public Object[][] buildMenuItemsCategoryOpenTestData() {
        return new Object[][]{
                {createVotingConfirmationCategory(Tense.PRESENT), true},
                {createVotingConfirmationCategory(Tense.PAST), false},
                {createVotingConfirmationCategory(Tense.FUTURE), false},
        };
    }

    @Test(dataProvider = "buildMenuItemsCategoryClosedMessageTestData")
    public void testBuildMenuItemsMenuLabel_GivenCategory_verifiesCategoryClosedMessage(ConfirmationCategoryStatus confirmationCategoryStatus,
                                                                                        String expectedCategoryClosedMessage) {
        List<ConfirmVotingMenuItem> menuItems = confirmVotingMenuItemFactory.buildMenuItems(singletonList(confirmationCategoryStatus));
        assertEquals(menuItems.size(), 1);
        assertEquals(menuItems.get(0).getCategoryClosedMessage(), expectedCategoryClosedMessage);
    }

    @DataProvider
    public Object[][] buildMenuItemsCategoryClosedMessageTestData() {
        LocalDate localDate = new LocalDate();

        return new Object[][]{
                {createVotingConfirmationCategory(Tense.PRESENT), ""},
                {createVotingConfirmationCategory(Tense.PAST), "@voting.confirmation.expired.message"},
                new Object[]{createVotingConfirmationCategory(Tense.FUTURE), String.format("[@voting.confirmation.not.started.message, %s]",
                        DateUtil.getFormattedShortDate(localDate))},
        };
    }

    private ConfirmationCategoryStatus createVotingConfirmationCategory(Tense tense) {
        return createVotingConfirmationCategory(tense, LockType.UNLOCKED);
    }

    private ConfirmationCategoryStatus createVotingConfirmationCategory(Tense tense, boolean needsVerification) {
        return createVotingConfirmationCategory(VotingCategory.FI, tense, LockType.UNLOCKED, VotingPhase.ADVANCE, "messageProperty",
                new LocalDate(), new LocalDate().plusDays(1), needsVerification);
    }

    private ConfirmationCategoryStatus createVotingConfirmationCategory(Tense tense, LockType lockType) {
        return createVotingConfirmationCategory(VotingCategory.FI, tense, lockType, VotingPhase.ADVANCE, "messageProperty",
                new LocalDate(), new LocalDate().plusDays(1), false);
    }

    private ConfirmationCategoryStatus createVotingConfirmationCategory(VotingCategory votingCategory, Tense tense, LockType lockType) {
        return createVotingConfirmationCategory(votingCategory, tense, lockType, VotingPhase.ADVANCE, "messageProperty",
                new LocalDate(), new LocalDate().plusDays(1), false);
    }

    private ConfirmationCategoryStatus createVotingConfirmationCategory(VotingCategory votingCategory, Tense tense, LockType lockType, VotingPhase votingPhase) {
        return createVotingConfirmationCategory(votingCategory, tense, lockType, votingPhase, "messageProperty",
                new LocalDate(), new LocalDate().plusDays(1), false);
    }

    private ConfirmationCategoryStatus createVotingConfirmationCategory(VotingCategory votingCategory, Tense tense, LockType lockType,
                                                                        VotingPhase votingPhase, String messageProperty,
                                                                        LocalDate startDate, LocalDate endingDate, boolean needsVerification) {
        return ConfirmationCategoryStatus.builder()
                .tense(tense)
                .locked(lockType)
                .needsVerification(needsVerification)
                .votingCategory(votingCategory)
                .messageProperty(messageProperty)
                .votingPhase(votingPhase)
                .startingDate(startDate)
                .endingDate(endingDate)
                .build();
    }

    @Test
    public void testVotingStartDate_fromVotingCategory_hasTimeSetToMidnightThatDay() {
        
        LocalDate startingDate = new LocalDate("2018-05-12");
        LocalDateTime expectedStartingDate = LocalDateTime.of(2018, 5, 12, 0, 0, 0, 0);
        
        ConfirmationCategoryStatus categoryStatus = aCategoryStatusWithStartingDate(startingDate);
        ConfirmVotingMenuItem menuItem = confirmVotingMenuItemFactory.buildMenuItems(singletonList(categoryStatus)).get(0);
        
        assertEquals(menuItem.getStartDate(), expectedStartingDate);
    }
    
    private ConfirmationCategoryStatus aCategoryStatusWithStartingDate(LocalDate startingDate) {
        return createVotingConfirmationCategory(VotingCategory.FI, Tense.PRESENT, LockType.UNLOCKED, VotingPhase.EARLY, 
                "messageProperty", startingDate, startingDate.plusDays(14), false);
    }
    
    @Test
    public void testVotingEndDate_fromVotingCategory_hasTimeSetToLastMillisDayBeforeVotingCategoryEndDate() {

        LocalDate endingDate = new LocalDate("2018-05-12");
        LocalDateTime expectedEndingDate = LocalDateTime.of(2018, 5, 11, 23, 59, 59, 999999999);

        ConfirmationCategoryStatus categoryStatus = aCategoryStatusWithEndingDate(endingDate);
        ConfirmVotingMenuItem menuItem = confirmVotingMenuItemFactory.buildMenuItems(singletonList(categoryStatus)).get(0);

        assertEquals(menuItem.getEndDateIncluding(), expectedEndingDate);
    }

    private ConfirmationCategoryStatus aCategoryStatusWithEndingDate(LocalDate endingDate) {
        return createVotingConfirmationCategory(VotingCategory.FI, Tense.PRESENT, LockType.UNLOCKED, VotingPhase.EARLY,
                "messageProperty", endingDate.minusDays(14), endingDate, false);
    }
}