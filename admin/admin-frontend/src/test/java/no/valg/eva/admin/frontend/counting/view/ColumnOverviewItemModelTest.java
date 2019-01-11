package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ColumnOverviewItemModelTest {
	private static final String VALUE1 = "value1";
	private static final String VALUE2 = "value2";
	private static final String DESCRIPTION1 = "description1";
	private static final String DESCRIPTION2 = "description2";

	@DataProvider
	public static Object[][] isTestData() {
		return new Object[][] {
				new Object[] { new ColumnOverviewTextItemModel(VALUE1) },
				new Object[] { new ColumnOverviewIconItemModel(VALUE1) },
				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1) }
		};
	}

	@DataProvider
	public static Object[][] equalsTestData() {
		return new Object[][] {
				new Object[] { new ColumnOverviewTextItemModel(VALUE1), new ColumnOverviewTextItemModel(VALUE1), true },
				new Object[] { new ColumnOverviewTextItemModel(VALUE1), new ColumnOverviewTextItemModel(VALUE2), false },
				new Object[] { new ColumnOverviewTextItemModel(1), new ColumnOverviewTextItemModel(1), true },
				new Object[] { new ColumnOverviewTextItemModel(1), new ColumnOverviewTextItemModel(2), false },

				new Object[] { new ColumnOverviewIconItemModel(VALUE1), new ColumnOverviewIconItemModel(VALUE1), true },
				new Object[] { new ColumnOverviewIconItemModel(VALUE1), new ColumnOverviewIconItemModel(VALUE2), false },

				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), true },
				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION2), false },
				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), new ColumnOverviewLinkItemModel(VALUE2, DESCRIPTION1), false },
				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), new ColumnOverviewLinkItemModel(VALUE2, DESCRIPTION2), false },

				new Object[] { new ColumnOverviewTextItemModel(VALUE1), new ColumnOverviewIconItemModel(VALUE1), false },
				new Object[] { new ColumnOverviewTextItemModel(VALUE1), new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), false },
				new Object[] { new ColumnOverviewIconItemModel(VALUE1), new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), false }
		};
	}

	@DataProvider
	public static Object[][] hashcodeTestData() {
		return new Object[][] {
				new Object[] { new ColumnOverviewTextItemModel(VALUE1), new ColumnOverviewTextItemModel(VALUE1), true },
				new Object[] { new ColumnOverviewTextItemModel(VALUE1), new ColumnOverviewTextItemModel(VALUE2), false },
				new Object[] { new ColumnOverviewTextItemModel(1), new ColumnOverviewTextItemModel(1), true },
				new Object[] { new ColumnOverviewTextItemModel(1), new ColumnOverviewTextItemModel(2), false },

				new Object[] { new ColumnOverviewIconItemModel(VALUE1), new ColumnOverviewIconItemModel(VALUE1), true },
				new Object[] { new ColumnOverviewIconItemModel(VALUE1), new ColumnOverviewIconItemModel(VALUE2), false },

				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), true },
				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION2), false },
				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), new ColumnOverviewLinkItemModel(VALUE2, DESCRIPTION1), false },
				new Object[] { new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), new ColumnOverviewLinkItemModel(VALUE2, DESCRIPTION2), false },

				new Object[] { new ColumnOverviewTextItemModel(VALUE1), new ColumnOverviewIconItemModel(VALUE1), true },
				new Object[] { new ColumnOverviewTextItemModel(VALUE1), new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), false },
				new Object[] { new ColumnOverviewIconItemModel(VALUE1), new ColumnOverviewLinkItemModel(VALUE1, DESCRIPTION1), false }
		};
	}

	@Test
	public void getValue_givenValue_returnsValue() throws Exception {
		assertThat(item(VALUE1).getValue()).isEqualTo(VALUE1);
		assertThat(item(null).getValue()).isNull();
	}

	@Test
	public void getDescription_givenDescriptions_returnsDescription() throws Exception {
		assertThat(item(VALUE1, DESCRIPTION1).getDescription()).isEqualTo(DESCRIPTION1);
		assertThat(item(VALUE1, null).getDescription()).isNull();
		assertThat(item(VALUE1).getDescription()).isNull();
	}

	@Test(dataProvider = "isTestData")
	public void isText_givenObjects_returnsTrueOrFalse(ColumnOverviewItemModel item) throws Exception {
		assertThat(item.isText()).isEqualTo(item instanceof ColumnOverviewTextItemModel);
	}

	@Test(dataProvider = "isTestData")
	public void isIcon_givenObjects_returnsTrueOrFalse(ColumnOverviewItemModel item) throws Exception {
		assertThat(item.isIcon()).isEqualTo(item instanceof ColumnOverviewIconItemModel);
	}

	@Test(dataProvider = "isTestData")
	public void isLink_givenObjects_returnsTrueOrFalse(ColumnOverviewItemModel item) throws Exception {
		assertThat(item.isLink()).isEqualTo(item instanceof ColumnOverviewLinkItemModel);
	}

	@Test(dataProvider = "equalsTestData")
	public void equals_givenValueAndDescription_isEqual(ColumnOverviewItemModel item1, ColumnOverviewItemModel item2, boolean expected) throws Exception {
		assertThat(item1.equals(item2)).isEqualTo(expected);
	}

	@Test
	public void equals_givenSimpleTestData_returnsTrueOrFalse() throws Exception {
		ColumnOverviewItemModel item = new ColumnOverviewItemModel(VALUE1) {
		};
		Object anObject = new Object();
		Object aNull = null;

		assertThat(item.equals(anObject)).isFalse();
		assertThat(item.equals(aNull)).isFalse();
		assertThat(item.equals(item)).isTrue();
	}

	@Test(dataProvider = "hashcodeTestData")
	public void testHashCode(ColumnOverviewItemModel item1, ColumnOverviewItemModel item2, boolean expected) throws Exception {
		assertThat(item1.hashCode() == item2.hashCode()).isEqualTo(expected);
	}

	private ColumnOverviewItemModel item(String value) {
		return new ColumnOverviewItemModel(value) {
		};
	}

	private ColumnOverviewItemModel item(String value, String description) {
		return new ColumnOverviewItemModel(value, description) {
		};
	}

}
