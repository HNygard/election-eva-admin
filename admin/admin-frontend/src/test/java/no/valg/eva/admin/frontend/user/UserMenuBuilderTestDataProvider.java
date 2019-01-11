package no.valg.eva.admin.frontend.user;

import no.valg.eva.admin.common.test.data.AreaPathTestData;
import no.valg.eva.admin.frontend.common.menu.Menu;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class UserMenuBuilderTestDataProvider {

   
    @DataProvider
    public static Object[][] forTestElectionDayMenu2_isElectronicMarkOffsConfigured() {
        return new Object[][]{
                {true, "@menu.approveVoting.approveVotingNegativeElectionDay", 2, AreaPathTestData.AREA_PATH_111111_11_11_1111},
                {false, "@menu.approveVoting.approveVotingNegativeElectionDay", 1, AreaPathTestData.AREA_PATH_111111_11_11_1111}
        };
    }

    @DataProvider
    public static Object[][] forTestAddIfAccess() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @DataProvider
    public static Object[][] forTestCountingMenu() {
        return new Object[][]{
                {true, 8},
                {false, 7},
        };
    }

    @DataProvider
    public static Object[][] forTestAddIfNotEmpty() {
        Menu menuWithNoChildren = new Menu("menuText1", "pageUrl1");
        Menu menuWithChildren = new Menu("menuText1", "pageUrl1");
        menuWithChildren.addChild(new Menu("childMenuText", "childPageUrl"));

        return new Object[][]{
                {menuWithNoChildren},
                {menuWithChildren}
        };
    }
}
