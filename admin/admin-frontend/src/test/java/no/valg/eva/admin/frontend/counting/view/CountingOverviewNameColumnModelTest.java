package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class CountingOverviewNameColumnModelTest extends MockUtilsTestCase {
	private static final String NAME = "name";

	@Test
	public void itemsFor_givenCountingOverview_returnsNameItem() throws Exception {
		CountingOverview countingOverview = createMock(CountingOverview.class);
		when(countingOverview.getName()).thenReturn(NAME);
		ColumnOverviewItemModel item = new ColumnOverviewTextItemModel(NAME);
		assertThat(new CountingOverviewNameColumnModel().itemsFor(countingOverview)).containsExactly(item);
	}

	@Test
	public void getHeader_givenNameColumn_returnsHeaderName() throws Exception {
		assertThat(new CountingOverviewNameColumnModel().getHeader()).isEqualTo("@count.overview.category");
	}
}
