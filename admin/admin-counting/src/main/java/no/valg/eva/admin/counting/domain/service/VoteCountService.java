package no.valg.eva.admin.counting.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.AbstractCount;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.domain.updater.BallotUpdater;
import no.valg.eva.admin.counting.domain.updater.CountUpdater;
import no.valg.eva.admin.counting.domain.validation.CountValidator;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.exception.ErrorCode.ERROR_CODE_0504_STALE_OBJECT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;

/**
 * Domain service for handling VoteCount instances.
 */
@Default
@ApplicationScoped
public class VoteCountService extends BaseCountService {

	private CountingCodeValueRepository countingCodeValueRepository;
	private MvElectionRepository mvElectionRepository;
	private MvAreaRepository mvAreaRepository;
	private BallotRepository ballotRepository;
	private BallotRejectionRepository ballotRejectionRepository;
	private ContestReportRepository contestReportRepository;
	private VoteCountRepository voteCountRepository;
	private ReportingUnitDomainService reportingUnitDomainService;
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;
	private VoteCountStatusendringTrigger voteCountStatusendringTrigger;

	public VoteCountService() {
		// For testing
	}

	@Inject
	public VoteCountService(
			ReportingUnitRepository reportingUnitRepository, ReportCountCategoryRepository reportCountCategoryRepository,
			VotingRepository votingRepository, ManualContestVotingRepository manualContestVotingRepository,
			CountingCodeValueRepository countingCodeValueRepository,
			MvElectionRepository mvElectionRepository, MvAreaRepository mvAreaRepository, BallotRepository ballotRepository,
			BallotRejectionRepository ballotRejectionRepository,
			ContestReportRepository contestReportRepository, VoteCountRepository voteCountRepository,
			ReportingUnitDomainService reportingUnitDomainService,
			AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService,
			VoteCountStatusendringTrigger voteCountStatusendringTrigger) {

		super(reportingUnitRepository, reportCountCategoryRepository, votingRepository, manualContestVotingRepository);
		this.countingCodeValueRepository = countingCodeValueRepository;
		this.mvElectionRepository = mvElectionRepository;
		this.mvAreaRepository = mvAreaRepository;
		this.ballotRepository = ballotRepository;
		this.ballotRejectionRepository = ballotRejectionRepository;
		this.contestReportRepository = contestReportRepository;
		this.voteCountRepository = voteCountRepository;
		this.reportingUnitDomainService = reportingUnitDomainService;
		this.antallStemmesedlerLagtTilSideDomainService = antallStemmesedlerLagtTilSideDomainService;
		this.voteCountStatusendringTrigger = voteCountStatusendringTrigger;
	}

	/**
	 * @return vote count for reporting unit, area and contest
	 */
	public VoteCount findVoteCount(
			ReportingUnit reportingUnit,
			CountContext context,
			MvArea countingMvArea,
			MvElection contestMvElection,
			CountQualifier countQualifier) {

		ContestReport contestReport = findContestReport(reportingUnit, contestMvElection);
		if (contestReport == null) {
			return null;
		}
		return contestReport.findFirstVoteCountByMvAreaCountQualifierAndCategory(countingMvArea.getPk(), countQualifier, context.getCategory());
	}

	public ContestReport findContestReport(ReportingUnit reportingUnit, MvElection contestMvElection) {
		return contestReportRepository.findByReportingUnitContest(reportingUnit.getPk(), contestMvElection.getContest().getPk());
	}

	/**
	 * @return final vote counts for report unit, area and contest
	 */
	public List<VoteCount> findFinalVoteCounts(ReportingUnit reportingUnit, MvElection contestMvElection, MvArea countingMvArea, CountCategory category) {
		ContestReport contestReport = findContestReport(reportingUnit, contestMvElection);
		if (contestReport == null) {
			return new ArrayList<>();
		}
		return contestReport.findVoteCountsByMvAreaCountQualifierAndCategory(countingMvArea.getPk(), FINAL, category);
	}

	public VoteCount findApprovedFinalVoteCount(ApprovedFinalCountRef approvedFinalCountRef, AreaPath operatorAreaPath) {
		return findApprovedFinalVoteCount(
				approvedFinalCountRef.reportingUnitTypeId(), approvedFinalCountRef.countContext(),
				approvedFinalCountRef.countingAreaPath(), operatorAreaPath);
	}

	public VoteCount findApprovedFinalVoteCount(ReportingUnitTypeId reportingUnitTypeId, CountContext countContext, AreaPath countingAreaPath,
			AreaPath operatorAreaPath) {
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(countContext.valgdistriktSti());

		ReportingUnit reportingUnit = reportingUnitDomainService.reportingUnitForFinalCount(reportingUnitTypeId, operatorAreaPath, countingAreaPath,
				contestMvElection);

		MvArea countingMvArea = mvAreaRepository.findSingleByPath(countingAreaPath);
		return findApprovedFinalVoteCount(reportingUnit, countContext, countingMvArea, contestMvElection);
	}

	public VoteCount findApprovedFinalVoteCount(ReportingUnit reportingUnit, CountContext context, MvArea countingMvArea, MvElection contestMvElection) {
		ContestReport contestReport = findContestReport(reportingUnit, contestMvElection);
		if (contestReport == null) {
			return null;
		}
		return contestReport.findApprovedVoteCountByMvAreaCountQualifierAndCategory(countingMvArea.getPk(), FINAL, context.getCategory());
	}

	public List<VoteCount> findVoteCountsFor(ReportingUnit reportingUnit, ElectionPath contestPath, CountStatus countStatus,
			CountQualifier countQualifier) {
		contestPath.assertContestLevel();
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		ContestReport contestReport = findContestReport(reportingUnit, contestMvElection);
		if (contestReport == null) {
			return emptyList();
		}
		return contestReport.findVoteCountsByCountQualifierAndStatus(countQualifier, countStatus);
	}

	public List<VoteCount> findPreliminaryVoteCountsByReportingUnitContestPathAndCategory(ReportingUnit reportingUnit, ElectionPath contestPath,
			CountCategory category) {
		contestPath.assertContestLevel();
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		ContestReport contestReport = findContestReport(reportingUnit, contestMvElection);
		if (contestReport == null) {
			return emptyList();
		}
		return contestReport.findVoteCountsByCategoryAndQualifier(category, PRELIMINARY);
	}

	/**
	 * @return true if when a report count category VF exists with special covers enabled, false otherwise
	 */
	public boolean useForeignSpecialCovers(final MvElection mvElectionContest, final Municipality municipality) {
		ReportCountCategory vfReportCountCategory = reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(
				municipality,
				mvElectionContest.getElectionGroup(),
				CountCategory.VF);
		return vfReportCountCategory != null && vfReportCountCategory.isSpecialCover();
	}

	/**
	 * @param context with contest path and category
	 * @param preliminaryCountArea mvArea for preliminary count
	 * @param mvElectionContest mvElection for contest
	 * @param operatorAreaPath area path for operator, based on operator's area and level
	 * @return pollingDistricts to find protocol counts for, empty list if category does not have protocol counts
	 */
	public Collection<PollingDistrict> pollingDistrictsForProtocolCount(
			CountContext context,
			MvArea preliminaryCountArea,
			MvElection mvElectionContest,
			AreaPath operatorAreaPath) {
		if (context.getCategory() != CountCategory.VO || (preliminaryCountArea.getMunicipality() != null
				&& !preliminaryCountArea.getMunicipality().isRequiredProtocolCount())) {
			return emptyList();
		}
		CountingMode countingMode = countingMode(context, preliminaryCountArea.getMunicipality(), mvElectionContest);
		if (countingMode == CENTRAL) {
			Collection<PollingDistrict> pollingDistricts = preliminaryCountArea.getMunicipality().regularPollingDistricts(true, true);
			if (areaIsMunicipalityOrBorough(preliminaryCountArea)) {
				return filterOutParentPollingDistricts(pollingDistricts);
			} else {
				return filterOutPollingDistrictsNotInOperatorAreaPollingDistrict(pollingDistricts, operatorAreaPath);
			}
		} else {
			if (preliminaryCountArea.erForelderstemmekrets()) {
				return preliminaryCountArea.getPollingDistrict().getChildPollingDistricts();
			}
			List<PollingDistrict> pollingDistricts = new ArrayList<>();
			pollingDistricts.add(preliminaryCountArea.getPollingDistrict());
			return pollingDistricts;
		}
	}

	private boolean areaIsMunicipalityOrBorough(MvArea preliminaryCountArea) {
		return preliminaryCountArea.getActualAreaLevel() == AreaLevelEnum.BOROUGH
				|| preliminaryCountArea.getPollingDistrict() != null && preliminaryCountArea.getPollingDistrict().isMunicipality();
	}

	/**
	 * Returns collection with the polling district corresponding to the polling district operatorAreaPath points to. Empty collection if no matches.
	 */
	private Collection<PollingDistrict> filterOutPollingDistrictsNotInOperatorAreaPollingDistrict(
			Collection<PollingDistrict> pollingDistricts,
			AreaPath operatorAreaPath) {

		Collection<PollingDistrict> filteredPollingDistricts = new ArrayList<>();
		for (PollingDistrict pollingDistrict : pollingDistricts) {
			if (pollingDistrict.getId().equals(operatorAreaPath.getPollingDistrictId())) {
				filteredPollingDistricts.add(pollingDistrict);
				return filteredPollingDistricts;
			}
		}
		return filteredPollingDistricts;
	}

	/**
	 * Parent polling district are removed from list because protocol counts are always on child polling districts.
	 * @param pollingDistricts collection to filter
	 * @return collection containing only child polling districts
	 */
	private Collection<PollingDistrict> filterOutParentPollingDistricts(Collection<PollingDistrict> pollingDistricts) {
		List<PollingDistrict> result = new ArrayList<>();
		for (PollingDistrict pollingDistrict : pollingDistricts) {
			if (!pollingDistrict.isParentPollingDistrict()) {
				result.add(pollingDistrict);
			}
		}
		return result;
	}

	public CountingMode countingMode(CountContext context, Municipality municipality, MvElection mvElectionContest) {
		return countingMode(context.getCategory(), municipality, mvElectionContest);
	}

	public CountingMode countingMode(CountCategory category, Municipality municipality, MvElection mvElectionContest) {
		if (mvElectionContest.getContest().isOnBoroughLevel()) {
			if (CountCategory.VO.equals(category)) {
				return BY_POLLING_DISTRICT;
			} else {
				return CENTRAL;
			}
		}
		ReportCountCategory reportCountCategory = getReportCountCategory(municipality, mvElectionContest.getElectionGroup(), category);
		if (reportCountCategory == null) {
			return null;
		}
		return reportCountCategory.getCountingMode();
	}

	/**
	 * Determines type of reporting unit based on counting mode.
	 * 
	 * @return STEMMESTYRET if counting mode is BY_POLLING_DISTRICT, VALGSTYRET otherwise
	 */
	public ReportingUnitTypeId reportingUnitTypeForPreliminaryCount(CountContext context, Municipality municipality, MvElection mvElectionContest) {
		if (mvElectionContest.getContest().isOnBoroughLevel() && context.getCategory() == CountCategory.VO) {
			return STEMMESTYRET;
		}
		if (countingMode(context, municipality, mvElectionContest) == BY_POLLING_DISTRICT) {
			return STEMMESTYRET;
		} else {
			return VALGSTYRET;
		}
	}

	/**
	 * @return true if count exists, else false
	 */
	public boolean countExists(AbstractCount count, MvElection contest, MvArea countingArea, ReportingUnit reportingUnit) {
		ContestReport contestReport = findContestReport(reportingUnit, contest);
		return contestReport != null && contestReport.findVoteCountByCountingAreaAndId(countingArea, count.getId()) != null;
	}

	/**
	 * Updates existing vote count.
	 * 
	 * @return the updated vote count
	 */
	public <T extends AbstractCount> VoteCount updateVoteCount(
			UserData userData,
			CountContext context,
			ReportingUnit reportingUnit,
			T count,
			MvArea countingArea,
			MvElection contestMvElection,
			CountUpdater<T> countUpdater,
			BallotUpdater<T> ballotUpdater,
			CountValidator<T> countValidator) {

		if (context.getCategory() == FO
				&& count.getQualifier() == CountQualifier.PRELIMINARY
				&& !antallStemmesedlerLagtTilSideDomainService.isAntallStemmesedlerLagtTilSideLagret(contestMvElection, countingArea.getMunicipality())) {
			throw new EvoteException("Antall stemmesedler lagt til side må settes først.");
		}
		ReportingUnitTypeId reportingUnitTypeId = ReportingUnitTypeId.fromId(reportingUnit.getReportingUnitType().getId());
		CountingMode countingMode = countingMode(context, countingArea.getMunicipality(), contestMvElection);
		countValidator.applyValidationRules(count, context, countingArea, countingMode, reportingUnitTypeId);
		Contest contest = contestMvElection.getContest();
		ContestReport contestReport = findContestReport(reportingUnit, contestMvElection);
		VoteCount aVoteCount = contestReport.findVoteCountByCountingAreaAndId(countingArea, count.getId());
		CountStatus aVoteCountCountStatus = aVoteCount.getCountStatus();
		VoteCountStatus voteCountStatus = countingCodeValueRepository.findVoteCountStatusByCountStatus(count.getStatus());
		throwExceptionIfStaleCount(count, aVoteCount);
		countUpdater.applyUpdates(aVoteCount, count, voteCountStatus);
		ballotUpdater.applyUpdates(
				aVoteCount, count, ballotMap(contest.getPk()), ballotRejectionMap(context.getCategory(), count.getQualifier()));
		aVoteCount.setDirty();
		contestReportRepository.update(userData, contestReport);
		voteCountStatusendringTrigger.fireEventForStatusendring(count, countingArea, contestMvElection, aVoteCountCountStatus, reportingUnitTypeId);
		return aVoteCount;
	}

	private <T extends AbstractCount> void throwExceptionIfStaleCount(T count, VoteCount aVoteCount) {
		int version = count.getVersion();
		int expectedVersion = aVoteCount.getAuditOplock();
		if (version != expectedVersion) {
			throw new EvoteException(
					ERROR_CODE_0504_STALE_OBJECT,
					new IllegalStateException(
							format("Outdated version. Tried to save an old count. Expected version <%d>, but got <%d>.", expectedVersion, version)));
		}
	}

	private Map<String, Ballot> ballotMap(long contestPk) {
		List<Ballot> ballots = ballotRepository.findApprovedByContest(contestPk);
		Map<String, Ballot> ballotMap = new HashMap<>();
		for (Ballot ballot : ballots) {
			ballotMap.put(ballot.getId(), ballot);
		}
		return ballotMap;
	}

	private Map<String, BallotRejection> ballotRejectionMap(CountCategory category, CountQualifier qualifier) {
		if (qualifier != FINAL) {
			return null;
		}
		List<BallotRejection> ballotRejections = ballotRejectionRepository.findBallotRejectionsByEarlyVoting(category.isEarlyVoting());
		Map<String, BallotRejection> ballotRejectionMap = new HashMap<>();
		for (BallotRejection ballotRejection : ballotRejections) {
			ballotRejectionMap.put(ballotRejection.getId(), ballotRejection);
		}
		return ballotRejectionMap;
	}

	/**
	 * @return true if protocol and preliminary count should be combined, false otherwise
	 */
	public boolean useCombinedProtocolAndPreliminaryCount(CountContext context, MvArea countArea, MvElection contest) {
		boolean notParentOrChildPollingDistrict = !countArea.erForelderstemmekrets() && !countArea.isChildPollingDistrict();
		boolean isRequiredProtocolCount = countArea.getMunicipality() == null || countArea.getMunicipality().isRequiredProtocolCount();
		return !contest.getContest().isOnBoroughLevel()
				&& countingMode(context, countArea.getMunicipality(), contest) == BY_POLLING_DISTRICT
				&& notParentOrChildPollingDistrict
				&& isRequiredProtocolCount;
	}

	/**
	 * @return true if user can access reporting unit for preliminary count
	 */
	public boolean userCanAccessReportingUnitForPreliminaryCount(
			CountContext context,
			AreaPath operatorAreaPath,
			MvElection contestMvElection,
			MvArea countingMvArea) {
		if (countingMvArea.isChildPollingDistrict()) {
			return false;
		}
		CountingMode countingMode = countingMode(context, countingMvArea.getMunicipality(), contestMvElection);
		if (countingMode == CENTRAL && countingMvArea.getPollingDistrict() != null && !countingMvArea.getPollingDistrict().isMunicipality()) {
			return false;
		}
		if (operatorAreaPath.isRootLevel() || operatorAreaPath.isCountyLevel()) {
			return true;
		}
		if (contestMvElection.getAreaLevel() == AreaLevelEnum.BOROUGH.getLevel() && countingMode == BY_POLLING_DISTRICT) {
			return true;
		}
		ReportingUnitTypeId typeId = reportingUnitTypeForPreliminaryCount(context, countingMvArea.getMunicipality(), contestMvElection);
		return reportingUnitRepository.existsFor(reportingUnitDomainService.areaPathForFindingReportingUnit(typeId, operatorAreaPath, countingMvArea), typeId);
	}

	public Long markOffCountForPreliminaryCount(
			CountContext context,
			MvElection mvElectionContest,
			MvArea countingMvArea,
			CountCategory countCategory) {
		PollingDistrict pollingDistrict = countingMvArea.getPollingDistrict();
		Municipality municipality = countingMvArea.getMunicipality();
		Borough borough = countingMvArea.getBorough();
		boolean electronicMarkOffs = municipality.isElectronicMarkoffs();
		VotingCategory[] votingCategories = VotingCategory.from(countCategory);
		switch (countCategory) {
		case FO:
			if (municipality.isSamlekommune()) {
				return notRejectedMarkOffCount(context, mvElectionContest, municipality, pollingDistrict, votingCategories, false);
			}
			return approvedMarkOffCount(context, mvElectionContest, municipality, borough, pollingDistrict, votingCategories, false);
		case FS:
			if (electronicMarkOffs) {
				return approvedMarkOffCount(context, mvElectionContest, municipality, borough, pollingDistrict, votingCategories, true);
			} else {
				return notRejectedMarkOffCount(context, mvElectionContest, municipality, pollingDistrict, votingCategories, true);
			}
		case BF:
			return markOffCountForPreliminaryCountForOtherBoroughs(countingMvArea.getBorough());
		default:
			if (electronicMarkOffs) {
				return approvedMarkOffCount(context, mvElectionContest, municipality, borough, pollingDistrict, votingCategories, false);
			} else {
				return notRejectedMarkOffCount(context, mvElectionContest, municipality, pollingDistrict, votingCategories, false);
			}
		}
	}

	private Long approvedMarkOffCount(
			CountContext context,
			MvElection mvElectionContest,
			Municipality municipality,
			Borough borough,
			PollingDistrict pollingDistrict,
			VotingCategory[] votingCategories,
			boolean lateValidationCovers) {

		CountingMode countingMode = countingMode(context, municipality, mvElectionContest);
		if (countingMode == CENTRAL_AND_BY_POLLING_DISTRICT) {
			return votingRepository
					.findApprovedVotingCountByPollingDistrictAndCategoriesAndLateValidation(pollingDistrict, votingCategories, lateValidationCovers);
		}
		if (countingMode == BY_TECHNICAL_POLLING_DISTRICT && isFirstTechnicalPollingDistrict(borough, pollingDistrict)) {
			return votingRepository.findApprovedVotingCountByMunicipalityAndCategoriesAndLateValidation(municipality, votingCategories, lateValidationCovers);
		}
		if (countingMode == BY_TECHNICAL_POLLING_DISTRICT) {
			return null;
		}
		if (mvElectionContest.getAreaLevel() == AreaLevelEnum.BOROUGH.getLevel()) {
			return votingRepository.findApprovedVotingCountByBoroughAndCategoriesAndLateValidation(borough, votingCategories, lateValidationCovers);
		}
		if (municipality.isSamlekommune()) {
			return votingRepository.findMarkOffForSamlekommuneInContest(mvElectionContest, lateValidationCovers);
		}
		return votingRepository.findApprovedVotingCountByMunicipalityAndCategoriesAndLateValidation(municipality, votingCategories, lateValidationCovers);
	}

	private boolean isFirstTechnicalPollingDistrict(Borough borough, PollingDistrict pollingDistrict) {
		PollingDistrict firstTechnicalPollingDistrict = borough.findFirstTechnicalPollingDistrict();
		return firstTechnicalPollingDistrict.getId().equals(pollingDistrict.getId());
	}

	private long notRejectedMarkOffCount(
			CountContext context,
			MvElection mvElectionContest,
			Municipality municipality,
			PollingDistrict pollingDistrict,
			VotingCategory[] votingCategories,
			boolean lateValidationCovers) {

		if (municipality.isSamlekommune()) {
			return votingRepository.findMarkOffForSamlekommuneInContest(mvElectionContest, lateValidationCovers);
		}

		if (countingMode(context, municipality, mvElectionContest) == CENTRAL_AND_BY_POLLING_DISTRICT) {
			return votingRepository.findNotRejectedVotingCountByPollingDistrictAndCategoriesAndLateValidation(pollingDistrict, votingCategories,
					lateValidationCovers);
		} else {
			return votingRepository
					.findNotRejectedVotingCountByMunicipalityAndCategoriesAndLateValidation(municipality, votingCategories, lateValidationCovers);
		}
	}

	private long markOffCountForPreliminaryCountForOtherBoroughs(Borough borough) {
		return votingRepository.findMarkOffInOtherBoroughs(borough.getPk());
	}

	boolean isLastReportingUnitForContest(ReportingUnitTypeId reportingUnitTypeId, CountContext countContext, AreaPath countingAreaPath) {
		ReportingUnit reportingUnit = reportingUnit(reportingUnitTypeId, countingAreaPath);
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(countContext.valgdistriktSti());
		AreaLevelEnum reportingUnitAreaLevel = reportingUnit.getActualAreaLevel();
		AreaLevelEnum contestAreaLevel = contestMvElection.contestMvArea().getActualAreaLevel();
		return reportingUnitAreaLevel == contestAreaLevel || reportingUnitAreaLevel == MUNICIPALITY && contestAreaLevel == BOROUGH;
	}

	private ReportingUnit reportingUnit(ReportingUnitTypeId reportingUnitTypeId, AreaPath countingAreaPath) {
		if (reportingUnitTypeId == FYLKESVALGSTYRET) {
			return reportingUnitRepository.findByAreaPathAndType(countingAreaPath.toCountyPath(), FYLKESVALGSTYRET);
		} else {
			return reportingUnitRepository.findByAreaPathAndType(countingAreaPath.toMunicipalityPath(), VALGSTYRET);
		}
	}

	public ContestReport createContestReport(ContestReport contestReport) {
		return contestReportRepository.create(contestReport);
	}

	/**
	 * @param operatorAreaPath path to area
	 * @param penultimateRecount true if the penultimate reporting unit performs a recount, that is true if municipality should have final counts as well as
	 *        preliminary counts.
	 * @return true if municipality final counts should be included in the counts structure
	 */
	public boolean includeMunicipalityFinalCounts(AreaPath operatorAreaPath, boolean penultimateRecount) {
		return penultimateRecount && userCanAccessReportingUnitForFinalCount(operatorAreaPath);
	}

	boolean userCanAccessReportingUnitForFinalCount(AreaPath operatorAreaPath) {
		return operatorAreaPath.isRootLevel() || operatorAreaPath.isCountyLevel() || reportingUnitRepository.existsFor(operatorAreaPath, VALGSTYRET);
	}

	public boolean isFinalCountReadyForSettlement(ElectionPath contestPath, FinalCount finalCount) {
		if (!finalCount.isRejectedBallotsProcessed()) {
			return false;
		}
		ReportingUnitTypeId reportingUnitTypeId = finalCount.getReportingUnitTypeId();
		return valgstyretAndMunicipalityOrBoroughElection(contestPath, reportingUnitTypeId)
				|| reportingUnitTypeId == FYLKESVALGSTYRET && isContestOnCountyLevel(contestPath.tilValghierarkiSti().tilValgdistriktSti())
				|| reportingUnitTypeId == OPPTELLINGSVALGSTYRET;
	}

	private boolean valgstyretAndMunicipalityOrBoroughElection(ElectionPath contestPath, ReportingUnitTypeId reportingUnitTypeId) {
		return reportingUnitTypeId == VALGSTYRET && isContestOnMunicipalityOrBoroughLevel(contestPath.tilValghierarkiSti().tilValgdistriktSti());
	}

	private boolean isContestOnMunicipalityOrBoroughLevel(ValgdistriktSti valgdistriktSti) {
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(valgdistriktSti);
		return contestMvElection.getContest().isOnMunicipalityLevel() || contestMvElection.getContest().isOnBoroughLevel();
	}

	private boolean isContestOnCountyLevel(ValgdistriktSti valgdistriktSti) {
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(valgdistriktSti);
		return contestMvElection.getContest().isOnCountyLevel();
	}

	public List<VoteCountDigest> voteCountDigestsForFylkesvalgstyret(Contest contest, MvArea mvArea) {
		return voteCountRepository.findDigestsByContestAndCountyReportingAreaAndMunicipalityCountingArea(contest, mvArea.getCounty(), mvArea.getMunicipality());
	}

	public List<VoteCountDigest> voteCountDigestsForValgstyret(Contest contest, MvArea mvArea) {
		return voteCountRepository.findDigestsByContestAndMunicipalityReportingArea(contest, mvArea.getMunicipality());
	}

	public List<VoteCountDigest> voteCountDigestsForOpptellingsvalgstyret(Contest contest, MvArea mvArea) {
		return voteCountRepository.findDigestsByContestAndMunicipalityCountingArea(contest, mvArea.getMunicipality());
	}
}
