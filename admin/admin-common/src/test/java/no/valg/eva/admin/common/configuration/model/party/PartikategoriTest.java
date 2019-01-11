package no.valg.eva.admin.common.configuration.model.party;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PartikategoriTest {

	@DataProvider()
	public Object[][] fromId() {
		return new Object[][] {
				{ "1", Partikategori.STORTING },
				{ "2", Partikategori.LANDSDEKKENDE },
				{ "3", Partikategori.LOKALT }
		};
	}

	@Test(dataProvider = "fromId")
	public void fromId_returnsEnum(String id, Partikategori partikategori) {
		assertThat(Partikategori.fromId(id)).isEqualTo(partikategori);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void fromId_unknownId_throwsIllegalArgumentException() {
		Partikategori.fromId("4");
	}
}
