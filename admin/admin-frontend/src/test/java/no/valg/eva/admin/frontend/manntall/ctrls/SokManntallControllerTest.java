package no.valg.eva.admin.frontend.manntall.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.service.configuration.MvAreaService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.frontend.common.ctrls.RedirectInfo.REDIRECT_INFO_SESSION_KEY;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.AVANSERT;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.FODSELSNUMMER;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.LOPENUMMER;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.MANNTALLSNUMMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SokManntallControllerTest extends BaseFrontendTest {

	@Test
	public void initialized_medValgOgOmrade_verifiserInit() throws Exception {
		SokManntallController ctrl = initializedSetUp();

		ctrl.initialized(contextPickerData());

		assertThat(ctrl.getKommune()).isNotNull();
		assertThat(ctrl.getKommuneListe()).hasSize(1);
		verify(getInjectMock(ManntallsSokWidget.class)).addListener(ctrl);
	}

	@Test
	public void initialized_medRedirectInfo_verifiserInit() throws Exception {
		SokManntallController ctrl = initializedSetUp();
		Voter velger = createMock(Voter.class);
		RedirectInfo redirectInfo = new RedirectInfo(velger, "/my/url", "title");
		when(getServletContainer().getRequestMock().getSession().getAttribute(REDIRECT_INFO_SESSION_KEY)).thenReturn(redirectInfo);

		ctrl.initialized(contextPickerData());

		assertThat(ctrl.getVelger()).isNotNull();
	}

	private SokManntallController initializedSetUp() throws Exception {
		SokManntallController ctrl = initializeMocks(SokManntallController.class);
		stub_kommuner();
		setupUserWith(MUNICIPALITY, true);
		when(getServletContainer().getHttpSessionMock().getAttribute(REDIRECT_INFO_SESSION_KEY)).thenReturn(null);
		MvArea kommune = new MvAreaBuilder(AREA_PATH_MUNICIPALITY).getValue();
		when(getInjectMock(MvAreaService.class).findSingleByPath(any(ValggeografiSti.class))).thenReturn(kommune);
		return ctrl;
	}

	@Test
	public void manntallsSokVelger_medVelgerUtenOmrade_returnererFeilmelding() throws Exception {
		SokManntallController ctrl = initializeMocks(SokManntallController.class);
		Voter velger = createMock(Voter.class);
		when(velger.getMvArea()).thenReturn(null);
		when(velger.getMunicipalityId()).thenReturn(AREA_PATH_POLLING_DISTRICT.getMunicipalityId());
		when(velger.getPollingDistrictId()).thenReturn(AREA_PATH_POLLING_DISTRICT.getPollingDistrictId());

		ctrl.manntallsSokVelger(velger);

		assertFacesMessage(SEVERITY_WARN, "[@electoralRoll.validation.noArea, 4444, 6666]");
	}

	@Test
	public void manntallsSokVelger_medVelgerAnnenKommune_returnererFeilmelding() throws Exception {
		SokManntallController ctrl = initializeMocks(SokManntallController.class);
		Voter velger = createMock(Voter.class);
		when(velger.isApproved()).thenReturn(true);
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_POLLING_DISTRICT).getValue();
		when(velger.getMvArea()).thenReturn(mvArea);
		AreaPath annenKommunePath = AreaPath.from("111111.22.33.1111.999999.8888");
		MvArea annenKommune = new MvAreaBuilder(annenKommunePath).getValue();
		mockFieldValue("kommune", annenKommune);

		ctrl.manntallsSokVelger(velger);

		assertFacesMessage(SEVERITY_WARN, "[@electoralRoll.validation.wrongMunicipality, Municipality 4444]");
	}

	@Test(dataProvider = "manntallsTomtResultatMelding")
	public void manntallsTomtResultatMelding_medDataProvider_verifiserForventet(ManntallsSokType type, String forventet) throws Exception {
		SokManntallController ctrl = initializeMocks(SokManntallController.class);
		when(getInjectMock(ManntallsSokWidget.class).getFodselsnummer()).thenReturn("12345678909");
		when(getInjectMock(ManntallsSokWidget.class).getManntallsnummer()).thenReturn("123456789098");

		assertThat(ctrl.manntallsTomtResultatMelding(type)).isEqualTo(forventet);
	}

	@DataProvider
	public Object[][] manntallsTomtResultatMelding() {
		return new Object[][] {
				{ LOPENUMMER, "@electoralRoll.personNotInElectoralRoll " },
				{ MANNTALLSNUMMER, "[@electoralRoll.numberNotInElectoralRoll, 123456789098]" },
				{ FODSELSNUMMER, "[@electoralRoll.ssnNotInElectoralRoll, 12345678909]" },
				{ AVANSERT, "@electoralRoll.personNotInElectoralRoll " }
		};
	}

	private void stub_kommuner() {
        List<Kommune> result = new ArrayList<>(singletonList(createMock(Kommune.class)));
		when(getInjectMock(ValggeografiService.class).kommunerForValghendelse(eq(getUserDataMock()))).thenReturn(result);
	}

	private Kontekst contextPickerData() {
		Kontekst data = new Kontekst();
		data.setValghierarkiSti(ValghierarkiSti.fra(ELECTION_PATH_ELECTION_GROUP));
		data.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));
		return data;
	}

	private void setupUserWith(AreaLevelEnum level, boolean isManntallSøkKommune) {
		when(getUserDataMock().getOperatorMvArea().getActualAreaLevel()).thenReturn(level);
		when(getInjectMock(UserDataController.class).getUserAccess().isManntallSøkKommune()).thenReturn(isManntallSøkKommune);
	}

}
