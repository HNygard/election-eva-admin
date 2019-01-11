package no.valg.eva.admin.common.configuration.model.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.central.ContestListProposalData;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;


public class ListProposalConfigTest extends MockUtilsTestCase {

	@Test
	public void isValid_withValidData_returnsTrue() throws Exception {
		ListProposalConfig config = getListProposalConfig("33");
		setValues(config);

		assertThat(config.isValid()).isTrue();
	}

	@Test
	public void isValid_withInvalidChildren_returnsFalse() throws Exception {
		ListProposalConfig config = getListProposalConfig("33");
		setValues(config);
		config.getChildren().add(getListProposalConfig("34"));

		assertThat(config.isValid()).isFalse();
	}

	@Test
	public void areaComparator_withDifferentAreaPaths_returnsSortedByPath() throws Exception {
		List<ListProposalConfig> list = Arrays.asList(
				getListProposalConfig("33"),
				getListProposalConfig("31"));

		List<ListProposalConfig> sorted = list.stream().sorted(ListProposalConfig.areaComparator()).collect(Collectors.toList());

		assertThat(sorted.get(0).getAreaPath().path()).isEqualTo("111111.22.31");
		assertThat(sorted.get(1).getAreaPath().path()).isEqualTo("111111.22.33");
	}

	private void setValues(ListProposalConfig config) {
		config.getContestListProposalData().setNumberOfPositions(10);
		config.getContestListProposalData().setMinCandidates(1);
		config.getContestListProposalData().setMaxCandidates(5);
		config.getContestListProposalData().setMinProposersNewParty(2);
		config.getContestListProposalData().setMinProposersOldParty(2);
		config.getContestListProposalData().setMaxWriteIn(2);
		config.getContestListProposalData().setMaxRenumber(2);
	}

	private ListProposalConfig getListProposalConfig(String id) {
		ContestListProposalData data = new ContestListProposalData(new Election(ElectionPath.from("111111.22.33")));
		return new ListProposalConfig(AreaPath.from("111111.22." + id), 1L, "Name", true, true, data, 0);
	}
}

