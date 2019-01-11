package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.service.AntallStemmesedlerLagtTilSideService;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AntallStemmesedlerLagtTilSideControllerTest extends BaseFrontendTest {

	private AntallStemmesedlerLagtTilSideController controller;
	private UserData userData;
	private FacesContext facesContext;
	private AntallStemmesedlerLagtTilSideService service;
	private AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide;

	@BeforeMethod
	public void setUp() throws Exception {
		controller = initializeMocks(AntallStemmesedlerLagtTilSideController.class);
		userData = getUserDataMock();
		facesContext = getFacesContextMock();
		service = getInjectMock(AntallStemmesedlerLagtTilSideService.class);
		antallStemmesedlerLagtTilSide = createMock(AntallStemmesedlerLagtTilSide.class);
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_MUNICIPALITY);
		Kontekst data = new Kontekst();
		data.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));
		getServletContainer().setRequestParameter(KONTEKST.toString(), data.serialize());
	}

	@Test
	public void init_givenElectionEventAdminAndNullAreaPath_redirectToPicker() throws Exception {
		when(userData.isElectionEventAdminUser()).thenReturn(true);
		getServletContainer().setRequestParameter(KONTEKST.toString(), null);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_ROOT);

		controller.init();

		verify(facesContext.getExternalContext()).redirect("/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|3][side|uri|null]");
	}

	@Test
    public void init_givenElectionEventAdminAndAreaPath_callsService() {
		when(userData.isElectionEventAdminUser()).thenReturn(true);

		controller.init();

		verify(service).hentAntallStemmesedlerLagtTilSide(userData, ValggeografiSti.kommuneSti(AREA_PATH_MUNICIPALITY));
	}

	@Test
    public void init_givenOrdinaryUser_callsService() {
		controller.init();

		verify(service).hentAntallStemmesedlerLagtTilSide(eq(userData), any(KommuneSti.class));
	}

	@Test
    public void getTotaltAntallStemmesedler_givenNullAntallStemmesedlerLagtTilSide_returnsDefaultValue() {
		assertThat(controller.getTotaltAntallStemmesedler()).isZero();
	}

	@Test
    public void getTotaltAntallStemmesedler_givenAntallStemmesedlerLagtTilSide_returnsValue() {
		when(antallStemmesedlerLagtTilSide.getTotaltAntallStemmesedlerLagtTilSideForValg()).thenReturn(1);
		when(service.hentAntallStemmesedlerLagtTilSide(any(UserData.class), any(KommuneSti.class))).thenReturn(antallStemmesedlerLagtTilSide);

		controller.init();

		assertThat(controller.getTotaltAntallStemmesedler()).isEqualTo(1);
	}

	@Test
    public void isLagreAntallStemmesedlerDisabled_givenNullAntallStemmesedlerLagtTilSide_returnsTrue() {
		assertThat(controller.isLagreAntallStemmesedlerDisabled()).isTrue();
	}

	@Test
    public void isLagreAntallStemmesedlerDisabled_givenLagringIkkeMulig_returnsTrue() {
		when(antallStemmesedlerLagtTilSide.isLagringAvAntallStemmesedlerLagtTilSideMulig()).thenReturn(false);
		when(service.hentAntallStemmesedlerLagtTilSide(any(UserData.class), any(KommuneSti.class))).thenReturn(antallStemmesedlerLagtTilSide);

		controller.init();

		assertThat(controller.isLagreAntallStemmesedlerDisabled()).isTrue();
	}

	@Test
    public void isLagreAntallStemmesedlerDisabled_givenLagringMulig_returnsFalse() {
		when(antallStemmesedlerLagtTilSide.isLagringAvAntallStemmesedlerLagtTilSideMulig()).thenReturn(true);
		when(service.hentAntallStemmesedlerLagtTilSide(any(UserData.class), any(KommuneSti.class))).thenReturn(antallStemmesedlerLagtTilSide);

		controller.init();

		assertThat(controller.isLagreAntallStemmesedlerDisabled()).isFalse();
	}

	@Test
    public void lagreAntallStemmesedler_givenAntallStemmesedlerLagtTilSide_callsService() {
		when(service.hentAntallStemmesedlerLagtTilSide(any(UserData.class), any(KommuneSti.class))).thenReturn(antallStemmesedlerLagtTilSide);

		controller.init();
		controller.lagreAntallStemmesedler();

		verify(service).lagreAntallStemmesedlerLagtTilSide(userData, antallStemmesedlerLagtTilSide);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@opptelling.antallStemmesedlerLagtTilSide.erLagret");
	}

	@Test
    public void getAntallStemmesedlerModel_givenController_returnsModel() {
		assertThat(controller.getAntallStemmesedlerModel()).isNotNull();
	}

	@Test
    public void getAntallStemmesedlerLagtTilSide_givenNullAntallStemmesedlerLagtTilSide_returnsNull() {
		assertThat(controller.getAntallStemmesedlerLagtTilSide()).isNull();
	}

	@Test
    public void getAntallStemmesedlerLagtTilSide_givenAntallStemmesedlerLagtTilSide_returnsObject() {
		when(service.hentAntallStemmesedlerLagtTilSide(any(UserData.class), any(KommuneSti.class))).thenReturn(antallStemmesedlerLagtTilSide);

		controller.init();

		assertThat(controller.getAntallStemmesedlerLagtTilSide()).isEqualTo(antallStemmesedlerLagtTilSide);
	}

	@Test
    public void getHeaderFooterStyle_givenNullAntallStemmesedlerLagtTilSide_returnsEmptyString() {
		assertThat(controller.getHeaderFooterStyle()).isEmpty();
	}

	@Test
    public void getHeaderFooterStyle_givenAntallStemmesedlerLagtTilSideWithMoreThanOneEntry_returnsEmptyString() {
		when(antallStemmesedlerLagtTilSide.getAntallStemmesedlerLagtTilSideForValgList().size()).thenReturn(2);
		when(service.hentAntallStemmesedlerLagtTilSide(any(UserData.class), any(KommuneSti.class))).thenReturn(antallStemmesedlerLagtTilSide);

		controller.init();

		assertThat(controller.getHeaderFooterStyle()).isEmpty();
	}

	@Test
    public void getHeaderFooterStyle_givenAntallStemmesedlerLagtTilSideWithOneEntry_returnsStyleString() {
		when(antallStemmesedlerLagtTilSide.getAntallStemmesedlerLagtTilSideForValgList().size()).thenReturn(1);
		when(service.hentAntallStemmesedlerLagtTilSide(any(UserData.class), any(KommuneSti.class))).thenReturn(antallStemmesedlerLagtTilSide);

		controller.init();

		assertThat(controller.getHeaderFooterStyle()).isEqualTo("hide-table-header hide-table-footer");
	}

}
