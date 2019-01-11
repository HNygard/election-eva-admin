package no.valg.eva.admin.common.rbac;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;

public class UserMenuMetadataTest extends MockUtilsTestCase {

	@Test(dataProvider = "hasMinimumMunicipalityAndElectionGroup")
	public void hasMinimumMunicipalityAndElectionGroup_withDataProvider_verifyExpected(ElectionPath electionPath, AreaPath areaPath, boolean expected) {
		UserMenuMetadata metadata = UserMenuMetadata.builder()
				.electionPath(electionPath)
				.areaPath(areaPath)
				.hasElectionsWithTypeProportionalRepresentation(false)
				.validVoteCountCategories(new ArrayList<>())
				.electronicMarkOffsConfigured(false)
				.accessToBoroughs(true)
				.scanningEnabled(true)
				.build();

		assertThat(metadata.hasMinimumMunicipalityAndElectionGroup()).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] hasMinimumMunicipalityAndElectionGroup() {
		return new Object[][] {
				{ ELECTION_PATH_ELECTION_EVENT, AREA_PATH_ROOT, true },
				{ ELECTION_PATH_ELECTION_EVENT, AREA_PATH_MUNICIPALITY, true },
				{ ELECTION_PATH_ELECTION_EVENT, AREA_PATH_POLLING_STATION, false },
				{ ELECTION_PATH_ELECTION_GROUP, AREA_PATH_ROOT, true },
				{ ELECTION_PATH_ELECTION_GROUP, AREA_PATH_MUNICIPALITY, true },
				{ ELECTION_PATH_ELECTION_GROUP, AREA_PATH_POLLING_STATION, false },
				{ ELECTION_PATH_CONTEST, AREA_PATH_ROOT, false },
				{ ELECTION_PATH_CONTEST, AREA_PATH_MUNICIPALITY, false },
				{ ELECTION_PATH_CONTEST, AREA_PATH_POLLING_STATION, false }
		};
	}

	@Test
	public void isValidCountCategoryId_withVoCategory_returnsTrueForVo() {
		UserMenuMetadata metadata = UserMenuMetadata.builder()
				.hasElectionsWithTypeProportionalRepresentation(false)
				.validVoteCountCategories(Collections.singletonList(VO))
				.electronicMarkOffsConfigured(false)
				.accessToBoroughs(true)
				.scanningEnabled(true)
				.build();

		assertThat(metadata.isValidCountCategoryId(VO)).isTrue();
	}

}
