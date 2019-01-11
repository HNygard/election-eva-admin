package no.valg.eva.admin.frontend.voting.ctrls.model;

import no.valg.eva.admin.common.voting.model.ProcessingType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ProcessingTypeTest {

    @Test(dataProvider = "getIdTestData")
    public void testGetId(ProcessingType processingType, String expectedId) {
        assertEquals(processingType.getId(), expectedId);
    }

    @DataProvider
    public Object[][] getIdTestData() {
        return new Object[][]{
                {ProcessingType.SUGGESTED_REJECTED, "SUGGESTED_REJECTED"},
                {ProcessingType.SUGGESTED_APPROVED, "SUGGESTED_APPROVED"}
        };
    }

    @Test(dataProvider = "getDisplayNameTestData")
    public void testGetDisplayName(ProcessingType processingType, String expectedDisplayName) {
        assertEquals(processingType.getDisplayName(), expectedDisplayName);
    }

    @DataProvider
    public Object[][] getDisplayNameTestData() {
        return new Object[][]{
                {ProcessingType.SUGGESTED_REJECTED, "@voting.confirmation.processing.type.suggestedRejected"},
                {ProcessingType.SUGGESTED_APPROVED, "@voting.confirmation.processing.type.suggestedApproved"}
        };
    }
}