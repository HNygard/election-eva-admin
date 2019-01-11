package no.valg.eva.admin.counting.domain.service;

import java.util.List;

import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;

import org.joda.time.LocalDate;

public abstract class BaseCountService {

	protected static final int BLANK_MODIFIED_COUNT_ZERO = 0;
	// services

	// repositories
	@Inject
	protected ReportingUnitRepository reportingUnitRepository;
	@Inject
	protected ReportCountCategoryRepository reportCountCategoryRepository;
	@Inject
	protected VotingRepository votingRepository;
	@Inject
	protected ManualContestVotingRepository manualContestVotingRepository;

	protected BaseCountService() {
	}

	protected BaseCountService(
			final ReportingUnitRepository reportingUnitRepository,
			final ReportCountCategoryRepository reportCountCategoryRepository,
			final VotingRepository votingRepository,
			final ManualContestVotingRepository manualContestVotingRepository) {

		this.reportingUnitRepository = reportingUnitRepository;
		this.reportCountCategoryRepository = reportCountCategoryRepository;
		this.votingRepository = votingRepository;
		this.manualContestVotingRepository = manualContestVotingRepository;
	}

	protected ReportCountCategory getReportCountCategory(final Municipality municipality, final ElectionGroup electionGroupPk, final CountCategory voteCountCategory) {
		return reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, electionGroupPk, voteCountCategory);
	}

	public List<ManualContestVoting> updateManualContestVotings(
			UserData userData,
			List<DailyMarkOffCount> dailyMarkOffCounts,
			Contest contest,
			MvArea mvArea) {

		List<ManualContestVoting> manualContestVotings = manualContestVotingRepository.findForVoByContestAndArea(contest.getPk(), mvArea.getPk());
		for (final ManualContestVoting manualContestVoting : manualContestVotings) {
			LocalDate electionDayDate = manualContestVoting.getElectionDay().getDate();
			for (final DailyMarkOffCount dailyMarkOffCount : dailyMarkOffCounts) {
				if (dailyMarkOffCount.getDate().equals(electionDayDate)) {
					manualContestVoting.setVotings(dailyMarkOffCount.getMarkOffCount());
					break;
				}
			}
		}
		return manualContestVotingRepository.updateMany(userData, manualContestVotings);
	}

	protected ContestReport initContestReport(final Contest contest, final ReportingUnit reportingUnit) {
		ContestReport contestReport = new ContestReport();
		contestReport.setReportingUnit(reportingUnit);
		contestReport.setContest(contest);
		return contestReport;
	}
}
