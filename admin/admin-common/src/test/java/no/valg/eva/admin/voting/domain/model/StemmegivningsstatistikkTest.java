package no.valg.eva.admin.voting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class StemmegivningsstatistikkTest {

	@Test
	public void leggTil_summererStatistikk() {
		
		Stemmegivningsstatistikk stemmegivningsstatistikk = new Stemmegivningsstatistikk(1, 2, 3, 4)
				.add(new Stemmegivningsstatistikk(1, 2, 3, 4));
		assertThat(stemmegivningsstatistikk.getGodkjenteFhsg()).isEqualTo(2);
		assertThat(stemmegivningsstatistikk.getGodkjenteVtsg()).isEqualTo(4);
		assertThat(stemmegivningsstatistikk.getForkastedeFhsg()).isEqualTo(6);
		assertThat(stemmegivningsstatistikk.getForkastedeVtsg()).isEqualTo(8);
		
	}
}
