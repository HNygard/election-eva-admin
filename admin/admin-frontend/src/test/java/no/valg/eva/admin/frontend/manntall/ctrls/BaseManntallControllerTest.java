package no.valg.eva.admin.frontend.manntall.ctrls;

import no.evote.service.configuration.MvAreaService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.Test;

import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.APPROVED_CONFIGURATION;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.NIVAER;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.GEOGRAFI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BaseManntallControllerTest extends BaseFrontendTest {

	@Test
	public void getKontekstVelgerOppsett_verifiserPickerSetup() throws Exception {
		BaseManntallController ctrl = ctrl();

		KontekstvelgerOppsett setup = ctrl.getKontekstVelgerOppsett();

		assertThat(setup.getElementer()).hasSize(1);
		assertThat(setup.getElementer().get(0).getType()).isSameAs(GEOGRAFI);
		assertThat(setup.getElementer().get(0).get(NIVAER)).isEqualTo(String.valueOf(MUNICIPALITY.getLevel()));
	}

	@Test
	public void initialized_medContextPickerData_verifiserInit() throws Exception {
		BaseManntallController ctrl = ctrl();
		Kontekst data = new Kontekst();
		data.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_MUNICIPALITY));

		ctrl.initialized(data);

		assertThat(ctrl.getKommune()).isNotNull();
	}

	@Test
	public void isEditerManntallTilgjengelig_medDemoModus_returnererTrue() throws Exception {
		BaseManntallController ctrl = ctrl();
		when(getInjectMock(UserDataController.class).getElectionEvent().isDemoElection()).thenReturn(true);

		assertThat(ctrl.isEditerManntallTilgjengelig()).isTrue();
	}

	@Test
	public void isEditerManntallTilgjengelig_medRiktigStatus_returnererTrue() throws Exception {
		BaseManntallController ctrl = ctrl();
		when(getInjectMock(UserDataController.class).getElectionEvent().isDemoElection()).thenReturn(false);
		when(getInjectMock(UserDataController.class).getElectionEvent().getElectionEventStatus().getId()).thenReturn(APPROVED_CONFIGURATION.id());

		assertThat(ctrl.isEditerManntallTilgjengelig()).isTrue();
	}

	@Test
	public void byggNavnelinje_medVelger_sjekkNavnelinje() throws Exception {
		BaseManntallController ctrl = ctrl();
		Voter velger = new Voter();
		velger.setFirstName("Test");
		velger.setMiddleName("Middle");
		velger.setLastName("Testesen");

		assertThat(ctrl.byggNavnelinje(velger)).isEqualTo("Testesen Test Middle");
	}

	@Test
	public void endreValgdistrikt_medVelgerOgDistrikt_oppdatererValgdistrikt() throws Exception {
		BaseManntallController ctrl = ctrl();
		Voter velger = new Voter();
		MvArea stemmekrets = new MvAreaBuilder(AREA_PATH_POLLING_DISTRICT).getValue();
		when(getInjectMock(MvAreaService.class).findSingleByPath(any(ValggeografiSti.class))).thenReturn(stemmekrets);

		ctrl.endreStemmekrets(velger, ValggeografiSti.stemmekretsSti(AREA_PATH_POLLING_DISTRICT));

		assertThat(velger.getPollingDistrictId()).isEqualTo(AREA_PATH_POLLING_DISTRICT.getPollingDistrictId());
		assertThat(velger.getCountryId()).isEqualTo(AREA_PATH_POLLING_DISTRICT.getCountryId());
		assertThat(velger.getCountyId()).isEqualTo(AREA_PATH_POLLING_DISTRICT.getCountyId());
		assertThat(velger.getMunicipalityId()).isEqualTo(AREA_PATH_POLLING_DISTRICT.getMunicipalityId());
		assertThat(velger.getBoroughId()).isEqualTo(AREA_PATH_POLLING_DISTRICT.getBoroughId());
		assertThat(velger.getMvArea()).isNotNull();
	}

	@Test
	public void setMailingAddressSpecified_medEttGyldigFelt_setterMailingAddressSpecified() throws Exception {
		BaseManntallController ctrl = ctrl();
		Voter velger = new Voter();
		velger.setMailingAddressLine1("test");

		ctrl.setMailingAddressSpecified(velger);

		assertThat(velger.isMailingAddressSpecified()).isTrue();

	}

	private BaseManntallController ctrl() throws Exception {
		return initializeMocks(new BaseManntallController() {
		});
	}

}
