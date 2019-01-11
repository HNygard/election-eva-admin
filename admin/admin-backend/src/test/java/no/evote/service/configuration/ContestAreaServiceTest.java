package no.evote.service.configuration;

import java.util.List;

import no.evote.security.UserData;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.assertj.core.api.Assertions;
import org.postgresql.util.PSQLException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.REPOSITORY })
public class ContestAreaServiceTest extends AbstractJpaTestBase {

	protected ServiceBackedRBACTestFixture rbacTestFixture;
	private ContestAreaRepository contestAreaRepository;
	private MvAreaRepository mvAreaRepository;
	private MvElectionRepository mvElectionRepository;
	private ContestRepository contestRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		BackendContainer backend = new BackendContainer(getEntityManager());
		backend.initServices();
		contestAreaRepository = backend.getContestAreaRepository();
		mvAreaRepository = backend.getMvAreaRepository();
		mvElectionRepository = backend.getMvElectionRepository();
		contestRepository = backend.getContestRepository();
		rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
		rbacTestFixture.init();
	}

	@Test
	public void create() {
		ContestArea contestArea = new ContestArea();
		UserData createUser = rbacTestFixture.getUserData("create");

		try {
			contestArea.setContest(contestRepository.findByPk(1L));
			contestArea.setMvArea(mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.02")));

			contestArea = contestAreaRepository.create(createUser, contestArea);

			List<ContestArea> contestAreasForContest = contestAreaRepository.findContestAreasForContest(contestArea.getContest().getPk());
			ContestArea savedContestArea = findContestArea(contestArea.getPk(), contestAreasForContest);
			Assert.assertNotNull(savedContestArea.getPk());
			Assert.assertNotNull(savedContestArea.getAuditTimestamp());
			Assert.assertEquals("I", savedContestArea.getAuditOperation());
			Assert.assertEquals(0, savedContestArea.getAuditOplock());
		} finally {
			contestAreaRepository.delete(createUser, contestArea.getPk());
		}
	}

	@Test
	public void create_withInvalidAreaLevel_throwsException() throws Exception {
		ContestArea contestArea = new ContestArea();
		UserData createUser = rbacTestFixture.getUserData("create");
		Contest contest = contestRepository.findByPk(1L);
		contestArea.setContest(contest);
		contestArea.setMvArea(mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.01.0101")));
		try {
			contestAreaRepository.create(createUser, contestArea);
			Assert.fail("Expected invalidAreaLevel did not occur");
		} catch (Exception e) {
			assertInvalidAreaLevel(e);
		}
	}

	@Test
	public void update_withInvalidAreaLevel_throwsException() throws Exception {
		UserData createUser = rbacTestFixture.getUserData("create");
		Contest contest = contestRepository.findByPk(1L);
		ContestArea contestArea = contestAreaRepository.findContestAreasForContest(contest.getPk()).iterator().next();
		contestArea.setMvArea(mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.01.0101")));
		try {
			contestAreaRepository.update(createUser, contestArea);
			Assert.fail("Expected invalidAreaLevel did not occur");
		} catch (Exception e) {
			assertInvalidAreaLevel(e);
		}
	}

	private void assertInvalidAreaLevel(Exception e) {
		Exception found = null;
		while (e != null && found == null) {
			if (e instanceof PSQLException) {
				found = e;
			} else {
				e = (Exception) e.getCause();
			}
		}
		Assertions.assertThat(found).isNotNull();
		Assertions.assertThat(found.getMessage()).isEqualTo("ERROR: @contest_area.invalidAreaLevel");
	}

	private ContestArea findContestArea(final Long pk, final List<ContestArea> contestAreasForContest) {
		for (ContestArea ca : contestAreasForContest) {
			if (ca.getPk().equals(pk)) {
				return ca;
			}
		}
		return null;
	}

	@Test
	public void testFindContestAreasForContest() {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from("200701.01.01.000001").tilValghierarkiSti());
		List<ContestArea> contestAreas = contestAreaRepository.findContestAreasForContest(mvElection.getContest().getPk());

		Assert.assertTrue(!contestAreas.isEmpty());
	}

	@Test
	public void testFindContestAreaForElectionAndMvArea() {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from("200701.01.02").tilValghierarkiSti()); // Kommunestyrevalg
		MvArea mvArea = mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.03.0301")); // Oslo kommune
		List<ContestArea> contestAreas = contestAreaRepository.findContestAreaForElectionAndMvArea(mvElection.getElection().getPk(), mvArea.getPk());
		Assert.assertTrue(!contestAreas.isEmpty());

		mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from("200701.01.01").tilValghierarkiSti()); // Fylkestingsvalg
		contestAreas = contestAreaRepository.findContestAreaForElectionAndMvArea(mvElection.getElection().getPk(), mvArea.getPk());
		Assert.assertTrue(contestAreas.isEmpty());

		mvArea = mvAreaRepository.findSingleByPath(AreaPath.from("200701.47.01")); // Østfold
		contestAreas = contestAreaRepository.findContestAreaForElectionAndMvArea(mvElection.getElection().getPk(), mvArea.getPk());
		Assert.assertTrue(!contestAreas.isEmpty());
	}

	@Test
	public void testFindContestAreaChildForElectionGroupAndMunicipality() {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from("200901.02.02.000001").tilValghierarkiSti()); // Sami contest
		MvArea mvArea = mvAreaRepository.findSingleByPath(AreaPath.from("200901.47.20.2002")); // Vardø kommune. is child area
		List<ContestArea> contestAreas = contestAreaRepository.findContestAreaChildForElectionGroupAndMunicipality(mvElection.getElectionGroup().getPk(),
				mvArea.getPk());
		Assert.assertTrue(!contestAreas.isEmpty());

		mvArea = mvAreaRepository.findSingleByPath(AreaPath.from("200901.47.20.2003")); // Vadsø kommune, not child area
		contestAreas = contestAreaRepository.findContestAreaChildForElectionGroupAndMunicipality(mvElection.getElectionGroup().getPk(), mvArea.getPk());

		Assert.assertTrue(contestAreas.isEmpty());
	}
}
