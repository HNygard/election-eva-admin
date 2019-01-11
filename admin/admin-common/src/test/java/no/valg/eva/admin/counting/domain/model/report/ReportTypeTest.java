package no.valg.eva.admin.counting.domain.model.report;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.testng.annotations.Test;

public class ReportTypeTest {

	@Test
	public void isStemmeskjema_whenStemmeskjema_true() throws Exception {
		assertThat(ReportType.STEMMESKJEMA_FE.isStemmeskjema()).isTrue();
	}

	@Test
	public void isStemmeskjema_whenNotStemmeskjema_false() throws Exception {
		assertThat(ReportType.VALGOPPGJOR.isStemmeskjema()).isFalse();
	}
}
