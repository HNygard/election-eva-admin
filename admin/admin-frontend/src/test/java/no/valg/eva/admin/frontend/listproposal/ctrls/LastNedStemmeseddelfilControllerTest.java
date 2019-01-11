package no.valg.eva.admin.frontend.listproposal.ctrls;

import no.evote.service.SpecialPurposeReportService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.listproposal.models.LastNedStemmeseddelfilPanel;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LastNedStemmeseddelfilControllerTest extends BaseFrontendTest {

	@Test
	public void init_lagerPanel() throws Exception {
		LastNedStemmeseddelfilController ctrl = initializeMocks(LastNedStemmeseddelfilController.class);

		ctrl.init();

		assertThat(ctrl.getPanel()).isNotNull();
	}

	@Test
	public void lastNed_medData_verifiserRapport() throws Exception {
		LastNedStemmeseddelfilController ctrl = initializeMocks(LastNedStemmeseddelfilController.class);
		LastNedStemmeseddelfilPanel panel = mockField("panel", LastNedStemmeseddelfilPanel.class);
		when(panel.getValg().navn()).thenReturn("Valg");
		when(panel.getValgdistrikt().id()).thenReturn("01");
		when(panel.getValgdistrikt().navn()).thenReturn("Valgdistrikt");
		when(panel.getValgdistrikt().sti()).thenReturn(ValghierarkiSti.valgdistriktSti(ELECTION_PATH_CONTEST));

		ctrl.lastNed();

		verify(getInjectMock(SpecialPurposeReportService.class)).generateBallots(eq(getUserDataMock()), any(ValgdistriktSti.class));
		verify(getServletContainer().getResponseMock()).addHeader(
				"Content-Disposition", "attachment; filename=\"[@rapport.meta.Report_27.filename, Valg, 01, Valgdistrikt]\"");
	}

}
