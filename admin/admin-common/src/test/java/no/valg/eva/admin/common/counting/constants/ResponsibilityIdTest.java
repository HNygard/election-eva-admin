package no.valg.eva.admin.common.counting.constants;

import org.testng.annotations.Test;

import java.util.EnumSet;

import static no.valg.eva.admin.common.counting.constants.ResponsibilityId.isUniqueResponsibility;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ResponsibilityIdTest {

    @Test
    public void testFromId() {
        for(ResponsibilityId responsibilityId : EnumSet.allOf(ResponsibilityId.class)){
            assertEquals(ResponsibilityId.fromId(responsibilityId.getId()), responsibilityId);
        }
    }

    @Test
    public void testIsUniqueResponsibility_True() {
        assertTrue(isUniqueResponsibility(ResponsibilityId.LEDER));
        assertTrue(isUniqueResponsibility(ResponsibilityId.NESTLEDER));
        assertTrue(isUniqueResponsibility(ResponsibilityId.SEKRETAER));
    }

    @Test
    public void testIsUniqueResponsibility_False() {

        for (ResponsibilityId responsibilityId : EnumSet.allOf(ResponsibilityId.class)) {
            if (!expectedUnique(responsibilityId)) {
                assertFalse(isUniqueResponsibility(responsibilityId));
            }
        }
    }

    private boolean expectedUnique(ResponsibilityId responsibilityId) {
        return ResponsibilityId.LEDER.equals(responsibilityId) 
                || ResponsibilityId.SEKRETAER.equals(responsibilityId)
                || ResponsibilityId.NESTLEDER.equals(responsibilityId);
    }
    
}