package no.valg.eva.admin.common.configuration.model.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class ParentPollingDistrictsTest extends MockUtilsTestCase {

	@Test
	public void isValid_withEmptyParents_returnsFalse() throws Exception {
		ParentPollingDistricts districts = new ParentPollingDistricts();

		assertThat(districts.isValid()).isFalse();
	}

	@Test
	public void isValid_withInvalidParent_returnsFalse() throws Exception {
		ParentPollingDistricts districts = new ParentPollingDistricts();
		ParentPollingDistrict parent = createMock(ParentPollingDistrict.class);
		when(parent.isValid()).thenReturn(false);
		districts.getParentPollingDistricts().add(parent);

		assertThat(districts.isValid()).isFalse();
	}

	@Test
	public void isValid_withValidParents_returnsTrue() throws Exception {
		ParentPollingDistricts districts = new ParentPollingDistricts();
		ParentPollingDistrict parent = new ParentPollingDistrict(AreaPath.from("111111.22.33.4444"));
		parent.setId("id");
		parent.setName("name");
		RegularPollingDistrict child = new RegularPollingDistrict(AreaPath.from("111111.22.33.4444"), PollingDistrictType.REGULAR);
		child.setId("id");
		child.setName("name");
		parent.addChild(child);
		districts.getParentPollingDistricts().add(parent);

		assertThat(districts.isValid()).isTrue();
	}
}
