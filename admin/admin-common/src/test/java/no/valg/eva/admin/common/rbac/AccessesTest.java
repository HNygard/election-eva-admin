package no.valg.eva.admin.common.rbac;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Brukere_Roller;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling_Sett_Til_Valgoppgjør;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Valghendelse;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valghendelse;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valghendelse_Liste;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Rettelser_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Forhånd_Registrer;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AccessesTest extends MockUtilsTestCase {

	@Test(dataProvider = "is")
	public void is_withDataProvider_verifyExpected(Accesses base, Accesses accesses, boolean expected) throws Exception {
		assertThat(base.is(accesses)).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] is() {
		return new Object[][] {
				{ Konfigurasjon, Konfigurasjon_Grunnlagsdata, false },
				{ Konfigurasjon_Grunnlagsdata, Konfigurasjon, false },
				{ Konfigurasjon_Grunnlagsdata, Konfigurasjon_Grunnlagsdata, true },
				{ Aggregert_Valghendelse, Konfigurasjon_Valghendelse, true },
				{ Aggregert_Valghendelse, Konfigurasjon_Valghendelse_Liste, true },
				{ Aggregert_Stemmegiving, Stemmegiving_Forhånd_Registrer, true },
				{ Aggregert_Opptelling_Sett_Til_Valgoppgjør, Opptelling_Rettelser_Rediger, true },
				{ Aggregert_Opptelling_Sett_Til_Valgoppgjør, Opptelling_Forhånd_Rediger, true },
				{ Konfigurasjon, Aggregert_Stemmegiving, false },

		};
	}

	@Test
	public void paths_withStandardAccess_returnsOnePath() throws Exception {
		assertThat(Konfigurasjon_Valghendelse_Liste.paths()).hasSize(1);
	}

	@Test
	public void paths_withAggregatedAccess_returnsMoreThanOnePath() throws Exception {
		assertThat(Aggregert_Brukere_Roller.paths().length > 1).isTrue();
	}

}
