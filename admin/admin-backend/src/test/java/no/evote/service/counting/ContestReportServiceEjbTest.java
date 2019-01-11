package no.evote.service.counting;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ContestReportServiceEjbTest extends MockUtilsTestCase {

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected <CONTEST> election path, but got <ELECTION>")
	public void hasContestReport_withElectionLevel_shouldReturnTrue() throws Exception {
		ContestReportServiceEjb ejb = initializeMocks(ContestReportServiceEjb.class);
		UserData userDataStub = createMock(UserData.class);

		ejb.hasContestReport(userDataStub, ElectionPath.from("752900.01.03"), AreaPath.from("752900.47.03.0301.030107"));
	}

	@Test
	public void hasContestReport_withContestLevel_shouldReturnTrue() throws Exception {
		ContestReportServiceEjb ejb = initializeMocks(ContestReportServiceEjb.class);
		UserData userDataStub = createMock(UserData.class);
		when(getInjectMock(ContestReportRepository.class).hasContestReport(any(Contest.class), any(ReportingUnit.class))).thenReturn(true);

		boolean result = ejb.hasContestReport(userDataStub, ElectionPath.from("752900.01.03.010101"), AreaPath.from("752900.47.03.0301.030107"));

		assertThat(result).isTrue();
	}
}
