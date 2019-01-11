package no.valg.eva.admin.configuration.test.repository;

import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

public final class MvElectionRepositoryMockUtil {
	private MvElectionRepositoryMockUtil() {
	}

	public static void findSingleDigestByPath(MvElectionRepository mvElectionRepository, ElectionPath electionPath, MvElectionDigest returnValue) {
		when(mvElectionRepository.findSingleDigestByPath(electionPath)).thenReturn(returnValue);
	}

	public static void findDigestsByPathAndLevel(
			MvElectionRepository mvElectionRepository, ElectionPath electionPath, ElectionLevelEnum level, List<MvElectionDigest> returnValue) {
		when(mvElectionRepository.findDigestsByPathAndLevel(electionPath, level)).thenReturn(returnValue);
	}

	public static void findDigestByElectionPathAndAreaPath(
			MvElectionRepository mvElectionRepository, ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, List<MvElectionDigest> returnValue) {
		when(mvElectionRepository.findDigestByElectionPathAndAreaPath(valghierarkiSti, valggeografiSti)).thenReturn(returnValue);
	}

	public static void matcherValghierarkiStiOgValggeografiSti(
			MvElectionRepository repository, ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, boolean returnValue) {
		when(repository.matcherValghierarkiStiOgValggeografiSti(valghierarkiSti, valggeografiSti)).thenReturn(returnValue);
	}

	public static void hasContestsForElectionAndArea(MvElectionRepository repository, ElectionPath electionPath, AreaPath areaPath, boolean returnValue) {
		when(repository.hasContestsForElectionAndArea(electionPath, areaPath)).thenReturn(returnValue);
	}
}
