package no.valg.eva.admin.configuration.application;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.PollingDistrictServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistricts;
import no.valg.eva.admin.common.configuration.model.local.RegularPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;



public class PollingDistrictApplicationServiceTest extends LocalConfigApplicationServiceTest {

	@Test(dataProvider = "findRegularPollingDistrictsByArea")
	public void findRegularPollingDistrictsByArea_withArea_verifyResult(PollingDistrictType type) throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withOrdinaryPollingDistricts(type, "0003", "0001", "0002").getValue());

		List<no.valg.eva.admin.common.configuration.model.local.PollingDistrict> result = service.findRegularPollingDistrictsByArea(userData(), MUNICIPALITY,
				false);

		assertThat(result).hasSize(3);
		assertThat(result.get(0).getId()).isEqualTo("0001");
		assertThat(result.get(1).getId()).isEqualTo("0002");
		assertThat(result.get(2).getId()).isEqualTo("0003");
	}

	@DataProvider(name = "findRegularPollingDistrictsByArea")
	public Object[][] findRegularPollingDistrictsByArea() {
		return new Object[][] {
				{ PollingDistrictType.REGULAR },
				{ PollingDistrictType.PARENT }
		};
	}

	@Test
	public void saveTechnicalPollingDistrict_withNew_verifyCreate() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withBoroughs("030100", "030122").getValue());
		TechnicalPollingDistrict district = new TechnicalPollingDistrict(MUNICIPALITY);
		district.setId("0001");
		district.setName("My district");

		TechnicalPollingDistrict result = service.saveTechnicalPollingDistrict(userData(), district);

		assertThat(result).isNotNull();
		ArgumentCaptor<PollingDistrict> captor = ArgumentCaptor.forClass(PollingDistrict.class);
		verify(getInjectMock(PollingDistrictRepository.class)).create(any(UserData.class), captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo("0001");
		assertThat(captor.getValue().getName()).isEqualTo("My district");
	}

	@Test
	public void saveTechnicalPollingDistrict_withExisting_verifyUpdate() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		PollingDistrict dbPollingDistrict = pollingDistrictEntity("0001");
		stub_pollingDistrictRepository_findByPk(dbPollingDistrict);
		TechnicalPollingDistrict district = new TechnicalPollingDistrict(AreaPath.from(MUNICIPALITY_BOROUGH.path() + ".1000"));
		district.setPk(10L);
		district.setId("0001");
		district.setName("My district");

		TechnicalPollingDistrict result = service.saveTechnicalPollingDistrict(userData(), district);

		assertThat(result).isNotNull();
		verify(dbPollingDistrict).setId("0001");
		verify(dbPollingDistrict).setName("My district");
	}

	@Test
	public void deleteTechnicalPollingDistrict_withValid_verifyDelete() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		TechnicalPollingDistrict district = new TechnicalPollingDistrict(MUNICIPALITY);
		district.setPk(10L);

		service.deleteTechnicalPollingDistrict(userData(), district);

		verify(getInjectMock(PollingDistrictRepository.class)).findByPk(10L);
		verify(getInjectMock(PollingDistrictRepository.class)).delete(any(UserData.class), anyLong());
	}

	@Test
	public void findTechnicalPollingDistrictsByArea_withDistricts_verifyResult() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withTechnicalPollingDistricts("0003", "0001", "0002").getValue());

		List<TechnicalPollingDistrict> result = service.findTechnicalPollingDistrictsByArea(userData(), MUNICIPALITY);

		assertThat(result).hasSize(3);
		assertThat(result.get(0).getId()).isEqualTo("0001");
		assertThat(result.get(1).getId()).isEqualTo("0002");
		assertThat(result.get(2).getId()).isEqualTo("0003");
	}

	@Test
	public void findTechnicalPollingDistrictByAreaAndId_withDistricts_verifyResult() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withTechnicalPollingDistricts("0003", "0001", "0002").getValue());

		TechnicalPollingDistrict result = service.findTechnicalPollingDistrictByAreaAndId(userData(), MUNICIPALITY,
				"0001");

		assertThat(result).isNotNull();
	}

	@Test
	public void findParentPollingDistrictsByArea_withBothParentsAndRegular_verifyResult() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		List<PollingDistrict> pollingDistricts = new ArrayList<>();
		pollingDistricts.addAll(new PollingDistrictsBuilder("1001", "1000").withType(PollingDistrictType.PARENT).getValues());
		pollingDistricts.addAll(new PollingDistrictsBuilder("0002", "0001").withType(PollingDistrictType.REGULAR).getValues());
		PollingDistrict[] pollingDistrictsArray = pollingDistricts.toArray(new PollingDistrict[pollingDistricts.size()]);
		stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withPollingDistricts(pollingDistrictsArray).getValue());

		ParentPollingDistricts result = service.findParentPollingDistrictsByArea(userData(), MUNICIPALITY);

		assertThat(result.getParentPollingDistricts()).hasSize(2);
		assertThat(result.getParentPollingDistricts().get(0).getId()).isEqualTo("1000");
		assertThat(result.getParentPollingDistricts().get(1).getId()).isEqualTo("1001");
		assertThat(result.getSelectableDistricts()).hasSize(2);
		assertThat(result.getSelectableDistricts().get(0).getId()).isEqualTo("0001");
		assertThat(result.getSelectableDistricts().get(1).getId()).isEqualTo("0002");
	}

	@Test(expectedExceptions = EvoteException.class)
	public void saveParentPollingDistrict_withExisting_throwsException() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		ParentPollingDistrict district = new ParentPollingDistrict(MUNICIPALITY);
		district.setPk(100L);

		service.saveParentPollingDistrict(userData(), district);
	}

	@Test
	public void saveParentPollingDistrict_withNew_verifyCreate() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		stub_municipalityByElectionEventAndId(municipality(AreaPath.OSLO_MUNICIPALITY_ID).withBoroughs("030100", "030122").getValue());
		ParentPollingDistrict district = new ParentPollingDistrict(MUNICIPALITY);
		district.getChildren().add(createMock(RegularPollingDistrict.class));
		PollingDistrict dbChild = getInjectMock(PollingDistrictRepository.class).findByPk(anyLong());
		
		ParentPollingDistrict result = service.saveParentPollingDistrict(userData(), district);

		assertThat(result).isNotNull();
		verify(getInjectMock(PollingDistrictServiceBean.class)).createParentPollingDistrict(any(UserData.class), any(PollingDistrict.class), anyList());
		verify(dbChild).checkVersion(district.getChildren().get(0));
	}

	@Test
	public void deleteParentPollingDistrict_verifyDelete() throws Exception {
		PollingDistrictApplicationService service = initializeMocks(PollingDistrictApplicationService.class);
		ParentPollingDistrict district = new ParentPollingDistrict(MUNICIPALITY);
		district.setPk(100L);

		service.deleteParentPollingDistrict(userData(), district);

		verify(getInjectMock(PollingDistrictRepository.class)).deleteParentPollingDistrict(any(UserData.class), any(PollingDistrict.class));
	}
}

