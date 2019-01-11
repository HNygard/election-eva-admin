package no.valg.eva.admin.configuration.service.impl;

import no.evote.service.backendmock.BackendContainer;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)

public class AreaLevelServiceImplTest extends AbstractJpaTestBase {
	private MvAreaRepository mvAreaRepository;

	@BeforeMethod(alwaysRun = true)
	public void initDependencies() {
		BackendContainer backend = new BackendContainer(getEntityManager());
		backend.initServices();
		mvAreaRepository = backend.getMvAreaRepository();
	}

	@Test
	public void testFindAllAreaLevels() {
		Assert.assertFalse(mvAreaRepository.findAllAreaLevels().isEmpty());
	}

	@Test
	public void testFindAreaLevelById() {
		Assert.assertNotNull(mvAreaRepository.findAreaLevelById("0"));
		Assert.assertNull(mvAreaRepository.findAreaLevelById("99"));
	}

	@Test
	public void testFindAreaLevelByPk() {
		Assert.assertNotNull(mvAreaRepository.findAreaLevelByPk(1L));
		Assert.assertNull(mvAreaRepository.findAreaLevelByPk(99L));
	}
}

