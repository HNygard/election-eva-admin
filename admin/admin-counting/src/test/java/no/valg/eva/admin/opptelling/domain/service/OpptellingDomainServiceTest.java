package no.valg.eva.admin.opptelling.domain.service;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.FYLKESVALGSTYRET;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.VALGSTYRET;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghierarkiStiTestData.valghierarkiSti;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpptellingDomainServiceTest extends MockUtilsTestCase {
	private OpptellingDomainService service;
	private VoteCountRepository voteCountRepository;
	private VoteCountCategoryRepository voteCountCategoryRepository;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(OpptellingDomainService.class);
		voteCountRepository = getInjectMock(VoteCountRepository.class);
		voteCountCategoryRepository = getInjectMock(VoteCountCategoryRepository.class);
	}

	@Test
	public void slettOpptellinger_gittUserDataOgValghierarkiStiOgValggeografiSti_kallerVoteCountRepository() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggeografiSti valggeografiSti = valggeografiSti();
		service.slettOpptellinger(valghierarkiSti, valggeografiSti);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, null, null);
	}

	@Test
	public void slettOpptellinger_gittUserDataOgValghierarkiStiOgValggeografiStiOgCountcategories_kallerVoteCountRepository() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggeografiSti valggeografiSti = valggeografiSti();
		CountCategory[] countCatgories = {FO, VO};
		VoteCountCategory voteCountCategory1 = voteCountCategory(1);
		VoteCountCategory voteCountCategory2 = voteCountCategory(2);
		when(voteCountCategoryRepository.findByEnum(FO)).thenReturn(voteCountCategory1);
		when(voteCountCategoryRepository.findByEnum(VO)).thenReturn(voteCountCategory2);
		service.slettOpptellinger(valghierarkiSti, valggeografiSti, countCatgories);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, 1, null);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, 2, null);
	}

	@Test
	public void slettOpptellinger_gittUserDataOgValghierarkiStiOgValggeografiStiOgStyretyper_kallerVoteCountRepository() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggeografiSti valggeografiSti = valggeografiSti();
		Styretype[] styretyper = {FYLKESVALGSTYRET, VALGSTYRET};
		service.slettOpptellinger(valghierarkiSti, valggeografiSti, styretyper);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, null, FYLKESVALGSTYRET);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, null, VALGSTYRET);
	}

	@Test
	public void slettOpptellinger_gittUserDataOgValghierarkiStiOgValggeografiStiOgCountCategoriesOgStyretyper_kallerVoteCountRepository() throws Exception {
		ValghierarkiSti valghierarkiSti = valghierarkiSti();
		ValggeografiSti valggeografiSti = valggeografiSti();
		CountCategory[] countCatgories = {FO, VO};
		Styretype[] styretyper = {FYLKESVALGSTYRET, VALGSTYRET};
		VoteCountCategory voteCountCategory1 = voteCountCategory(1);
		VoteCountCategory voteCountCategory2 = voteCountCategory(2);
		when(voteCountCategoryRepository.findByEnum(FO)).thenReturn(voteCountCategory1);
		when(voteCountCategoryRepository.findByEnum(VO)).thenReturn(voteCountCategory2);
		service.slettOpptellinger(valghierarkiSti, valggeografiSti, countCatgories, styretyper);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, 1, FYLKESVALGSTYRET);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, 2, FYLKESVALGSTYRET);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, 1, VALGSTYRET);
		verify(voteCountRepository).slettOpptellinger(valghierarkiSti, valggeografiSti, 2, VALGSTYRET);
	}

	private VoteCountCategory voteCountCategory(long pk) {
		VoteCountCategory voteCountCategory = createMock(VoteCountCategory.class);
		when(voteCountCategory.getPk()).thenReturn(pk);
		return voteCountCategory;
	}
}
