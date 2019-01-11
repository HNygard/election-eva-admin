package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.evote.service.configuration.ResponsibleOfficerServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.ElectoralRollSearch;
import no.valg.eva.admin.common.configuration.model.local.DisplayOrder;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Responsibility;
import no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.ResponsibleOfficerRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ReportingUnitApplicationServiceTest extends LocalConfigApplicationServiceTest {

	@Test
	public void findByArea_withFiveResponsibleOfficers_returnsFiveResponsibleOfficers() throws Exception {
		ReportingUnitApplicationService service = initializeMocks(ReportingUnitApplicationService.class);
		stub_findResponsibleOfficersForReportingUnit(5);

		List<no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer> result = service.findByArea(userData(), MUNICIPALITY);

		assertThat(result).hasSize(5);
	}

	@Test
	public void findByArea_withRootArea_returnsOneResponsibleOfficers() throws Exception {
		ReportingUnitApplicationService service = initializeMocks(ReportingUnitApplicationService.class);
		stub_findResponsibleOfficersForReportingUnit(5);
		UserData userData = userData();
		when(userData.getOperatorMvElection().getElectionPath()).thenReturn("111111");

		List<no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer> result = service.findByArea(userData, ROOT);

		assertThat(result).hasSize(5);
	}

	@Test
	public void save_withNewOfficer_verifyCreate() throws Exception {
		ReportingUnitApplicationService service = initializeMocks(ReportingUnitApplicationService.class);
		no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer officer = createMock(
				no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer.class);
		when(officer.getPk()).thenReturn(null);
		stub_save(officer());

		no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer result = service.save(userData(), officer);

		assertThat(result).isNotNull();
		assertThat(result.getAreaPath()).isEqualTo(MUNICIPALITY);
		assertThat(result.getResponsibilityId()).isSameAs(ResponsibilityId.LEDER);
		assertThat(result.getFirstName()).isEqualTo("Test");
		assertThat(result.getLastName()).isEqualTo("Testesen");
		verify(getInjectMock(ReportingUnitDomainService.class)).getReportingUnit(any(UserData.class), any(AreaPath.class));
        verify(getInjectMock(ResponsibleOfficerRepository.class)).findResponsibilityById(any());
	}

	@Test
	public void save_withExistingOfficer_verifyUpdate() throws Exception {
		ReportingUnitApplicationService service = initializeMocks(ReportingUnitApplicationService.class);
		no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer officer = createMock(
				no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer.class);
		when(officer.getPk()).thenReturn(10L);
		stub_save(officer());

		no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer result = service.save(userData(), officer);

		assertThat(result).isNotNull();
        verify(getInjectMock(ResponsibleOfficerRepository.class)).findResponsibilityById(any());
	}

	@Test
	public void search_withSsn_verifyResult() throws Exception {
		ReportingUnitApplicationService service = initializeMocks(ReportingUnitApplicationService.class);
		ElectoralRollSearch search = new ElectoralRollSearch();
		search.setSsn("10037549911");
		stub_findByElectionEventAreaAndId(5);

		List<no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer> result = service.search(userData(), MUNICIPALITY, search);

		assertThat(result).hasSize(5);
	}

	@Test
	public void search_withBirthDateAndName_verifyResult() throws Exception {
		ReportingUnitApplicationService service = initializeMocks(ReportingUnitApplicationService.class);
		ElectoralRollSearch search = new ElectoralRollSearch();
		search.setBirthDate(new LocalDate(2000, 1, 1));
		search.setName("name");
		stub_searchVoter(5);

		List<no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer> result = service.search(userData(), MUNICIPALITY, search);

		assertThat(result).hasSize(5);
	}

	@Test
	public void delete_withOfficer_verifyDelete() throws Exception {
		ReportingUnitApplicationService service = initializeMocks(ReportingUnitApplicationService.class);
		no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer officer = createMock(
				no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer.class);

		service.delete(userData(), officer);

		verify(getInjectMock(ResponsibleOfficerRepository.class)).findByPk(anyLong());
		verify(getInjectMock(ResponsibleOfficerServiceBean.class)).delete(any(UserData.class), any(ResponsibleOfficer.class));
	}

	@Test
	public void saveResponsibleOfficerDisplayOrder_withOneDisplayOrder_verifyUpdate() throws Exception {
		ReportingUnitApplicationService service = initializeMocks(ReportingUnitApplicationService.class);
		ResponsibleOfficer responsibleOfficer = createMock(ResponsibleOfficer.class);
		when(getInjectMock(ResponsibleOfficerRepository.class).findByPk(anyLong())).then(invocation -> responsibleOfficer);

		service.saveResponsibleOfficerDisplayOrder(userData(), MUNICIPALITY, Collections.singletonList(new DisplayOrder(10L, 1, 0)));

		verify(getInjectMock(ResponsibleOfficerServiceBean.class), times(1)).save(any(UserData.class), eq(responsibleOfficer));
		verify(responsibleOfficer).setDisplayOrder(1);
	}

	private List<ResponsibleOfficer> stub_findResponsibleOfficersForReportingUnit(int size) {
		List<ResponsibleOfficer> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(officer());
		}
		when(getInjectMock(ResponsibleOfficerRepository.class).findResponsibleOfficersForReportingUnit(anyLong())).thenReturn(result);
		return result;
	}

	private void stub_save(ResponsibleOfficer officer) {
		when(getInjectMock(ResponsibleOfficerServiceBean.class).save(any(UserData.class), any(ResponsibleOfficer.class))).thenReturn(officer);
	}

	private List<Voter> stub_findByElectionEventAreaAndId(int size) {
		List<Voter> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(voter());
		}
		when(getInjectMock(VoterRepository.class).findByElectionEventAreaAndId(anyLong(), any(AreaPath.class), anyString())).thenReturn(result);
		return result;
	}

	private List<Voter> stub_searchVoter(int size) {
		List<Voter> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(voter());
		}
		when(getInjectMock(VoterRepository.class).searchVoter(any(Voter.class), anyString(), anyString(),
                any(), anyInt(), eq(false), anyLong())).thenReturn(result);
		return result;
	}

	private Voter voter() {
		return createMock(Voter.class);
	}

	private ResponsibleOfficer officer() {
		ReportingUnit ru = createMock(ReportingUnit.class);
		when(ru.getMvArea().getAreaPath()).thenReturn(MUNICIPALITY.path());
		Responsibility responsibility = createMock(Responsibility.class);
		when(responsibility.getId()).thenReturn(ResponsibilityId.LEDER.getId());
		ResponsibleOfficer officer = new ResponsibleOfficer();
		officer.setFirstName("Test");
		officer.setLastName("Testesen");
		officer.setReportingUnit(ru);
		officer.setResponsibility(responsibility);
		return officer;
	}
}

