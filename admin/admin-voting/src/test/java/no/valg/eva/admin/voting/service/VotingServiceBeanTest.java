package no.valg.eva.admin.voting.service;

import no.evote.service.configuration.MvAreaServiceBean;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.VelgerMelding;
import no.valg.eva.admin.voting.domain.model.VelgerMeldingType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_ORDINAER;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.FORHANDSTEMME_ANNEN_KOMMUNE;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.FORHANDSTEMME_STENGT_PGA_AVKRYSSNINGSMANNTALL_KJORT;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.STEMMERETT_VED_SAMETINGSVALG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class VotingServiceBeanTest extends MockUtilsTestCase {

	private static final AreaPath ANNEN_KOMMUNE = AreaPath.from("111111.22.33.5555.555555.6666.7777");

	@Test
	public void hentVelgerSomSkalStemme_medForhaandsstemmerStengtPgaAvkryssingsmanntallKjort_sjekkStatus() throws Exception {
		VotingServiceBean bean = initializeMocks(VotingServiceBean.class);
		Voter velger = velger(AREA_PATH_POLLING_PLACE);
		MvArea mvArea = medAvkryssningsmanntallKjort(mvArea(AREA_PATH_POLLING_PLACE));

		VelgerSomSkalStemme result = bean.hentVelgerSomSkalStemme(FORHANDSSTEMME_ORDINAER, ELECTION_PATH_ELECTION_GROUP, mvArea.areaPath(), velger);

		assertThat(result.getStemmetypeListe()).hasSize(2);
		assertThat(result.isKanRegistrereStemmegivning()).isFalse();
		assertThat(result.getVelgerMeldinger()).hasSize(1);
		assertVelgerMelding(result, FORHANDSTEMME_STENGT_PGA_AVKRYSSNINGSMANNTALL_KJORT);
	}

	@Test
	public void hentVelgerSomSkalStemme_medFiktivVelger_sjekkStatus() throws Exception {
		VotingServiceBean bean = initializeMocks(VotingServiceBean.class);
		Voter velger = fiktiv(velger(AREA_PATH_POLLING_PLACE));
		MvArea mvArea = mvArea(AREA_PATH_POLLING_PLACE);

		VelgerSomSkalStemme result = bean.hentVelgerSomSkalStemme(FORHANDSSTEMME_ORDINAER, ELECTION_PATH_ELECTION_GROUP, mvArea.areaPath(), velger);

		assertThat(result.getStemmetypeListe()).hasSize(2);
		assertThat(result.isKanRegistrereStemmegivning()).isTrue();
		assertThat(result.getVelgerMeldinger()).isEmpty();
	}

	@Test
	public void hentVelgerSomSkalStemme_medForhandAnnenKommune_sjekkStatus() throws Exception {
		VotingServiceBean bean = initializeMocks(VotingServiceBean.class);
		Voter velger = stemmerettOgsaVedSametingsvalg(velger(ANNEN_KOMMUNE));
		MvArea mvArea = mvArea(AREA_PATH_POLLING_PLACE);

		VelgerSomSkalStemme result = bean.hentVelgerSomSkalStemme(FORHANDSSTEMME_ORDINAER, ELECTION_PATH_ELECTION_GROUP, mvArea.areaPath(), velger);

		assertThat(result.isKanRegistrereStemmegivning()).isTrue();
		assertVelgerMelding(result, STEMMERETT_VED_SAMETINGSVALG);
		assertVelgerMelding(result, FORHANDSTEMME_ANNEN_KOMMUNE);

	}

	private void assertVelgerMelding(VelgerSomSkalStemme result, VelgerMeldingType type) {
		for (VelgerMelding velgerMelding : result.getVelgerMeldinger()) {
			if (velgerMelding.getVelgerMeldingType().equals(type)) {
				return;
			}
		}
		fail("Forventet " + type + " ikke funnet");
	}

	private Voter velger(AreaPath areaPath) {
		Voter velger = createMock(Voter.class);
		MvArea mvArea = new MvAreaBuilder(areaPath).getValue();
		String municipalityId = mvArea.getMunicipalityId();
		when(velger.getMunicipalityId()).thenReturn(municipalityId);
		when(getInjectMock(VoterRepository.class).findByPk(anyLong())).thenReturn(velger);
		stub_findAdvanceVotingCategories();
		return velger;
	}

	private Voter fiktiv(Voter velger) {
		when(velger.isFictitious()).thenReturn(true);
		return velger;
	}

	private Voter stemmerettOgsaVedSametingsvalg(Voter velger) {
		when(velger.isStemmerettOgsaVedSametingsvalg()).thenReturn(true);
		return velger;
	}

	private MvArea mvArea(AreaPath areaPath) {
		MvArea mvArea = new MvAreaBuilder(areaPath).getValue();
		when(getInjectMock(MvAreaServiceBean.class).findSingleByPath(areaPath)).thenReturn(mvArea);
		return mvArea;
	}

	private MvArea medAvkryssningsmanntallKjort(MvArea mvArea) {
		when(mvArea.getMunicipality().isAvkrysningsmanntallKjort()).thenReturn(true);
		return mvArea;
	}

	private void stub_findAdvanceVotingCategories() {
		List<VotingCategory> votingCategories = asList(
				votingCategory(VO),
				votingCategory(FI));
		when(getInjectMock(VotingRepository.class).findAdvanceVotingCategories()).thenReturn(votingCategories);
	}

	private VotingCategory votingCategory(no.valg.eva.admin.common.voting.VotingCategory votingCategory) {
		VotingCategory vc = new VotingCategory();
		vc.setId(votingCategory.getId());
		vc.setName(votingCategory.getName());
		return vc;
	}

}
