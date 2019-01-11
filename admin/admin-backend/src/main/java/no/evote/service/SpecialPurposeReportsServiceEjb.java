package no.evote.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.backend.i18n.MessageProvider;
import no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.ReportAuditEvent;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.reporting.model.ReportExecution;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.service.ManntallsnummerDomainService;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.counting.repository.ContestInfoRepository;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.CONTEST;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.ELECTION;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.ELECTION_EVENT;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.ELECTION_GROUP;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.REPORT_LOCALE;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Last_Ned_Underlag;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Historikk;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Valgkort;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Valgkort_Tomt;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Strekkodelapper;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * @see SpecialPurposeReportService
 */
@Stateless(name = "SpecialPurposeReportService")
@Remote(SpecialPurposeReportService.class)
public class SpecialPurposeReportsServiceEjb implements SpecialPurposeReportService {
	private static final String UNCHECKED = "unchecked";
	private static final String EMPTY = "";
	public static final String ELECTION_CARD_REPORT_URI = "/reports/EVA/095.special_purpose/900.election_card/EmptyElectionCard";
	public static final String PDF = "pdf";
	public static final String ELECTORAL_ROLL_CHANGES_REPORT_URI = "/reports/EVA/095.special_purpose/100.electoral_roll_audit/ElectoralRollChanges";
	public static final String SCANNING_BOX_LABEL_REPORT_URI = "/reports/EVA/095.special_purpose/200.sticker/ScanningBoxSticker";
	public static final String BALLOT_PRINTING_BASE_REPORT_URI = "/reports/EVA/095.special_purpose/300.base_for_list_candidates/Report_27";
	public static final String NUMBER_OF_STICKERS_PARAM = "num";
	public static final String COUNTRY_ID_PARAM = "countryId";
	public static final String COUNTY_ID_PARAM = "countyId";
	public static final String MUNICIPALITY_ID_PARAM = "municipalityId";
	public static final String MUNICIPALITY_NAME_PARAM = "municipalityName";
	public static final String BOROUGH_ID_PARAM = "boroughId";
	public static final String BOROUGH_NAME_PARAM = "boroughName";
	public static final String POLLING_DISTRICT_ID_PARAM = "pollingDistrictId";
	public static final String POLLING_DISTRICT_NAME_PARAM = "pollingDistrictName";
	public static final String MV_ELECTION_PK_PARAM = "mvepk";
	public static final String CATEGORY_NAME_PARAM = "catName";
	public static final String CATEGORY_ID_PARAM = "catId";
	public static final String TYPE_PARAM = "type";
	public static final String ENDRINGSTYPE_PARAM = "endringstype";
	public static final String START_DATE_PARAM = "startDate";
	public static final String END_DATE_PARAM = "endDate";
	public static final String ELECTION_EVENT_PK_PARAM = "electionEventPk";
	public static final String SELECTED_SEARCH_MODE_PARAM = "selectedSearchMode";
	public static final String SEARCH_ONLY_APPROVED_PARAM = "searchOnlyApproved";
	public static final String DATE_MASK_PARAM = "dateMask";
	public static final String NAME_PARAM = "name";
	public static final String ADDRESS_PARAM = "address";
	public static final String ZIP_PARAM = "zip";
	public static final String TOWN_PARAM = "town";
	public static final String BIRTHDAY_PARAM = "birthday";
	public static final String DISTRICT_PARAM = "district";
	public static final String POLLING_PLACE_NAME_PARAM = "pollingPlaceName";
	public static final String ELECTION_EVENT_NAME_PARAM = "electionEventName";
	public static final String IS_SAME_ELECTION_CARD_PARAM = "isSameElectionCard";
	public static final String VOTER_PK_PARAM = "vpk";
	public static final String MANNTALLSNUMMER_PARAM = "manntallsnummer";
	public static final String REPORTING_UNIT_ADDR_HEADER_PARAM = "repUnitAddrHeader";
	public static final String REPORTING_UNIT_ADDR_HEADER_NAME_PARAM = "repUnitAddrHeaderName";
	public static final String REPORTING_UNIT_ADDR_PARAM = "repUnitAddr";
	public static final String ELECTION_NAME = "ELECTION_NAME";
	public static final String CONTEST_NAME = "CONTEST_NAME";
	public static final String EXCEL_2007 = "xlsx";

	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private ElectionRepository electionRepository;
	@Inject
	private ContestAreaRepository contestAreaRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private ContestInfoRepository contestInfoRepository;
	@Inject
	private JasperReportServiceBean jasperReportService;
	@Inject
	private AuditLogServiceBean auditLogService;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private ManntallsnummerDomainService manntallsnummerDomainService;
	@Inject
	private ContestRepository contestRepository;

	/**
	 * Generates a election card for a voter
	 */
	@Security(accesses = Manntall_Valgkort, type = READ)
	@Override
	public byte[] generateElectionCard(final UserData userData, final Long vpk, final MvArea mvArea, final MvElection mvElection) {
		Map<String, String> parameters = prepareElectionCardParameters(vpk, mvArea, mvElection);
		return rapportgenereringMedAuditlogg(userData, ELECTION_CARD_REPORT_URI, parameters, PDF);
	}

	private byte[] rapportgenereringMedAuditlogg(UserData userData, String reportUri, Map<String, String> parameters, String format) {
		ReportExecution reportExecution = jasperReportService.executeReport(userData, reportUri, parameters, format);
		auditLogService.addToAuditTrail(new ReportAuditEvent(userData, reportExecution, AuditEventTypes.GenerateReport, Outcome.Success, null));
		return reportExecution.getContent();
	}

	/**
	 * Generates a empty election card with specified voter data
	 */
	@Security(accesses = Manntall_Valgkort_Tomt, type = READ)
	@Override
	public byte[] generateEmptyElectionCard(
			UserData userData, Voter voter, ValggruppeSti valggruppeSti, KommuneSti kommuneSti, String pollingDistrictId, String pollingPlaceName) {
		Long vpk = -1L;

		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(valggruppeSti);
		MvArea mvArea = mvAreaRepository.findSingleByPath(kommuneSti.areaPath());

		Map<String, String> parameters = prepareElectionCardParameters(vpk, mvArea, mvElection);
		parameters.put(NAME_PARAM, voter.getNameLine());
		parameters.put(ADDRESS_PARAM, voter.getAddressLine1());
		parameters.put(ZIP_PARAM, voter.getPostalCode());
		parameters.put(TOWN_PARAM, voter.getPostTown());
		parameters.put(BIRTHDAY_PARAM, voter.getDateOfBirth() != null ? lastTwoDigitsOfVoterBirthDay(voter) : EMPTY);
		parameters.put(MUNICIPALITY_ID_PARAM, mvArea.getMunicipalityId());
		parameters.put(DISTRICT_PARAM, pollingDistrictId);
		parameters.put(POLLING_PLACE_NAME_PARAM, pollingPlaceName);
		return jasperReportService.executeReport(userData, ELECTION_CARD_REPORT_URI, parameters, PDF).getContent();
	}

	private String lastTwoDigitsOfVoterBirthDay(Voter voter) {
		return DateTimeFormat.forPattern("yy").print(voter.getDateOfBirth());
	}

	/**
	 * Generates a barcode report for the scanning application
	 */
	@Security(accesses = Opptelling_Strekkodelapper, type = READ)
	@SuppressWarnings(UNCHECKED)
	@Override
	public byte[] generateScanningBoxLabel(UserData userData, ElectionPath electionPath, CountCategory countCategory, AreaPath areaPath,
			Integer numberOfStickers) {
		Map<String, String> parameters = new HashMap<>();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		ContestInfo contestInfo = contestInfoRepository.contestForElectionAndArea(mvElection.getElection(), mvArea);
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(contestInfo.getElectionPath().tilValghierarkiSti());

		Municipality municipality = mvArea.getMunicipality();
		PollingDistrict pollingDistrict = mvArea.getPollingDistrict();
		String messageProperty = countCategory.messageProperty();
		parameters.put(NUMBER_OF_STICKERS_PARAM, numberOfStickers.toString());
		parameters.put(COUNTRY_ID_PARAM, mvArea.getCountryId());
		parameters.put(COUNTY_ID_PARAM, mvArea.getCountyId());
		parameters.put(MUNICIPALITY_ID_PARAM, municipality.getId());
		String municipalityName = "";
		if (mvElection.getAreaLevel() == AreaLevelEnum.COUNTY.getLevel()) {
			municipalityName = municipality.getName();
		}
		parameters.put(MUNICIPALITY_NAME_PARAM, municipalityName);
		parameters.put(BOROUGH_ID_PARAM, mvArea.getBoroughId());
		parameters.put(BOROUGH_NAME_PARAM, mvArea.getBoroughName());
		if (pollingDistrict != null) {
			parameters.put(POLLING_DISTRICT_ID_PARAM, pollingDistrict.getId());
			parameters.put(POLLING_DISTRICT_NAME_PARAM, pollingDistrict.getName());
		} else {
			parameters.put(POLLING_DISTRICT_ID_PARAM, EMPTY);
			parameters.put(POLLING_DISTRICT_NAME_PARAM, EMPTY);
		}
		parameters.put(MV_ELECTION_PK_PARAM, contestMvElection.getPk().toString());
		parameters.put(CATEGORY_NAME_PARAM, messageProperty);
		parameters.put(CATEGORY_ID_PARAM, countCategory.getId());
		parameters.put(TYPE_PARAM, "1");

		return jasperReportService.executeReport(userData, SCANNING_BOX_LABEL_REPORT_URI, parameters, PDF).getContent();
	}

	@Security(accesses = Manntall_Historikk, type = READ)
	@Override
	public byte[] generateElectoralRollHistoryForMunicipality(final UserData userData, final Municipality municipality, final char endringstype,
			final LocalDate startDate, final LocalDate endDate, final Long electionEventPk,
			final String selectedSearchMode, final Boolean searchOnlyApproved) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(MUNICIPALITY_ID_PARAM, municipality.getId());
		parameters.put(ENDRINGSTYPE_PARAM, Character.toString(endringstype));
		parameters.put(START_DATE_PARAM, startDate.toString("YYYY-MM-dd") + " 00:00:00");
		parameters.put(END_DATE_PARAM, endDate.toString("YYYY-MM-dd") + " 23:59:59");
		parameters.put(ELECTION_EVENT_PK_PARAM, electionEventPk.toString());
		parameters.put(SELECTED_SEARCH_MODE_PARAM, selectedSearchMode);
		parameters.put(SEARCH_ONLY_APPROVED_PARAM, Boolean.TRUE.equals(searchOnlyApproved) ? "true" : "false");
		parameters.put(DATE_MASK_PARAM, "YYYY-MM-DD HH24:MI:SS");
		return jasperReportService.executeReport(userData, ELECTORAL_ROLL_CHANGES_REPORT_URI, parameters, PDF).getContent();
	}

	@Override
	@Security(accesses = Listeforslag_Last_Ned_Underlag, type = READ)
	public byte[] generateBallots(UserData userData, ValgdistriktSti valgdistriktSti) {
		Contest valgdistrikt = contestRepository.findSingleByPath(valgdistriktSti.electionPath());
		ElectionEvent valghendelse = valgdistrikt.getElection().getElectionGroup().getElectionEvent();
		Map<String, String> parameters = new HashMap<>();
		parameters.put(ELECTION_EVENT, valghendelse.getId());
		parameters.put(ELECTION_GROUP, valgdistrikt.getElection().getElectionGroup().getId());
		parameters.put(ELECTION, valgdistrikt.getElection().getId());
		parameters.put(CONTEST, valgdistrikt.getId());
		parameters.put(ELECTION_NAME, resolveElectionNameFor(valgdistrikt));
		parameters.put(CONTEST_NAME, valgdistrikt.getName());

		MvArea contestArea = getContestAreaForLocale(valgdistrikt).getMvArea();

		if (contestArea.getAreaLevel() == MUNICIPALITY.getLevel()) {
			parameters.put(REPORT_LOCALE, localeWithVariant(valghendelse, contestArea.getMunicipality().getLocale()));
		} else if (contestArea.getAreaLevel() == COUNTY.getLevel()) {
			parameters.put(REPORT_LOCALE, localeWithVariant(valghendelse, contestArea.getCounty().getLocale()));
		}
		return jasperReportService.executeReport(userData, BALLOT_PRINTING_BASE_REPORT_URI, parameters, EXCEL_2007).getContent();
	}
	
	String resolveElectionNameFor(Contest contest) {

		String electionName = contest.getElectionName();
		Valgtype electionType = contest.getElection().getValgtype();
		
		if (electionType != null && isNotEmpty(contest.getContestAreaList())) {
			java.util.Locale locale = resolveLocaleForContestAreaList(contest.getContestAreaList());
			electionName = MessageProvider.get(locale, "@common.electionType[" + electionType.name() + "]");
			electionName = String.format("%s %s", electionName, resolveElectionYear(contest));
		}
		return electionName;
	}
	
	private java.util.Locale resolveLocaleForContestAreaList(List<ContestArea> cal) {
		
		final ContestArea contestArea = cal.stream()
				.filter(ca -> ca.isParentArea() || !ca.isChildArea())
				.findFirst()
				.orElse(null);
		
		if (contestArea != null) {
			Locale areaLocale = null;
			MvArea mva = contestArea.getMvArea();
			AreaLevelEnum areaLevel = mva.getActualAreaLevel();

			if (areaLevel == AreaLevelEnum.COUNTY) {
				areaLocale = mva.getCounty().getLocale();
			}
			else if (areaLevel.equalOrlowerThan(AreaLevelEnum.MUNICIPALITY)) {
				areaLocale = mva.getMunicipality().getLocale();
			}
			
			if (areaLocale != null) {
				return areaLocale.toJavaLocale();
			}
		}
		return EvoteConstants.DEFAULT_JAVA_LOCALE;
	}
	
	private String resolveElectionYear(Contest contest) {
        Set<ElectionDay> eDays = contest.getElection().getElectionGroup().getElectionEvent().getElectionDays();
        if (eDays != null && !eDays.isEmpty()) {
            return eDays.iterator().next().electionYear();
        }
        return "";
    }

	private ContestArea getContestAreaForLocale(Contest valgdistrikt) {
		if (valgdistrikt.getContestAreaSet().size() == 1) {
			return valgdistrikt.getContestAreaSet().iterator().next();
		}
		// Find parent
		for (Iterator<ContestArea> iter = valgdistrikt.getContestAreaSet().iterator(); iter.hasNext();) {
			ContestArea contestArea = iter.next();
			if (contestArea.isParentArea()) {
				return contestArea;
			}
		}
		throw new RuntimeException("Unable to find ContestArea for locale for contest " + valgdistrikt.electionPath());
	}

	private String localeWithVariant(ElectionEvent electionEvent, Locale locale) {
		return format("%s_%s", locale.toJavaLocale().toString(), electionEvent.getId());
	}

	private String getDateMaskFromMunicipality(final Municipality municipality) {
		if (municipality.getLocale().getId().equals("en-GB")) {
			return "DD/MM/YYYY";
		}
		return "DD.MM.YYYY";
	}

	/**
	 * Finds and adds standard information for election card
	 */
	private Map<String, String> prepareElectionCardParameters(Long voterPk, MvArea mvArea, MvElection mvElection) {
		Map<String, String> parameters = new HashMap<>();
		ElectionEvent electionEvent = mvElection.getElectionEvent();

		// Decide if this is sami election
		boolean isSamiElection = false;
		ElectionGroup electionGroup = mvElection.getElectionGroup();
		if (electionGroup != null) {
			parameters.put(ELECTION_EVENT_NAME_PARAM, electionGroup.getName());
			isSamiElection = isSamiElection(electionGroup);
			if (isSamiElection) {
				// Decide if municipality of voter is normal municipality or child area in sami election
				List<ContestArea> contestAreas = contestAreaRepository.findContestAreaChildForElectionGroupAndMunicipality(electionGroup.getPk(),
						mvArea.getPk());
				if (contestAreas.size() == 1) {
					// Municipality is a child Area. Assume this is Sami election. Find the parent counting electoral board (opptellings valgstyre)
					ReportingUnit countElectoralBoardReportingUnit = reportingUnitRepository.findCountElectoralBoardByContest(contestAreas.get(0).getContest());
					addReportingUnitAddress(countElectoralBoardReportingUnit, parameters, "@voting.card.count_electoral_board_reporting_unit_sami_in");
				} else {
					addReportingUnitAddress(mvArea, mvElection, parameters, "@voting.card.reporting_unit_sami_in"); // Sami and normal area
				}
			} else {
				addReportingUnitAddress(mvArea, mvElection, parameters, "@voting.card.reporting_unit_in"); // Non sami
			}
		} else {
			parameters.put(ELECTION_EVENT_NAME_PARAM, electionEvent.getName());
		}

		Voter voter = (voterPk != -1) ? voterRepository.findByPk(voterPk) : null;
		if (voter != null && voter.getNumber() != null) {
			parameters.put(MANNTALLSNUMMER_PARAM,
					manntallsnummerDomainService.beregnFulltManntallsnummer(voter.getNumber(), electionEvent).getManntallsnummer());
		}

		parameters.put(VOTER_PK_PARAM, voterPk.toString());

		if (voter != null) {
			String voterLocale = voter.getMvArea().getMunicipality().getLocale().toJavaLocale().toString();
			parameters.put(REPORT_LOCALE, voterLocale + "_" + electionEvent.getId());
		}

		parameters.put(IS_SAME_ELECTION_CARD_PARAM, isSamiElection ? "true" : "false");

		if (mvArea != null) {
			parameters.put(DATE_MASK_PARAM, getDateMaskFromMunicipality(mvArea.getMunicipality()));
		}

		return parameters;
	}

	private boolean isSamiElection(ElectionGroup electionGroup) {
		List<Election> elections = electionRepository.findElectionsByElectionGroup(electionGroup.getPk());
		return elections.size() == 1 && !elections.get(0).isSingleArea() && elections.get(0).getAreaLevel() == MUNICIPALITY.getLevel();
	}

	private void addReportingUnitAddress(final MvArea mvArea, final MvElection mvElection,
			final Map<String, String> parameters, final String addressHeaderTextId) {
		if (mvArea != null && mvElection != null) {
			ReportingUnit reportingUnit = reportingUnitRepository.findByMvElectionMvArea(mvElection.getPk(), mvArea.getPk());
			addReportingUnitAddress(reportingUnit, parameters, addressHeaderTextId);
		}
	}

	private void addReportingUnitAddress(final ReportingUnit reportingUnit, final Map<String, String> parameters,
			final String addressHeaderTextId) {
		if (reportingUnit != null) {
			parameters.put(REPORTING_UNIT_ADDR_HEADER_PARAM, addressHeaderTextId);
			parameters.put(REPORTING_UNIT_ADDR_HEADER_NAME_PARAM, reportingUnitNameLine(reportingUnit));
			parameters.put(REPORTING_UNIT_ADDR_PARAM, generateReportingUnitAddress(reportingUnit));
		} else {
			parameters.put(REPORTING_UNIT_ADDR_HEADER_PARAM, EMPTY);
			parameters.put(REPORTING_UNIT_ADDR_HEADER_NAME_PARAM, EMPTY);
			parameters.put(REPORTING_UNIT_ADDR_PARAM, EMPTY);
		}
	}

	private String reportingUnitNameLine(ReportingUnit reportingUnit) {
		String nameLine = reportingUnit.getMvArea().getMunicipalityName();
		if (nameLine == null) {
			nameLine = reportingUnit.getNameLine();
		}
		return nameLine;
	}

	private String generateReportingUnitAddress(final ReportingUnit reportingUnit) {
		StringBuilder sbuilder = new StringBuilder();

		sbuilder.append("\n");
		for (String s : new String[] { reportingUnit.getAddressLine1(), reportingUnit.getAddressLine2() }) {
			if (s != null && s.length() > 0) {
				sbuilder.append(s);
				sbuilder.append("\n");
			}
		}

		if (!StringUtils.isEmpty(reportingUnit.getPostalCode())) {
			sbuilder.append(reportingUnit.getPostalCode()).append(" ");
		}
		if (!StringUtils.isEmpty(reportingUnit.getPostTown())) {
			sbuilder.append(reportingUnit.getPostTown());
		}
		return sbuilder.toString();
	}
}
