package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.evote.service.configuration.ContestServiceBean;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.election.Contest;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import javax.transaction.TransactionSynchronizationRegistry;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContestApplicationServiceTest extends MockUtilsTestCase {

	@Test
	public void get_withPath_verifyLookupAndMapping() throws Exception {
		ContestApplicationService service = initializeMocks(ContestApplicationService.class);

		service.get(userData(), ELECTION_PATH_CONTEST);

		verify(getInjectMock(ContestMapper.class)).toCommon(any(no.valg.eva.admin.configuration.domain.model.Contest.class));
		verify(getInjectMock(ContestRepository.class)).findSingleByPath(ELECTION_PATH_CONTEST);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Create Contest requires one contest area")
	public void save_withNewContestWithoutContestArea_throwsInvalidArgumentException() throws Exception {
		ContestApplicationService service = initializeMocks(ContestApplicationService.class);
		Contest contest = new Contest(election());

		service.save(userData(), contest);
	}

	@Test
	public void save_withNewContest_verifyCreate() throws Exception {
		ContestApplicationService service = initializeMocks(ContestApplicationService.class);
		Contest contest = createMock(Contest.class);
		when(contest.getPk()).thenReturn(null);
        when(contest.getContestAreas()).thenReturn(Collections.singletonList(AREA_PATH_MUNICIPALITY));
		MvArea mvArea = mvArea();
		stub_findSingleByPath(mvArea);

		service.save(userData(), contest);

		verify(contest).setId("004444");
		verify(contest).setName("Municipality 22");
		verify(getInjectMock(ContestMapper.class)).updateEntity(any(no.valg.eva.admin.configuration.domain.model.Contest.class), eq(contest));
		verify(getInjectMock(ContestServiceBean.class)).create(
				any(UserData.class),
				any(no.valg.eva.admin.configuration.domain.model.Contest.class),
				eq(mvArea),
				eq(true),
				eq(false));
	}

	@Test
	public void save_withExistingContest_verifyUpdate() throws Exception {
		ContestApplicationService service = initializeMocks(ContestApplicationService.class);
		Contest contest = createMock(Contest.class);
		when(contest.getPk()).thenReturn(1L);

		service.save(userData(), contest);

		verify(getInjectMock(ContestMapper.class)).updateEntity(any(no.valg.eva.admin.configuration.domain.model.Contest.class), eq(contest));
		verify(getInjectMock(ContestServiceBean.class)).update(
				any(UserData.class),
				any(no.valg.eva.admin.configuration.domain.model.Contest.class),
				any(TransactionSynchronizationRegistry.class));
	}

	@Test
	public void delete_withPath_deletesContest() throws Exception {
		ContestApplicationService service = initializeMocks(ContestApplicationService.class);

		service.delete(userData(), ELECTION_PATH_CONTEST);

		verify(getInjectMock(ContestServiceBean.class)).delete(any(UserData.class), anyLong(), any(TransactionSynchronizationRegistry.class));
	}

	private UserData userData() {
		return createMock(UserData.class);
	}

	private Election election() {
		return new Election(ELECTION_PATH_ELECTION_GROUP);
	}

	private MvArea stub_findSingleByPath(MvArea mvArea) {
        when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(), any(AreaPath.class))).thenReturn(mvArea);
		return mvArea;
	}

	private MvArea mvArea() {
		MvAreaBuilder builder = new MvAreaBuilder(AREA_PATH_MUNICIPALITY);
		return builder.getValue();
	}

}
