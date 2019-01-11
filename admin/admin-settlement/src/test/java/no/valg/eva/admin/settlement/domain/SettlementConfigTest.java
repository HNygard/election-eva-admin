package no.valg.eva.admin.settlement.domain;

import static no.valg.eva.admin.settlement.domain.SettlementConfig.PERSONAL;
import static no.valg.eva.admin.settlement.domain.SettlementConfig.PERSONAL_AND_WRITE_IN;
import static no.valg.eva.admin.settlement.domain.SettlementConfig.RENUMBER;
import static no.valg.eva.admin.settlement.domain.SettlementConfig.RENUMBER_AND_STRIKEOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.configuration.domain.model.Election;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SettlementConfigTest {

	@DataProvider
	public static Object[][] electionsWithKnownConfig() {
		return new Object[][] {
				{ false, false, true, false, PERSONAL },
				{ false, false, true, true, PERSONAL_AND_WRITE_IN },
				{ true, false, false, false, RENUMBER },
				{ true, true, false, false, RENUMBER_AND_STRIKEOUT }
		};
	}

	@DataProvider
	public static Object[][] electionsWithUnknownConfig() {
		return new Object[][] {
				{ false, false, false, false },
				{ false, false, false, true },
				{ false, true, false, false },
				{ false, true, false, true },
				{ false, true, true, false },
				{ false, true, true, true },
				{ true, false, false, true },
				{ true, false, true, false },
				{ true, false, true, true },
				{ true, true, false, true },
				{ true, true, true, false },
				{ true, true, true, true }
		};
	}

	@Test(dataProvider = "electionsWithKnownConfig")
	public void from_givenElectionWithKnownConfig_returnsCorrectConfig(
			boolean renumber, boolean strikeout, boolean personal, boolean writeIn, SettlementConfig expectedConfig) throws Exception {
		Election election = election(renumber, strikeout, personal, writeIn);
		assertThat(SettlementConfig.from(election)).isSameAs(expectedConfig);
	}

	@Test(dataProvider = "electionsWithUnknownConfig", expectedExceptions = IllegalArgumentException.class)
	public void from_givenElectionWithUnknownConfig_throwsException(boolean renumber, boolean strikeout, boolean personal, boolean writeIn) throws Exception {
		SettlementConfig.from(election(renumber, strikeout, personal, writeIn));
	}

	private Election election(boolean renumber, boolean strikeout, boolean personal, boolean writeIn) {
		Election election = mock(Election.class);
		when(election.isRenumber()).thenReturn(renumber);
		when(election.isStrikeout()).thenReturn(strikeout);
		when(election.isPersonal()).thenReturn(personal);
		when(election.isWritein()).thenReturn(writeIn);
		return election;
	}
}
