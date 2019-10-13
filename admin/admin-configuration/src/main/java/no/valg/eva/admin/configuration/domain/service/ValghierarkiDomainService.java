package no.valg.eva.admin.configuration.domain.service;

import static java.util.stream.Collectors.toList;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.configuration.domain.model.filter.MvElectionDigestFilter.nodeHarMinstEttValgdistriktForValggeografi;
import static no.valg.eva.admin.configuration.domain.model.filter.MvElectionDigestFilter.valggeografiNivaaErValghendelseEllerNodeHarValggeografiNivaa;
import static no.valg.eva.admin.configuration.domain.model.mapper.Mapper.map;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.configuration.domain.model.factory.ElectionFilterFactory;
import no.valg.eva.admin.configuration.domain.model.filter.ElectionFilterEnum;
import no.valg.eva.admin.configuration.domain.model.mapper.valghierarki.ValgMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valghierarki.ValgdistriktMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valghierarki.ValggruppeMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valghierarki.ValghendelseMapper;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import no.valg.eva.admin.felles.valghierarki.model.Valghendelse;

@Default
@ApplicationScoped
public class ValghierarkiDomainService {
	@Inject
	private MvElectionRepository mvElectionRepository;

	public ValghierarkiDomainService() {

	}

	public ValghierarkiDomainService(MvElectionRepository mvElectionRepository) {
		this.mvElectionRepository = mvElectionRepository;
	}

	public List<MvElection> getElectionGroupsFor(ElectionPath electionEventPath) {
		return mvElectionRepository.findByPathAndLevel(electionEventPath, ELECTION_GROUP);
	}

	public List<MvElection> getElectionsFor(UserData userData, CountCategory countCategory, ElectionPath electionGroupPath, Process process) {
		final AreaPath operatorAreaPath = AreaPath.from(userData.getOperatorMvArea().getAreaPath());
		List<MvElection> mvElections = mvElectionRepository.findByPathAndLevel(electionGroupPath, ELECTION);

		final Optional<ElectionFilterEnum> electionFilter = ElectionFilterFactory.build(userData, countCategory, process);

		return mvElections.stream()
				.filter(mvElection -> {
					ElectionPath electionPath = ElectionPath.from(mvElection.getElectionPath());
					boolean hasContestsForElectionAndOperatorArea = mvElectionRepository.hasContestsForElectionAndArea(electionPath, operatorAreaPath);
					return electionFilter
							.map(mvElectionFilter -> hasContestsForElectionAndOperatorArea && mvElectionFilter.test(mvElection))
							.orElse(hasContestsForElectionAndOperatorArea);
				})
				.collect(toList());
	}

	public List<MvElection> getContestsFor(UserData userData, ElectionPath electionPath) {
		return mvElectionRepository.findContestsForElectionAndArea(electionPath, userData.getOperatorAreaPath());
	}

	public boolean isElectionOnBoroughLevel(ElectionPath electionPath) {
		MvElection contestMvElection = mvElectionRepository.findFirstByPathAndLevel(electionPath, CONTEST);
		// if first contest for election path is on borough level then election is on borough level
		return contestMvElection.getContest().isOnBoroughLevel();
	}

	public Valghendelse valghendelse(ValghendelseSti valghendelseSti) {
		MvElectionDigest mvElectionDigest = mvElectionRepository.findSingleDigestByPath(valghendelseSti.electionPath());
		return ValghendelseMapper.valghendelse(mvElectionDigest);
	}

	public Valggruppe valggruppe(ValggruppeSti valggruppeSti) {
		MvElectionDigest mvElectionDigest = mvElectionRepository.findSingleDigestByPath(valggruppeSti.electionPath());
		return ValggruppeMapper.valggruppe(mvElectionDigest);
	}

	public List<Valggruppe> valggrupper(ValghendelseSti valghendelseSti) {
		List<MvElectionDigest> mvElectionDigests = mvElectionRepository.findDigestsByPathAndLevel(valghendelseSti.electionPath(), ELECTION_GROUP);
		return mvElectionDigests.stream().map(ValggruppeMapper::valggruppe).collect(toList());
	}

	public Valg valg(ValgSti valgSti) {
		MvElectionDigest mvElectionDigest = mvElectionRepository.findSingleDigestByPath(valgSti.electionPath());
		return ValgMapper.valg(mvElectionDigest);
	}

	@SuppressWarnings("unchecked")
	public List<Valg> valg(ValggruppeSti valggruppeSti, ValggeografiSti operatorValggeografiSti,
						   boolean valggeografiNivaaErValghendelseEllerNodeHarValggeografiNivaa, CountCategory countCategory) {
		List<MvElectionDigest> mvElectionDigests = mvElectionRepository.findDigestsByPathAndLevel(valggruppeSti.electionPath(), ELECTION);
		List<Predicate<MvElectionDigest>> filtre = new ArrayList<>();
		filtre.add(nodeHarMinstEttValgdistriktForValggeografi(mvElectionRepository, operatorValggeografiSti));
		if (valggeografiNivaaErValghendelseEllerNodeHarValggeografiNivaa) {
			filtre.add(valggeografiNivaaErValghendelseEllerNodeHarValggeografiNivaa(mvElectionRepository, operatorValggeografiSti.nivaa()));
		}
		if (countCategory == BF) {
			filtre.add(m -> m.valggeografiNivaa() == BYDEL);
		}
		return map(mvElectionDigests, ValgMapper::valg, filtre.toArray(new Predicate[filtre.size()]));
	}

	public Valgdistrikt valgdistrikt(ValgdistriktSti valgdistriktSti) {
		MvElectionDigest mvElectionDigest = mvElectionRepository.findSingleDigestByPath(valgdistriktSti.electionPath());
		return ValgdistriktMapper.valgdistrikt(mvElectionDigest);
	}

	public List<Valgdistrikt> valgdistrikter(ValgSti valgSti, ValggeografiSti operatorValggeografiSti) {
		List<MvElectionDigest> mvElectionDigests = mvElectionRepository.findDigestsByPathAndLevel(valgSti.electionPath(), CONTEST);
		return map(mvElectionDigests, ValgdistriktMapper::valgdistrikt, nodeHarMinstEttValgdistriktForValggeografi(mvElectionRepository, operatorValggeografiSti));
	}

	public List<Valgdistrikt> valgdistrikterFiltrertPaaGeografi(ValgSti valgSti, ValggeografiSti valggeografiSti) {
		List<MvElectionDigest> mvElectionDigests = mvElectionRepository.findDigestByElectionPathAndAreaPath(valgSti, valggeografiSti);
		return map(mvElectionDigests, ValgdistriktMapper::valgdistrikt);
	}
}
