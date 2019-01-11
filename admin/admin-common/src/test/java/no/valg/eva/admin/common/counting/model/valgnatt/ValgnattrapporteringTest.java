package no.valg.eva.admin.common.counting.model.valgnatt;

import static no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering.NOT_SENT;
import static no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering.OK;
import static no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering.RESENDES;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ValgnattrapporteringTest {

	@Test
	public void oppdaterStatusSendt_oppdatererStatusOgTidspunkt() {
		Valgnattrapportering valgnattrapportering = new Valgnattrapportering(null, null, null, null, null, null, false, null, null);
		
		valgnattrapportering.oppdaterStatusSendt();
		
		assertThat(valgnattrapportering.getStatus()).isEqualTo(Valgnattrapportering.SENDING);
		assertThat(valgnattrapportering.getAuditTimestamp()).isNotNull();
	}

	@Test
	public void kanRapporteres_ikkeKlarForRapportering_false() {
		Valgnattrapportering valgnattrapportering = new Valgnattrapportering(null, null, null, null, null, null, false, null, null);
		assertThat(valgnattrapportering.kanRapporteres()).isFalse();	
	}

	@Test
	public void kanRapporteres_klarForRapporteringStatusOK_false() {
		Valgnattrapportering valgnattrapportering = new Valgnattrapportering(null, null, null, null, null, null, true, null, OK);
		assertThat(valgnattrapportering.kanRapporteres()).isFalse();	
	}

	@Test
	public void kanRapporteres_klarForRapporteringStatusResendes_true() {
		Valgnattrapportering valgnattrapportering = new Valgnattrapportering(null, null, null, null, null, null, true, null, RESENDES);
		assertThat(valgnattrapportering.kanRapporteres()).isTrue();	
	}

	@Test
	public void kanRapporteres_klarForRapporteringStatusNotSent_true() {
		Valgnattrapportering valgnattrapportering = new Valgnattrapportering(null, null, null, null, null, null, true, null, NOT_SENT);
		assertThat(valgnattrapportering.kanRapporteres()).isTrue();	
	}
	
	@Test
	public void ferdigRapportert_klarForRapporteringOgOK_true() {
		Valgnattrapportering valgnattrapportering = new Valgnattrapportering(null, null, null, null, null, null, true, null, OK);
		assertThat(valgnattrapportering.ferdigRapportert()).isTrue();
	}
}
