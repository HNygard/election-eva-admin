package no.valg.eva.admin.common.counting.model.countingoverview;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class CountingOverviewTest extends MockUtilsTestCase {
	@Test
	public void getStatus_givenStatuses_returnsStatus() throws Exception {
		Status status1 = createMock(Status.class);
		Status status2 = createMock(Status.class);
		Status status3 = createMock(Status.class);
		CountingOverview countingOverview = new CountingOverview() {
			@Override
			public String getName() {
				return null;
			}

			@Override
			public AreaPath getAreaPath() {
				return null;
			}

			@Override
			public List<Status> getStatuses() {
				return asList(status1, status2);
			}
		};
		when(status1.merge(status2)).thenReturn(status3);

		assertThat(countingOverview.getStatus()).isEqualTo(status3);
	}
}
