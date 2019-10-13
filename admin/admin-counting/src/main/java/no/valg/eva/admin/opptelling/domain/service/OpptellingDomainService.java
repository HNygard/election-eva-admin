package no.valg.eva.admin.opptelling.domain.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;

@Default
@ApplicationScoped
public class OpptellingDomainService {
	@Inject
	private VoteCountRepository voteCountRepository;
	@Inject
	private VoteCountCategoryRepository voteCountCategoryRepository;

	public void slettOpptellinger(ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti) {
		voteCountRepository.slettOpptellinger(valghierarkiSti, valggeografiSti, null, null);
	}

	public void slettOpptellinger(ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, CountCategory[] countCategories) {
		for (CountCategory countCategory : countCategories) {
			VoteCountCategory voteCountCategory = voteCountCategoryRepository.findByEnum(countCategory);
			voteCountRepository.slettOpptellinger(valghierarkiSti, valggeografiSti, voteCountCategory.getPk().intValue(), null);
		}
	}

	public void slettOpptellinger(ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, Styretype[] styretyper) {
		for (Styretype styretype : styretyper) {
			voteCountRepository.slettOpptellinger(valghierarkiSti, valggeografiSti, null, styretype);
		}
	}

	public void slettOpptellinger(ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, CountCategory[] countCategories, Styretype[] styretyper) {
		for (CountCategory countCategory : countCategories) {
			VoteCountCategory voteCountCategory = voteCountCategoryRepository.findByEnum(countCategory);
			for (Styretype styretype : styretyper) {
				voteCountRepository.slettOpptellinger(valghierarkiSti, valggeografiSti, voteCountCategory.getPk().intValue(), styretype);
			}
		}
	}
}
