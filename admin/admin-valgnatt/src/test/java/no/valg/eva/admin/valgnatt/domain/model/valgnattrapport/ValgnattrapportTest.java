package no.valg.eva.admin.valgnatt.domain.model.valgnattrapport;

import static org.assertj.core.api.Java6Assertions.assertThat;

import no.valg.eva.admin.counting.domain.model.report.ReportType;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValgnattrapportTest {

    @Test
    public void countQualifier_ikkeStemmeskjemaRapportType_blankStreng() {
        Valgnattrapport valgnattrapport = new Valgnattrapport(null, ReportType.GEOGRAFI_STEMMEBERETTIGEDE);

        assertThat(valgnattrapport.countQualifier()).isEqualTo("");
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] {
                {ReportType.STEMMESKJEMA_FF, "ff"},
                {ReportType.STEMMESKJEMA_FE, "fe"},
                {ReportType.STEMMESKJEMA_VF, "vf"},
                {ReportType.STEMMESKJEMA_VE, "ve"}
        };
    }

    @Test(dataProvider = "testData")
    public void countQualifier_stemmeskjemaRapportType(ReportType reportType, String expectedQualifier) {
        Valgnattrapport valgnattrapport = new Valgnattrapport(null, reportType);

        assertThat(valgnattrapport.countQualifier()).isEqualTo(expectedQualifier);
    }

    @Test
    public void isNotSent_statusOK_isFalse() {
        Valgnattrapport valgnattrapport = new Valgnattrapport(null, null, null, null, null, ValgnattrapportStatus.OK, null, false);
        assertThat(valgnattrapport.isNotSent()).isFalse();
    }

    @Test
    public void isNotSent_statusNotOK_isTrue() {
        Valgnattrapport valgnattrapport = new Valgnattrapport(null, null, null, null, null, ValgnattrapportStatus.NOT_SENT, null, false);
        assertThat(valgnattrapport.isNotSent()).isTrue();
    }
}
