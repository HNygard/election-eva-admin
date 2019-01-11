package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.RoleItem;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class ElectionOptionsTest extends BaseFrontendTest {

	@Test
	public void init_withNonElectionLevelRole_verifyState() throws Exception {
		ElectionOptions opts = opts(null);

		assertThat(opts.isReady()).isTrue();
		assertThat(opts.isRender()).isFalse();
		assertThat(opts.isRenderSelect(CONTEST)).isFalse();
	}

	@Test
	public void init_withElectionLevelRole_verifyState() throws Exception {
		ElectionOptions opts = opts(CONTEST);

		assertThat(opts.isReady()).isFalse();
		assertThat(opts.isRender()).isTrue();
		assertThat(opts.getElectionGroups()).hasSize(1);
		assertThat(opts.getSelectedElectionGroup()).isNotNull();
		assertThat(opts.getElections()).hasSize(1);
		assertThat(opts.getSelectedElection()).isNotNull();
		assertThat(opts.getContests()).hasSize(2);
		assertThat(opts.getSelectedContest()).isNull();
		assertThat(opts.isRenderSelect(ELECTION_GROUP)).isFalse();
		assertThat(opts.isRenderSelect(ELECTION)).isFalse();
		assertThat(opts.isRenderSelect(CONTEST)).isTrue();
	}

	@Test(dataProvider = "getElectionPath")
	public void getElectionPath(ElectionLevelEnum level, String expected) throws Exception {
		ElectionOptions opts = opts(level);
		if (level == CONTEST) {
			opts.setSelectedContest(opts.getContests().get(0));
		}

		assertThat(opts.getElectionPath()).isEqualTo(ElectionPath.from(expected));
	}

	@DataProvider
	public Object[][] getElectionPath() {
		return new Object[][] {
				{ ELECTION_GROUP, "100100.01" },
				{ ELECTION, "100100.01.01" },
				{ CONTEST, "100100.01.01.000001" }
		};
	}

	@Test
	public void getAsObject_withPath_returnsMvElection() throws Exception {
		ElectionOptions opts = opts(CONTEST);

		assertThat(opts.getAsObject(null, null, "100100.01")).isSameAs(opts.getElectionGroups().get(0));
		assertThat(opts.getAsObject(null, null, "100100.01.01")).isSameAs(opts.getElections().get(0));
		assertThat(opts.getAsObject(null, null, "100100.01.01.000002")).isSameAs(opts.getContests().get(1));
	}

	@Test
	public void getAsString_withMvElection_returnsPath() throws Exception {
		ElectionOptions opts = opts(CONTEST);

		assertThat(opts.getAsString(null, null, opts.getElectionGroups().get(0))).isEqualTo("100100.01");
		assertThat(opts.getAsString(null, null, opts.getElections().get(0))).isEqualTo("100100.01.01");
		assertThat(opts.getAsString(null, null, opts.getContests().get(1))).isEqualTo("100100.01.01.000002");
	}

	private ElectionOptions opts(ElectionLevelEnum electionLevel) throws Exception {
		ElectionOptions opts = initializeMocks(ElectionOptions.class);
		RoleItem roleItem = createMock(RoleItem.class);
		when(roleItem.getElectionLevel()).thenReturn(electionLevel);
		stub_findByPathAndLevel(ELECTION_GROUP, 1);
		stub_findByPathAndLevel(ELECTION, 1);
		stub_findByPathAndLevel(CONTEST, 2);

		opts.init(roleItem);

		return opts;
	}

	private void stub_findByPathAndLevel(ElectionLevelEnum level, int size) {
		List<MvElectionMinimal> list = new ArrayList<>();
		String base = "100100";
		if (ELECTION_GROUP.isLowerThan(level)) {
			base += ".01";
		}
		if (ELECTION.isLowerThan(level)) {
			base += ".01";
		}
		for (int i = 0; i < size; i++) {
			MvElectionMinimal mvElection = createMock(MvElectionMinimal.class);
			int pad = level == CONTEST ? 6 : 2;
			String path = base + "." + StringUtils.leftPad(String.valueOf(i + 1), pad, '0');
			when(mvElection.toElectionPath()).thenReturn(ElectionPath.from(path));
			when(mvElection.getPath()).thenReturn(path);
			when(mvElection.getName()).thenReturn(i + "name");
			list.add(mvElection);
		}
		when(getInjectMock(MvElectionService.class).findByPathAndLevel(eq(getUserDataMock()), any(ElectionPath.class), eq(level))).thenReturn(list);
	}

}

