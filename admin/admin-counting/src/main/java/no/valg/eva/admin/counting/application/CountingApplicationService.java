package no.valg.eva.admin.counting.application;

import static java.lang.String.format;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.evote.constants.EvoteConstants.BALLOT_BLANK;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling_Sett_Til_Valgoppgjør;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forkastelser_Manuelt;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Opphev_Endelig_Telling;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Opphev_Foreløpig_Telling;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.ValidateException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ManualContestVotingsAuditDetails;
import no.valg.eva.admin.common.auditlog.auditevents.ManualVotingAuditDetails;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.Counts;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolAndPreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.common.counting.validator.ApprovePreliminaryCountValidator;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.VoteCountFactory;
import no.valg.eva.admin.counting.domain.auditevents.FinalCountAuditEvent;
import no.valg.eva.admin.counting.domain.auditevents.PreliminaryCountAuditEvent;
import no.valg.eva.admin.counting.domain.auditevents.ProtocolAndPreliminaryCountAuditEvent;
import no.valg.eva.admin.counting.domain.auditevents.ProtocolCountAuditEvent;
import no.valg.eva.admin.counting.domain.auditevents.ThreadLocalVoteCountAuditDetailsMap;
import no.valg.eva.admin.counting.domain.auditevents.VoteCountAuditDetails;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.FinalCountService;
import no.valg.eva.admin.counting.domain.service.ProtocolCountService;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindPreliminaryCountService;
import no.valg.eva.admin.counting.domain.service.votecount.FindProtocolCountService;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.domain.updater.FinalBallotUpdater;
import no.valg.eva.admin.counting.domain.updater.FinalCountUpdater;
import no.valg.eva.admin.counting.domain.updater.PreliminaryBallotUpdater;
import no.valg.eva.admin.counting.domain.updater.PreliminaryCountUpdater;
import no.valg.eva.admin.counting.domain.updater.ProtocolBallotUpdater;
import no.valg.eva.admin.counting.domain.updater.ProtocolCountUpdater;
import no.valg.eva.admin.counting.domain.validation.CountValidator;
import no.valg.eva.admin.counting.domain.validation.FinalCountValidator;
import no.valg.eva.admin.counting.domain.validation.GetCountsValidator;
import no.valg.eva.admin.counting.domain.validation.PreliminaryCountValidator;
import no.valg.eva.admin.counting.domain.validation.ProtocolCountValidator;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;

@Stateless(name = "CountingService")
@Remote(CountingService.class)
public class CountingApplicationService implements CountingService {
	private static final RejectFinalCountValidator REJECT_FINAL_COUNT_VALIDATOR = new RejectFinalCountValidator();

	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private ManualContestVotingRepository manualContestVotingRepository;
	@Inject
	private VotingRepository votingRepository;
	@Inject
	private ReportCountCategoryRepository reportCountCategoryRepository;
	@Inject
	private CountingCodeValueRepository countingCodeValueRepository;
	@Inject
	private BallotRejectionRepository ballotRejectionRepository;
	@Inject
	private FindProtocolCountService findProtocolCountService;
	@Inject
	private FindCountService findCountService;
	@Inject
	private FindPreliminaryCountService findPreliminaryCountService;
	@Inject
	private ReportingUnitDomainService reportingUnitDomainService;
	@Inject
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;
	@Inject
	private VoteCountService voteCountService;
	@Inject
	private VoteCountStatusendringTrigger voteCountStatusendringTrigger;

	private ProtocolCountService protocolCountService;
	private FinalCountService finalCountService;
	private VoteCountFactory voteCountFactory;
	private GetCountsValidator getCountsValidator;

	public CountingApplicationService() {
		// for CDI inntil construotor injection er på plass
	}

	@PostConstruct
	void init() {
		getCountsValidator = new GetCountsValidator();

		voteCountFactory = new VoteCountFactory(
				reportingUnitRepository,
				reportCountCategoryRepository,
				votingRepository,
				manualContestVotingRepository,
				countingCodeValueRepository,
				voteCountService,
				antallStemmesedlerLagtTilSideDomainService, voteCountStatusendringTrigger);

		protocolCountService = new ProtocolCountService(
				reportingUnitRepository,
				reportCountCategoryRepository,
				votingRepository,
				manualContestVotingRepository);

		finalCountService = new FinalCountService(
				reportCountCategoryRepository,
				countingCodeValueRepository,
				reportingUnitRepository,
				ballotRejectionRepository,
				voteCountService, voteCountStatusendringTrigger);
	}

	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public FinalCount findApprovedFinalCount(UserData userData, ApprovedFinalCountRef ref) {
		CountContext context = ref.countContext();
		AreaPath countingAreaPath = ref.countingAreaPath();
		getCountsValidator.validate(context, countingAreaPath, userData.getOperatorAreaPath());
		AreaPath operatorAreaPath = userData.getOperatorAreaPath();
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(context.valgdistriktSti());
		MvArea countingMvArea = mvAreaRepository.findSingleByPath(countingAreaPath);
		AreaLevelEnum operatorAreaLevel = userData.getOperatorAreaLevel();
		if (operatorAreaLevel == COUNTY || userData.isElectionEventAdminUser() && ref.reportingUnitTypeId() == FYLKESVALGSTYRET
				|| userData.isSamiElectionCountyUser()) {
			return findCountService.findApprovedCountyFinalCount(operatorAreaPath, context, countingMvArea, contestMvElection);
		}
		if (operatorAreaLevel == MUNICIPALITY || operatorAreaLevel == ROOT) {
			return findCountService.findApprovedMunicipalityFinalCount(operatorAreaPath, context, countingMvArea, contestMvElection);
		}
		return null;
	}

	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public Counts getCounts(UserData userData, CountContext context, AreaPath countingAreaPath) {

		getCountsValidator.validate(context, countingAreaPath, userData.getOperatorAreaPath());

		MvArea operatorMvArea = userData.getOperatorMvArea();
		AreaPath operatorAreaPath = AreaPath.from(operatorMvArea.getAreaPath());
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(context.valgdistriktSti());
		MvArea countingMvArea = mvAreaRepository.findSingleByPath(countingAreaPath);

		boolean penultimateRecount = contestMvElection.getContest().isContestOrElectionPenultimateRecount();
		Counts counts = new Counts(context,
				contestMvElection.getElectionName(),
				contestMvElection.getContestName(),
				penultimateRecount,
				countingMvArea.getMunicipalityName(),
				countingMvArea.erForelderstemmekrets());
		List<ProtocolCount> protocolCounts = findProtocolCountService.findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection);
		counts.setProtocolCounts(protocolCounts);

		if (userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea)) {
			PreliminaryCount preliminaryCount = findPreliminaryCountService.findPreliminaryCount(operatorAreaPath, context, countingMvArea, contestMvElection);
			counts.setPreliminaryCount(preliminaryCount);
			if (voteCountService.useCombinedProtocolAndPreliminaryCount(context, countingMvArea, contestMvElection)) {
				counts.updateProtocolAndPreliminaryCount();
			}
		}
		if (includeMunicipalityFinalCounts(operatorAreaPath, penultimateRecount)) {
			List<FinalCount> finalCounts = findCountService.findMunicipalityFinalCounts(operatorAreaPath, contestMvElection, countingMvArea,
					context.getCategory());
			counts.setFinalCountsAndUpdateActiveCount(finalCounts);
		}
		if (userCanAccessReportingUnitForCountyFinalCount(operatorAreaPath)) {
			List<FinalCount> countyFinalCounts = findCountService.findCountyFinalCounts(operatorAreaPath, contestMvElection, countingMvArea,
					context.getCategory());
			counts.setCountyFinalCountsAndUpdateActiveCount(countyFinalCounts);
		}

		return counts;
	}

	private boolean includeMunicipalityFinalCounts(AreaPath operatorAreaPath, boolean penultimateRecount) {
		return voteCountService.includeMunicipalityFinalCounts(operatorAreaPath, penultimateRecount);
	}

	private boolean userCanAccessReportingUnitForCountyFinalCount(AreaPath operatorAreaPath) {
		return operatorAreaPath.isRootLevel() || reportingUnitRepository.existsFor(operatorAreaPath, FYLKESVALGSTYRET);
	}

	private boolean userCanAccessReportingUnitForPreliminaryCount(
			CountContext context,
			AreaPath operatorAreaPath,
			MvElection contestMvElection,
			MvArea countingMvArea) {
		return voteCountService.userCanAccessReportingUnitForPreliminaryCount(context, operatorAreaPath, contestMvElection, countingMvArea);
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Rediger, type = WRITE)
	@AuditLog(eventClass = ProtocolCountAuditEvent.class, eventType = AuditEventTypes.SaveCount)
	public ProtocolCount saveCount(UserData userData, CountContext context, ProtocolCount protocolCount) {
		protocolCount.validate();

		MvArea operatorMvArea = userData.getOperatorMvArea();
		AreaPath operatorAreaPath = AreaPath.from(operatorMvArea.getAreaPath());
		MvArea protocolCountArea = mvAreaRepository.findSingleByPath(protocolCount.getAreaPath());
		MvElection mvElectionContest = mvElectionRepository.finnEnkeltMedSti(context.valgdistriktSti());

		ReportingUnit reportingUnit = reportingUnitRepository.findByAreaPathAndType(
				reportingUnitDomainService.areaPathForFindingReportingUnit(ReportingUnitTypeId.STEMMESTYRET, operatorAreaPath, protocolCountArea),
				ReportingUnitTypeId.STEMMESTYRET);

		boolean countExists = voteCountService.countExists(protocolCount, mvElectionContest, protocolCountArea, reportingUnit);
		Contest contest = mvElectionContest.getContest();
		VoteCount protocolVoteCount;
		List<ManualContestVoting> manualContestVotings = null;
		if (countExists) {
			protocolVoteCount = voteCountService.updateVoteCount(userData, context, reportingUnit, protocolCount, protocolCountArea, mvElectionContest,
					new ProtocolCountUpdater(), new ProtocolBallotUpdater(), new ProtocolCountValidator());
			if (!protocolCountArea.getMunicipality().isElectronicMarkoffs()) {
				manualContestVotings = protocolCountService.updateManualContestVotings(userData, protocolCount.getDailyMarkOffCounts(), contest,
						protocolCountArea);
			}
		} else {
			protocolCount.setStatus(CountStatus.SAVED);
			Long contestPk = contest.getPk();
			Affiliation blankAffiliation = affiliationRepository.getAffiliationById(BALLOT_BLANK, contestPk);
			protocolVoteCount = voteCountFactory.createProtocolVoteCount(
					reportingUnit,
					protocolCount,
					protocolCountArea,
					mvElectionContest,
					blankAffiliation);
			protocolCount.setId(protocolVoteCount.getId());

			if (!(protocolCountArea.getMunicipality().isElectronicMarkoffs())) {
				manualContestVotings = protocolCountService.createManualXiMs(userData, protocolCount.getDailyMarkOffCounts(), contest,
						protocolCountArea);
			}
		}
		protocolCount.setStatus(CountStatus.SAVED);
		protocolCount.setVersion(protocolVoteCount.getAuditOplock());

		auditLogProtocolVoteCount(protocolVoteCount, manualContestVotings);

		return protocolCount;
	}

	private void auditLogProtocolVoteCount(VoteCount protocolVoteCount, List<ManualContestVoting> manualContestVotings) {
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(PROTOCOL, new VoteCountAuditDetails(protocolVoteCount, false, false));
		auditManualContestVotings(manualContestVotings);
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Rediger, type = WRITE)
	@AuditLog(eventClass = ProtocolCountAuditEvent.class, eventType = AuditEventTypes.ApproveCount)
	public ProtocolCount approveCount(final UserData userData, CountContext context, final ProtocolCount protocolCount) {
		saveCount(userData, context, protocolCount);
		return updateProtocolCount(userData, context, protocolCount, APPROVED);
	}

	@Override
	@Security(accesses = Opptelling_Opphev_Foreløpig_Telling, type = WRITE)
	@AuditLog(eventClass = ProtocolCountAuditEvent.class, eventType = AuditEventTypes.RevokeCount)
	public ProtocolCount revokeCount(final UserData userData, final CountContext context, final ProtocolCount protocolCount) {
		return updateProtocolCount(userData, context, protocolCount, CountStatus.REVOKED);
	}

	private ProtocolCount updateProtocolCount(UserData userData, CountContext context, ProtocolCount protocolCount, CountStatus countStatus) {
		MvArea protocolCountArea = mvAreaRepository.findSingleByPath(protocolCount.getAreaPath());
		MvElection mvElectionContest = mvElectionRepository.finnEnkeltMedSti(context.valgdistriktSti());
		MvArea operatorMvArea = userData.getOperatorMvArea();
		AreaPath operatorAreaPath = AreaPath.from(operatorMvArea.getAreaPath());

		ReportingUnit reportingUnit = reportingUnitRepository.findByAreaPathAndType(
				reportingUnitDomainService.areaPathForFindingReportingUnit(ReportingUnitTypeId.STEMMESTYRET, operatorAreaPath, protocolCountArea),
				ReportingUnitTypeId.STEMMESTYRET);

		boolean countExists = voteCountService.countExists(protocolCount, mvElectionContest, protocolCountArea, reportingUnit);
		VoteCount protocolVoteCount;
		List<ManualContestVoting> manualContestVotings = null;
		if (countExists) {
			protocolCount.setStatus(countStatus);
			protocolVoteCount = voteCountService.updateVoteCount(userData, context, reportingUnit, protocolCount, protocolCountArea, mvElectionContest,
					new ProtocolCountUpdater(), new ProtocolBallotUpdater(), new ProtocolCountValidator());
			protocolCount.setVersion(protocolVoteCount.getAuditOplock());
			if (!protocolCountArea.getMunicipality().isElectronicMarkoffs()) {
				manualContestVotings = protocolCountService.updateManualContestVotings(userData, protocolCount.getDailyMarkOffCounts(),
						mvElectionContest.getContest(),
						protocolCountArea);
			}
		} else {
			throw new IllegalStateException("No count found to update");
		}

		auditLogProtocolVoteCount(protocolVoteCount, manualContestVotings);

		return protocolCount;
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Rediger, type = WRITE)
	@AuditLog(eventClass = ProtocolAndPreliminaryCountAuditEvent.class, eventType = AuditEventTypes.SaveCount)
	public ProtocolAndPreliminaryCount saveCount(UserData userData, CountContext context, ProtocolAndPreliminaryCount protocolAndPreliminaryCount) {
		return ProtocolAndPreliminaryCount.from(
				this.saveCount(userData, context, protocolAndPreliminaryCount.getProtocolCount()),
				this.saveCount(userData, context, protocolAndPreliminaryCount.getPreliminaryCount()));
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Rediger, type = WRITE)
	@AuditLog(eventClass = ProtocolAndPreliminaryCountAuditEvent.class, eventType = AuditEventTypes.ApproveCount)
	public ProtocolAndPreliminaryCount approveCount(final UserData userData, CountContext context,
			final ProtocolAndPreliminaryCount protocolAndPreliminaryCount) {
		return ProtocolAndPreliminaryCount.from(
				this.approveCount(userData, context, protocolAndPreliminaryCount.getProtocolCount()),
				this.approveCount(userData, context, protocolAndPreliminaryCount.getPreliminaryCount()));
	}

	@Override
	@Security(accesses = Opptelling_Opphev_Foreløpig_Telling, type = WRITE)
	@AuditLog(eventClass = ProtocolAndPreliminaryCountAuditEvent.class, eventType = AuditEventTypes.RevokeCount)
	public ProtocolAndPreliminaryCount revokeCount(final UserData userData, CountContext context,
			final ProtocolAndPreliminaryCount protocolAndPreliminaryCount) {
		return ProtocolAndPreliminaryCount.from(
				this.revokeCount(userData, context, protocolAndPreliminaryCount.getProtocolCount()),
				this.revokeCount(userData, context, protocolAndPreliminaryCount.getPreliminaryCount()));
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Rediger, type = WRITE)
	@AuditLog(eventClass = PreliminaryCountAuditEvent.class, eventType = AuditEventTypes.SaveCount)
	public PreliminaryCount saveCount(final UserData userData, final CountContext context, final PreliminaryCount count) {
		count.validate();

		MvArea operatorMvArea = userData.getOperatorMvArea();
		AreaPath operatorAreaPath = AreaPath.from(operatorMvArea.getAreaPath());
		MvArea countingArea = mvAreaRepository.findSingleByPath(count.getAreaPath());
		MvElection mvElectionContest = mvElectionRepository.finnEnkeltMedSti(context.valgdistriktSti());
		Contest contest = mvElectionContest.getContest();

		ReportingUnit reportingUnit = reportingUnitForPreliminaryCount(context, operatorAreaPath, mvElectionContest, countingArea);

		boolean countExists = voteCountService.countExists(count, mvElectionContest, countingArea, reportingUnit);
		VoteCount preliminaryVoteCount;
		List<ManualContestVoting> manualContestVotings = null;
		if (countExists) {
			preliminaryVoteCount = voteCountService.updateVoteCount(userData, context, reportingUnit, count, countingArea, mvElectionContest,
					new PreliminaryCountUpdater(), new PreliminaryBallotUpdater(), new PreliminaryCountValidator());
			if (count.useDailyMarkOffCounts()) {
				manualContestVotings = protocolCountService.updateManualContestVotings(userData, count.getDailyMarkOffCounts(), contest, countingArea);
			}
		} else {
			count.setStatus(CountStatus.SAVED);
			Map<String, Ballot> ballotsForContest = findBallotsForContest(mvElectionContest);
			preliminaryVoteCount = voteCountFactory.createPreliminaryVoteCount(reportingUnit, context, count, countingArea, mvElectionContest,
					ballotsForContest);
			count.setId(preliminaryVoteCount.getId());
			if (count.useDailyMarkOffCounts()) {
				manualContestVotings = protocolCountService.createManualXiMs(userData, count.getDailyMarkOffCounts(), contest, countingArea);
			}
		}
		count.setStatus(CountStatus.SAVED);
		count.setVersion(preliminaryVoteCount.getAuditOplock());

		auditLogPreliminaryVoteCount(preliminaryVoteCount, manualContestVotings);

		return count;
	}

	private void auditLogPreliminaryVoteCount(VoteCount preliminaryVoteCount, List<ManualContestVoting> manualContestVotings) {
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(
				PRELIMINARY, new VoteCountAuditDetails(preliminaryVoteCount, false, false));
		auditManualContestVotings(manualContestVotings);
	}

	private void auditManualContestVotings(List<ManualContestVoting> manualContestVotings) {
		if (manualContestVotings != null) {
			List<ManualVotingAuditDetails> manualContestVotingsAuditDetailsList = manualContestVotings.stream()
					.map(this::manualVotingAuditDetails)
					.collect(Collectors.toList());
			ManualContestVotingsAuditDetails.THREAD_LOCAL.set(new ManualContestVotingsAuditDetails(manualContestVotingsAuditDetailsList));
		}
	}

	private ManualVotingAuditDetails manualVotingAuditDetails(ManualContestVoting manualContestVoting) {
		return new ManualVotingAuditDetails(manualContestVoting.getElectionDay().getDate(), manualContestVoting.getVotings());
	}

	private ReportingUnit reportingUnitForPreliminaryCount(CountContext context, AreaPath operatorAreaPath,
			MvElection mvElectionContest, MvArea countingMvArea) {
		ReportingUnitTypeId typeId = voteCountService.reportingUnitTypeForPreliminaryCount(context, countingMvArea.getMunicipality(), mvElectionContest);
		return reportingUnitRepository.findByAreaPathAndType(
				reportingUnitDomainService.areaPathForFindingReportingUnit(typeId, operatorAreaPath, countingMvArea), typeId);
	}

	private Map<String, Ballot> findBallotsForContest(MvElection mvElectionContest) {
		Map<String, Ballot> ballotMap = new HashMap<>();
		List<Affiliation> affiliations = affiliationRepository.findApprovedByContest(mvElectionContest.getContest().getPk());
		for (final Affiliation affiliation : affiliations) {
			Ballot ballot = affiliation.getBallot();
			ballotMap.put(ballot.getId(), ballot);
		}
		return ballotMap;
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Rediger, type = WRITE)
	@AuditLog(eventClass = PreliminaryCountAuditEvent.class, eventType = AuditEventTypes.ApproveCount)
	public PreliminaryCount approveCount(final UserData userData, CountContext context, final PreliminaryCount count) {
		saveCount(userData, context, count);
		validateApprovePreliminaryCount(userData, context, count);
		return updateCount(userData, context, count, APPROVED);
	}

	private void validateApprovePreliminaryCount(UserData userData, CountContext context, PreliminaryCount count) {
		if (count.getCategory() == VO) {
			int total = getVoCountValidatorTotal(userData, context, count);
			ApprovePreliminaryCountValidator.forVo(total).validate(count);
		} else {
			ApprovePreliminaryCountValidator.forOtherCategoriesThanVo().validate(count);
		}
	}

	private int getVoCountValidatorTotal(UserData userData, CountContext context, PreliminaryCount count) {
		int total = 0;
		if (count.getDailyMarkOffCounts() == null) {
			MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(context.valgdistriktSti());
			MvArea countingMvArea = mvAreaRepository.findSingleByPath(count.getAreaPath());
			AreaPath operatorAreaPath = userData.getOperatorAreaPath();
			List<ProtocolCount> protocolCounts = findProtocolCountService.findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection);
			for (ProtocolCount protocolCount : protocolCounts) {
				if (!protocolCount.isApproved()) {
					throw new ValidateException("Protocol count must be approved before approving preliminary count");
				}
				total += protocolCount.getTotalBallotCount();
			}
		} else {
			total = count.getDailyMarkOffCounts().getMarkOffCount();
		}
		return total;
	}

	@Override
	@Security(accesses = Opptelling_Opphev_Foreløpig_Telling, type = WRITE)
	@AuditLog(eventClass = PreliminaryCountAuditEvent.class, eventType = AuditEventTypes.RevokeCount)
	public PreliminaryCount revokeCount(final UserData userData, CountContext context, final PreliminaryCount count) {
		return updateCount(userData, context, count, CountStatus.REVOKED);
	}

	private PreliminaryCount updateCount(UserData userData, CountContext context, PreliminaryCount count, CountStatus countStatus) {
		count.validate();
		MvArea countArea = mvAreaRepository.findSingleByPath(count.getAreaPath());
		MvElection mvElectionContest = mvElectionRepository.finnEnkeltMedSti(context.valgdistriktSti());
		MvArea operatorMvArea = userData.getOperatorMvArea();
		AreaPath operatorAreaPath = AreaPath.from(operatorMvArea.getAreaPath());

		ReportingUnit reportingUnit = reportingUnitForPreliminaryCount(context, operatorAreaPath, mvElectionContest, countArea);
		count.setStatus(countStatus);
		VoteCount preliminaryVoteCount = voteCountService.updateVoteCount(userData, context, reportingUnit, count, countArea, mvElectionContest,
				new PreliminaryCountUpdater(), new PreliminaryBallotUpdater(), new PreliminaryCountValidator());
		count.setVersion(preliminaryVoteCount.getAuditOplock());
		List<ManualContestVoting> manualContestVotings = null;
		if (count.useDailyMarkOffCounts()) {
			manualContestVotings = protocolCountService.updateManualContestVotings(userData, count.getDailyMarkOffCounts(), mvElectionContest.getContest(),
					countArea);
		}
		auditLogPreliminaryVoteCount(preliminaryVoteCount, manualContestVotings);

		return count;
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Rediger, type = WRITE)
	@AuditLog(eventClass = FinalCountAuditEvent.class, eventType = AuditEventTypes.SaveCount)
	public FinalCount saveCount(UserData userData, CountContext context, FinalCount finalCount) {
		CountStatus status = finalCount.getStatus() == CountStatus.NEW ? CountStatus.SAVED : finalCount.getStatus();
		return saveFinalCount(userData, context, finalCount, status, new FinalCountValidator(), finalCount.getReportingUnitTypeId());
	}

	@Override
	@Security(accesses = Opptelling_Forkastelser_Manuelt, type = WRITE)
	@AuditLog(eventClass = FinalCountAuditEvent.class, eventType = AuditEventTypes.ProcessRejectedBallots)
	public FinalCount processRejectedBallots(UserData userData, CountContext context, FinalCount finalCount) {
		if (finalCount.getStatus() != APPROVED || !finalCount.isRejectedBallotsProcessed()) {
			throw new IllegalStateException("final count is not in correct state for processing rejected ballots");
		}
		return saveFinalCount(userData, context, finalCount, APPROVED, new FinalCountValidator(), finalCount.getReportingUnitTypeId());
	}

	FinalCount saveFinalCount(UserData userData, CountContext context, FinalCount finalCount, CountStatus status,
			CountValidator<FinalCount> validator, ReportingUnitTypeId reportingUnitTypeId) {

		finalCount.validate();
		validateReportingTypeIdForFinalCount(reportingUnitTypeId);
		validateOperatorAreaForSaveFinalCount(reportingUnitTypeId, userData.getOperatorMvArea());

		MvArea countingArea = mvAreaRepository.findSingleByPath(finalCount.getAreaPath());

		MvElection mvElectionContest = mvElectionRepository.finnEnkeltMedSti(context.valgdistriktSti());
		ElectionGroup electionGroup = mvElectionContest.getElectionGroup();

		if (mvElectionContest.getContest().isOnBoroughLevel()) {
			finalCountService.validateCountCategoryForBoroughElection(context.getCategory());
		} else {
			Municipality municipality = countingArea.getMunicipality();
			finalCountService.validateCountCategory(context.getCategory(), municipality, electionGroup);
		}

		Map<String, Ballot> ballotMap = findBallotsForContest(mvElectionContest);

		ReportingUnit reportingUnit = reportingUnitDomainService.reportingUnitForFinalCount(
				reportingUnitTypeId, userData.getOperatorAreaPath(), finalCount.getAreaPath(), mvElectionContest);

		finalCount.setStatus(status);
		if (!voteCountService.countExists(finalCount, mvElectionContest, countingArea, reportingUnit)) {
			return finalCountService.saveNewFinalCount(
					reportingUnit,
					context,
					finalCount,
					countingArea,
					ballotMap,
					mvElectionContest);
		} else {
			VoteCount finalVoteCount = voteCountService.updateVoteCount(userData, context, reportingUnit, finalCount, countingArea, mvElectionContest,
					new FinalCountUpdater(), new FinalBallotUpdater(), validator);
			auditLogFinalVoteCount(finalVoteCount);
			finalCount.setVersion(finalVoteCount.getAuditOplock());
			return finalCount;
		}
	}

	private void auditLogFinalVoteCount(VoteCount finalVoteCount) {
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(FINAL, new VoteCountAuditDetails(finalVoteCount, true, true));
	}

	private void validateOperatorAreaForSaveFinalCount(ReportingUnitTypeId reportingUnitTypeId, MvArea operatorMvArea) {
		if (operatorMvArea.getAreaLevel() == ROOT.getLevel()) {
			return;
		}
		if (reportingUnitTypeId.equals(VALGSTYRET)) {
			operatorMvArea.validateAreaLevel(AreaLevelEnum.MUNICIPALITY);
		} else {
			operatorMvArea.validateAreaLevel(AreaLevelEnum.COUNTY);
		}
	}

	private void validateReportingTypeIdForFinalCount(final ReportingUnitTypeId reportingUnitTypeId) {
		if (!(reportingUnitTypeId == VALGSTYRET || reportingUnitTypeId == FYLKESVALGSTYRET || reportingUnitTypeId == OPPTELLINGSVALGSTYRET)) {
			throw new ValidateException(format(
					"expected the reporting unit to be <%s>, but was <%s>",
					FYLKESVALGSTYRET,
					reportingUnitTypeId.name()));
		}
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Rediger, type = WRITE)
	@AuditLog(eventClass = FinalCountAuditEvent.class, eventType = AuditEventTypes.ApproveCount)
	public FinalCount approveCount(UserData userData, CountContext context, FinalCount finalCount) {
		if (finalCount.getTotalRejectedBallotCount() == 0) {
			finalCount.setRejectedBallotsProcessed(true);
		}
		if (voteCountService.isFinalCountReadyForSettlement(context.getContestPath(), finalCount)) {
			return saveFinalCount(userData, context, finalCount, CountStatus.TO_SETTLEMENT, new FinalCountValidator(), finalCount.getReportingUnitTypeId());
		}
		return saveFinalCount(userData, context, finalCount, APPROVED, new FinalCountValidator(), finalCount.getReportingUnitTypeId());
	}

	@Override
	@Security(accesses = Opptelling_Opphev_Endelig_Telling, type = WRITE)
	@AuditLog(eventClass = FinalCountAuditEvent.class, eventType = AuditEventTypes.RevokeCount)
	public FinalCount revokeCount(UserData userData, CountContext context, FinalCount finalCount) {
		finalCount.setRejectedBallotsProcessed(false);
		return saveFinalCount(userData, context, finalCount, CountStatus.REVOKED, REJECT_FINAL_COUNT_VALIDATOR, finalCount.getReportingUnitTypeId());
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Sett_Til_Valgoppgjør, type = WRITE)
	@AuditLog(eventClass = FinalCountAuditEvent.class, eventType = AuditEventTypes.ReadyForSettlement)
	public FinalCount updateFinalCountStatusToSettlement(UserData userData, ApprovedFinalCountRef ref) {
		FinalCount approvedFinalCount = findApprovedFinalCount(userData, ref);
		if (ref.reportingUnitTypeId() != null && ref.reportingUnitTypeId() != approvedFinalCount.getReportingUnitTypeId()) {
			throw new IllegalStateException("ReportingUnit on final count and count ref should match if set on count ref..");
		}
		return saveFinalCount(userData, ref.countContext(), approvedFinalCount, CountStatus.TO_SETTLEMENT, new FinalCountValidator(),
				approvedFinalCount.getReportingUnitTypeId());
	}

	private static class RejectFinalCountValidator implements CountValidator<FinalCount> {
		@Override
		public void applyValidationRules(
				FinalCount count, CountContext context, MvArea countingArea, CountingMode countingMode, ReportingUnitTypeId reportingUnitTypeId) {
			// no validation
		}
	}
}
