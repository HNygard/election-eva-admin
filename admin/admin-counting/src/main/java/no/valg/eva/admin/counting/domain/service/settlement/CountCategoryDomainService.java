package no.valg.eva.admin.counting.domain.service.settlement;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;

/**
 * Contains Counting domain logic related to Settlement.
 */
@Default
@ApplicationScoped
public class CountCategoryDomainService {
	@Inject
	private VoteCountCategoryRepository voteCountCategoryRepository;
	@Inject
	private ReportCountCategoryRepository reportCountCategoryRepository;

	public CountCategoryDomainService(VoteCountCategoryRepository voteCountCategoryRepository, ReportCountCategoryRepository reportCountCategoryRepository) {
		this.voteCountCategoryRepository = voteCountCategoryRepository;
		this.reportCountCategoryRepository = reportCountCategoryRepository;
	}
	public CountCategoryDomainService() {

	}

	public List<CountCategory> countCategories(Contest contest) {
		if (contest.isOnCountyLevel() || !contest.isSingleArea()) {
			return countCategories(voteCountCategoryRepository.categoriesForContest(contest), VoteCountCategory::getCountCategory);
		}
		Municipality municipality = contest.getFirstContestArea().getMvArea().getMunicipality();
		return countCategories(voteCountCategoryRepository.categoriesForContestAndMunicipality(contest, municipality), VoteCountCategory::getCountCategory);
	}

	public List<CountCategory> countCategories(Contest contest, Municipality municipality) {
		if (contest.isOnBoroughLevel()) {
			return countCategories(voteCountCategoryRepository.categoriesForContest(contest), VoteCountCategory::getCountCategory);
		}
		return countCategories(reportCountCategoryRepository.findByContestAndMunicipality(contest, municipality), ReportCountCategory::getCountCategory);
	}

	private <T> List<CountCategory> countCategories(List<T> list, Function<T, CountCategory> mapper) {
		return list
				.stream()
				.map(mapper)
				.collect(toList());
	}
}
