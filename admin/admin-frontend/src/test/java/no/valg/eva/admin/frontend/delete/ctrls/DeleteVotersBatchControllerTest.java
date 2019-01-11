package no.valg.eva.admin.frontend.delete.ctrls;

import no.evote.model.Batch;
import no.evote.service.BatchService;
import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.Test;

import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.DELETE_VOTERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DeleteVotersBatchControllerTest extends BaseFrontendTest {

	@Test
	public void init_withBatch_returnsOneBatch() throws Exception {
		DeleteVotersBatchController ctrl = initializeMocks(DeleteVotersBatchController.class);
		when(getInjectMock(BatchService.class).listBatchesByEventAndCategory(eq(getUserDataMock()), eq(DELETE_VOTERS), any())).thenReturn(
				mockList(1, Batch.class));

		ctrl.init();

		assertThat(ctrl.getBatches()).hasSize(1);

	}
}
