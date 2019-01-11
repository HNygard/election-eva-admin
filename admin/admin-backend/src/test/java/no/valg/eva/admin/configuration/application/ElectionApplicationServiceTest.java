package no.valg.eva.admin.configuration.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.service.configuration.ElectionBaseTest;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })
public class ElectionApplicationServiceTest extends ElectionBaseTest {

	@BeforeMethod
	public void setUp() {
		super.init();
	}

	@Test
	public void getElectionGroups_withUserData_returnsTwoGroups() throws Exception {
		List<ElectionGroup> result = getElectionGroupService().getElectionGroups(rbacTestFixture.getUserData());

		assertThat(result).hasSize(2);
	}

	@Test
	public void get_withPath_returnsGroup() throws Exception {
		ElectionGroup result = getElectionGroupService().get(rbacTestFixture.getUserData(), getElectionGroup().electionPath());

		assertThat(result).isNotNull();
	}

	@Test
	public void save_withNew_createsNew() throws Exception {
		ElectionGroup electionGroup = electionGroup("99");

		SaveElectionResponse response = getElectionGroupService().save(rbacTestFixture.getUserData(), electionGroup);

		electionGroup = (ElectionGroup) response.getVersionedObject();
		assertThat(response.idNotUniqueError()).isFalse();
		assertThat(electionGroup.getElectionGroupRef()).isNotNull();
	}

	@Test
	public void save_withExisting_updatesGroup() throws Exception {
		ElectionGroup electionGroup = getElectionGroupService().get(rbacTestFixture.getUserData(), getElectionGroup().electionPath());
		electionGroup.setName("Nytt navn");

		SaveElectionResponse response = getElectionGroupService().save(rbacTestFixture.getUserData(), electionGroup);

		assertThat(response.idNotUniqueError()).isFalse();
		no.valg.eva.admin.configuration.domain.model.ElectionGroup updated = getElectionGroupRepository().findByPk(electionGroup.getElectionGroupRef().getPk());
		assertThat(updated.getName()).isEqualTo("Nytt navn");
	}

	@Test
	public void delete_withExisting_deletesGroup() throws Exception {
		ElectionGroup electionGroup = electionGroup("99");
		SaveElectionResponse response = getElectionGroupService().save(rbacTestFixture.getUserData(), electionGroup);
		ElectionGroup saved = (ElectionGroup) response.getVersionedObject();

		getElectionGroupService().delete(rbacTestFixture.getUserData(), saved.getElectionGroupPath());

		assertThat(getElectionGroupRepository().findByPk(saved.getElectionGroupRef().getPk())).isNull();
	}

	private ElectionGroup electionGroup(String id) {
		ElectionGroup result = new ElectionGroup(getElectionEvent().electionPath());
		result.setId(id);
		result.setName("Name " + id);
		return result;
	}

}
