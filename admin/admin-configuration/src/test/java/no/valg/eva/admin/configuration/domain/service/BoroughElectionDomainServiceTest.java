package no.valg.eva.admin.configuration.domain.service;

import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BoroughElectionDomainServiceTest extends MockUtilsTestCase {

    @Test(dataProvider = "forElectionPathAndMvAreaHasAccessToBoroughs")
    public void electionPathAndMvAreaHasAccessToBoroughs_givenElectionPaths_VerifyAccessToBoroughs(List<MvElection> findByPathAndLevelList,
                                                                                                   List<MvElection> findContestsForElectionAndAreaList,
                                                                                                   boolean expectingAccessToBoroughs)
            throws NoSuchFieldException, IllegalAccessException {
        BoroughElectionDomainService boroughElectionDomainService = initializeMocks(new BoroughElectionDomainService(null));
        MvElectionRepository mvElectionRepository = getInjectMock(MvElectionRepository.class);

        ElectionPath electionPath = ElectionPath.from("200701.01.01");
        AreaPath areaPath = AreaPath.from("200701");

        MvElection mvElection = new MvElection();
        mvElection.setAreaLevel(BOROUGH.getLevel());

        when(mvElectionRepository.findByPathAndLevel(electionPath, CONTEST)).thenReturn(findByPathAndLevelList);
        when(mvElectionRepository.findContestsForElectionAndArea(electionPath, areaPath)).thenReturn(findContestsForElectionAndAreaList);

        assertEquals(boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(electionPath, areaPath), expectingAccessToBoroughs);
    }

    @DataProvider
    private Object[][] forElectionPathAndMvAreaHasAccessToBoroughs() {
        MvElection mvElection = new MvElection();
        mvElection.setAreaLevel(BOROUGH.getLevel());

        List<MvElection> mvElectionList = Collections.singletonList(mvElection);

        return new Object[][]{
                {mvElectionList, Collections.emptyList(), false},
                {Collections.emptyList(), mvElectionList, false},
                {Collections.emptyList(), Collections.emptyList(), false},
                {mvElectionList, mvElectionList, true}
        };
    }

    @Test(dataProvider = "forTestCountingModeForBoroughWithCountCategory")
    public void countingModeForBoroughWithCountCategory_givenCountCategory_verifiesCountingMode(CountCategory countCategory, CountingMode countingMode) throws Exception {
        BoroughElectionDomainService boroughElectionDomainService = initializeMocks(new BoroughElectionDomainService(null));

        assertThat(boroughElectionDomainService.countingModeForBoroughWithCountCategory(countCategory)).isEqualTo(countingMode);
    }

    @DataProvider
    private Object[][] forTestCountingModeForBoroughWithCountCategory() {
        return new Object[][]{
                {CountCategory.VO, CountingMode.BY_POLLING_DISTRICT},
                {CountCategory.BF, CountingMode.CENTRAL},
                {CountCategory.FO, CountingMode.CENTRAL},
                {CountCategory.FS, CountingMode.CENTRAL},
                {CountCategory.VB, CountingMode.CENTRAL},
                {CountCategory.VF, CountingMode.CENTRAL},
                {CountCategory.VS, CountingMode.CENTRAL}
        };
    }
}
