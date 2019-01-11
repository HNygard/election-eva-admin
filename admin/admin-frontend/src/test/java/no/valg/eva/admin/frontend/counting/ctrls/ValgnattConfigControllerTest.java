package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;
import no.valg.eva.admin.common.counting.service.valgnatt.ValgnattReportService;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ValgnattConfigControllerTest extends BaseFrontendTest {

	private static final String AN_ELECTION_PATH = "150001.01.01";
	private static final String AN_ELECTION_NAME = "valg";
	private static final String A_CONTEST_NAME = "valgdistrikt";
	private static final String AN_AREA_PATH = "150001.47.01.0101";
	private static final String A_MESSAGE = "@valgnatt.skjema[geografi_stemmeberettigede]";

	@Test
	public void exportGeographyAndVoters_valgnattrapportHasStatusSending() throws Exception {
		List<Valgnattrapportering> fakeElectionDataList = makeValgnattSkjemaList();
		ValgnattConfigController valgnattConfigController = makeValgnattConfigController(fakeElectionDataList);
		when(getInjectMock(UserData.class).getOperatorAreaPath()).thenReturn(AreaPath.from(AN_AREA_PATH));
		ContestInfo contestInfo = new ContestInfo(AN_ELECTION_PATH, AN_ELECTION_NAME, A_CONTEST_NAME, AN_AREA_PATH);
		valgnattConfigController.onTabChange(contestInfo);

		valgnattConfigController.exportGeographyAndVoters();
		assertThat(fakeElectionDataList.get(0).getStatus()).isEqualTo("Sending");
	}

	@Test
	public void reportTypeName_returnMessageForTextId() throws Exception {
		ValgnattConfigController valgnattConfigController = makeValgnattConfigController(makeValgnattSkjemaList());

		assertThat(valgnattConfigController.reportTypeName(ReportType.GEOGRAFI_STEMMEBERETTIGEDE)).isEqualTo(A_MESSAGE);
	}

	private ValgnattConfigController makeValgnattConfigController(List<Valgnattrapportering> fakeElectionDataList) throws Exception {
		ValgnattConfigController ctrl = initializeMocks(ValgnattConfigController.class);
		when(getInjectMock(ValgnattReportService.class).rapporteringerForGrunnlagsdata(any(UserData.class), any(ElectionPath.class)
		)).thenReturn(fakeElectionDataList);
		return ctrl;
	}

	private List<Valgnattrapportering> makeValgnattSkjemaList() {
		List<Valgnattrapportering> list = new ArrayList<>();
		list.add(new Valgnattrapportering(null, null, null, null, ReportType.GEOGRAFI_STEMMEBERETTIGEDE, null, false, new DateTime(), null));
		list.add(new Valgnattrapportering(null, null, null, null, ReportType.PARTIER_OG_KANDIDATER, null, false, new DateTime(), null));
		return list;
	}
}
