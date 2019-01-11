package no.valg.eva.admin.frontend.electoralroll.ctrls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VoterAuditControllerTest extends BaseFrontendTest {

	private VoterAuditController controller;

	@BeforeMethod
	public void setUp() throws Exception {
		controller = initializeMocks(VoterAuditController.class);
	}

	@Test
	public void testGetPageTitleMeta_returns_EmptyList_when_selectedMvArea_is_null() throws Exception {
		assertThat(controller.getPageTitleMeta()).isEmpty();
	}

	@Test
	public void testGetPageTitleMeta_is_called_with_correct_MvElection() throws Exception {
		MvArea area = mockField("kommune", MvArea.class);

		controller.getPageTitleMeta();

		verify(getInjectMock(PageTitleMetaBuilder.class)).area(area);
	}
}
