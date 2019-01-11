package no.valg.eva.admin.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.util.MathUtil.calculatePercentage;
import static org.testng.Assert.assertEquals;

public class MathUtilTest {

    @Test(dataProvider = "calculatePercentageTestData")
    public void testCalculatePercentage(double part, double total, Object expectedResult) {
        assertEquals(calculatePercentage(part, total), expectedResult);
    }

    @DataProvider
    public Object[][] calculatePercentageTestData() {
        return new Object[][]{
                {0, 10, 0},
                {0, 0, 0},
                {10, 10, 100},
                {5, 10, 50},
                {-1, 10, -10},
                {100, 10, 1000},
                {0.52, 13.5, 4},
        };
    }
}