package no.valg.eva.admin.rbac.repository;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import no.evote.service.backendmock.RepositoryBackedRBACTestFixture;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class AccessRepositoryTest extends AbstractJpaTestBase {
	private AccessRepository accessRepository;
	private GenericTestRepository genericTestRepository;
	private RepositoryBackedRBACTestFixture rbacTestFixture;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		accessRepository = new AccessRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());

		rbacTestFixture = new RepositoryBackedRBACTestFixture(getEntityManager());
		rbacTestFixture.init();
	}

	@Test
	public void findAccessByPk() {
		Access a1 = accessRepository.findAllAccesses().get(0);
		Access a2 = accessRepository.findAccessByPk(a1.getPk());
		Assert.assertEquals(a1, a2);
	}

	@Test
	public void getIncludedAccesses() {
		Assert.assertTrue(accessRepository.getIncludedAccesses(rbacTestFixture.getRoleVotingCountElectionDayAll()).size() == 2);
	}

	@Test
	public void findByPath() {
		Assert.assertTrue(accessRepository.findAccessByPath(Konfigurasjon_Grunnlagsdata.paths()[0]).getName().equals("@access.konfig.grunnlagsdata"));
	}

	@Test
	public void findByPathNotExists() {
		Assert.assertEquals(accessRepository.findAccessByPath("ThisAccessDoesNotEXIST"), null);
	}

	@Test
	public void findAll() {
		List<Access> allAccesses = accessRepository.findAllAccesses();
		Assert.assertEquals(allAccesses.size(), new HashSet<Access>(allAccesses).size());
	}

	@Test
	public void getIncludedAccessesNoDisabledRolesIncludesIncludedRoles() {
		Set<String> accesses = new HashSet<>();
		accesses.addAll(getPaths(accessRepository.getIncludedAccesses(rbacTestFixture.getRoleVotingCountElectionDayRead())));
		accesses.addAll(getPaths(accessRepository.getIncludedAccesses(rbacTestFixture.getRoleVotingCountElectionDayApprove())));
		Assert.assertTrue(accessRepository.getIncludedAccessesNoDisabledRoles(rbacTestFixture.getRoleVotingCountElectionDayAll()).equals(accesses));
	}

	@Test
	public void getIncludedAccessesNoDisabledRolesExcludesInactiveRoles() {
		rbacTestFixture.getRoleVotingCountElectionDayRead().setActive(false);
		genericTestRepository.updateEntity(rbacTestFixture.getRoleVotingCountElectionDayRead());

		Set<String> accesses = new HashSet<>();
		accesses.addAll(getPaths(accessRepository.getIncludedAccesses(rbacTestFixture.getRoleVotingCountElectionDayApprove())));

		Assert.assertEquals(accessRepository.getIncludedAccessesNoDisabledRoles(rbacTestFixture.getRoleVotingCountElectionDayAll()), accesses);
	}

	@Test(enabled = false)
	public void getIncludedAccessesNoDisabledRolesIncludesChildrenOfIncludedRoles() {
		Set<String> accesses = new HashSet<>();
		accesses.addAll(getPaths(accessRepository.getIncludedAccesses(rbacTestFixture.getRoleVotingCountElectionDayRead())));
		accesses.addAll(getPaths(accessRepository.getIncludedAccesses(rbacTestFixture.getRoleVotingCountElectionDayApprove())));
		accesses.addAll(getPaths(findDescendingAccesses(rbacTestFixture.getRolePartyTVoteCountVotingCountElectionDayAll().getAccesses())));
		Assert.assertEquals(accessRepository.getIncludedAccessesNoDisabledRoles(rbacTestFixture.getRolePartyTVoteCountVotingCountElectionDayAll()), accesses);
	}

	@Test(enabled = false)
	public void getIncludedAccessesNoDisabledRolesExcludesChildrenOfInactiveRoles() {
		rbacTestFixture.getRoleVotingCountElectionDayAll().setActive(false);
		genericTestRepository.updateEntity(rbacTestFixture.getRoleVotingCountElectionDayAll());

		Set<String> accesses = new HashSet<>();
		accesses.addAll(getPaths(findDescendingAccesses(rbacTestFixture.getRolePartyTVoteCountVotingCountElectionDayAll().getAccesses())));

		Assert.assertEquals(accessRepository.getIncludedAccessesNoDisabledRoles(rbacTestFixture.getRolePartyTVoteCountVotingCountElectionDayAll()), accesses);
	}

	private Set<String> getPaths(final Set<Access> accesses) {
		Set<String> accessStrings = new HashSet<>();
		for (Access a : accesses) {
			accessStrings.add(a.getPath());
		}
		return accessStrings;
	}

	private Set<Access> findDescendingAccesses(final Set<Access> accesses) {
		Set<Access> accesses2 = new HashSet<>();
		for (Access a : accesses) {
			accesses2.addAll(findDescendingAccesses(a));
		}

		return accesses2;
	}

	@SuppressWarnings("unchecked")
	private Set<Access> findDescendingAccesses(final Access access) {
		Query q = getEntityManager().createNativeQuery("SELECT * FROM access a WHERE text2ltree(a.access_path) <@ text2ltree(?1)", Access.class);
		q.setParameter(1, access.getPath());

		return new HashSet<Access>(q.getResultList());
	}

}
