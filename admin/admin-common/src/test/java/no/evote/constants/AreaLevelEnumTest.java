package no.evote.constants;

import static java.util.Arrays.asList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.NONE;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.AreaLevelEnum.POLLING_STATION;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.evote.constants.AreaLevelEnum.comparator;
import static no.evote.constants.AreaLevelEnum.getLevel;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;


public class AreaLevelEnumTest {

	private static final int INVALID_AREA_LEVEL = 154;

	@Test
	public void getLevel_givenAnAreaLevel_returnsCorrespondingEnumValue() {
		Assertions.assertThat(getLevel(ROOT.getLevel())).isEqualTo(ROOT);
		Assertions.assertThat(getLevel(COUNTRY.getLevel())).isEqualTo(COUNTRY);
		Assertions.assertThat(getLevel(COUNTY.getLevel())).isEqualTo(COUNTY);
		Assertions.assertThat(getLevel(MUNICIPALITY.getLevel())).isEqualTo(MUNICIPALITY);
		Assertions.assertThat(getLevel(BOROUGH.getLevel())).isEqualTo(BOROUGH);
		Assertions.assertThat(getLevel(POLLING_DISTRICT.getLevel())).isEqualTo(POLLING_DISTRICT);
		Assertions.assertThat(getLevel(POLLING_PLACE.getLevel())).isEqualTo(POLLING_PLACE);
		Assertions.assertThat(getLevel(POLLING_STATION.getLevel())).isEqualTo(POLLING_STATION);
	}

	@Test
	public void getLevel_givenANonValidAreaLevel_returnsLevelNone() {
		Assertions.assertThat(getLevel(NONE.getLevel())).isEqualTo(NONE);
		Assertions.assertThat(getLevel(INVALID_AREA_LEVEL)).isEqualTo(NONE);
	}

	@Test
	public void comparator_withUnsorted_verifySorting() throws Exception {
		List<AreaLevelEnum> unsorted = asList(MUNICIPALITY, COUNTRY, BOROUGH, COUNTY, ROOT, POLLING_STATION, POLLING_PLACE, POLLING_DISTRICT);

		List<AreaLevelEnum> sorted = unsorted.stream().sorted(comparator()).collect(Collectors.toList());

		assertThat(sorted.get(0)).isSameAs(ROOT);
		assertThat(sorted.get(1)).isSameAs(COUNTRY);
		assertThat(sorted.get(2)).isSameAs(COUNTY);
		assertThat(sorted.get(3)).isSameAs(MUNICIPALITY);
		assertThat(sorted.get(4)).isSameAs(BOROUGH);
		assertThat(sorted.get(5)).isSameAs(POLLING_DISTRICT);
		assertThat(sorted.get(6)).isSameAs(POLLING_PLACE);
		assertThat(sorted.get(7)).isSameAs(POLLING_STATION);
	}
}

