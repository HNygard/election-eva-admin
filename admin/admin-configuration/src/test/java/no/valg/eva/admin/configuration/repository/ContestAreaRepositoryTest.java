package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.PersistenceUnitUtil;

import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ContestAreaRepositoryTest extends AbstractJpaTestBase {
	
	@Test
	public void finnForValghendelseMedValgdistrikt_forEnValghendelse_returnererAlleOmraaderMedValgdistriktEagerLoadet() {
		PersistenceUnitUtil puUtil = getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
		GenericTestRepository genericTestRepository = new GenericTestRepository(getEntityManager());
		ElectionEvent valghendelse2007 = genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", "200701");

		ContestAreaRepository contestAreaRepository = new ContestAreaRepository(getEntityManager());
		List<ContestArea> valgdistriktsomraader = contestAreaRepository.finnForValghendelseMedValgdistrikt(valghendelse2007);

		assertThat(valgdistriktsomraader.size()).isEqualTo(468);
		assertThat(puUtil.isLoaded(valgdistriktsomraader.get(0), "contest")).isTrue();
	}

}