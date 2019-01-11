package no.valg.eva.admin.configuration.domain.service;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.StemmekretsStiTestData.STEMMEKRETS_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class OpptellingskategoriDomainServiceTest extends MockUtilsTestCase {
	@Test
	public void countCategoriesForValgSti_gittUserDataOgValgSti_returnererCountCategories() throws Exception {
		OpptellingskategoriDomainService service = initializeMocks(OpptellingskategoriDomainService.class);
		MvElectionRepository mvElectionRepository = getInjectMock(MvElectionRepository.class);
		VoteCountCategoryRepository voteCountCategoryRepository = getInjectMock(VoteCountCategoryRepository.class);

		ValggeografiSti operatorValggeografiSti = createMock(ValggeografiSti.class);
		ValgSti valgSti = createMock(ValgSti.class);
		MvElection mvElection = createMock(MvElection.class);
		List<VoteCountCategory> voteCountCategories = voteCountCategories();

		when(mvElectionRepository.finnEnkeltMedSti(valgSti)).thenReturn(mvElection);
		when(voteCountCategoryRepository.findByElectionAndAreaPath(mvElection.getElection(), operatorValggeografiSti.areaPath())).thenReturn(voteCountCategories);

		List<CountCategory> countCategories = service.countCategoriesForValgSti(operatorValggeografiSti, valgSti);

		assertThat(countCategories).containsExactly(FO, FS, VO);
	}

	@Test
	public void countCategories_gittUserData_returnererCountCategories() throws Exception {
		OpptellingskategoriDomainService service = initializeMocks(OpptellingskategoriDomainService.class);
		MvElectionRepository mvElectionRepository = getInjectMock(MvElectionRepository.class);
		VoteCountCategoryRepository voteCountCategoryRepository = getInjectMock(VoteCountCategoryRepository.class);

		ValggeografiSti operatorValggeografiSti = createMock(ValggeografiSti.class);
		MvElection mvElection = createMock(MvElection.class);
		List<VoteCountCategory> voteCountCategories = voteCountCategories();

		when(operatorValggeografiSti.valghendelseSti().valghendelseId()).thenReturn(VALGHENDELSE_ID_111111);
		when(mvElectionRepository.finnEnkeltMedSti(VALGHENDELSE_STI)).thenReturn(mvElection);
		when(voteCountCategoryRepository.findByElectionEventAndAreaPath(mvElection.getElectionEvent(), operatorValggeografiSti.areaPath()))
				.thenReturn(voteCountCategories);

		List<CountCategory> countCategories = service.countCategories(operatorValggeografiSti);

		assertThat(countCategories).containsExactly(FO, FS, VO);
	}

	@Test
	public void countCategories_gittUserDataPaaStemmekrets_returnererKunVo() throws Exception {
		OpptellingskategoriDomainService service = initializeMocks(OpptellingskategoriDomainService.class);
		MvElectionRepository mvElectionRepository = getInjectMock(MvElectionRepository.class);

		MvElection mvElection = createMock(MvElection.class);

		when(mvElectionRepository.finnEnkeltMedSti(VALGHENDELSE_STI)).thenReturn(mvElection);

		List<CountCategory> countCategories = service.countCategories(STEMMEKRETS_STI);

		assertThat(countCategories).containsExactly(VO);
	}

	private List<VoteCountCategory> voteCountCategories() {
		return asList(voteCountCategory(FO), voteCountCategory(FS), voteCountCategory(VO));
	}

	private VoteCountCategory voteCountCategory(CountCategory countCategory) {
		VoteCountCategory voteCountCategory = createMock(VoteCountCategory.class);
		when(voteCountCategory.getCountCategory()).thenReturn(countCategory);
		return voteCountCategory;
	}

}
