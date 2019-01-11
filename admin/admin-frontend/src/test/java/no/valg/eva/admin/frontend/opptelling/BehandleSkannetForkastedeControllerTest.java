package no.valg.eva.admin.frontend.opptelling;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.Test;

public class BehandleSkannetForkastedeControllerTest extends BaseFrontendTest {
	@Test
	public void url_gittController_returnerUrl() throws Exception {
		assertThat(new BehandleSkannetForkastedeController().url())
				.isEqualTo("/secure/counting/approveScannedRejectedCount.xhtml?category=%s&contestPath=%s&areaPath=%s&fraMeny=true");
	}
}
