package no.valg.eva.admin.configuration.repository;

import static org.testng.Assert.assertEquals;

import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class MvAreaRepositoryTest extends AbstractJpaTestBase {

	private GenericTestRepository genericTestRepository;
	private MvAreaRepository mvAreaRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		BackendContainer backend = new BackendContainer(getEntityManager());
		backend.initServices();

		mvAreaRepository = new MvAreaRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
		RBACTestFixture rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
		rbacTestFixture.init();
	}

	@Test
	public void findSingleByPollingPlaceIdAndMunicipalityPk_searchPollingPlaceId9999_findPollingPlaceNotInBallotBox() {
		MvArea mvAreaOslo = genericTestRepository.findEntityByProperty(MvArea.class, "areaPath", "000000.47.03.0301");
		MvArea pollingPlaceEnvelop = mvAreaRepository.findSingleByPollingPlaceIdAndMunicipalityPk("9999", mvAreaOslo.getMunicipality().getPk());

		assertEquals(pollingPlaceEnvelop.getPollingPlaceName(), "Konvolutt - sentral registrering");
	}

	@Test
	public void singleDigestByPath_pathForOslo_returnsDigestForOslo() {
		MvAreaDigest mvAreaDigest = mvAreaRepository.findSingleDigestByPath(new AreaPath("000000.47.03.0301"));

		assertEquals(mvAreaDigest.getMunicipalityName(), "Oslo");
		assertEquals(mvAreaDigest.getCountyName(), "Oslo");
		assertEquals(mvAreaDigest.getAreaPath(), "000000.47.03.0301");
	}
}
