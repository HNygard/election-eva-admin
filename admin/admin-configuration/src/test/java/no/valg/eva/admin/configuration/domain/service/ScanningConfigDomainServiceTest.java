package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.util.ExcelUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static no.valg.eva.admin.util.ExcelUtil.VALUES_FROM_PAIRS_F;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ScanningConfigDomainServiceTest extends AbstractJpaTestBase {

    @Test
    public void exportRows_shouldHaveCorrectRows() throws Exception {
        ScanningConfigDomainService scanningConfigService = initializeMocks(ScanningConfigDomainService.class);
        when(getInjectMock(MunicipalityRepository.class).findByElectionEventWithScanningConfig(any())).thenReturn(createMunicipalities());
        when(getInjectMock(CountyRepository.class).findByElectionEventWithScanningConfig(any())).thenReturn(createCounties());
        
        byte[] result = scanningConfigService.exportScanningConfigAsExcelFile(1L);
        List<String[]> rows = transform(ExcelUtil.getRowDataFromExcelFile(new ByteArrayInputStream(result)).getRows(), VALUES_FROM_PAIRS_F::apply);
        
        Assert.assertEquals(rows.size(), 6);
        Assert.assertEquals(rows.get(0)[0], "07");
        Assert.assertEquals(rows.get(0)[2], null);
        Assert.assertEquals(rows.get(1)[2], "0701");
        Assert.assertEquals(rows.get(2)[0], "08");
        Assert.assertEquals(rows.get(3)[2], "0802");
        Assert.assertEquals(rows.get(5)[2], "1003");
    }
    
    private List<Municipality> createMunicipalities() {
        ArrayList<Municipality> municipalities = new ArrayList<>();
        List<County> counties = createCounties();
        municipalities.add(new Municipality("0701", "Kommune1", counties.get(0)));
        municipalities.add(new Municipality("0802", "Kommune2", counties.get(1)));
        municipalities.add(new Municipality("1003", "Kommune3", counties.get(2)));
        for (Municipality municipality : municipalities) {
            municipality.getOrCreateScanningConfig().setScanning(true);
        }
        return municipalities;
    }
    
    private List<County> createCounties() {
        ArrayList<County> counties = new ArrayList<>();
        counties.add(new County("07", "Fylke1", null));
        counties.add(new County("08", "Fylke2", null));
        counties.add(new County("10", "Fylke3", null));
        for (County county : counties) {
            county.getOrCreateScanningConfig().setScanning(true);
        }
        return counties;
    }
}
