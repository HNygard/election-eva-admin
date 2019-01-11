package no.valg.eva.admin.frontend.reportingunit.ctrls;

import no.evote.dto.ReportingUnitTypeDto;
import no.evote.exception.EvoteException;
import no.evote.service.configuration.ReportingUnitTypeService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportingUnitTypeControllerTest extends BaseFrontendTest {

	private ReportingUnitTypeController ctrl;

	@BeforeMethod
	public void setUp() throws Exception {
		this.ctrl = initializeMocks(ReportingUnitTypeController.class);
	}

	@Test
    public void doInit_withCentralConfigStatus_checkReportingUnitTypeDtoList() {
        when(getInjectMock(ReportingUnitTypeService.class).populateReportingUnitTypeDto(eq(getUserDataMock()), any()))
                .thenReturn(singletonList(new ReportingUnitTypeDto()));

		ctrl.init();

		assertThat(ctrl.getReportingUnitTypeDtoList().size()).isEqualTo(1);
	}

	@Test
    public void doInit_withLocalConfigStatus_verifyErrorMessage() {
		when(getInjectMock(UserDataController.class).getElectionEvent()
				.getElectionEventStatus().getId()).thenReturn(ElectionEventStatusEnum.LOCAL_CONFIGURATION.id());

		ctrl.init();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@reporting_unit_type.election_event_not_central");
	}

	@Test
    public void setReportingUnitTypeRow_withDTO_verifyReportingUnit() {
		MvElection election1 = new MvElection();
		election1.setPk(1L);
		MvElection election2 = new MvElection();
		election2.setPk(2L);
		ReportingUnitTypeDto dto = new ReportingUnitTypeDto();
		dto.setElections(Arrays.asList(election1, election2));
        dto.setSelectedElections(singletonList(election2));

		ctrl.setReportingUnitTypeRow(dto);

		assertThat(ctrl.getElectionList().size()).isEqualTo(2);
		assertThat(ctrl.getElectionList().get(0).isReportingUnit()).isFalse();
		assertThat(ctrl.getElectionList().get(1).isReportingUnit()).isTrue();
	}

	@Test(dataProvider = "changeAreaLevel")
    public void updateElectionReportingUnits_withDataProvider_verifyExpected(String error, FacesMessage.Severity expectedSeverity, String expectedMessage) {
		ReportingUnitTypeDto dto = new ReportingUnitTypeDto();
        ctrl.setElectionList(new ArrayList<>());
		if (error != null) {
			doThrow(new EvoteException(error)).when(getInjectMock(ReportingUnitTypeService.class))
					.updateMvElectionReportingUnits(eq(getUserDataMock()), anyList(), anyLong());
		}

		ctrl.updateElectionReportingUnits(dto);

		assertFacesMessage(expectedSeverity, expectedMessage);
        verify(getInjectMock(ReportingUnitTypeService.class)).populateReportingUnitTypeDto(eq(getUserDataMock()), any());
	}

	@DataProvider(name = "changeAreaLevel")
	private static Object[][] changeAreaLevel() {
		return new Object[][] {
				{ "@reporting_unit.duplicate", FacesMessage.SEVERITY_ERROR, "@reporting_unit.duplicate" },
				{ "ERROR", FacesMessage.SEVERITY_ERROR, "@reporting_unit_type.update_election_reporting_unit_fail" },
				{ null, FacesMessage.SEVERITY_INFO, "@reporting_unit_type.update_election_reporting_unit_success" }
		};
	}
}
