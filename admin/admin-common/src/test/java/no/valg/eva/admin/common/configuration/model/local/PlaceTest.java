package no.valg.eva.admin.common.configuration.model.local;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class PlaceTest extends MockUtilsTestCase {

	private static final AreaPath COUNTY = AreaPath.from("111111.22.33");
	private static final AreaPath MUNICIPALITY = AreaPath.from("111111.22.33.4444");

	@Test
	public void equals_withSameInstance_returnsTrue() throws Exception {
		Place place = new MyPlace(MUNICIPALITY);

		assertThat(place.equals(place)).isTrue();
	}

	@Test(dataProvider = "equalsDataProvider")
	public void equals_withDataProvider_verifyExpected(Place place, Object other, boolean expected) throws Exception {
		assertThat(place.equals(other)).isEqualTo(expected);
	}

	@DataProvider(name = "equalsDataProvider")
	public Object[][] equalsDataProvider() {
		return new Object[][] {
				{ place(), "", false },
				{ place(1L, "1"), place(2L, "1"), false },
				{ place(1L, "1"), place(1L, "2"), false },
				{ place(MUNICIPALITY, 1L, "1"), place(COUNTY, 1L, "1"), false },
				{ place(), place(), true }
		};
	}

	private Place place() {
		return place(MUNICIPALITY, 1L, "1");
	}

	private Place place(Long pk, String id) {
		return place(MUNICIPALITY, pk, id);
	}

	private Place place(AreaPath path, Long pk, String id) {
		Place result = new MyPlace(path);
		result.setPk(pk);
		result.setId(id);
		return result;
	}

	@Test
	public void equals_withDifferentPks_returnsFalse() throws Exception {
		Place place = new MyPlace(MUNICIPALITY);
		place.setPk(10L);
		Place place2 = new MyPlace(MUNICIPALITY);
		place.setPk(11L);

		assertThat(place.equals(place2)).isFalse();
	}

	@Test
	public void equals_withDifferentIds_returnsFalse() throws Exception {
		Place place = new MyPlace(MUNICIPALITY);
		place.setId("10");
		Place place2 = new MyPlace(MUNICIPALITY);
		place.setId("11");

		assertThat(place.equals(place2)).isFalse();
	}

	@Test
	public void equals_withDifferentIdsss_returnsFalse() throws Exception {
		Place place = new MyPlace(MUNICIPALITY);
		place.setId("10");
		Place place2 = new MyPlace(MUNICIPALITY);
		place.setId("11");

		assertThat(place.equals(place2)).isFalse();
	}

	@Test
	public void hashCode_with_should() throws Exception {

	}

	class MyPlace extends Place {
		MyPlace(AreaPath path) {
			super(path, 0);
		}
	}
}

