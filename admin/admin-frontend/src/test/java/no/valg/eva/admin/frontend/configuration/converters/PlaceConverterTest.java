package no.valg.eva.admin.frontend.configuration.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.faces.convert.ConverterException;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;

import org.testng.annotations.Test;


public class PlaceConverterTest extends BaseFrontendTest {

	@Test
	public void getAsObject_withNoValue_returnsNull() throws Exception {
		PlaceConverter converter = new PlaceConverter(null);

		assertThat(converter.getAsObject(null, null, null)).isNull();
	}

	@Test(expectedExceptions = ConverterException.class)
	public void getAsObject_withInvalidValue_throwsException() throws Exception {
		PlaceConverter converter = new PlaceConverter(getSource("0001"));

		converter.getAsObject(null, null, "0002");
	}

	@Test
	public void getAsObject_withValidValue_returnsObject() throws Exception {
		PlaceConverter converter = new PlaceConverter(getSource("0001"));

		assertThat(converter.getAsObject(null, null, "0001")).isNotNull();
	}

	@Test
	public void getAsString_withString_returnsSameString() throws Exception {
		PlaceConverter converter = new PlaceConverter(getSource("0001"));

		assertThat(converter.getAsString(null, null, "0001")).isEqualTo("0001");
	}

	@Test
	public void getAsString_withPlace_returnsPlaceId() throws Exception {
		PlaceConverter converter = new PlaceConverter(getSource("0001"));

		assertThat(converter.getAsString(null, null, place("0001"))).isEqualTo("0001");
	}

	@Test
	public void getAsString_withInvalidObject_returnsEmptyString() throws Exception {
		PlaceConverter converter = new PlaceConverter(getSource("0001"));

		assertThat(converter.getAsString(null, null, 100L)).isEmpty();
	}

	private PlaceConverterSource<AdvancePollingPlace> getSource(final String... ids) {
		return new PlaceConverterSource<AdvancePollingPlace>() {
			@Override
			public PlaceConverter getPlaceConverter() {
				return null;
			}

			@Override
			public List<AdvancePollingPlace> getPlaces() {
				List<AdvancePollingPlace> result = new ArrayList<>();
				for (String id : ids) {
					result.add(place(id));
				}
				return result;
			}
		};
	}

	private AdvancePollingPlace place(String id) {
		AdvancePollingPlace place = new AdvancePollingPlace(AreaPath.from("111111.22.33.4444"));
		place.setId(id);
		return place;
	}
}

