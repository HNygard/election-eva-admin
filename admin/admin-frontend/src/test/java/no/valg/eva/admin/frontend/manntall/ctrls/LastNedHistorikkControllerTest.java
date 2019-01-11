package no.valg.eva.admin.frontend.manntall.ctrls;

import no.evote.model.views.VoterAudit;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voter.service.VoterAuditService;
import no.valg.eva.admin.configuration.domain.model.Voter;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class LastNedHistorikkControllerTest extends BaseFrontendTest {

	@Test
	public void lastManntallsHistorikk_medVelger_verifiserListe() throws Exception {
		LastNedHistorikkController ctrl = initializeMocks(LastNedHistorikkController.class);
		when(getInjectMock(VoterAuditService.class).getHistoryForVoter(eq(getUserDataMock()), anyLong()))
                .thenReturn(Collections.singletonList(createMock(VoterAudit.class)));

		ctrl.lastManntallsHistorikk(createMock(Voter.class));

		assertThat(ctrl.getManntallshistorikk()).hasSize(1);
	}

}
