package no.valg.eva.admin.counting.domain.service.votecount;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.builder.PreliminaryCountBuilder;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.ElectionDayRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;

@Default
@ApplicationScoped
public class FindPreliminaryCountService {
	@Inject
	private VoteCountService voteCountService;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private ElectionDayRepository electionDayRepository;
	@Inject
	private ManualContestVotingRepository manualContestVotingRepository;
	@Inject
	private VotingRepository votingRepository;
	@Inject
	private ReportingUnitDomainService reportingUnitDomainService;
	@Inject
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;

	public FindPreliminaryCountService() {

	}
	public FindPreliminaryCountService(VoteCountService voteCountService, ReportingUnitRepository reportingUnitRepository,
			AffiliationRepository affiliationRepository, ElectionDayRepository electionDayRepository,
			ManualContestVotingRepository manualContestVotingRepository, VotingRepository votingRepository,
			ReportingUnitDomainService reportingUnitDomainService, AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService) {
		this.voteCountService = voteCountService;
		this.reportingUnitRepository = reportingUnitRepository;
		this.affiliationRepository = affiliationRepository;
		this.electionDayRepository = electionDayRepository;
		this.manualContestVotingRepository = manualContestVotingRepository;
		this.votingRepository = votingRepository;
		this.reportingUnitDomainService = reportingUnitDomainService;
		this.antallStemmesedlerLagtTilSideDomainService = antallStemmesedlerLagtTilSideDomainService;
	}

	/**
	 * Finds preliminary count for area.
	 */
	public PreliminaryCount findPreliminaryCount(AreaPath operatorAreaPath, CountContext context, MvArea countingMvArea, MvElection contestMvElection) {
		ReportingUnit reportingUnit = findReportingUnit(operatorAreaPath, context, contestMvElection, countingMvArea);
		return preliminaryCount(reportingUnit, context, contestMvElection, countingMvArea);
	}

	private PreliminaryCount preliminaryCount(
			ReportingUnit reportingUnit, CountContext context, MvElection contestMvElection, MvArea countingMvArea) {

		CountCategory category = context.getCategory();
		Contest contest = contestMvElection.getContest();
		Municipality municipality = countingMvArea.getMunicipality();
		VoteCount preliminaryVoteCount = voteCountService.findVoteCount(reportingUnit, context, countingMvArea, contestMvElection, PRELIMINARY);
		boolean manualCount = preliminaryVoteCount == null || preliminaryVoteCount.isManualCount();

		PreliminaryCountBuilder builder = PreliminaryCountBuilder.create(
				category,
				new AreaPath(countingMvArea.getAreaPath()),
				countingMvArea.getAreaName(),
				reportingUnit.getNameLine(),
				manualCount,
				municipality.isElectronicMarkoffs(),
				municipality.isRequiredProtocolCount());
		builder.applyDataProvider(new PreliminaryCountDataProvider(affiliationRepository, voteCountService, reportingUnit, preliminaryVoteCount,
				context, contestMvElection, countingMvArea, antallStemmesedlerLagtTilSideDomainService));

		if (applyDailyMarkoff(category, countingMvArea)) {
			if (municipality.isElectronicMarkoffs()) {
				builder.applyElectionDays(getElectionDays(countingMvArea));
				Collection<Voting> votings = votingRepository.findApprovedVotingsByPollingDistrictAndCategories(countingMvArea.getPollingDistrict(),
						new VotingCategory[] { VotingCategory.VO, VotingCategory.VF });
				builder.applyVotings(votings);
			} else if (preliminaryVoteCount == null) {
				builder.applyElectionDays(getElectionDays(countingMvArea)).build();
			} else {
				builder.applyManualContestVotings(manualContestVotingRepository.findForVoByContestAndArea(contest.getPk(), countingMvArea.getPk()));
			}
		}
		return builder.build();
	}

	private ReportingUnit findReportingUnit(AreaPath operatorAreaPath, CountContext context, MvElection contestMvElection, MvArea countingMvArea) {
		ReportingUnitTypeId typeId = voteCountService.reportingUnitTypeForPreliminaryCount(context, countingMvArea.getMunicipality(), contestMvElection);
		AreaPath areaPathForReportingUnit = reportingUnitDomainService.areaPathForFindingReportingUnit(typeId, operatorAreaPath, countingMvArea);
		return reportingUnitRepository.findByAreaPathAndType(areaPathForReportingUnit, typeId);
	}

	private boolean applyDailyMarkoff(CountCategory category, MvArea mvArea) {
		return category == CountCategory.VO && !mvArea.getMunicipality().isRequiredProtocolCount();
	}

	private List<ElectionDay> getElectionDays(MvArea mvArea) {
		if (mvArea.getPollingDistrict().isMunicipality()) {
			// findForMunicipality contains all elections days for municipality. We only want 1 pr day.
			final Set<LocalDate> seen = new HashSet<>();
			return newArrayList(electionDayRepository.findForMunicipality(mvArea.getMunicipality().getPk()).stream().filter(day -> {
				if (seen.contains(day.getDate())) {
					return false;
				}
				seen.add(day.getDate());
				return true;
			}).collect(Collectors.toList()));
		} else {
			return electionDayRepository.findForPollingDistrict(mvArea.getPollingDistrict().getPk());
		}
	}

}
