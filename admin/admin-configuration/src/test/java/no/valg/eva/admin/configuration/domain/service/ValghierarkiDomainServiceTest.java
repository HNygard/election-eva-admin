package no.valg.eva.admin.configuration.domain.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static no.valg.eva.admin.common.Process.COUNTING;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_12;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_13;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.BOROUGH_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.MUNICIPALITY_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.mvArea;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.CONTEST_NAME_1;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.CONTEST_NAME_2;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.CONTEST_PATH_1;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.CONTEST_PATH_2;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_EVENT_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_GROUP_NAME_DEFAULT;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_GROUP_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_NAME_1;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_NAME_2;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_NAME_3;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_PATH_1;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_PATH_2;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_PATH_3;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.ELECTION_PATH_DEFAULT;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.contest;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.mvElection;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.userData;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvElectionDigestTestData.MV_ELECTION_DIGESTS_111111_11_11_11111X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvElectionDigestTestData.MV_ELECTION_DIGESTS_111111_11_1X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvElectionDigestTestData.MV_ELECTION_DIGESTS_111111_1X;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvElectionDigestTestData.MV_ELECTION_DIGEST_111111;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvElectionDigestTestData.MV_ELECTION_DIGEST_111111_11;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvElectionDigestTestData.MV_ELECTION_DIGEST_111111_11_11;
import static no.valg.eva.admin.configuration.test.data.domain.model.MvElectionDigestTestData.MV_ELECTION_DIGEST_111111_11_11_111111;
import static no.valg.eva.admin.configuration.test.repository.MvElectionRepositoryMockUtil.findDigestByElectionPathAndAreaPath;
import static no.valg.eva.admin.configuration.test.repository.MvElectionRepositoryMockUtil.findDigestsByPathAndLevel;
import static no.valg.eva.admin.configuration.test.repository.MvElectionRepositoryMockUtil.findSingleDigestByPath;
import static no.valg.eva.admin.configuration.test.repository.MvElectionRepositoryMockUtil.hasContestsForElectionAndArea;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI_111111_11_11;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI_111111_11;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_12;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_13;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_NAVN_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_111111_11_11_111112;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_111111_11_11_111113;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_NAVN_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_111111_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_111111_12;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_111111_13;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_NAVN_111111_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_NAVN_111111;
import static no.valg.eva.admin.felles.test.valghierarki.model.ValghierarkiAssert.assertThat;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import no.valg.eva.admin.felles.valghierarki.model.Valghendelse;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValghierarkiDomainServiceTest extends MockUtilsTestCase {
	private ValghierarkiDomainService service;
	private MvElectionRepository mvElectionRepository;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(ValghierarkiDomainService.class);
		mvElectionRepository = getInjectMock(MvElectionRepository.class);
	}

	@Test
	public void valghendelse_gittValghendelseSti_returnererValghendelse() throws Exception {
		findSingleDigestByPath(mvElectionRepository, ELECTION_PATH_111111, MV_ELECTION_DIGEST_111111);
		Valghendelse resultat = service.valghendelse(VALGHENDELSE_STI);
		assertThat(resultat).harNavnLikMed(VALGHENDELSE_NAVN_111111);
		assertThat(resultat).harStiLikMed(VALGHENDELSE_STI);
	}

	@Test
	public void valggruppe_gittValggruppeSti_returnerValggruppe() throws Exception {
		findSingleDigestByPath(mvElectionRepository, ELECTION_PATH_111111_11, MV_ELECTION_DIGEST_111111_11);
		Valggruppe resultat = service.valggruppe(VALGGRUPPE_STI_111111_11);
		assertThat(resultat).harNavnLikMed(VALGGRUPPE_NAVN_111111_11);
		assertThat(resultat).harStiLikMed(VALGGRUPPE_STI_111111_11);
	}

	@Test
	public void valggrupper_gittValghendelseSti_returnererValggrupperForValghendelse() throws Exception {
		findDigestsByPathAndLevel(mvElectionRepository, ELECTION_PATH_111111, ELECTION_GROUP, MV_ELECTION_DIGESTS_111111_1X);
		List<Valggruppe> resultat = service.valggrupper(VALGHENDELSE_STI);
		assertThat(resultat).containsExactly(VALGGRUPPE_111111_11, VALGGRUPPE_111111_12, VALGGRUPPE_111111_13);
	}

	@Test
	public void valg_gittValgSti_returnerValg() throws Exception {
		findSingleDigestByPath(mvElectionRepository, ELECTION_PATH_111111_11_11, MV_ELECTION_DIGEST_111111_11_11);
		Valg resultat = service.valg(VALG_STI_111111_11_11);
		assertThat(resultat).harNavnLikMed(VALG_NAVN_111111_11_11);
		assertThat(resultat).harStiLikMed(VALG_STI_111111_11_11);
	}

	@Test
	public void valg_gittValggruppeStiOgValggeografiStiOgValggeografiNivaaErValghendelseEllerNodeHarValggeografiNivaaFalse_returnerForventedeValg()
			throws Exception {
		ValggeografiSti operatorValggeografiSti = valggeografiSti();
		findDigestsByPathAndLevel(mvElectionRepository, ELECTION_PATH_111111_11, ELECTION, MV_ELECTION_DIGESTS_111111_11_1X);
		hasContestsForElectionAndArea(mvElectionRepository, ELECTION_PATH_111111_11_11, operatorValggeografiSti.areaPath(), true);
		hasContestsForElectionAndArea(mvElectionRepository, ELECTION_PATH_111111_11_12, operatorValggeografiSti.areaPath(), true);
		hasContestsForElectionAndArea(mvElectionRepository, ELECTION_PATH_111111_11_13, operatorValggeografiSti.areaPath(), true);
		List<Valg> resultat = service.valg(VALGGRUPPE_STI, operatorValggeografiSti, false, null);
		assertThat(resultat).containsExactly(VALG_111111_11_11, VALG_111111_11_12, VALG_111111_11_13);
	}

	@Test(dataProvider = "valggeografiStiOgForventetResultat")
	public void valg_gittValggruppeStiOgFylkeskommuneStiOgValggeografiNivaaErValghendelseEllerNodeHarValggeografiNivaaTrue_returnerForventedeValg(
			ValggeografiNivaa valggeografiNivaa, Valg[] forventetResultat)
			throws Exception {
		ValggeografiSti operatorValggeografiSti = valggeografiSti();
		when(operatorValggeografiSti.nivaa()).thenReturn(valggeografiNivaa);
		findDigestsByPathAndLevel(mvElectionRepository, ELECTION_PATH_111111_11, ELECTION, MV_ELECTION_DIGESTS_111111_11_1X);
		hasContestsForElectionAndArea(mvElectionRepository, ELECTION_PATH_111111_11_11, operatorValggeografiSti.areaPath(), true);
		hasContestsForElectionAndArea(mvElectionRepository, ELECTION_PATH_111111_11_12, operatorValggeografiSti.areaPath(), true);
		hasContestsForElectionAndArea(mvElectionRepository, ELECTION_PATH_111111_11_13, operatorValggeografiSti.areaPath(), true);
		MvElection mvElection = createMock(MvElection.class);
		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_PATH_111111_11_11, CONTEST, COUNTY)).thenReturn(singletonList(mvElection));
		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_PATH_111111_11_12, CONTEST, MUNICIPALITY)).thenReturn(singletonList(mvElection));
		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_PATH_111111_11_13, CONTEST, BOROUGH)).thenReturn(singletonList(mvElection));
		List<Valg> resultat = service.valg(VALGGRUPPE_STI, operatorValggeografiSti, true, null);
		assertThat(resultat).containsExactly(forventetResultat);
	}

	@DataProvider
	public Object[][] valggeografiStiOgForventetResultat() {
		return new Object[][]{
				{VALGHENDELSE, new Valg[]{VALG_111111_11_11, VALG_111111_11_12, VALG_111111_11_13}},
				{FYLKESKOMMUNE, new Valg[]{VALG_111111_11_11}},
				{KOMMUNE, new Valg[]{VALG_111111_11_12, VALG_111111_11_13}}
		};
	}

	@Test
	public void valg_gittValggruppeStiOgKommuneStiOgValgPaaFylkesnivaaMedEttValgdistriktPaaKommuneNivaa_returnerForventetValg() throws Exception {
		ValggeografiSti operatorValggeografiSti = valggeografiSti();
		when(operatorValggeografiSti.nivaa()).thenReturn(KOMMUNE);
		findDigestsByPathAndLevel(mvElectionRepository, ELECTION_PATH_111111_11, ELECTION, MV_ELECTION_DIGESTS_111111_11_1X);
		hasContestsForElectionAndArea(mvElectionRepository, ELECTION_PATH_111111_11_11, operatorValggeografiSti.areaPath(), true);
		MvElection mvElection = createMock(MvElection.class);
		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_PATH_111111_11_11, CONTEST, MUNICIPALITY)).thenReturn(singletonList(mvElection));
		List<Valg> resultat = service.valg(VALGGRUPPE_STI, operatorValggeografiSti, true, null);
		assertThat(resultat).containsExactly(VALG_111111_11_11);
	}

	@Test
	public void valgdistrikt_gittValgdistriktSti_returnererValgdistrikt() throws Exception {
		findSingleDigestByPath(mvElectionRepository, ELECTION_PATH_111111_11_11_111111, MV_ELECTION_DIGEST_111111_11_11_111111);
		Valgdistrikt resultat = service.valgdistrikt(VALGDISTRIKT_STI);
		assertThat(resultat).harNavnLikMed(VALGDISTRIKT_NAVN_111111_11_11_111111);
		assertThat(resultat).harStiLikMed(VALGDISTRIKT_STI);
	}

	@Test
	public void valgdistrikter_gittValgStiOgValggeografiSti_returnererValgdistrikterSomMatcherValggeografi() throws Exception {
		ValggeografiSti operatorValggeografiSti = valggeografiSti();
		findDigestsByPathAndLevel(mvElectionRepository, ELECTION_PATH_111111_11_11, CONTEST, MV_ELECTION_DIGESTS_111111_11_11_11111X);
		hasContestsForElectionAndArea(mvElectionRepository, ELECTION_PATH_111111_11_11_111111, operatorValggeografiSti.areaPath(), true);
		List<Valgdistrikt> resultat = service.valgdistrikter(VALG_STI, operatorValggeografiSti);
		assertThat(resultat).containsExactly(VALGDISTRIKT_111111_11_11_111111);
	}

	@Test
	public void testValgdistrikterFiltrertPaaGeografi() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		findDigestByElectionPathAndAreaPath(mvElectionRepository, VALG_STI, valggeografiSti, MV_ELECTION_DIGESTS_111111_11_11_11111X);
		List<Valgdistrikt> resultat = service.valgdistrikterFiltrertPaaGeografi(VALG_STI, valggeografiSti);
		assertThat(resultat).containsExactly(VALGDISTRIKT_111111_11_11_111111, VALGDISTRIKT_111111_11_11_111112, VALGDISTRIKT_111111_11_11_111113);
	}

	@Test
	public void getElectionGroupsFor_givenUserDataAndElectionEventPath_returnsAllElectionGroupsInElectionEvent() throws Exception {
		ElectionPath electionEventPath = new ElectionPath(ELECTION_EVENT_PATH_DEFAULT);
		MvElection mvElection = mvElection(ELECTION_GROUP_PATH_DEFAULT, ELECTION_GROUP_NAME_DEFAULT);
		when(mvElectionRepository.findByPathAndLevel(electionEventPath, ELECTION_GROUP)).thenReturn(singletonList(mvElection));

		List<MvElection> actual = service.getElectionGroupsFor(electionEventPath);

		List<MvElection> expected = singletonList(mvElection);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void getElectionsFor_givenUserDataAndElectionGroupPath_returnsElectionsWithContestsInArea() throws Exception {
		ElectionPath electionGroupPath = new ElectionPath(ELECTION_GROUP_PATH_DEFAULT);
		MvElection mvElection1 = mvElection(ELECTION_PATH_1, ELECTION_NAME_1, MUNICIPALITY);
		MvElection mvElection2 = mvElection(ELECTION_PATH_2, ELECTION_NAME_2, MUNICIPALITY);
		MvElection mvElection3 = mvElection(ELECTION_PATH_3, ELECTION_NAME_3, MUNICIPALITY);

		when(mvElectionRepository.hasContestsForElectionAndArea(ElectionPath.from(ELECTION_PATH_1), AreaPath.from(MUNICIPALITY_PATH_DEFAULT))).thenReturn(true);
		when(mvElectionRepository.hasContestsForElectionAndArea(ElectionPath.from(ELECTION_PATH_2), AreaPath.from(MUNICIPALITY_PATH_DEFAULT))).thenReturn(true);
		when(mvElectionRepository.hasContestsForElectionAndArea(ElectionPath.from(ELECTION_PATH_3), AreaPath.from(MUNICIPALITY_PATH_DEFAULT))).thenReturn(false);
		when(mvElectionRepository.findByPathAndLevel(electionGroupPath, ELECTION)).thenReturn(asList(mvElection1, mvElection2, mvElection3));

		List<MvElection> actual = service.getElectionsFor(userData(MUNICIPALITY_PATH_DEFAULT), CountCategory.VO, electionGroupPath, COUNTING);

		List<MvElection> expected = asList(mvElection1, mvElection2);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void getElectionsFor_givenForBfFilter_returnsElectionsPickerWithOnlyBoroughElection() throws Exception {
		ElectionPath electionGroupPath = new ElectionPath(ELECTION_GROUP_PATH_DEFAULT);
		MvElection mvElection1 = mvElection(ELECTION_PATH_1, ELECTION_NAME_1, COUNTY);
		MvElection mvElection2 = mvElection(ELECTION_PATH_2, ELECTION_NAME_2, MUNICIPALITY);
		MvElection mvElection3 = mvElection(ELECTION_PATH_3, ELECTION_NAME_3, BOROUGH);

		when(mvElectionRepository.hasContestsForElectionAndArea(ElectionPath.from(ELECTION_PATH_1), AreaPath.from(MUNICIPALITY_PATH_DEFAULT))).thenReturn(true);
		when(mvElectionRepository.hasContestsForElectionAndArea(ElectionPath.from(ELECTION_PATH_2), AreaPath.from(MUNICIPALITY_PATH_DEFAULT))).thenReturn(true);
		when(mvElectionRepository.hasContestsForElectionAndArea(ElectionPath.from(ELECTION_PATH_3), AreaPath.from(MUNICIPALITY_PATH_DEFAULT))).thenReturn(true);
		when(mvElectionRepository.findByPathAndLevel(electionGroupPath, ELECTION)).thenReturn(asList(mvElection1, mvElection2, mvElection3));

		List<MvElection> actual = service.getElectionsFor(userData(MUNICIPALITY_PATH_DEFAULT), CountCategory.BF, electionGroupPath, COUNTING);

		List<MvElection> expected = singletonList(mvElection3);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void getContestsFor_givenUserDataAndElectionPath_returnsAllContestsInElection() throws Exception {
		UserData userData = userData(ELECTION_EVENT_PATH_DEFAULT);
		ElectionPath electionPath = new ElectionPath(ELECTION_PATH_DEFAULT);
		MvElection mvElection1 = mvElection(CONTEST_PATH_1, CONTEST_NAME_1);
		MvElection mvElection2 = mvElection(CONTEST_PATH_2, CONTEST_NAME_2);
		when(mvElectionRepository.findContestsForElectionAndArea(electionPath, userData.getOperatorAreaPath())).thenReturn(asList(mvElection1, mvElection2));

		List<MvElection> actual = service.getContestsFor(userData, electionPath);

		List<MvElection> expected = asList(mvElection1, mvElection2);
		assertThat(actual).isEqualTo(expected);
	}
	
	@Test
	public void isElectionOnBoroughLevel_givenBoroughElection_returnsTrue() throws Exception {
		ElectionPath electionPath = ElectionPath.from(ELECTION_PATH_DEFAULT);

		when(mvElectionRepository.findFirstByPathAndLevel(electionPath, CONTEST)).thenReturn(mvElection(contest(mvArea(BOROUGH_PATH_DEFAULT))));

		assertThat(service.isElectionOnBoroughLevel(electionPath)).isTrue();
	}

	@Test
	public void isElectionOnBoroughLevel_givenNotBoroughElection_returnsFalse() throws Exception {
		ElectionPath electionPath = ElectionPath.from(ELECTION_PATH_DEFAULT);

		when(mvElectionRepository.findFirstByPathAndLevel(electionPath, CONTEST)).thenReturn(mvElection(contest(mvArea(MUNICIPALITY_PATH_DEFAULT))));

		assertThat(service.isElectionOnBoroughLevel(electionPath)).isFalse();
	}
}
