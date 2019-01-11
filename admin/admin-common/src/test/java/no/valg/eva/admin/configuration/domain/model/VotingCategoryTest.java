package no.valg.eva.admin.configuration.domain.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static org.testng.Assert.assertEquals;

public class VotingCategoryTest {

    @BeforeMethod
    public void setUp() {
    }

    @Test(dataProvider = "votingCategoryById")
    public void testVotingCategoryById_givenVotingCategory_verifiesCorrectVotingCategoryEnum(VotingCategory votingCategory, no.valg.eva.admin.common.voting.VotingCategory expectedVotingCategoryEnum) {
        assertEquals(votingCategory.votingCategoryById(), expectedVotingCategoryEnum);
    }

    @DataProvider
    public static Object[][] votingCategoryById() {
        return new Object[][]{
                {votingCategory(FI), FI},
                {votingCategory(VS), VS},
                {votingCategory(FB), FB},
                {votingCategory(FU), FU},
                {votingCategory(VB), VB},
                {votingCategory(VO), VO},
                {votingCategory(FE), FE},
                {votingCategory(VF), VF},
                {votingCategory(FA), FA},
        };
    }

    private static VotingCategory votingCategory(no.valg.eva.admin.common.voting.VotingCategory votingCategoryEnum) {
        VotingCategory votingCategory = new VotingCategory();
        votingCategory.setId(votingCategoryEnum.getId());

        return votingCategory;
    }
}