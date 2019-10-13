package no.valg.eva.admin.counting.domain.service.votecount;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.builder.ProtocolCountBuilder;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.ElectionDayRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;
import no.valg.eva.admin.voting.domain.model.Voting;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Default
@ApplicationScoped
public class FindProtocolCountService {

	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private VoteCountService voteCountService;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private VotingRepository votingRepository;
	@Inject
	private ElectionDayRepository electionDayRepository;
	@Inject
	private ManualContestVotingRepository manualContestVotingRepository;

	public FindProtocolCountService() {

	}

	public FindProtocolCountService(MvAreaRepository mvAreaRepository, VoteCountService voteCountService, ReportingUnitRepository reportingUnitRepository,
			VotingRepository votingRepository, ElectionDayRepository electionDayRepository, ManualContestVotingRepository manualContestVotingRepository) {
		this.mvAreaRepository = mvAreaRepository;
		this.voteCountService = voteCountService;
		this.reportingUnitRepository = reportingUnitRepository;
		this.votingRepository = votingRepository;
		this.electionDayRepository = electionDayRepository;
		this.manualContestVotingRepository = manualContestVotingRepository;
	}

	/**
	 * @return list of protocol counts for an operator, count context, counting area and contest
	 */
	public List<ProtocolCount> findProtocolCounts(AreaPath operatorAreaPath, CountContext context, MvArea countingMvArea, MvElection contestMvElection) {
		AreaPath countingAreaPath = new AreaPath(countingMvArea.getPath());
		Collection<PollingDistrict> pollingDistricts = voteCountService.pollingDistrictsForProtocolCount(context, countingMvArea, contestMvElection,
				operatorAreaPath);

		List<ProtocolCount> protocolCounts = new ArrayList<>();
		for (final PollingDistrict pollingDistrict : pollingDistricts) {
			AreaPath pollingDistrictAreaPath = countingAreaPath.toPollingDistrictSubPath(pollingDistrict.getId());
			MvArea pollingDistrictMvArea = mvAreaRepository.findSingleByPath(pollingDistrictAreaPath);
			ProtocolCount protocolCount = findProtocolCount(pollingDistrictAreaPath, context, pollingDistrictMvArea, contestMvElection);
			protocolCounts.add(protocolCount);
		}
        protocolCounts.sort(Comparator.comparing(pc -> pc.getAreaPath().path()));
		return protocolCounts;
	}

	private ProtocolCount findProtocolCount(AreaPath pollingDistrictAreaPath, CountContext context, MvArea countingMvArea, MvElection contestMvElection) {
		Contest contest = contestMvElection.getContest();
		ReportingUnit reportingUnit = reportingUnitRepository.findByAreaPathAndType(pollingDistrictAreaPath, ReportingUnitTypeId.STEMMESTYRET);
		String reportingUnitName = reportingUnit.getNameLine();

		String areaName = countingMvArea.getAreaName();
		Municipality municipality = countingMvArea.getMunicipality();
		boolean electronicMarkOffs = municipality.isElectronicMarkoffs();
		boolean foreignSpecialCoversEnabled = voteCountService.useForeignSpecialCovers(contestMvElection, municipality);

		VoteCount protocolVoteCount = voteCountService.findVoteCount(reportingUnit, context, countingMvArea, contestMvElection, CountQualifier.PROTOCOL);

        ProtocolCountBuilder builder = ProtocolCountBuilder.create(
				new AreaPath(countingMvArea.getPath()),
				areaName,
				reportingUnitName,
				electronicMarkOffs,
				foreignSpecialCoversEnabled,
				contest.isOnBoroughLevel(),
                isManualCount(protocolVoteCount));
		if (protocolVoteCount != null) {
			builder.applyProtocolVoteCount(protocolVoteCount);
		}
		if (electronicMarkOffs) {
			List<ElectionDay> electionDays = electionDayRepository.findForPollingDistrict(countingMvArea.getPollingDistrict().getPk());
			builder.applyElectionDays(electionDays);
			Collection<Voting> votings = votingRepository.findApprovedVotingsByPollingDistrictAndCategories(countingMvArea.getPollingDistrict(),
					new VotingCategory[] { VotingCategory.VO, VotingCategory.VF });
			if (contest.isOnBoroughLevel()) {
				builder.applyElectionDaysForOtherContests(electionDays);
				Collection<Voting> votingsForThisContest = new ArrayList<>();
				Collection<Voting> votingsForAnotherContest = new ArrayList<>();
				for (Voting voting : votings) {
					Borough votingBorough = voting.getMvArea().getBorough();
					if (votingBorough.equals(countingMvArea.getBorough())) {
						votingsForThisContest.add(voting);
					} else {
						votingsForAnotherContest.add(voting);
					}
				}
				builder.applyVotings(votingsForThisContest);
				builder.applyVotingsForAnotherContest(votingsForAnotherContest);
			} else {
				builder.applyVotings(votings);
			}
        } else if (isIngenPapirmanntallskryss(countingMvArea, contest, protocolVoteCount)) {
			builder.applyElectionDays(electionDayRepository.findForPollingDistrict(countingMvArea.getPollingDistrict().getPk())).build();
		} else {
			builder.applyManualContestVotings(antallPapirmanntallskryss(countingMvArea, contest));
		}

		return builder.build();
	}

    private boolean isManualCount(VoteCount protocolVoteCount) {
        return protocolVoteCount == null || protocolVoteCount.isManualCount();
    }

    private boolean isIngenPapirmanntallskryss(MvArea countingMvArea, Contest contest, VoteCount protocolVoteCount) {
        return protocolVoteCount == null || antallPapirmanntallskryss(countingMvArea, contest).isEmpty();
    }

	private List<ManualContestVoting> antallPapirmanntallskryss(MvArea countingMvArea, Contest contest) {
		return manualContestVotingRepository.findForVoByContestAndArea(contest.getPk(), countingMvArea.getPk());
	}
}
