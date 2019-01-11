package no.valg.eva.admin.configuration.application;

import no.evote.exception.EvoteException;
import no.evote.util.MockUtils;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.election.GenericElectionType;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListProposalApplicationServiceTest extends LocalConfigApplicationServiceTest {

	private static final long PK1 = 1L;
	private static final long PK2 = 2L;
	private static final long PK3 = 3L;

	@Test
	public void findByArea_withNoContestFound_retunsNull() throws Exception {
		ListProposalApplicationService service = initializeMocks(ListProposalApplicationService.class);
		stub_findByElectionEventAndArea(COUNTY, new ArrayList<>());

		assertThat(service.findByArea(userData(), COUNTY)).isNull();
	}

	@Test(expectedExceptions = EvoteException.class)
	public void findByArea_withMoreThanOneContestFound_throwsException() throws Exception {
		ListProposalApplicationService service = initializeMocks(ListProposalApplicationService.class);
		stub_findByElectionEventAndArea(COUNTY, Arrays.asList(createMock(Contest.class), createMock(Contest.class)));

		service.findByArea(userData(), COUNTY);
	}

	@Test
	public void findByArea_withOneContestFound_returnsResult() throws Exception {
		ListProposalApplicationService service = initializeMocks(ListProposalApplicationService.class);
        stub_findByElectionEventAndArea(COUNTY, Collections.singletonList(contest("Oslo")));

		ListProposalConfig result = service.findByArea(userData(), COUNTY);

		assertThat(result).isNotNull();
	}

	@Test
	public void findByArea_withContestAndChildren_returnsResult() throws Exception {
		ListProposalApplicationService service = initializeMocks(ListProposalApplicationService.class);
		stubWithChildren();

		ListProposalConfig result = service.findByArea(userData(), MUNICIPALITY);

		assertThat(result).isNotNull();
		assertThat(result.getChildren()).hasSize(2);
		assertThat(result.getChildren().get(0).getContestName()).isEqualTo("Bjerke");
		assertThat(result.getChildren().get(1).getContestName()).isEqualTo("Alna");
	}

	@Test
	public void save_withConfig_verifySave() throws Exception {
		ListProposalApplicationService service = initializeMocks(ListProposalApplicationService.class);
        stub_findByElectionEventAndArea(MUNICIPALITY, Collections.singletonList(contest("Oslo")));
		ListProposalConfig config = listProposalConfig(PK1, MUNICIPALITY);

		ListProposalConfig result = service.save(userData(), config, true);

		assertThat(result).isNotNull();
		verify(getInjectMock(ContestRepository.class)).findByPk(anyLong());
	}

	@Test
	public void save_withConfigWithChildren_verifySave() throws Exception {
		ListProposalApplicationService service = initializeMocks(ListProposalApplicationService.class);
		stubWithChildren();
		ListProposalConfig config = listProposalConfig(PK1, MUNICIPALITY);
		List<ListProposalConfig> children = Arrays.asList(
				listProposalConfig(PK2, MUNICIPALITY.add("444401")),
				listProposalConfig(PK3, MUNICIPALITY.add("444402")));
		when(config.getChildren()).thenReturn(children);

		ListProposalConfig result = service.save(userData(), config, true);

		assertThat(result).isNotNull();
		verify(getInjectMock(ContestRepository.class)).findByPk(PK1);
		verify(getInjectMock(ContestRepository.class)).findByPk(PK2);
		verify(getInjectMock(ContestRepository.class)).findByPk(PK3);
	}

	private ListProposalConfig listProposalConfig(long pk, AreaPath areaPath) {
		ListProposalConfig result = createMock(ListProposalConfig.class);
		when(result.getContestPk()).thenReturn(pk);
		when(result.getAreaPath()).thenReturn(areaPath);
		return result;
	}

	private void stubWithChildren() throws Exception {
		stub_contestMapper();
        stub_findByElectionEventAndArea(MUNICIPALITY, Collections.singletonList(contest("Oslo")));
        stub_findByElectionEventAndArea(MUNICIPALITY.add("444401"), Collections.singletonList(contest("Bjerke")));
        stub_findByElectionEventAndArea(MUNICIPALITY.add("444402"), Collections.singletonList(contest("Alna")));
		stub_municipalityByElectionEventAndId(new MunicipalityBuilder(MUNICIPALITY)
				.withBoroughs("444401", "444402").getValue());
	}

	private void stub_findByElectionEventAndArea(AreaPath areaPath, List<Contest> list) {
		when(getInjectMock(ContestRepository.class).findByElectionEventAndArea(anyLong(), eq(areaPath))).thenReturn(list);
	}

	private void stub_contestMapper() throws Exception {
		ElectionMapper electionMapper = new ElectionMapper(createMock(MvElectionRepository.class), createMock(ElectionRepository.class));
		ContestMapper contestMapper = new ContestMapper(createMock(ContestReportRepository.class), electionMapper);
		MockUtils.setPrivateField(assertTestObject(), "contestMapper", contestMapper, true);
	}

	private Contest contest(String name) {
		Contest result = createMock(Contest.class);
		when(result.getName()).thenReturn(name);
		when(result.getElection().getElectionType().getId()).thenReturn(GenericElectionType.F.toString());
		return result;
	}
}
