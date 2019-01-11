package no.valg.eva.admin.frontend.manntall.ctrls;

import no.evote.service.SpecialPurposeReportService;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import org.testng.annotations.Test;

import javax.servlet.ServletOutputStream;

import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SkrivUtValgkortControllerTest extends BaseFrontendTest {

	@Test
	public void isVisSkrivValgkortKnapp_medVelgerMedManntallsLinje_returnererTrue() throws Exception {
		SkrivUtValgkortController ctrl = initializeMocks(SkrivUtValgkortController.class);
		Voter velger = createMock(Voter.class);
		when(velger.getElectoralRollLine()).thenReturn(10);

		assertThat(ctrl.isVisSkrivValgkortKnapp(createMock(Voter.class))).isTrue();
	}

	@Test
	public void skrivValgkort_medVelger_skriverUtValgkort() throws Exception {
		SkrivUtValgkortController ctrl = initializeMocks(SkrivUtValgkortController.class);
		Voter velger = createMock(Voter.class);
		when(velger.getMvArea().getAreaLevel()).thenReturn(POLLING_DISTRICT.getLevel());
		when(velger.getMvArea().areaPath().toMunicipalityPath()).thenReturn(AREA_PATH_MUNICIPALITY);
		String result = stub_generateElectionCard("hello");

		ctrl.skrivValgkort(velger, createMock(MvElection.class), createMock(MvArea.class));

		verify(getInjectMock(MvAreaService.class)).findSingleByPath(AREA_PATH_MUNICIPALITY);
		ServletOutputStream out = getServletContainer().getResponseMock().getOutputStream();
		verify(out).write(result.getBytes());
	}

	private String stub_generateElectionCard(String result) {
		when(getInjectMock(SpecialPurposeReportService.class).generateElectionCard(
				eq(getUserDataMock()), anyLong(), any(MvArea.class), any(MvElection.class))).thenReturn(result.getBytes());
		return result;
	}

}

