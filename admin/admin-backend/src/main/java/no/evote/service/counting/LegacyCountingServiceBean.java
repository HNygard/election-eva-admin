package no.evote.service.counting;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.ContestRelAreaRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.repository.ContestReportRepository;

@Default
@ApplicationScoped
public class LegacyCountingServiceBean {
	@Inject
	private ContestReportRepository contestReportRepository;
	@Inject
	private ReportCountCategoryRepository reportCountCategoryService;
	@Inject
	private ContestRelAreaRepository contestRelAreaRepository;
	@Inject
	private PollingDistrictRepository pollingDistrictRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;

	public LegacyCountingServiceBean() {

	}

	public ReportCountCategory getReportCountCategoryFromMunicipality(Municipality municipality, Contest contest, VoteCountCategory votingCountCategory) {
		try {
			ElectionGroup electionGroup = contest.getElection().getElectionGroup();
			return reportCountCategoryService.findByMunAndEGAndVCC(municipality.getPk(), electionGroup.getPk(), votingCountCategory.getPk());
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Get a contest report for at contest and reporting unit, if no one exist return a new one (not saved)
	 */
	public ContestReport makeContestReport(Contest contest, ReportingUnit reportingUnit) {
		ContestReport contestReport = contestReportRepository.findByReportingUnitContest(reportingUnit.getPk(), contest.getPk());
		if (contestReport == null) {
			contestReport = new ContestReport();
			contestReport.setReportingUnit(reportingUnit);
			contestReport.setContest(contest);
		}
		return contestReport;
	}

	/**
	 * Validate a selected polling district against configuration
	 */
	public void validateCountingStatus(
			ReportingUnit reportingUnit, Long mvElectionPk, VoteCountCategory voteCountCategory, Contest contest, CountingMode countingMode) {
		MvArea mvArea = reportingUnit.getMvArea();
		int areaLevel = mvArea.getAreaLevel();

		if (areaLevel > AreaLevelEnum.POLLING_DISTRICT.getLevel()) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0401_TO_LOW_AREA_LEVEL, null);
		}

		long pollingDistricts = countPollingDistrictsBelow(mvElectionPk, voteCountCategory, contest, mvArea);

		// kommune
		if (areaLevel == MUNICIPALITY.getLevel()) {
			// Lokalt - fordelt på krets og Nest siste styre skal ikke telle både foreløpig og endelig
			// kommune teller då ikke i det hele tatt
			if (countingMode == CountingMode.BY_POLLING_DISTRICT && !contest.isContestOrElectionPenultimateRecount()) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0403_NOT_CENTRAL_PRELIMINARY_COUNT_AND_NOT_PENULTIMATE_COUNT, null);
			}
		}

		// no lower counts
		if (pollingDistricts == 0) {
			if (areaLevel == AreaLevelEnum.COUNTY.getLevel() && contest.isContestOrElectionPenultimateRecount()) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0404_NO_LOWER_IS_READY, null);
			} else if (areaLevel == MUNICIPALITY.getLevel() && CountCategory.VO.getId().equals(voteCountCategory.getId())) {
				Municipality municipality = mvArea.getMunicipality();
				if (municipality.isRequiredProtocolCount()) {
					throw new EvoteException(ErrorCode.ERROR_CODE_0404_NO_LOWER_IS_READY, null);
				}
			}
		}
	}

	private long countPollingDistrictsBelow(Long mvElectionPk, VoteCountCategory voteCountCategory, Contest contest, MvArea mvArea) {
		if (contest.isOnBoroughLevel() && voteCountCategory.getCountCategory() != CountCategory.VO) {
			return 0;
		}
		if (contest.isOnBoroughLevel()) {
			long pollingDistricts = 0;
			List<MvArea> pollingDistrictMvAreas = mvAreaRepository.findByPathAndLevel(AreaPath.from(mvArea.getAreaPath()), AreaLevelEnum.POLLING_DISTRICT);
			for (MvArea pollingDistrictMvArea : pollingDistrictMvAreas) {
				PollingDistrict pollingDistrict = pollingDistrictMvArea.getPollingDistrict();
				if (pollingDistrict.isMunicipality() || pollingDistrict.isTechnicalPollingDistrict() || pollingDistrict.isParentPollingDistrict()) {
					continue;
				}
				ReportingUnit stemmestyret =
						reportingUnitRepository.findByAreaPathAndType(AreaPath.from(pollingDistrictMvArea.getAreaPath()), ReportingUnitTypeId.STEMMESTYRET);
				ContestReport contestReport = makeContestReport(contest, stemmestyret);
				VoteCount protocolVoteCount =
						contestReport.findFirstVoteCountByCountQualifierAndCategory(CountQualifier.PROTOCOL, voteCountCategory.getCountCategory());
				if (protocolVoteCount != null && protocolVoteCount.isApproved()) {
					pollingDistricts++;
				}
			}
			return pollingDistricts;
		}
		return contestRelAreaRepository.countPollingDistrictBelow(mvElectionPk, mvArea.getPk(), voteCountCategory.getId());
	}

	/**
	 * validates the selected area and category against configuration and lower counts
	 */
	public void validateSelectedPollingDistrictAndCategory(UserData userData, PollingDistrict pollingDistrict, Municipality municipality,
			VoteCountCategory voteCountCategory, CountingMode countingMode, MvArea reportingUnitMvArea,
			MvElection mvElection) {
		int areaLevel = reportingUnitMvArea.getAreaLevel();
		boolean isVo = CountCategory.VO.getId().equals(voteCountCategory.getId());
		boolean centralPreliminaryCount = countingMode.isCentralPreliminaryCount();
		boolean pollingDistrictCount = countingMode.isPollingDistrictCount();
		boolean technicalPollingDistrictCount = countingMode.isTechnicalPollingDistrictCount();
		boolean parentPollingDistrict = pollingDistrict.isParentPollingDistrict();
		boolean hasParentPollingDistrict = pollingDistrict.getPollingDistrict() != null;
		boolean municipalityPollingDistrict = pollingDistrict.isMunicipality();
		boolean technicalPollingDistrict = pollingDistrict.isTechnicalPollingDistrict();

		// validate config
		
		if (!pollingDistrictCount && !technicalPollingDistrictCount && !municipalityPollingDistrict
				&& (!isVo || (isVo && areaLevel < AreaLevelEnum.POLLING_DISTRICT.getLevel()))) {
			// valgt krets er ikke en som gjelder hele kommunen ved opptelling Sentralt
			throw new EvoteException("@count.error.pollingDistrict.conf");
		} else if (pollingDistrictCount && municipalityPollingDistrict) {
			// valgt krets er en som gjelder hele kommunen men opptelling skjer kretsvis
			throw new EvoteException("@count.error.pollingDistrict.conf");
		} else if (!pollingDistrictCount && municipalityPollingDistrict && isVo && areaLevel == AreaLevelEnum.POLLING_DISTRICT.getLevel()) {
			// Valgt krets kan ikke brukes. Urnetelling skall skje kretsvis
			throw new EvoteException("@count.error.pollingDistrict.protocol_on_municipality");
		} else if (hasParentPollingDistrict && areaLevel < AreaLevelEnum.POLLING_DISTRICT.getLevel()) {
			// Valgt krets kan ikke brukes. Kretsen tilhører en samlekrets.
			throw new EvoteException("@count.error.pollingDistrict_has_parent");
		} else if (parentPollingDistrict && areaLevel == AreaLevelEnum.POLLING_DISTRICT.getLevel() && centralPreliminaryCount) {
			// Valgt krets kan ikke brukes. urne telling skall ikke skje på samlekrets.
			throw new EvoteException("@count.error.pollingDistrict_is_parent");
		} else if (technicalPollingDistrict && !technicalPollingDistrictCount) {
			// Valgt krets kan ikke brukes. Det er en teknisk krets.
			throw new EvoteException("@count.error.technical_polling_district.not_technical_polling_district_count");
		} else if (!technicalPollingDistrict && technicalPollingDistrictCount) {
			// Valgt krets kan ikke brukes. Det er ikke en teknisk krets.
			throw new EvoteException("@count.error.not_technical_polling_district.technical_polling_district_count");
		}


		boolean penultimateRecount = mvElection.getContest().isContestOrElectionPenultimateRecount();
		boolean municipalityCountNeeded = !(!penultimateRecount && !centralPreliminaryCount && pollingDistrictCount);
		
		boolean areaWithLowerCounts = (!isVo && areaLevel < MUNICIPALITY.getLevel())
				|| (isVo && areaLevel < AreaLevelEnum.POLLING_DISTRICT.getLevel());

		// validate lower counts
		if (areaWithLowerCounts) {
			int countCountsOnPollingDistrict = countCountsOnPollingDistrict(pollingDistrict, voteCountCategory, reportingUnitMvArea, mvElection);
			validateAreaWithLowerCounts(userData, pollingDistrict, municipality, mvElection, areaLevel, centralPreliminaryCount, pollingDistrictCount,
					parentPollingDistrict, countCountsOnPollingDistrict, municipalityCountNeeded);
		}
	}

	// henter ut antall godkjente tellinger
	private int countCountsOnPollingDistrict(
			PollingDistrict pollingDistrict, VoteCountCategory voteCountCategory, MvArea reportingUnitMvArea, MvElection mvElection) {
		Contest contest = mvElection.getContest();
		if (contest.isOnBoroughLevel()) {
			String pollingDistrictId = pollingDistrict.getId();
			Long municipalityPk = pollingDistrict.getBorough().getMunicipality().getPk();
			MvArea countingMvArea =
					mvAreaRepository.findSingleByPollingDistrictIdAndMunicipalityPk(pollingDistrictId, municipalityPk);
			ReportingUnit stemmestyret =
					reportingUnitRepository.findByAreaPathAndType(AreaPath.from(countingMvArea.getAreaPath()), ReportingUnitTypeId.STEMMESTYRET);
			ContestReport contestReport = makeContestReport(contest, stemmestyret);
			VoteCount protocolVoteCount =
					contestReport.findFirstVoteCountByCountQualifierAndCategory(CountQualifier.PROTOCOL, voteCountCategory.getCountCategory());
			if (protocolVoteCount != null && protocolVoteCount.isApproved()) {
				return 1;
			} else {
				return 0;
			}
		}
		return contestRelAreaRepository.countCountsOnPollingDistrict(
				mvElection.getPk(), reportingUnitMvArea.getPk(), voteCountCategory.getId(), pollingDistrict.getPk());
	}

	private void validateAreaWithLowerCounts(UserData userData, PollingDistrict pollingDistrict, Municipality municipality,
			MvElection mvElection, int reportingUnitAreaLevel, boolean centralPreliminaryCount, boolean pollingDistrictCount,
			boolean parentPollingDistrict, int countCountsOnPollingDistrict, boolean municipalityCountNeeded) {
		if (reportingUnitAreaLevel == MUNICIPALITY.getLevel() && municipality.isRequiredProtocolCount()) { // Kommune VO med urnetlling
			if (pollingDistrictCount && centralPreliminaryCount && countCountsOnPollingDistrict != 1 && !parentPollingDistrict) {
				// Urne er ikke godkjent
				throw new EvoteException(ErrorCode.ERROR_CODE_0404_NO_LOWER_IS_READY, null);
			} else if (pollingDistrictCount && !centralPreliminaryCount && countCountsOnPollingDistrict != 2 && !parentPollingDistrict) {
				// Urne og føreløpig er ikke godkjent
				throw new EvoteException(ErrorCode.ERROR_CODE_0404_NO_LOWER_IS_READY, null);
			} else if (!pollingDistrictCount && !isAllLowerPollingDistrictsDone(userData, municipality, mvElection.getContest())) {
				// alle urnetelinger er ikke klare
				throw new EvoteException(ErrorCode.ERROR_CODE_0404_NO_LOWER_IS_READY, null);
			} else if (parentPollingDistrict && !isAllChildPollingDistrictsDone(userData, pollingDistrict, mvElection.getContest())) {
				// alle barnekretser er ikke klare
				throw new EvoteException(ErrorCode.ERROR_CODE_0404_NO_LOWER_IS_READY, null);
			}

		} else if (reportingUnitAreaLevel == COUNTY.getLevel()) {
			if (countCountsOnPollingDistrict == 0 && municipalityCountNeeded) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0404_NO_LOWER_IS_READY, null);
			}
		}
	}

	boolean isAllLowerPollingDistrictsDone(UserData userData, Municipality municipality, Contest contest) {
		int totalPollingDistricts = pollingDistrictRepository.countProtocolPollingDistrictsByMunicipality(municipality.getPk());
		int readyPollingDistricts = pollingDistrictRepository.countReadyProtocolPollingDistrictsByContestMunicipality(contest.getPk(), municipality.getPk());
		return readyPollingDistricts == totalPollingDistricts;
	}

	boolean isAllChildPollingDistrictsDone(UserData userData, PollingDistrict pollingDistrict, Contest contest) {
		int totalPollingDistricts = pollingDistrictRepository.countChildPollingDistricts(pollingDistrict.getPk());
		int readyPollingDistricts = pollingDistrictRepository.countReadyChildPollingDistricts(contest.getPk(), pollingDistrict.getPk());

		return readyPollingDistricts == totalPollingDistricts;
	}

	/*
	 * Setters only used for mocking in tests
	 */
	void setContestRelAreaRepository(ContestRelAreaRepository contestRelAreaService) {
		this.contestRelAreaRepository = contestRelAreaService;
	}
}
