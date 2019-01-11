package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.faces.application.FacesMessage;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.election.LocaleId;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LanguageConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test(dataProvider = "init")
	public void init_withDataProvider_verifyExpected(AreaPath path) throws Exception {
		LanguageConfigurationController ctrl = ctrl(path);

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.LANGUAGE);
		assertThat(ctrl.getLocaleId()).isNotNull();
	}

	@DataProvider(name = "init")
	public Object[][] init() {
		return new Object[][] {
				{ COUNTY },
				{ MUNICIPALITY }
		};
	}

	@Test(dataProvider = "button")
	public void button_withDataProvider_verifyExpected(ButtonType buttonType, boolean isEditable, boolean isRendered, boolean isDisabled)
			throws Exception {
		LanguageConfigurationController ctrl = ctrl(MUNICIPALITY, isEditable);

		Button button = ctrl.button(buttonType);

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@DataProvider(name = "button")
	public Object[][] button() {
		return new Object[][] {
				{ ButtonType.DONE, false, true, true },
				{ ButtonType.DONE, true, true, false },
				{ ButtonType.APPROVE_TO_SETTLEMENT, true, false, true }
		};
	}

	@Test
	public void saveDone_withCounty_verifySave() throws Exception {
		LanguageConfigurationController ctrl = ctrl(COUNTY);

		ctrl.saveDone();

		verifySaveConfigStatus();
		verify(ctrl.getCountyConfigStatus(), times(2)).setLocaleId(ctrl.getCountyConfigStatus().getLocaleId());
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@config.local.language.updatedMessage, County name, @locale[nb-NO].name]");
	}

	@Test
	public void saveDone_withMunicipality_verifySave() throws Exception {
		LanguageConfigurationController ctrl = ctrl(MUNICIPALITY);

		ctrl.saveDone();

		verifySaveConfigStatus();
		verify(ctrl.getMunicipalityConfigStatus(), times(2)).setLocaleId(ctrl.getMunicipalityConfigStatus().getLocaleId());
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@config.local.language.updatedMessage, Municipality name, @locale[nn-NO].name]");
	}

	@Test
	public void getName_returnsCorrectName() throws Exception {
		LanguageConfigurationController ctrl = ctrl(COUNTY);

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.language.name");
	}

	@Test(dataProvider = "hasAccessProvider")
	public void hasAccess_withDataProvider_verifyExpected(AreaPath path, boolean expected) throws Exception {
		LanguageConfigurationController ctrl = ctrl(path);

		assertThat(ctrl.hasAccess()).isEqualTo(expected);
	}

	@DataProvider(name = "hasAccessProvider")
	public Object[][] hasAccessProvider() {
		return new Object[][] {
				{ COUNTY, true },
				{ MUNICIPALITY, true }
		};
	}

	@Test
	public void isDoneStatus_withCountyAndNotDone_returnsFalse() throws Exception {
		LanguageConfigurationController ctrl = ctrl(COUNTY);
		when(ctrl.getCountyConfigStatus().isLanguage()).thenReturn(false);

		assertThat(ctrl.isDoneStatus()).isFalse();
	}

	@Test
	public void isDoneStatus_withMunicipalityAndDone_returnsTrue() throws Exception {
		LanguageConfigurationController ctrl = ctrl(MUNICIPALITY);
		when(ctrl.getMunicipalityConfigStatus().isLanguage()).thenReturn(true);

		assertThat(ctrl.isDoneStatus()).isTrue();
	}

	@Test
	public void setDoneStatus_withCounty_setsCountyStatus() throws Exception {
		LanguageConfigurationController ctrl = ctrl(COUNTY);

		ctrl.setDoneStatus(true);

		verify(ctrl.getCountyConfigStatus()).setLanguage(true);
	}

	@Test
	public void setDoneStatus_withMunicipality_setsMunicipalityStatus() throws Exception {
		LanguageConfigurationController ctrl = ctrl(MUNICIPALITY);

		ctrl.setDoneStatus(true);

		verify(ctrl.getMunicipalityConfigStatus()).setLanguage(true);
	}

	@Test(dataProvider = "getHeaderHint")
	public void getHeaderHint_withDataSource_verifyExpected(AreaPath path, String expected) throws Exception {
		assertThat(ctrl(path).getHeaderHint()).isEqualTo(expected);
	}

	@DataProvider(name = "getHeaderHint")
	public Object[][] getHeaderHint() {
		return new Object[][] {
				{ COUNTY, "@config.local.language.header_choose_hint_county" },
				{ MUNICIPALITY, "@config.local.language.header_choose_hint_municipality" }
		};
	}

	@Test
	public void canBeSetToDone_returnsTrue() throws Exception {
		assertThat(ctrl(MUNICIPALITY).canBeSetToDone()).isTrue();
	}

	private LanguageConfigurationController ctrl(AreaPath path) throws Exception {
		return ctrl(path, true);
	}

	private LanguageConfigurationController ctrl(AreaPath path, boolean isEditable) throws Exception {
		LanguageConfigurationController ctrl = ctrl(initializeMocks(new LanguageConfigurationController() {
			@Override
			public boolean isEditable() {
				return isEditable;
			}
		}), path);
		if (path.isCountyLevel()) {
			when(ctrl.getCountyConfigStatus().getCountyName()).thenReturn("County name");
			when(ctrl.getCountyConfigStatus().getLocaleId()).thenReturn(new LocaleId("nb-NO"));
		} else if (path.isMunicipalityLevel()) {
			when(ctrl.getMunicipalityConfigStatus().getMunicipalityName()).thenReturn("Municipality name");
			when(ctrl.getMunicipalityConfigStatus().getLocaleId()).thenReturn(new LocaleId("nn-NO"));
		}
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataGodkjenne()).thenReturn(isEditable);
		ctrl.init();
		return ctrl;
	}
}
