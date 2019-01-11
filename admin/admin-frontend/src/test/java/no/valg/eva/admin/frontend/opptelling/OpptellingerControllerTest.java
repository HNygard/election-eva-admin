package no.valg.eva.admin.frontend.opptelling;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

import org.testng.annotations.Test;

public class OpptellingerControllerTest extends BaseFrontendTest {
	@Test
	public void initialized_gittKontekst_redirecterTilUrl() throws Exception {
		OpptellingerController controller = initializeMocks(opptellingerController());
		ContestInfoService contestInfoService = getInjectMock(ContestInfoService.class);
		when(contestInfoService
				.findContestPathByElectionAndArea(getUserDataMock(), ELECTION_PATH_111111_11_11, AREA_PATH_111111_11_11_1111_111111_1111))
				.thenReturn(ELECTION_PATH_111111_11_11_111111);
		Kontekst kontekst = new Kontekst();
		kontekst.setCountCategory(FO);
		kontekst.setValggeografiSti(STEMMEKRETS_STI);
		kontekst.setValghierarkiSti(VALG_STI);
		controller.initialized(kontekst);
		getServletContainer().verifyRedirect("url?countCategory=FO&contestPath=111111.11.11.111111&areaPath=111111.11.11.1111.111111.1111");
	}

	@Test
	public void getSideTittel_gittController_returnererDummy() throws Exception {
		assertThat(opptellingerController().getSideTittel()).isEqualTo("dummy");
	}

	private OpptellingerController opptellingerController() {
		return new OpptellingerController() {
			@Override
			protected String url() {
				return "url?countCategory=%s&contestPath=%s&areaPath=%s";
			}

			@Override
			public KontekstvelgerOppsett getKontekstVelgerOppsett() {
				return new KontekstvelgerOppsett();
			}
		};
	}
}
