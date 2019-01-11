package no.valg.eva.admin.common.voting;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.EnumSet;

import static java.lang.String.format;
import static java.util.EnumSet.allOf;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategoryTest.VotingCategoryType.ADVANCE;
import static no.valg.eva.admin.common.voting.VotingCategoryTest.VotingCategoryType.ELECTION_DAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class VotingCategoryTest extends MockUtilsTestCase {

    private static final boolean XIM = true;
    private static final boolean PAPER = false;
    private static final boolean BOROUGH = true;
    private static final boolean MUNICIPALITY = false;

    private static final VotingCategory[] FI = {VotingCategory.FI};
    private static final VotingCategory[] VO = {VotingCategory.VO};
    private static final VotingCategory[] VF = {VotingCategory.VF};
    private static final VotingCategory[] VS = {VotingCategory.VS};
    private static final VotingCategory[] VB = {VotingCategory.VB};
    private static final VotingCategory[] FI_FU_FB_FE = {VotingCategory.FI, VotingCategory.FU, FB, VotingCategory.FE};
    private static final VotingCategory[] VS_VB = {VotingCategory.VS, VotingCategory.VB};
    private static final VotingCategory[] VS_VF = {VotingCategory.VS, VotingCategory.VF};

    private static final int CURRENT_VOTING_CATEGORY_ENUM_VALUE_SIZE = 9;

    @Test
    public void testSize_givenCurrentVotingCategory_verifiesOriginalSize() {
        assertEquals(EnumSet.allOf(VotingCategory.class).size(), CURRENT_VOTING_CATEGORY_ENUM_VALUE_SIZE);
    }

    @Test(dataProvider = "fromIdTestData")
    public void testFromId_givenCategory_verifiesCategory(String votingCategoryId, VotingCategory expectedVotingCategory) {
        assertEquals(VotingCategory.fromId(votingCategoryId), expectedVotingCategory);
    }

    @DataProvider
    public Object[][] fromIdTestData() {
        return new Object[][]{
                {"VS", VotingCategory.VS},
                {"VB", VotingCategory.VB},
                {"VO", VotingCategory.VO,},
                {"VF", VotingCategory.VF,},
                {"FB", FB,},
                {"FI", VotingCategory.FI,},
                {"FU", VotingCategory.FU,},
                {"FE", VotingCategory.FE},
                {"FA", VotingCategory.FA},
        };
    }

    @Test(dataProvider = "getNameWithPhaseTestData")
    public void testGetName_givenCategory_verifiesName(VotingCategory votingCategory, VotingPhase votingPhase, String expectedName) {
        assertEquals(votingCategory.getName(votingPhase), expectedName);
    }

    @DataProvider
    public Object[][] getNameWithPhaseTestData() {
        return new Object[][]{
                {FB, VotingPhase.ADVANCE, "@voting_category[ADVANCE_FB].name"},
                {VotingCategory.FU, VotingPhase.ADVANCE, "@voting_category[ADVANCE_FU].name"},
                {VotingCategory.FI, VotingPhase.ADVANCE, "@voting_category[ADVANCE_FI].name"},
                {VotingCategory.FE, VotingPhase.ADVANCE, "@voting_category[ADVANCE_FE].name"},
                {VotingCategory.FA, VotingPhase.ADVANCE, "@voting_category[ADVANCE_FA].name"},
                {VotingCategory.VS, VotingPhase.ELECTION_DAY, "@voting_category[ELECTION_DAY_VS].name"},
                {VotingCategory.VB, VotingPhase.ELECTION_DAY, "@voting_category[ELECTION_DAY_VB].name"},
                {VotingCategory.VO, VotingPhase.ELECTION_DAY, "@voting_category[ELECTION_DAY_VO].name"},
                {VotingCategory.VF, VotingPhase.ELECTION_DAY, "@voting_category[ELECTION_DAY_VF].name"},
                {VotingCategory.FI, VotingPhase.LATE, "@voting_category[LATE_FI].name"}
        };
    }


    @Test(dataProvider = "countCategories")
    public void from_givenCountCategory_returnsVotingCategories(CountCategory countCategory, VotingCategory[] expected) {
        VotingCategory[] votingCategories = VotingCategory.from(countCategory);
        assertThat(votingCategories).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] countCategories() {
        return new Object[][]{
                {CountCategory.VO, VO},
                {CountCategory.VF, VF},
                {CountCategory.BF, VF},
                {CountCategory.VS, VS},
                {CountCategory.VB, VB},
                {CountCategory.FO, FI_FU_FB_FE},
                {CountCategory.FS, FI_FU_FB_FE}
        };
    }

    @Test(dataProvider = "phasesAndElections")
    public void from_givenVotingRegistrationPhaseAndMvElection_returnsVotingCategories(VotingPhase votingPhase, boolean electronicMarkoffs,
                                                                                       boolean onBoroughLevel, VotingCategory[] expected) {
        VotingCategory[] votingCategories = VotingCategory.from(votingPhase, electronicMarkoffs, onBoroughLevel);
        assertThat(votingCategories).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] phasesAndElections() {
        return new Object[][]{
                {VotingPhase.EARLY, XIM, BOROUGH, FI},
                {VotingPhase.EARLY, XIM, MUNICIPALITY, FI},
                {VotingPhase.EARLY, PAPER, MUNICIPALITY, FI},

                {VotingPhase.ADVANCE, XIM, BOROUGH, FI_FU_FB_FE},
                {VotingPhase.ADVANCE, XIM, MUNICIPALITY, FI_FU_FB_FE},
                {VotingPhase.ADVANCE, PAPER, MUNICIPALITY, FI_FU_FB_FE},

                {VotingPhase.ELECTION_DAY, XIM, BOROUGH, VS_VB},
                {VotingPhase.ELECTION_DAY, XIM, MUNICIPALITY, VS_VB},
                {VotingPhase.ELECTION_DAY, PAPER, MUNICIPALITY, VS_VF},

                {VotingPhase.LATE, XIM, BOROUGH, FI},
                {VotingPhase.LATE, XIM, MUNICIPALITY, FI},
                {VotingPhase.LATE, PAPER, MUNICIPALITY, FI}
        };
    }

    @Test(dataProvider = "illegalPhasesAndElections", expectedExceptions = IllegalStateException.class)
    public void from_givenVotingRegistrationPhaseAndMvElection_returnsException(VotingPhase votingPhase, boolean electronicMarkoffs,
                                                                                boolean onBoroughLevel) {
        VotingCategory.from(votingPhase, electronicMarkoffs, onBoroughLevel);
    }

    @DataProvider
    public Object[][] illegalPhasesAndElections() {
        return new Object[][]{
                {VotingPhase.EARLY, PAPER, BOROUGH},
                {VotingPhase.ADVANCE, PAPER, BOROUGH},
                {VotingPhase.ELECTION_DAY, PAPER, BOROUGH},
                {VotingPhase.LATE, PAPER, BOROUGH}
        };
    }

    @Test(dataProvider =  "getNameTestData")
    public void testGetName(VotingCategory votingCategory, String expectedName) {
        assertEquals(votingCategory.getName(), expectedName);
    }

    @DataProvider
    public Object[][] getNameTestData() {
        return new Object[][]{
                {VotingCategory.FB, name(VotingCategory.FB)},
                {VotingCategory.FU, name(VotingCategory.FU)},
                {VotingCategory.FI, name(VotingCategory.FI)},
                {VotingCategory.FA, name(VotingCategory.FA)},
                {VotingCategory.FE, name(VotingCategory.FE)},
                {VotingCategory.VS, name(VotingCategory.VS)},
                {VotingCategory.VB, name(VotingCategory.VB)},
                {VotingCategory.VO, name(VotingCategory.VO)},
                {VotingCategory.VF, name(VotingCategory.VF)},
        };
    }


    private String name(VotingCategory votingCategory) {
        return format("@voting_category[%s].name", votingCategory.getId());
    }

    @Test(dataProvider = "getIdTestData")
    public void testGetId_givenCategory_VerifiesName(VotingCategory votingCategory, String expectedId) {
        assertEquals(votingCategory.getId(), expectedId);
    }

    @DataProvider
    public Object[][] getIdTestData() {
        return new Object[][]{
                {VotingCategory.VS, "VS"},
                {VotingCategory.VB, "VB"},
                {VotingCategory.VO, "VO"},
                {VotingCategory.VF, "VF"},
                {FB, "FB"},
                {VotingCategory.FI, "FI"},
                {VotingCategory.FU, "FU"},
                {VotingCategory.FE, "FE"},
                {VotingCategory.FA, "FA"},
        };
    }

    @Test(dataProvider = "isElectionDayVotingCategoryTestData")
    public void testIsElectionDayVotingCategory(VotingCategory votingCategory, boolean expectsElectionDayType) {
        boolean isElectionDayVotingCategory = VotingCategory.isElectionDayVotingCategory(votingCategory);
        assertEquals(isElectionDayVotingCategory, expectsElectionDayType);
    }

    @DataProvider
    public Object[][] isElectionDayVotingCategoryTestData() {

        return allOf(VotingCategory.class).stream()
                .map(votingCategory ->
                        new Object[]{votingCategory, ELECTION_DAY == categoryType(votingCategory)})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "isAdvanceVotingCategoryTestData")
    public void testIsAdvanceVotingCategory(VotingCategory votingCategory, boolean expectsElectionDayType) {
        boolean isElectionDayVotingCategory = VotingCategory.isAdvanceVotingCategory(votingCategory);
        assertEquals(isElectionDayVotingCategory, expectsElectionDayType);
    }

    @DataProvider
    public Object[][] isAdvanceVotingCategoryTestData() {

        return allOf(VotingCategory.class).stream()
                .map(votingCategory ->
                        new Object[]{votingCategory, ADVANCE == categoryType(votingCategory)})
                .toArray(Object[][]::new);
    }

    private VotingCategoryType categoryType(VotingCategory votingCategory) {
        switch (votingCategory) {
            case VO:
            case VF:
            case VS:
            case VB:
                return ELECTION_DAY;
            case FI:
            case FU:
            case FA:
            case FB:
            case FE:
                return ADVANCE;
            default:
                throw new IllegalArgumentException("Unknown category: " + votingCategory);
        }
    }

    enum VotingCategoryType {
        ELECTION_DAY, ADVANCE
    }
}