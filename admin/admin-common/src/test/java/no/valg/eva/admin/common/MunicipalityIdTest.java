package no.valg.eva.admin.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class MunicipalityIdTest {

    @Test
    public void countyId_for0301_is03() {
        assertThat(new MunicipalityId(AreaPath.OSLO_MUNICIPALITY_ID).countyId()).isEqualTo("03");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructor_illegalId_throwsException() {
        new MunicipalityId("A301");
    }
}
