package no.valg.eva.admin.configuration.repository.valgnatt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ReportConfiguration;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class ValgnattElectoralRollRepositoryTest extends AbstractJpaTestBase {

	private static final int EXPECTED_RESULT_SIZE = 11;
	private static final int EXPECTED_VOTERS_IN_HALDEN_0000 = 596;
	private static final ElectionPath ELECTION_PATH = ElectionPath.from("200701");
	private static final int EXPECTED_CONFIG_SIZE = 1693;
    private static final long KOMMUNESTYRE_VALG_PK = 2L;
    private ValgnattElectoralRollRepository valgnattElectoralRollRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		new ValgnattElectoralRollRepository();
		valgnattElectoralRollRepository = new ValgnattElectoralRollRepository(getEntityManager());
	}

	@Test
	public void valgnattElectoralRoll_returnsElectoralRollFromTestdata() {
		List<ElectoralRollCount> electoralRollCounts = valgnattElectoralRollRepository.valgnattElectoralRoll(ELECTION_PATH);
		assertThat(electoralRollCounts).hasSize(EXPECTED_RESULT_SIZE);
		assertThat(electoralRollCounts.get(0).getVoterTotal()).isEqualTo(EXPECTED_VOTERS_IN_HALDEN_0000);
	}

	@Test
	public void valgnattReportConfiguration_returnsReportConfiguration() {
        MvElection mvElection = new MvElection();
        Election election = new Election();
        election.setPk(KOMMUNESTYRE_VALG_PK);
        mvElection.setElection(election);
        mvElection.setAreaLevel(AreaLevelEnum.MUNICIPALITY.getLevel());
        List<ReportConfiguration> reportConfigurations = valgnattElectoralRollRepository.valgnattReportConfiguration(mvElection);
		assertThat(reportConfigurations).hasSize(EXPECTED_CONFIG_SIZE);
	}
}
