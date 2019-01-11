package no.valg.eva.admin.common.counting.model.countingoverview;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;

public class CategoryCountingOverviewTest extends MockUtilsTestCase {
	private static final CountCategory CATEGORY = VO;
	private static final ElectionPath CONTEST_PATH = ElectionPath.from("111111.11.11.111111");
	private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111.111111.1111");

	@Test
	public void getName_givenCategoryCountingOverview_returnsCategoryMessageProperty() {
		assertThat(categoryCountingOverview().getName()).isEqualTo(CATEGORY.messageProperty());
	}

	private CategoryCountingOverview categoryCountingOverview() {
		return new CategoryCountingOverview(CATEGORY, CONTEST_PATH, AREA_PATH, singletonList(new CountingStatus()));
	}
}
