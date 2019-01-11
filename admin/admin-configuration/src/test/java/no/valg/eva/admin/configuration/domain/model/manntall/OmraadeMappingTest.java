package no.valg.eva.admin.configuration.domain.model.manntall;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OmraadeMappingTest {

	@Test
	public void equals_gittUlikeFraOmraade_returnererFalse() {
		OmraadeMapping omraadeMapping1 = new OmraadeMapping("123", "456");
		OmraadeMapping omraadeMapping2 = new OmraadeMapping("456", "456");
		Assertions.assertThat(omraadeMapping1.equals(omraadeMapping2)).isFalse();
	}

	@Test(dataProvider = "ikkeOmraadeMapping")
	public void equals_gittNoeAnnetOmraaddeMapping_returnererFalse(Object ikkeOmraadeMapping) {
		OmraadeMapping omraadeMapping = new OmraadeMapping("123", "456");
		assertThat(omraadeMapping.equals(ikkeOmraadeMapping)).isFalse();
	}

	@DataProvider
	public Object[][] ikkeOmraadeMapping() {
		return new Object[][] {
				{ null },
				{ "tullOgTøys" }
		};
	}

}
