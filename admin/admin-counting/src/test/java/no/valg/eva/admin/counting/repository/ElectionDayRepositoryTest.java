package no.valg.eva.admin.counting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ElectionDayRepositoryTest extends AbstractJpaTestBase {

	private ElectionDayRepository electionDayRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		electionDayRepository = new ElectionDayRepository(getEntityManager());
	}

	@Test
	public void findForPollingDistrict() throws Exception {
		PollingDistrict pollingDistrict = (PollingDistrict) getEntityManager()
				.createQuery("select pd from PollingDistrict pd, MvArea mva "
						+ "where pd.id = '0014' and pd.pk = mva.pollingDistrict.pk and mva.areaPath = '200701.47.19.1941.194100.0014'").getSingleResult();

		List<ElectionDay> electionDayList = electionDayRepository.findForPollingDistrict(pollingDistrict.getPk());

		assertThat(electionDayList.size()).isEqualTo(2);
	}
}
