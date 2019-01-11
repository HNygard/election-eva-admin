package no.valg.eva.admin.frontend.settlement.ctrls;

import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class CorrectionsReportControllerTest extends BaseFrontendTest {

    private CorrectionsReportController ctrl;

    @BeforeMethod
    public void setUp() throws Exception {
        ctrl = initializeMocks(CorrectionsReportController.class);
    }

    @Test(dataProvider = "areaLevelAndElectionLevel")
    public void initialized_givenContext_validatesAreaLevelAndElectionLevelCorrectly(int areaLevel, int electionLevel, boolean expectedInvalid) {
        MvArea mvAreaStub = createMock(MvArea.class);
        MvElection mvElectionStub = mock(MvElection.class, RETURNS_DEEP_STUBS);
        when(mvAreaStub.getAreaLevel()).thenReturn(areaLevel);
        when(mvElectionStub.getElection().getAreaLevel()).thenReturn(electionLevel);
        when(getInjectMock(MvAreaService.class).findSingleByPath(any(ValggeografiSti.class))).thenReturn(mvAreaStub);
        when(getInjectMock(MvElectionService.class).findSingleByPath(any(ValghierarkiSti.class))).thenReturn(mvElectionStub);

        ctrl.initialized(mock(Kontekst.class, RETURNS_DEEP_STUBS));

        assertEquals(ctrl.isExportButtonDisabled(), expectedInvalid);
        if (expectedInvalid) {
            assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@settlement.error.invalid_level");
        }
    }

    @DataProvider
    public Object[][] areaLevelAndElectionLevel() {
        return new Object[][]{
                {3, 3, false},
                {3, 4, true}
        };
    }
}
