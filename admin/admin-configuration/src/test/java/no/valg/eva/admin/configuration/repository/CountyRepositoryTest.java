package no.valg.eva.admin.configuration.repository;

import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.PersistenceUnitUtil;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = TestGroups.REPOSITORY)
public class CountyRepositoryTest extends AbstractJpaTestBase {

	private CountyRepository countyRepository;
	private MunicipalityRepository municipalityRepository;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		countyRepository = new CountyRepository(getEntityManager());
		municipalityRepository = new MunicipalityRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
	}

	@Test
	public void findByPkWithScanningConfig_always_eagerLoadsScanningConfig() {
		PersistenceUnitUtil puUtil = getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");
		County countyWithLazyScanningConfig = municipalityRepository.municipalityByElectionEventAndId(electionEvent.getPk(), "0101").getCounty();
		assertThat(puUtil.isLoaded(countyWithLazyScanningConfig, "scanningConfig")).isFalse();

		County countyWithEagerScanningConfig = countyRepository.findByPkWithScanningConfig(countyWithLazyScanningConfig.getPk());

		assertThat(puUtil.isLoaded(countyWithEagerScanningConfig, "scanningConfig")).isTrue();
	}

	@Test
	public void findByElectionEventWithScanningConfig_always_eagerLoadsScanningConfig() {
		PersistenceUnitUtil puUtil = getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
		ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");
		County singleCounty = municipalityRepository.municipalityByElectionEventAndId(electionEvent.getPk(), "0101").getCounty();
		singleCounty.getOrCreateScanningConfig().setScanning(true);

		List<County> countiesWithEagerScanningConfig = countyRepository.findByElectionEventWithScanningConfig(electionEvent.getPk());

		assertThat(puUtil.isLoaded(countiesWithEagerScanningConfig.get(0), "scanningConfig")).isTrue();
	}
}
