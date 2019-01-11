package no.valg.eva.admin.configuration.repository;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.Test;

import javax.persistence.PersistenceUnitUtil;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = TestGroups.REPOSITORY)
public class PollingPlaceRepositoryTest extends AbstractJpaTestBase {
	
	@Test
	public void findPollingPlaceWithOpeningHours_forEnValghendelse_henterAllePollingPlacesOgTilhorendeOpeningHours() {
		PersistenceUnitUtil puUtil = getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
		GenericTestRepository genericTestRepository = new GenericTestRepository(getEntityManager());
		ElectionEvent valghendelse2007 = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");

		PollingPlaceRepository pollingPlaceRepository = new PollingPlaceRepository(getEntityManager());
		List<PollingPlace> pollingPlaceOgOpeningHours = pollingPlaceRepository.findPollingPlacesWithOpeningHours(valghendelse2007);

		assertThat(pollingPlaceOgOpeningHours.size()).isEqualTo(3560);
		assertThat(puUtil.isLoaded(pollingPlaceOgOpeningHours.get(0), "openingHours")).isTrue();
	}
	
}