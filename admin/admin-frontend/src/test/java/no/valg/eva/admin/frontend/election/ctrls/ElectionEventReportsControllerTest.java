package no.valg.eva.admin.frontend.election.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rapport.model.ReportCategory;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rapport.service.RapportService;
import no.valg.eva.admin.common.rbac.Access;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.GRUNNLAGSDATA;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Grunnlagsdata_Brukere_Og_Roller_Kommune;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ElectionEventReportsControllerTest extends BaseFrontendTest {

	@Test
	public void init_withReports_verifyState() throws Exception {
		ElectionEventReportsController ctrl = initializeMocks(ElectionEventReportsController.class);
		stub_rapporterForValghendelse();

		ctrl.init();

		assertThat(ctrl.getMenus()).hasSize(1);
	}

	@Test
	public void saveReports_withReports_savesReportsAndReturnsMessage() throws Exception {
		ElectionEventReportsController ctrl = initializeMocks(ElectionEventReportsController.class);
		stub_rapporterForValghendelse();
		ctrl.init();

		ctrl.saveReports();

		assertFacesMessage(SEVERITY_INFO, "@election.event.reports.saved");
	}

	private List<ValghendelsesRapport> stub_rapporterForValghendelse() {
        List<ValghendelsesRapport> reports = singletonList(
                valghendelsesRapport("rapport_1", GRUNNLAGSDATA, Rapport_Grunnlagsdata_Brukere_Og_Roller_Kommune.getAccess()));
		when(getInjectMock(RapportService.class).rapporterForValghendelse(eq(getUserDataMock()), any(ElectionPath.class))).thenReturn(reports);
		return reports;
	}

	private ValghendelsesRapport valghendelsesRapport(String rapportId, ReportCategory kategori, Access access) {
		return new ValghendelsesRapport(rapportId, kategori, access);
	}

}
