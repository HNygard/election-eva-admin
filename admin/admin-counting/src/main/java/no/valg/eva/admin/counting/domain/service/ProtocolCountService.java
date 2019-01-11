package no.valg.eva.admin.counting.domain.service;

import static no.valg.eva.admin.common.voting.VotingCategory.VO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;

import org.joda.time.LocalDate;

/**
 * Domain service for handling vote counts of type protocol count (urnetellinger).
 */
public class ProtocolCountService extends BaseCountService {

	public ProtocolCountService(ReportingUnitRepository reportingUnitRepository, ReportCountCategoryRepository reportCountCategoryRepository,
			VotingRepository votingRepository, ManualContestVotingRepository manualContestVotingRepository) {

		super(reportingUnitRepository, reportCountCategoryRepository, votingRepository, manualContestVotingRepository);
	}

	/**
	 * Creates manual contest votings for area and election days (manuelle kryss i manntall, XiM)
	 */
	public List<ManualContestVoting> createManualXiMs(
			UserData userData, List<DailyMarkOffCount> dailyMarkOffCounts, Contest contest, MvArea mvArea) {
		List<ManualContestVoting> manualXiMs = new ArrayList<>();
		ElectionEvent electionEvent = contest.getElection().getElectionGroup().getElectionEvent();
		Collection<ElectionDay> electionDays = electionEvent.getElectionDays();
		for (final DailyMarkOffCount dailyMarkOffCount : dailyMarkOffCounts) {
			ManualContestVoting manualContestVoting = new ManualContestVoting();
			manualContestVoting.setVotings(dailyMarkOffCount.getMarkOffCount());

			LocalDate electionDayDate = dailyMarkOffCount.getDate();
			ElectionDay selectedElectionDay = null;
			for (final ElectionDay electionDay : electionDays) {
				if (electionDay.getDate().equals(electionDayDate)) {
					selectedElectionDay = electionDay;
					break;
				}
			}
			manualContestVoting.setElectionDay(selectedElectionDay);

			VotingCategory voVotingCategory = votingRepository.findVotingCategoryById(VO.getId());
			manualContestVoting.setVotingCategory(voVotingCategory);

			manualContestVoting.setMvArea(mvArea);
			manualContestVoting.setContest(contest);

			manualXiMs.add(manualContestVoting);
		}

		return manualContestVotingRepository.createMany(userData, manualXiMs);
	}
}
