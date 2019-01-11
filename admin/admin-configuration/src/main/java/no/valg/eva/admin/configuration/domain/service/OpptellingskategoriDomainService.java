package no.valg.eva.admin.configuration.domain.service;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;

import java.util.List;

import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;

public class OpptellingskategoriDomainService {
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private VoteCountCategoryRepository voteCountCategoryRepository;

	public OpptellingskategoriDomainService() {
		// CDI
	}

	@Inject
	public OpptellingskategoriDomainService(MvElectionRepository mvElectionRepository, VoteCountCategoryRepository voteCountCategoryRepository) {
		this.mvElectionRepository = mvElectionRepository;
		this.voteCountCategoryRepository = voteCountCategoryRepository;
	}

	public List<CountCategory> countCategoriesForValgSti(ValggeografiSti operatorValggeografiSti, ValgSti valgSti) {
		Election election = mvElectionRepository.finnEnkeltMedSti(valgSti).getElection();
		AreaPath operatorAreaPath = operatorValggeografiSti.areaPath();
		List<VoteCountCategory> voteCountCategories = voteCountCategoryRepository.findByElectionAndAreaPath(election, operatorAreaPath);
		return map(voteCountCategories);
	}

	public List<CountCategory> countCategories(ValggeografiSti operatorValggeografiSti) {
		if (operatorValggeografiSti.isStemmekretsSti()) {
			return singletonList(VO);
		}
		ValghendelseSti valghendelseSti = new ValghendelseSti(operatorValggeografiSti.valghendelseSti().valghendelseId());
		AreaPath operatorAreaPath = operatorValggeografiSti.areaPath();
		ElectionEvent electionEvent = mvElectionRepository.finnEnkeltMedSti(valghendelseSti).getElectionEvent();
		List<VoteCountCategory> voteCountCategories = voteCountCategoryRepository.findByElectionEventAndAreaPath(electionEvent, operatorAreaPath);
		return map(voteCountCategories);
	}

	private List<CountCategory> map(List<VoteCountCategory> voteCountCategories) {
		return voteCountCategories.stream()
				.map(VoteCountCategory::getCountCategory)
				.collect(toList());
	}
}
