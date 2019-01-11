package no.valg.eva.admin.voting.domain.electoralroll;

import no.valg.eva.admin.common.AreaPath;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BoroughIdResolverTest {
	
	@DataProvider
	public static Object[][] municipalityAndPollingDistrictToBoroughId() {
		return new Object[][] {
				{ "0101", "0201", "010102" },
				{ AreaPath.OSLO_MUNICIPALITY_ID, "1601", "030104" },
				{ AreaPath.OSLO_MUNICIPALITY_ID, "1701", "030107" },
				{ AreaPath.OSLO_MUNICIPALITY_ID, "1702", "030108" },
				{ AreaPath.OSLO_MUNICIPALITY_ID, "0502", "030105" }
		};
	}

	@Test(dataProvider = "municipalityAndPollingDistrictToBoroughId")
	public void boroughIdFor_givenParameters_isExpectedBoroughId(final String kommunenr, final String valgkrets, String expectedBoroughId) {
		VoterRecord skdVoterRecord = new FakeVoterRecord() {
			@Override
			public Character endringstypeChar() {
				return null;
			}

			@Override
			public boolean isElectoralRollChange() {
				return false;
			}

			@Override
			public String kommunenr() {
				return kommunenr;
			}
			
			@Override
			public String legacyKommunenr() { return kommunenr; }

			@Override
			public String valgkrets() {
				return valgkrets;
			}
			
			@Override
			public String legacyValgkrets() { return valgkrets; }
		};

		assertThat(BoroughIdResolver.boroughIdFor(skdVoterRecord)).isEqualTo(expectedBoroughId);
	}

}
