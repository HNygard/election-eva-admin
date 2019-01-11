package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;

import org.testng.annotations.Test;

public class CountingOverviewColumnModelTest {
	private static final String HEADER = "header";

	@Test
	public void getHeader_givenHeader_returnsHeader() throws Exception {
		CountingOverviewColumnModel column = new CountingOverviewColumnModel(HEADER) {
			@Override
			public List<ColumnOverviewItemModel> itemsFor(CountingOverview countingOverview) {
				return null;
			}
		};
		assertThat(column.getHeader()).isEqualTo(HEADER);
	}
}
