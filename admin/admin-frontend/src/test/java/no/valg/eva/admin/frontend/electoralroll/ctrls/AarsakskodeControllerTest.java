package no.valg.eva.admin.frontend.electoralroll.ctrls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.Aarsakskode;

import org.testng.annotations.Test;

public class AarsakskodeControllerTest extends BaseFrontendTest {

	@Test
	public void init_withAarsakskodes_returnsAarsakskodeMap() throws Exception {
		AarsakskodeController ctrl = initializeMocks(AarsakskodeController.class);
		stub_findAllAarsakskoder();

		ctrl.init();

		assertThat(ctrl.getAarsakskodeMap()).hasSize(1);
	}

	private void stub_findAllAarsakskoder() {
		when(getInjectMock(VoterService.class).findAllAarsakskoder()).thenReturn(Arrays.asList(aarsakskode()));
	}

	private Aarsakskode aarsakskode() {
		Aarsakskode result = new Aarsakskode();
		result.setId("id1");
		result.setName("name1");
		return result;
	}
}
