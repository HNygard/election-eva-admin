package no.evote.service.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.assertEquals;

import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.application.ElectionGroupMapper;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })
public class ElectionGroupServiceBeanTest extends ElectionBaseTest {

	@BeforeMethod
	public void setUp() {
		super.init();
	}

	@Test
	public void get_withExisting_returnsGroup() {
		ElectionGroup electionGroup = getElectionGroupServiceBean().get(getElectionGroup().electionPath());

		assertThat(electionGroup).isNotNull();
	}

	@Test
	public void testCreateElectionGroup() {
		assertEquals(getElectionGroup(), getElectionGroupRepository().findByPk(getElectionGroup().getPk()));
	}

	@Test
	public void create_withDuplicateId_returnsDuplicateReponse() throws Exception {
		SaveElectionResponse response = getElectionGroupServiceBean().create(rbacTestFixture.getUserData(),
				ElectionGroupMapper.toElectionGroup(getElectionGroup()));

		assertThat(response.idNotUniqueError()).isTrue();
	}

	@Test
	public void create_withValidId_createsNew() throws Exception {
		SaveElectionResponse response = getElectionGroupServiceBean().create(rbacTestFixture.getUserData(), createElectionGroup("99"));

		assertThat(response.idNotUniqueError()).isFalse();
		assertThat(response.getVersionedObject()).isNotNull();
	}

	@Test
	public void update_withEditedDuplicateId_returnsDuplicateReponse() throws Exception {
		SaveElectionResponse response = getElectionGroupServiceBean().create(rbacTestFixture.getUserData(), createElectionGroup("99"));
		ElectionGroup saved = getElectionGroup(response);
		saved.setId(getElectionGroup().getId());

		response = getElectionGroupServiceBean().update(rbacTestFixture.getUserData(), saved);

		assertThat(response.idNotUniqueError()).isTrue();
	}

	@Test
	public void update_withChangeName_updatesElectionGroup() {
		long pk = getElectionGroup().getPk();
		getElectionGroup().setName("Updated name");

		getElectionGroupServiceBean().update(rbacTestFixture.getUserData(), ElectionGroupMapper.toElectionGroup(getElectionGroup()));

		no.valg.eva.admin.configuration.domain.model.ElectionGroup electionGroupAfterUpdate = getElectionGroupRepository().findByPk(pk);
		Assert.assertNotNull(electionGroupAfterUpdate);
		Assert.assertEquals(electionGroupAfterUpdate.getName(), "Updated name");
	}

	@Test
	public void getElectionGroupsWithoutElections_withNonElectionGroups_returnsGroups() throws Exception {
		getElectionGroupServiceBean().create(rbacTestFixture.getUserData(), createElectionGroup("98"));
		getElectionGroupServiceBean().create(rbacTestFixture.getUserData(), createElectionGroup("99"));

		assertThat(getElectionGroupServiceBean().getElectionGroupsWithoutElections(getElectionEvent().getPk())).hasSize(2);
	}

	private ElectionGroup createElectionGroup(String id) {
		ElectionGroup electionGroup = new ElectionGroup(
				getElectionEvent().electionPath());
		electionGroup.setId(id);
		electionGroup.setName("My group");
		return electionGroup;
	}

	private ElectionGroup getElectionGroup(SaveElectionResponse response) {
		return (ElectionGroup) response.getVersionedObject();
	}
}
