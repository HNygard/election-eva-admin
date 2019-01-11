package no.valg.eva.admin.configuration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.service.backendmock.BackendContainer;
import no.evote.service.configuration.ProposerServiceBean;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })

public class ProposerServiceBeanTest extends AbstractJpaTestBase {

	private ProposerServiceBean proposerService;
	private MvElectionRepository mvElectionRepository;
	private BallotRepository ballotRepository;

	@BeforeMethod(alwaysRun = true)
	public void initDependencies() {
		BackendContainer backend = new BackendContainer(getEntityManager());
		backend.initServices();

		proposerService = backend.getProposerServiceBean();
		mvElectionRepository = backend.getMvElectionRepository();
		ballotRepository = backend.getBallotRepository();
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
		  expectedExceptionsMessageRegExp = "changeDisplayOrder displayOrderFrom\\[1\\] equal to displayOrderTo\\[1\\]")
	public void changeDisplayOrder_withEqualFromTo_throwsException() throws Exception {

		Proposer proposerMock = mock(Proposer.class, RETURNS_DEEP_STUBS);
		when(proposerMock.getBallot().getPk()).thenReturn(5L);

		proposerService.changeDisplayOrder(proposerMock, 1, 1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
		  expectedExceptionsMessageRegExp = "changeDisplayOrder from/to \\[1/1000\\] does not match actual size 2")
	public void changeDisplayOrder_withOutOfBoundsFromTo_throwsException() throws Exception {
		Proposer proposerMock = mock(Proposer.class, RETURNS_DEEP_STUBS);
		when(proposerMock.getBallot().getPk()).thenReturn(5L);

		proposerService.changeDisplayOrder(proposerMock, 1, 1000);
	}

	@Test
	public void changeDisplayOrder_withLowFromAndHighTo_returnsResult() throws Exception {
		Proposer proposerMock = mock(Proposer.class, RETURNS_DEEP_STUBS);
		when(proposerMock.getBallot().getPk()).thenReturn(ballotPkForContest1AndBallotSp());

		List<Proposer> result = proposerService.changeDisplayOrder(proposerMock, 1, 2);

		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0).getFirstName()).isEqualTo("Hans");
		assertThat(result.get(0).getDisplayOrder()).isEqualTo(1);
		assertThat(result.get(1).getFirstName()).isEqualTo("Peder");
		assertThat(result.get(1).getDisplayOrder()).isEqualTo(2);
	}

	@Test
	public void changeDisplayOrder_withHighFromAndLowTo_returnsResult() throws Exception {
		Proposer proposerMock = mock(Proposer.class, RETURNS_DEEP_STUBS);
		when(proposerMock.getBallot().getPk()).thenReturn(ballotPkForContest1AndBallotSp());

		List<Proposer> result = proposerService.changeDisplayOrder(proposerMock, 2, 1);

		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0).getFirstName()).isEqualTo("Hans");
		assertThat(result.get(0).getDisplayOrder()).isEqualTo(1);
		assertThat(result.get(1).getFirstName()).isEqualTo("Peder");
		assertThat(result.get(1).getDisplayOrder()).isEqualTo(2);
	}

	private long ballotPkForContest1AndBallotSp() {
		Long contestPk = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("200701", "01", "01", "000001")).getContest().getPk();
		return ballotRepository.findPkByContestAndId(contestPk, "SP");
	}
}

