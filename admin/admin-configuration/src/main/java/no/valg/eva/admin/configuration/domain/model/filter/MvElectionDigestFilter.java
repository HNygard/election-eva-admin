package no.valg.eva.admin.configuration.domain.model.filter;

import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;

import java.util.function.Predicate;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

public final class MvElectionDigestFilter {
	private MvElectionDigestFilter() {
	}

	public static Predicate<MvElectionDigest> nodeHarMinstEttValgdistriktForValggeografi(
			MvElectionRepository mvElectionRepository, ValggeografiSti operatorValggeografiSti) {
		if (operatorValggeografiSti != null) {
			return m -> mvElectionRepository.hasContestsForElectionAndArea(m.electionPath(), operatorValggeografiSti.areaPath());
		}
		return m -> true;
	}

	public static Predicate<MvElectionDigest> valggeografiNivaaErValghendelseEllerNodeHarValggeografiNivaa(
			MvElectionRepository mvElectionRepository, ValggeografiNivaa operatorValggeografiNivaa) {
		if (operatorValggeografiNivaa == VALGHENDELSE) {
			return m -> true;
		}
		// Merk! Her er det en spesialhåndtering for bydelsvalg. En operatør på kommune nivå har tilgang til valgene på bydelsnivå.
		return m -> operatorValggeografiNivaa == KOMMUNE && valgHarValgdistriktPaaValgggeografiNivaa(mvElectionRepository, m.electionPath(), BYDEL)
				|| valgHarValgdistriktPaaValgggeografiNivaa(mvElectionRepository, m.electionPath(), operatorValggeografiNivaa);
	}

	private static boolean valgHarValgdistriktPaaValgggeografiNivaa(
			MvElectionRepository mvElectionRepository, ElectionPath electionPath, ValggeografiNivaa valggeografiNivaa) {
		return mvElectionRepository.findByPathAndLevelAndAreaLevel(electionPath, CONTEST, valggeografiNivaa.tilAreaLevelEnum()).size() > 0;
	}
}
