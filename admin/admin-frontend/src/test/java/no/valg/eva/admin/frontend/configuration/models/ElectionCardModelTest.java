package no.valg.eva.admin.frontend.configuration.models;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ElectionCardModelTest extends BaseFrontendTest {

	@Test
    public void constructor_withDifferentInfoTexts_verifyState() {
		ElectionCardModel model = model();

		assertThat(model.getElectionCard()).isNotNull();
		assertThat(model.getPollingPlace()).isNotNull();
		assertThat(model.getReportingUnit()).isNotNull();
		assertThat(model.isCustomInfoText()).isTrue();
		assertThat(model.getMunicipalityId()).isEqualTo("4444");
		assertThat(model.getPollingDistrictId()).isEqualTo("1000");
	}

	@Test
    public void getElectionCardInfoTextMaxLength_returnsMaxLength() {
		ElectionCardModel model = new ElectionCardModel(model().getElectionCard());

		assertThat(model.getElectionCardInfoTextMaxLength()).isEqualTo(150);
	}

	@Test
    public void setElectionCardInfoText_withoutPlace_setsTextOnCard() {
		ElectionCardModel model = model(null);

		model.setElectionCardInfoText("Hello");

		assertThat(model.isInfoTextChanged()).isTrue();
		verify(model.getElectionCard()).setInfoText("Hello");
	}

	@Test
    public void setElectionCardInfoText_withPlace_setsTextOnPlace() {
		ElectionCardModel model = model();

		model.setElectionCardInfoText("Hello");

		assertThat(model.isInfoTextChanged()).isTrue();
		verify(model.getPollingPlace()).setInfoText("Hello");
	}

	@Test
    public void getLabel_withoutPlace_returnsReportingUnitLeafAreaPathId() {
		ElectionCardModel model = model(null);

		assertThat(model.getLabel()).isEqualTo("4444-Area name");
	}

	@Test
    public void getLabel_withPlace_returnsPlaceId() {
		ElectionCardModel model = model();

		assertThat(model.getLabel()).isEqualTo("1000-Parent name");
	}

	@Test
    public void getAddress_withoutPlace_returnsReportingUnitAddress() {
		ElectionCardModel model = model(null);

		assertThat(model.getAddress()).isEqualTo("Address");
	}

	@Test
    public void setAddress_withNoChange_doesNothing() {
		ElectionCardModel model = model(null);

		model.setAddress(model.getAddress());

		assertThat(model.isAddressChanged()).isFalse();
		verify(model.getReportingUnit(), never()).setAddress(anyString());
	}

	@Test
    public void setAddress_withChange_setsNewAddress() {
		ElectionCardModel model = model(null);

		model.setAddress("New");

		assertThat(model.isAddressChanged()).isTrue();
		verify(model.getReportingUnit()).setAddress("New");
	}

	@Test
    public void getPostalCode_withoutPlace_returnsReportingUnitPostalCode() {
		ElectionCardModel model = model(null);

		assertThat(model.getPostalCode()).isEqualTo("0123");
	}

	@Test
    public void setPostalCode_withNoChange_doesNothing() {
		ElectionCardModel model = model(null);

		model.setPostalCode(model.getPostalCode());

		assertThat(model.isAddressChanged()).isFalse();
		verify(model.getReportingUnit(), never()).setPostalCode(anyString());
	}

	@Test
    public void setPostalCode_withChange_setsNewPostalCode() {
		ElectionCardModel model = model(null);

		model.setPostalCode("New");

		assertThat(model.isAddressChanged()).isTrue();
		verify(model.getReportingUnit()).setPostalCode("New");
	}

	@Test
    public void getPostTown_withoutPlace_returnsReportingUnitPostTown() {
		ElectionCardModel model = model(null);

		assertThat(model.getPostTown()).isEqualTo("PostTown");
	}

	@Test
    public void setPostTown_withNoChange_doesNothing() {
		ElectionCardModel model = model(null);

		model.setPostTown(model.getPostTown());

		assertThat(model.isAddressChanged()).isFalse();
		verify(model.getReportingUnit(), never()).setPostTown(anyString());
	}

	@Test
    public void setPostTown_withChange_setsNewPostalCode() {
		ElectionCardModel model = model(null);

		model.setPostTown("New");

		assertThat(model.isAddressChanged()).isTrue();
		verify(model.getReportingUnit()).setPostTown("New");
	}

	private ElectionCardModel model() {
		return model(place());
	}

	private ElectionCardModel model(ElectionDayPollingPlace place) {
		ElectionCardConfig config = createMock(ElectionCardConfig.class);
		when(config.getInfoText()).thenReturn("Hello");
		when(config.getReportingUnit().getAreaName()).thenReturn("Area name");
		when(config.getReportingUnit().getAreaPath()).thenReturn(AreaPath.from("111111.22.33.4444"));
		when(config.getReportingUnit().getAddress()).thenReturn("Address");
		when(config.getReportingUnit().getPostalCode()).thenReturn("0123");
		when(config.getReportingUnit().getPostTown()).thenReturn("PostTown");
		return new ElectionCardModel(config, place);
	}

	private ElectionDayPollingPlace place() {
		ElectionDayPollingPlace result = createMock(ElectionDayPollingPlace.class);
		when(result.getInfoText()).thenReturn("Yo");
		when(result.getPath()).thenReturn(AreaPath.from("111111.22.33.4444.444400.1000"));
		when(result.getParentName()).thenReturn("Parent name");
		return result;
	}
}

