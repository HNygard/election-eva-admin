package no.valg.eva.admin.settlement.application;

import no.evote.constants.EvoteConstants;
import no.evote.dto.CandidateVoteCountDto;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.CreateSettlementAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.DeleteSettlementAuditEvent;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.Count;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.settlement.model.AffiliationVoteCount;
import no.valg.eva.admin.common.settlement.model.BallotCount;
import no.valg.eva.admin.common.settlement.model.BallotCountSummary;
import no.valg.eva.admin.common.settlement.model.BallotInfo;
import no.valg.eva.admin.common.settlement.model.CandidateSeat;
import no.valg.eva.admin.common.settlement.model.CountingArea;
import no.valg.eva.admin.common.settlement.model.SettlementStatus;
import no.valg.eva.admin.common.settlement.model.SettlementSummary;
import no.valg.eva.admin.common.settlement.model.SimpleBallotCount;
import no.valg.eva.admin.common.settlement.model.SplitBallotCount;
import no.valg.eva.admin.common.settlement.service.SettlementService;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.settlement.CountCategoryDomainService;
import no.valg.eva.admin.counting.domain.service.settlement.CountingAreaDomainService;
import no.valg.eva.admin.counting.domain.service.votecount.FindCountService;
import no.valg.eva.admin.settlement.application.mapper.AffiliationVoteCountMapper;
import no.valg.eva.admin.settlement.application.mapper.CandidateSeatMapper;
import no.valg.eva.admin.settlement.domain.OppgjorStatusendringTrigger;
import no.valg.eva.admin.settlement.domain.SettlementDomainService;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.repository.SettlementRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.NEW;
import static no.valg.eva.admin.common.counting.model.CountStatus.REVOKED;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Valgoppgjør;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Kommunens_Tall;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgoppgjør_Gjennomføre;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgoppgjør_Se;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "SettlementService")
@Default
@Remote(SettlementService.class)
public class SettlementApplicationService implements SettlementService {
    private static final String ERROR_MSG_USER_NOT_AT_COUNTY_LEVEL = "Operator is not at county level.";
    private static final String ERROR_MSG_USER_NOT_MUNICIPALITY_USER = "Operator is not at municipality level.";
    private static final String ERROR_MSG_USER_NOT_ACCESS_TO_CONTEST_AREA = "Operator with area path %s has not access to contest area with path %s.";

    @Inject
    private FindCountService findCountService;
    @Inject
    private MvElectionRepository mvElectionRepository;
    @Inject
    private VoteCountService voteCountService;
    @Inject
    private ContestRepository contestRepository;
    @Inject
    private SettlementDomainService settlementDomainService;
    @Inject
    private SettlementRepository settlementRepository;
    @Inject
    private CountCategoryDomainService countCategoryDomainService;
    @Inject
    private CountingAreaDomainService countingAreaDomainService;
    @Inject
    private AffiliationVoteCountMapper affiliationVoteCountMapper;
    @Inject
    private CandidateSeatMapper candidateSeatMapper;
    @Inject
    private OppgjorStatusendringTrigger oppgjorStatusendringTrigger;

    public SettlementApplicationService() {
        // CDI
    }

    public SettlementApplicationService(
            FindCountService findCountService, MvElectionRepository mvElectionRepository, VoteCountService voteCountService,
            ContestRepository contestRepository, SettlementDomainService settlementDomainService, SettlementRepository settlementRepository,
            CountCategoryDomainService countCategoryDomainService, CountingAreaDomainService countingAreaDomainService,
            AffiliationVoteCountMapper affiliationVoteCountMapper, CandidateSeatMapper candidateSeatMapper, OppgjorStatusendringTrigger oppgjorStatusendringTrigger) {
        this.findCountService = findCountService;
        this.mvElectionRepository = mvElectionRepository;
        this.voteCountService = voteCountService;
        this.contestRepository = contestRepository;
        this.settlementDomainService = settlementDomainService;
        this.settlementRepository = settlementRepository;
        this.countCategoryDomainService = countCategoryDomainService;
        this.countingAreaDomainService = countingAreaDomainService;
        this.affiliationVoteCountMapper = affiliationVoteCountMapper;
        this.candidateSeatMapper = candidateSeatMapper;
        this.oppgjorStatusendringTrigger = oppgjorStatusendringTrigger;
    }

    @Override
    @Security(accesses = Opptelling_Valgoppgjør_Gjennomføre, type = WRITE)
    @AuditLog(eventClass = CreateSettlementAuditEvent.class, eventType = AuditEventTypes.Create)
    public void createSettlement(UserData userData, ElectionPath contestPath) {
        Contest contest = contestRepository.findSingleByPath(contestPath);
        settlementDomainService.createSettlement(userData, contest);
    }

    @Override
    @Security(accesses = Beskyttet_Slett_Valgoppgjør, type = WRITE)
    @AuditLog(eventClass = DeleteSettlementAuditEvent.class, eventType = AuditEventTypes.DeletedAllInArea)
    public void deleteSettlements(UserData userData, ElectionPath electionPath, AreaPath areaPath) {
        settlementRepository.deleteSettlements(electionPath, areaPath);
        oppgjorStatusendringTrigger.fireEventForStatusendring(electionPath, areaPath);
    }

    @Override
    @Security(accesses = Aggregert_Opptelling, type = READ)
    public boolean hasSettlementForContest(UserData userData, ElectionPath contestPath) {
        Contest contest = contestRepository.findSingleByPath(contestPath);
        return settlementRepository.findSettlementByContest(contest.getPk()) != null;
    }

    @Override
    @Security(accesses = Opptelling_Valgoppgjør_Se, type = READ)
    public Map<CountCategory, SettlementStatus> settlementStatusMap(UserData userData, ElectionPath contestPath) {
        MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
        AreaPath contestAreaPath = contestMvElection.contestAreaPath(); // brukes for validering
        verifyThatUserHasAccessToSettlementStatusForGivenContestAndArea(userData.getOperatorAreaPath(), contestPath, contestAreaPath);
        return settlementStatusMap(userData.getOperatorAreaPath(), contestMvElection);
    }

    private void verifyThatUserHasAccessToSettlementStatusForGivenContestAndArea(AreaPath operatorAreaPath, ElectionPath contestPath,
                                                                                 AreaPath contestAreaPath) {
        contestPath.assertContestLevel();
        verifyOperatorAreaLevelForSettlementStatus(operatorAreaPath, contestAreaPath);
        verifyContestAreaWithinOperatorArea(operatorAreaPath, contestAreaPath);
    }

    private void verifyOperatorAreaLevelForSettlementStatus(AreaPath operatorAreaPath, AreaPath contestAreaPath) {
        if (operatorAreaPath.isRootLevel()) {
            return;
        }
        if (contestAreaPath.isCountyLevel()) {
            verifyOperatorAreaLevelAtCountyLevel(operatorAreaPath);
            return;
        }
        verifyOperatorAreaLevelAtMunicipalityLevel(operatorAreaPath);
    }

    private void verifyOperatorAreaLevelAtCountyLevel(AreaPath operatorAreaPath) {
        if (!operatorAreaPath.isCountyLevel()) {
            throw new IllegalArgumentException(ERROR_MSG_USER_NOT_AT_COUNTY_LEVEL);
        }
    }

    private void verifyOperatorAreaLevelAtMunicipalityLevel(AreaPath operatorAreaPath) {
        if (!operatorAreaPath.isMunicipalityLevel()) {
            throw new IllegalArgumentException(ERROR_MSG_USER_NOT_MUNICIPALITY_USER);
        }
    }

    private void verifyContestAreaWithinOperatorArea(AreaPath operatorAreaPath, AreaPath contestAreaPath) {
        if (!contestAreaPath.isSubpathOf(operatorAreaPath)) {
            throw new IllegalArgumentException(format(ERROR_MSG_USER_NOT_ACCESS_TO_CONTEST_AREA, operatorAreaPath.toString(), contestAreaPath.toString()));
        }
    }

    private Map<CountCategory, SettlementStatus> settlementStatusMap(AreaPath operatorAreaPath, MvElection contestMvElection) {
        Map<CountCategory, SettlementStatus> settlementStatusMap = new HashMap<>();
        List<CountCategory> countCategories = countCategoryDomainService.countCategories(contestMvElection.getContest());
        for (CountCategory countCategory : countCategories) {
            settlementStatusMap.put(countCategory, settlementStatus(operatorAreaPath, contestMvElection, countCategory));
        }
        return settlementStatusMap;
    }

    private SettlementStatus settlementStatus(AreaPath operatorAreaPath, MvElection contestMvElection, CountCategory category) {
        Municipality municipality = contestMvElection.contestMvArea().getMunicipality();
        List<CountingArea> countingAreas = countingAreasForSettlementStatus(operatorAreaPath, contestMvElection, category);
        if (municipality != null && contestMvElection.getContest().isSingleArea()) {
            CountingMode countingMode = voteCountService.countingMode(category, municipality, contestMvElection);
            return new SettlementStatus(category, countingMode, countingAreas);
        } else {
            return new SettlementStatus(category, countingAreas);
        }
    }

    private List<CountingArea> countingAreasForSettlementStatus(
            final AreaPath operatorAreaPath, final MvElection contestMvElection, final CountCategory category) {
        return countingAreaDomainService.countingMvAreas(contestMvElection, category)
                .stream()
                .map(countingMvArea -> new CountingArea(countingMvArea,
                        resolveFinalCountStatus(finalCountsForSettlementStatus(operatorAreaPath, contestMvElection, countingMvArea, category))))
                .collect(Collectors.toList());
    }

    private List<FinalCount> finalCountsForSettlementStatus(AreaPath operatorAreaPath, MvElection contestMvElection, MvArea countingMvArea,
                                                            CountCategory category) {
        if (contestMvElection.getContest().isOnCountyLevel() || !contestMvElection.getContest().isSingleArea()) {
            return findCountService.findCountyFinalCounts(operatorAreaPath, contestMvElection, countingMvArea, category);
        }
        return findCountService.findMunicipalityFinalCounts(operatorAreaPath, contestMvElection, countingMvArea, category);
    }

    private CountStatus resolveFinalCountStatus(List<FinalCount> finalCounts) {
        Count finalCount = findFirstFinalCountByStatus(finalCounts, TO_SETTLEMENT, APPROVED);
        if (finalCount == null) {
            finalCount = findFirstFinalCountByStatus(finalCounts, SAVED, REVOKED);
        }
        return finalCount != null ? finalCount.getStatus() : NEW;
    }

    private Count findFirstFinalCountByStatus(List<FinalCount> finalBoroughCounts, CountStatus firstCountStatus, CountStatus... restCountStatuses) {
        EnumSet<CountStatus> countStatuses = EnumSet.of(firstCountStatus, restCountStatuses);
        for (Count count : finalBoroughCounts) {
            if (countStatuses.contains(count.getStatus())) {
                return count;
            }
        }
        return null;
    }

    @Override
    @Security(accesses = {Opptelling_Valgoppgjør_Se, Opptelling_Kommunens_Tall}, type = READ)
    public SettlementSummary settlementSummary(UserData userData, ElectionPath contestPath) {
        MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
        AreaPath operatorAreaPath = userData.getOperatorAreaPath();
        AreaPath contestAreaPath = contestMvElection.contestAreaPath(operatorAreaPath);
        verifyThatUserHasAccessToSettlementSummaryForGivenContestAndArea(userData, operatorAreaPath, contestPath, contestAreaPath);
        List<? extends Count> counts = countsForSettlementSummary(operatorAreaPath, contestPath, contestAreaPath, contestMvElection);
        List<CountCategory> countCategories = countCategoryDomainService.countCategories(contestMvElection.getContest());
        return settlementSummary(countCategories, counts);
    }

    private void verifyThatUserHasAccessToSettlementSummaryForGivenContestAndArea(UserData userData, AreaPath operatorAreaPath, ElectionPath contestPath,
                                                                                  AreaPath contestAreaPath) {
        contestPath.assertContestLevel();
        verifyOperatorAreaLevelForSettlementSummary(operatorAreaPath);
        if (!userData.isOpptellingsvalgstyret()) {
            verifyOperatorAreaWithinContestArea(operatorAreaPath, contestAreaPath);
        }
    }

    private List<? extends Count> countsForSettlementSummary(AreaPath operatorAreaPath, ElectionPath contestPath, AreaPath contestAreaPath,
                                                             MvElection contestMvElection) {
        boolean singleArea = contestMvElection.getElection().isSingleArea();
        if (singleArea && (contestAreaPath.isMunicipalityLevel() || contestAreaPath.isBoroughLevel())) {
            return findCountService.findMunicipalityCountsByStatus(operatorAreaPath, contestPath, TO_SETTLEMENT);
        }
        if (!contestAreaPath.isSamiValgkretsPath() && !singleArea || contestAreaPath.isCountyLevel() && operatorAreaPath.isMunicipalityLevel()) {
            return findCountService.findMunicipalityCountsByStatus(operatorAreaPath, contestPath, APPROVED);
        }
        return findCountService.findCountyFinalCountsByStatus(operatorAreaPath, contestPath, TO_SETTLEMENT, contestMvElection);
    }

    private SettlementSummary settlementSummary(List<CountCategory> countCategories, List<? extends Count> counts) {
        List<BallotCountSummary<SplitBallotCount>> ordinaryBallotCountSummaries = summarizeOrdinaryBallotCounts(counts);
        BallotCountSummary<SimpleBallotCount> blankBallotCountSummary = summarizeBlankBallotCounts(counts);
        List<BallotCountSummary<SimpleBallotCount>> rejectedBallotCountSummaries = summarizeRejectedBallotCounts(counts);
        return new SettlementSummary(countCategories, ordinaryBallotCountSummaries, blankBallotCountSummary, rejectedBallotCountSummaries);
    }

    private void verifyOperatorAreaLevelForSettlementSummary(AreaPath operatorAreaPath) {
        if (operatorAreaPath.isRootLevel() || operatorAreaPath.isCountyLevel()) {
            return;
        }
        verifyOperatorAreaLevelAtMunicipalityLevel(operatorAreaPath);
    }

    private void verifyOperatorAreaWithinContestArea(AreaPath operatorAreaPath, AreaPath contestAreaPath) {
        if (!operatorAreaPath.isSubpathOf(contestAreaPath) && !(contestAreaPath.isBoroughLevel() && operatorAreaPath.isMunicipalityLevel())) {
            throw new IllegalArgumentException(format(ERROR_MSG_USER_NOT_ACCESS_TO_CONTEST_AREA, operatorAreaPath.toString(), contestAreaPath.toString()));
        }
    }

    private List<BallotCountSummary<SplitBallotCount>> summarizeOrdinaryBallotCounts(List<? extends Count> counts) {
        Map<BallotInfo, BallotCountSummary<SplitBallotCount>> ordinaryBallotCountSummaryMap = new LinkedHashMap<>();
        for (Count count : counts) {
            CountCategory countCategory = count.getCategory();
            List<no.valg.eva.admin.common.counting.model.BallotCount> ballotCounts = count.getBallotCounts();
            for (no.valg.eva.admin.common.counting.model.BallotCount ballotCount : ballotCounts) {
                BallotInfo ballotInfo = new BallotInfo(ballotCount.getId(), ballotCount.getName());
                int modifiedBallotCount = ballotCount.getModifiedCount();
                int unmodifiedBallotCount = ballotCount.getUnmodifiedCount();
                BallotCountSummary<SplitBallotCount> ordinaryBallotCountSummary = getOrCreateBallotCountSummary(ordinaryBallotCountSummaryMap, ballotInfo);
                updateSplitBallotCountSummary(ordinaryBallotCountSummary, countCategory, modifiedBallotCount, unmodifiedBallotCount);
            }
        }
        return buildArrayListFromMapValues(ordinaryBallotCountSummaryMap);
    }

    private BallotCountSummary<SimpleBallotCount> summarizeBlankBallotCounts(List<? extends Count> counts) {
        BallotCountSummary<SimpleBallotCount> blankBallotCountSummary = new BallotCountSummary<>(
                new BallotInfo(EvoteConstants.BALLOT_BLANK, "@party[BLANK].name"));
        for (Count count : counts) {
            CountCategory countCategory = count.getCategory();
            updateSimpleBallotCountSummary(blankBallotCountSummary, countCategory, count.getBlankBallotCount());
        }
        return blankBallotCountSummary;
    }

    private List<BallotCountSummary<SimpleBallotCount>> summarizeRejectedBallotCounts(List<? extends Count> counts) {
        Map<BallotInfo, BallotCountSummary<SimpleBallotCount>> rejectedBallotCountSummaryMap = new LinkedHashMap<>();
        for (Count count : counts) {
            CountCategory countCategory = count.getCategory();
            List<RejectedBallotCount> rejectedBallotCounts = count.getRejectedBallotCounts();
            for (RejectedBallotCount rejectedBallotCount : rejectedBallotCounts) {
                BallotInfo ballotInfo = new BallotInfo(rejectedBallotCount.getId(), rejectedBallotCount.getName());
                BallotCountSummary<SimpleBallotCount> rejectedBallotCountSummary = getOrCreateBallotCountSummary(rejectedBallotCountSummaryMap, ballotInfo);
                updateSimpleBallotCountSummary(rejectedBallotCountSummary, countCategory, rejectedBallotCount.getCount());
            }
        }
        return buildArrayListFromMapValues(rejectedBallotCountSummaryMap);
    }

    private void updateSplitBallotCountSummary(BallotCountSummary<SplitBallotCount> ballotCountSummary, CountCategory countCategory, int modifiedBallotCount,
                                               int unmodifiedBallotCount) {
        SplitBallotCount ballotCountForBallotCountSummary = ballotCountSummary.getBallotCount(countCategory);
        if (ballotCountForBallotCountSummary == null) {
            ballotCountForBallotCountSummary = new SplitBallotCount(countCategory, modifiedBallotCount, unmodifiedBallotCount);
            ballotCountSummary.addBallotCount(ballotCountForBallotCountSummary);
        } else {
            int summarizedModifiedBallotCount = ballotCountForBallotCountSummary.getModifiedBallotCount() + modifiedBallotCount;
            ballotCountForBallotCountSummary.setModifiedBallotCount(summarizedModifiedBallotCount);
            int summarizedUnmodifiedBallotCount = ballotCountForBallotCountSummary.getUnmodifiedBallotCount() + unmodifiedBallotCount;
            ballotCountForBallotCountSummary.setUnmodifiedBallotCount(summarizedUnmodifiedBallotCount);
        }
    }

    private void updateSimpleBallotCountSummary(BallotCountSummary<SimpleBallotCount> ballotCountSummary, CountCategory countCategory, int ballotCount) {
        SimpleBallotCount ballotCountForBallotCountSummary = ballotCountSummary.getBallotCount(countCategory);
        if (ballotCountForBallotCountSummary == null) {
            ballotCountForBallotCountSummary = new SimpleBallotCount(countCategory, ballotCount);
            ballotCountSummary.addBallotCount(ballotCountForBallotCountSummary);
        } else {
            int summarizedBallotCount = ballotCountForBallotCountSummary.getBallotCount() + ballotCount;
            ballotCountForBallotCountSummary.setBallotCount(summarizedBallotCount);
        }
    }

    private <T extends BallotCount> BallotCountSummary<T> getOrCreateBallotCountSummary(Map<BallotInfo, BallotCountSummary<T>> ballotCountSummaryMap,
                                                                                        BallotInfo ballotInfo) {
        BallotCountSummary<T> ordinaryBallotCountSummary = ballotCountSummaryMap.get(ballotInfo);
        if (ordinaryBallotCountSummary == null) {
            ordinaryBallotCountSummary = new BallotCountSummary<>(ballotInfo);
            ballotCountSummaryMap.put(ballotInfo, ordinaryBallotCountSummary);
        }
        return ordinaryBallotCountSummary;
    }

    private <T> List<T> buildArrayListFromMapValues(Map<?, T> map) {
        return new ArrayList<>(map.values());
    }

    @Override
    @Security(accesses = Opptelling_Valgoppgjør_Se, type = READ)
    public List<AffiliationVoteCount> findAffiliationVoteCountsBySettlement(UserData userData, ElectionPath contestPath) {
        return affiliationVoteCountMapper.affiliationVoteCounts(settlementRepository.findAffiliationVoteCountsBySettlement(settlement(contestPath)));
    }

    @Override
    @Security(accesses = Opptelling_Valgoppgjør_Se, type = READ)
    public List<Integer> findMandatesBySettlement(UserData userData, ElectionPath contestPath) {
        return settlementRepository.findMandatesBySettlement(settlement(contestPath));
    }

    @Override
    @Security(accesses = Opptelling_Valgoppgjør_Se, type = READ)
    public Map<Long, List<CandidateVoteCountDto>> findCandidateVoteCountsBySettlement(UserData userData, ElectionPath contestPath) {
        return settlementRepository.findCandidateVoteCountsBySettlement(settlement(contestPath));
    }

    @Override
    @Security(accesses = Opptelling_Valgoppgjør_Se, type = READ)
    public List<CandidateSeat> findAffiliationCandidateSeatsBySettlement(UserData userData, ElectionPath contestPath) {
        return candidateSeatMapper.candidateSeats(settlementRepository.findAffiliationCandidateSeatsBySettlement(settlement(contestPath)));
    }

    private Settlement settlement(ElectionPath contestPath) {
        Contest contest = contestRepository.findSingleByPath(contestPath);
        return settlementRepository.findSettlementByContest(contest);
    }
}
