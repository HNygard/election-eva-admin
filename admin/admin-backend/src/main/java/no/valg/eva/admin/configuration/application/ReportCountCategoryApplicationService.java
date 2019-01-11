package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.ReportCountCategoryServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.config.ReportCountCategoryAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ReportCountCategory;
import no.valg.eva.admin.common.configuration.service.ReportCountCategoryService;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.service.CountingModeDomainService;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.service.settlement.CountCategoryDomainService;

@Stateless(name = "ReportCountCategoryService")
@Remote(ReportCountCategoryService.class)
public class ReportCountCategoryApplicationService implements ReportCountCategoryService {

	private MunicipalityRepository municipalityRepository;
	private ReportCountCategoryServiceBean reportCountCategoryServiceBean;
	private ContestRepository contestRepository;
	private CountingModeDomainService countingModeDomainService;
	private CountCategoryDomainService countCategoryDomainService;
	private MvElectionRepository mvElectionRepository;

	@Inject
	public ReportCountCategoryApplicationService(MunicipalityRepository municipalityRepository,
			ReportCountCategoryServiceBean reportCountCategoryServiceBean, ContestRepository contestRepository,
			CountingModeDomainService countingModeDomainService, CountCategoryDomainService countCategoryDomainService,
			MvElectionRepository mvElectionRepository) {
		this.municipalityRepository = municipalityRepository;
		this.reportCountCategoryServiceBean = reportCountCategoryServiceBean;
		this.contestRepository = contestRepository;
		this.countingModeDomainService = countingModeDomainService;
		this.countCategoryDomainService = countCategoryDomainService;
		this.mvElectionRepository = mvElectionRepository;
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public List<ReportCountCategory> findCountCategoriesByArea(UserData userData, AreaPath areaPath, ElectionPath electionGroupPath) {
		areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
		electionGroupPath.assertElectionGroupLevel();

		Municipality municipality = getMunicipality(userData, areaPath);

		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionGroupPath.tilValghierarkiSti());

		List<no.valg.eva.admin.configuration.domain.model.ReportCountCategory> categories = reportCountCategoryServiceBean
				.findReportCountCategoryElementByArea(municipality, mvElection.getElectionGroup());

		List<ReportCountCategory> reportCountCategories = new ArrayList<>();
		for (no.valg.eva.admin.configuration.domain.model.ReportCountCategory category : categories) {
			// For each category, find selected mode and selectable modes. If at least 1 found, create and add CountCategory
			List<CountingMode> countingModes = new ArrayList<>();
			CountingMode selected = category.getCountingMode();
			if (category.isEditable()) {
				// Category is editable for selecting among modes, get selectable modes.
				countingModes = getSelectableModes(category);
			} else {
				// Category is disabled, add only selected as selectable.
				countingModes.add(selected);
			}
			if (!countingModes.isEmpty()) {
				ReportCountCategory countCategory;
				if (category.getPk() == null) {
					countCategory = new ReportCountCategory(category.getCountCategory(), countingModes);
				} else {
					countCategory = new ReportCountCategory(category.getCountCategory(), countingModes, category.getAuditOplock());
				}
				countCategory.setCountingMode(selected);
				countCategory.setEditable(category.isEditable());
				reportCountCategories.add(countCategory);
			}
		}
		return sortVotingCategoryList(reportCountCategories);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public List<ReportCountCategory> findBoroughCountCategoriesByArea(UserData userData, AreaPath areaPath) {
		areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);

		Municipality municipality = getMunicipality(userData, areaPath);
		if (!municipality.hasBoroughs()) {
			return Collections.emptyList();
		}
		Contest contest = getBoroughContest(municipality);
		if (contest == null) {
			return Collections.emptyList();
		}

		List<ReportCountCategory> reportCountCategories = createBoroughReportCountCategories(contest, municipality);
		return sortVotingCategoryList(reportCountCategories);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
	@AuditLog(eventClass = ReportCountCategoryAuditEvent.class, eventType = AuditEventTypes.Update)
	public List<ReportCountCategory> updateCountCategories(UserData userData, AreaPath areaPath, ElectionPath electionGroupPath,
			List<ReportCountCategory> reportCountCategories) {
		areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
		electionGroupPath.assertElectionGroupLevel();

		Municipality municipality = getMunicipality(userData, areaPath);
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionGroupPath.tilValghierarkiSti());

		List<no.valg.eva.admin.configuration.domain.model.ReportCountCategory> fromDb = reportCountCategoryServiceBean
				.findReportCountCategoryElementByArea(municipality, mvElection.getElectionGroup());
		List<no.valg.eva.admin.configuration.domain.model.ReportCountCategory> updateDb = new ArrayList<>();
		for (ReportCountCategory category : reportCountCategories) {
			no.valg.eva.admin.configuration.domain.model.ReportCountCategory reportCountCategory = null;
			for (no.valg.eva.admin.configuration.domain.model.ReportCountCategory db : fromDb) {
				if (category.getCategory() == db.getCountCategory()) {
					reportCountCategory = db;
					break;
				}
			}
			if (reportCountCategory == null) {
				// We expect to find a match here since "categories" originally comes from "findReportCountCategoryElementByArea(UserData, AreaPath)".
				throw new EvoteException("Could not locate ReportCountCategory " + category.getCategory() + " for " + areaPath);
			}
			reportCountCategory.checkVersion(category);
			reportCountCategory.setCentralPreliminaryCount(category.getCountingMode().isCentralPreliminaryCount());
			reportCountCategory.setPollingDistrictCount(category.getCountingMode().isPollingDistrictCount());
			reportCountCategory.setTechnicalPollingDistrictCount(category.getCountingMode().isTechnicalPollingDistrictCount());
			updateDb.add(reportCountCategory);
		}

		reportCountCategoryServiceBean.updateCategories(userData, municipality, mvElection.getElectionGroup(), updateDb);
		return findCountCategoriesByArea(userData, areaPath, electionGroupPath);
	}

	private List<CountingMode> getSelectableModes(no.valg.eva.admin.configuration.domain.model.ReportCountCategory category) {
		List<CountingMode> result = new ArrayList<>();
		// Get the default or selected mode for this ReportCountCategory
		CountingMode selected = category.getCountingMode();
		// Check each 4 modes and if they are to be included. Either because it is the selected or that is should be selectable.
		result.add(CountingMode.CENTRAL);
		if (selected == CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT || includeCentralPollingDistrict(category)) {
			result.add(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		}
		if (selected == CountingMode.BY_POLLING_DISTRICT || includePollingDistrict(category)) {
			result.add(CountingMode.BY_POLLING_DISTRICT);
		}
		if (selected == CountingMode.BY_TECHNICAL_POLLING_DISTRICT || includeTechnicalPollingDistrict(category)) {
			result.add(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
		}
		return result;
	}

	private List<ReportCountCategory> createBoroughReportCountCategories(Contest contest, Municipality municipality) {
		List<CountCategory> categories = countCategoryDomainService.countCategories(contest, municipality);
		Function<CountCategory, CountingMode> mapper = countingModeDomainService.countingModeMapper(contest, municipality);
		List<ReportCountCategory> result = new ArrayList<>();
		for (CountCategory cat : categories) {
			CountingMode mode = mapper.apply(cat);
			ReportCountCategory reportCountCategory = new ReportCountCategory(cat, Arrays.asList(mode));
			reportCountCategory.setEditable(false);
			reportCountCategory.setCountingMode(mode);
			result.add(reportCountCategory);
		}
		return result;

	}

	private Contest getBoroughContest(Municipality municipality) {
		List<Contest> boroughContests = contestRepository.findBoroughContestsInMunicipality(municipality);
		if (boroughContests.size() > 0) {
			return boroughContests.get(0);
		}
		return null;
	}

	List<ReportCountCategory> sortVotingCategoryList(List<ReportCountCategory> countCategories) {
		Map<String, ReportCountCategory> countCategoriesMap = new HashMap<>();
		for (ReportCountCategory category : countCategories) {
			countCategoriesMap.put(category.getCategory().getId(), category);
		}
		ReportCountCategory forhaand = countCategoriesMap.remove(FO.getId());
		ReportCountCategory valgting = countCategoriesMap.remove(VO.getId());
		ReportCountCategory beredskap = countCategoriesMap.remove(VB.getId());

		List<ReportCountCategory> sortedList = new ArrayList<>(countCategoriesMap.values());
		Collections.sort(sortedList, new SortById());
		if (forhaand != null) {
			sortedList.add(0, forhaand);
		}
		if (valgting != null) {
			sortedList.add(0, valgting);
		}
		if (beredskap != null) {
			sortedList.add(beredskap);
		}
		return sortedList;
	}

	static class SortById implements Comparator<ReportCountCategory>, Serializable {

		@Override
		public int compare(final ReportCountCategory o1, final ReportCountCategory o2) {
			return o1.getCategory().getId().compareTo(o2.getCategory().getId());
		}
	}

	boolean includeCentralPollingDistrict(no.valg.eva.admin.configuration.domain.model.ReportCountCategory category) {
		if (category.getVoteCountCategory().isCategoryForOrdinaryAdvanceVotes() && category.getElectionGroup().isAdvanceVoteInBallotBox()) {
			return false;
		}
		if (category.getVoteCountCategory().isMandatoryCentralCount()) {
			if (category.getVoteCountCategory().isMandatoryTotalCount()) {
				return false;
			}
		} else {
			if (category.getVoteCountCategory().isMandatoryTotalCount()) {
				return false;
			}
		}
		return true;
	}

	boolean includePollingDistrict(no.valg.eva.admin.configuration.domain.model.ReportCountCategory category) {
		if (category.getVoteCountCategory().isCategoryForOrdinaryAdvanceVotes() && category.getElectionGroup().isAdvanceVoteInBallotBox()) {
			return false;
		}
		if (category.getVoteCountCategory().isMandatoryCentralCount()) {
			return false;
		}
		if (category.getVoteCountCategory().isMandatoryTotalCount()) {
			return false;
		}
		return true;
	}

	boolean includeTechnicalPollingDistrict(no.valg.eva.admin.configuration.domain.model.ReportCountCategory category) {
		if (!category.getMunicipality().isTechnicalPollingDistrictsAllowed()) {
			return false;
		}
		return category.isTechnicalPollingDistrictCountConfigurable();
	}

	private Municipality getMunicipality(UserData userData, AreaPath areaPath) {
		return municipalityRepository.municipalityByElectionEventAndId(userData.getElectionEventPk(), areaPath.getMunicipalityId());
	}


	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public ReportCountCategory findFirstByAreaAndCountCategory(UserData userData, AreaPath areaPath, ElectionPath electionGroupPath, CountCategory countCategory) {
		areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
		electionGroupPath.assertElectionGroupLevel();
		assertCountCategoryNotNull(countCategory);
		
		return findCountCategoriesByArea(userData, areaPath, electionGroupPath).stream()
				.filter(rcc -> rcc.getCategory().getId().equals(countCategory.getId()))
				.findFirst()
				.orElse(null);
	}
	
	private void assertCountCategoryNotNull(CountCategory cc) {
		if (cc == null) {
			throw new IllegalArgumentException("count category cannot be null");
		}
	}
}
