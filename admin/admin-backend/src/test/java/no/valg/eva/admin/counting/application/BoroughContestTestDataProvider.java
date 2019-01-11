package no.valg.eva.admin.counting.application;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.evote.service.backendmock.BackendContainer;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.status.CountyStatusEnum;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyStatus;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityStatus;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.PartyCategory;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.counting.domain.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.counting.repository.AntallStemmesedlerLagtTilSideRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * Helper class that creates testdata (fixtures) for borough contests (bydelsvalg).
 */

class BoroughContestTestDataProvider {
	public static final int ELECTION_YEAR = 2015;
	public static final String ELECTION_EVENT_ID = "950004";
	public static final String ELECTION_ID = "03";
	public static final String ELECTION_GROUP_ID = "01";
	public static final ElectionPath OSLO_ELECTION_PATH = ElectionPath.from(ELECTION_EVENT_ID + "." + ELECTION_GROUP_ID + "." + ELECTION_ID);
	public static final AreaPath OSLO_MUNICIPALITY_AREA_PATH = AreaPath.from(ELECTION_EVENT_ID).add("47.03.0301");
	public static final String GAMLE_OSLO_BOROUGH_ID = "030101";
	public static final AreaPath GAMLE_OSLO_AREA_PATH = OSLO_MUNICIPALITY_AREA_PATH.add(GAMLE_OSLO_BOROUGH_ID);
	public static final String KAMPEN_SKOLE_POLLING_DISTRICT_ID = "0103";
	public static final AreaPath KAMPEN_SKOLE_AREA_PATH = GAMLE_OSLO_AREA_PATH.add(KAMPEN_SKOLE_POLLING_DISTRICT_ID);
	public static final LocalDate ELECTION_DAY_1 = new LocalDate(2015, 9, 13);
	public static final LocalDate ELECTION_DAY_2 = new LocalDate(2015, 9, 14);

	private final BackendContainer backend;
	private final EntityManager entityManager;

	BoroughContestTestDataProvider(BackendContainer backend, EntityManager entityManager) {
		this.backend = backend;
		this.entityManager = entityManager;
	}

	public static AreaPath getAreaPathForBoroughId(String boroughId) {
		return AreaPath.from(OSLO_MUNICIPALITY_AREA_PATH + "." + boroughId);
	}

	public static ElectionPath getPathForBoroughContest(String contestId) {
		return ElectionPath.from(ELECTION_EVENT_ID + "." + ELECTION_GROUP_ID + "." + ELECTION_ID + "." + contestId);
	}

	private static LocalDate getEndDateOfBirthForElectionIn(int electionYear) {
		int birthYear = electionYear - 18;
		return new LocalDate(birthYear, 12, 31);
	}

	public BoroughContestObjects createBoroughContestInOslo() {
		// Implementation comment:
		// Do not pass BoroughContestObjects down the call-chain. This is to guarantee an execution order where dependencies are met.
		BoroughContestObjects objects = new BoroughContestObjects();

		objects.nbNoLocale = getNbNoLocale();
		objects.adminUserData = createUserDataForAdministrator(objects.nbNoLocale);
		objects.electionEvent = createElectionEvent(objects.adminUserData, objects.nbNoLocale);

		createAreas(objects.electionEvent, objects.nbNoLocale);
		copyRolesFromAdminEvent(objects.electionEvent);
		setMvAreaOnUserData(objects.adminUserData, backend.getMvAreaRepository().findSingleByPath(AreaPath.from(ELECTION_EVENT_ID)));

		objects.electionGroup = createElectionGroup(objects.adminUserData, objects.electionEvent);
		objects.election = createElection(objects.adminUserData, objects.electionGroup);
		List<NewlyCreatedContest> newlyCreatedContests = createContests(objects.adminUserData, objects.election);
		objects.registerNewlyCreatedContests(newlyCreatedContests);

		MvArea osloMunicipality = backend.getMvAreaRepository().findSingleByPath(OSLO_MUNICIPALITY_AREA_PATH);
		MvArea kampenPollingDistrict = backend.getMvAreaRepository().findSingleByPath(KAMPEN_SKOLE_AREA_PATH);
		createReportingUnit(objects.adminUserData, OSLO_ELECTION_PATH, osloMunicipality, ReportingUnitTypeId.VALGSTYRET, "Oslo");
		createReportingUnit(objects.adminUserData, OSLO_ELECTION_PATH, kampenPollingDistrict, ReportingUnitTypeId.STEMMESTYRET, "Kampen skole");

		objects.opptellingsansvarligUserData = createOpptellingsansvarligUserData(objects.nbNoLocale, objects.electionEvent);

		Party arbeiderpartiet = createArbeiderpartiet(objects.adminUserData, objects.electionEvent);

		createBallotsAndAffiliations(objects.contestIdToContestMap, arbeiderpartiet, objects.nbNoLocale, objects.adminUserData, 2);

		createElectionDay(ELECTION_DAY_1, objects.electionEvent, objects.adminUserData);
		createElectionDay(ELECTION_DAY_2, objects.electionEvent, objects.adminUserData);

		createAntallStemmesedlerLagtTilSide(objects, newlyCreatedContests, osloMunicipality);

		return objects;
	}

	private void createAntallStemmesedlerLagtTilSide(BoroughContestObjects objects, List<NewlyCreatedContest> newlyCreatedContests, MvArea osloMunicipality) {
		AntallStemmesedlerLagtTilSideRepository antallStemmesedlerLagtTilSideRepository = backend.getAntallStemmesedlerLagtTilSideRepository();
		for (NewlyCreatedContest newlyCreatedContest : newlyCreatedContests) {
			antallStemmesedlerLagtTilSideRepository.create(objects.adminUserData,
					new AntallStemmesedlerLagtTilSide(osloMunicipality.getMunicipality(), objects.election.getElectionGroup(), newlyCreatedContest.contest, 1));
		}
	}

	private void createElectionDay(LocalDate date, ElectionEvent electionEvent, UserData userData) {
		ElectionDay electionDay = new ElectionDay();
		electionDay.setDate(date);
		electionDay.setStartTime(new LocalTime(9, 0));
		electionDay.setEndTime(new LocalTime(18, 0));
		electionDay.setElectionEvent(electionEvent);
		backend.getElectionEventRepository().createElectionDay(userData, electionDay);
	}

	private void createBallotsAndAffiliations(Map<String, Contest> contestIdToContestMap, Party party, Locale locale, UserData userData, int displayOrder) {
		for (Contest contest : contestIdToContestMap.values()) {
			Ballot ballot = new Ballot(contest, party.getId(), true);
			BallotStatus approved = backend.getBallotRepository().findBallotStatusById(BallotStatus.BallotStatusValue.APPROVED.getId());
			ballot.setBallotStatus(approved);
			ballot.setLocale(locale);
			ballot.setDisplayOrder(displayOrder);
			ballot = backend.getBallotRepository().createBallot(userData, ballot);

			Affiliation affiliation = new Affiliation();
			affiliation.setBallot(ballot);
			affiliation.setParty(party);
			affiliation.setApproved(true);

			ballot.setAffiliation(affiliation);

			backend.getAffiliationRepository().createAffiliation(userData, affiliation);
			backend.getBallotRepository().updateBallot(userData, ballot);
		}
	}

	private Party createArbeiderpartiet(UserData userData, ElectionEvent electionEvent) {
		PartyCategory stortingsparti = backend.getPartyCategoryRepository().findById("1");
		Party arbeiderpartiet = new Party("A", 10, stortingsparti, electionEvent);
		arbeiderpartiet.setTranslatedPartyName("Arbeiderpartiet");
		return backend.getPartyServiceBean().create(userData, arbeiderpartiet);
	}

	private ElectionEvent createElectionEvent(UserData userData, Locale nbNo) {
		ElectionEventRepository electionEventRepository = backend.getElectionEventRepository();

		ElectionEvent electionEvent = new ElectionEvent(ELECTION_EVENT_ID, "KFB - 2015", nbNo);
		electionEvent.setElectoralRollLinesPerPage(30);
		electionEvent.setElectionEventStatus(
				electionEventRepository.findElectionEventStatusById(ElectionEventStatusEnum.CENTRAL_CONFIGURATION.id()));
		electionEvent = electionEventRepository.create(userData, electionEvent);

		return electionEvent;
	}

	private void createAreas(ElectionEvent electionEvent, Locale locale) {
		// Implementation comment: Saves about 30 s creating own objects, compared to copying from admin event

		Country norge = new Country("47", "Norge", electionEvent);
		entityManager.persist(norge);

		CountyStatus countyStatusApproved = (CountyStatus) entityManager.createQuery("select cs from CountyStatus cs where cs.id = "
				+ (Integer) CountyStatusEnum.APPROVED_CONFIGURATION.id()).getSingleResult();
		County osloFylke = new County("03", "Oslo", norge);
		osloFylke.setLocale(locale);
		osloFylke.setCountyStatus(countyStatusApproved);
		entityManager.persist(osloFylke);

		MunicipalityStatus statusApproved = (MunicipalityStatus) entityManager.createQuery("select ms from MunicipalityStatus ms where ms.id = "
				+ (Integer) MunicipalityStatusEnum.APPROVED_CONFIGURATION.id()).getSingleResult();

		Municipality osloKommune = new Municipality(AreaPath.OSLO_MUNICIPALITY_ID, "Oslo", osloFylke);
		osloKommune.setLocale(locale);
		osloKommune.setMunicipalityStatus(statusApproved);
		osloKommune.setElectronicMarkoffs(true);
		osloKommune.setRequiredProtocolCount(true);
		osloKommune.setTechnicalPollingDistrictsAllowed(true);
		entityManager.persist(osloKommune);

		Borough bydelGamleOslo = new Borough("030101", "Gamle Oslo", osloKommune);
		bydelGamleOslo.setMunicipality1(false);
		entityManager.persist(bydelGamleOslo);

		PollingDistrict kampenSkole = new PollingDistrict("0103", "Kampen skole", bydelGamleOslo);
		kampenSkole.setMunicipality(false);
		kampenSkole.setParentPollingDistrict(false);
		kampenSkole.setTechnicalPollingDistrict(false);
		entityManager.persist(kampenSkole);
	}

	private void copyRolesFromAdminEvent(ElectionEvent electionEvent) {
		ElectionEvent adminEvent = backend.getElectionEventRepository().findById(ROOT_ELECTION_EVENT_ID);

		backend.getElectionEventRepository().copyRoles(adminEvent, electionEvent);
	}

	private ElectionGroup createElectionGroup(UserData adminUserData, ElectionEvent electionEvent) {
		ElectionGroup electionGroup = new ElectionGroup(ELECTION_GROUP_ID, "Kommune- og fylkestingsvalget 2015", electionEvent);
		electionGroup.setElectionEvent(electionEvent);

		backend.getElectionGroupRepository().create(adminUserData, electionGroup);

		return electionGroup;
	}

	private Election createElection(UserData adminUserData, ElectionGroup electionGroup) {
		ElectionType electionTypeF = (ElectionType) entityManager.createQuery("select et from ElectionType et where et.id = 'F'").getSingleResult();

		Election bydelsvalg = new Election(ELECTION_ID, "Bydelsvalg 2015", electionTypeF, AreaLevelEnum.BOROUGH.getLevel(), electionGroup);
		bydelsvalg.setSingleArea(true);
		bydelsvalg.setRenumber(false);
		bydelsvalg.setWritein(true);
		bydelsvalg.setStrikeout(false);
		bydelsvalg.setBaselineVoteFactor(BigDecimal.ONE);
		bydelsvalg.setEndDateOfBirth(getEndDateOfBirthForElectionIn(ELECTION_YEAR));
		bydelsvalg.setPersonal(true);
		bydelsvalg.setCandidateRankVoteShareThreshold(BigDecimal.ZERO);
		bydelsvalg.setSettlementFirstDivisor(new BigDecimal("1.4"));
		bydelsvalg.setPenultimateRecount(true);
		bydelsvalg.setRenumberLimit(false);
		bydelsvalg.setCandidatesInContestArea(true);
		bydelsvalg.setLevelingSeats(0);
		bydelsvalg.setLevelingSeatsVoteShareThreshold(BigDecimal.ZERO);

		return backend.getElectionRepository().create(adminUserData, bydelsvalg);
	}

	private List<NewlyCreatedContest> createContests(UserData adminUserData, Election election) {
		List<NewlyCreatedContest> newlyCreatedContests = new ArrayList<>();
		newlyCreatedContests.add(createContest(adminUserData, election, "030101", "Gamle Oslo"));

		return newlyCreatedContests;
	}

	private NewlyCreatedContest createContest(UserData adminUserData, Election election, String contestId, String name) {
		Contest contest = new Contest();
		contest.setId(contestId);
		contest.setName(name);
		contest.setMinProposersOldParty(2); // varies irl.
		contest.setMinProposersNewParty(15); // varies irl.
		contest.setMinCandidates(15); // varies irl. - some have 7
		contest.setMaxCandidates(21);
		contest.setMaxWriteIn(5);
		contest.setNumberOfPositions(15);
		contest.setElection(election);

		AreaPath areaPath = getAreaPathForBoroughId(contestId); // boroughId = contestId
		MvArea boroughMvArea = backend.getMvAreaRepository().findSingleByPath(areaPath);

		contest = backend.getContestServiceBean().create(adminUserData, contest, boroughMvArea);
		return new NewlyCreatedContest(contestId, contest);
	}

	private ReportingUnit createReportingUnit(UserData userData, ElectionPath electionPath, MvArea mvArea, ReportingUnitTypeId reportingUnitTypeId,
			String name) {
		ReportingUnit reportingUnit = new ReportingUnit();
		reportingUnit.setNameLine(name);
		reportingUnit.setMvElection(backend.getMvElectionRepository().finnEnkeltMedSti(electionPath.tilValghierarkiSti()));
		reportingUnit.setMvArea(mvArea);

		ReportingUnitType reportingUnitType = (ReportingUnitType) entityManager.createQuery(
				"select rut from ReportingUnitType rut where rut.id = " + reportingUnitTypeId.getId()).getSingleResult();
		reportingUnit.setReportingUnitType(reportingUnitType);

		reportingUnit = backend.getReportingUnitRepository().create(userData, Arrays.asList(reportingUnit)).get(0);

		return reportingUnit;
	}

	private UserData createUserDataForAdministrator(Locale locale) {
		ElectionEvent adminEvent = backend.getElectionEventRepository().findById(ROOT_ELECTION_EVENT_ID);
		Operator administrator = createOperator("12345612345", "Jack", "Administrator", adminEvent);
		Role administratorRole = backend.getRoleRepository().findByElectionEventAndId(adminEvent, "system_admin");
		MvElection mvElection = backend.getMvElectionRepository().finnEnkeltMedSti(ElectionPath.from(ROOT_ELECTION_EVENT_ID).tilValghierarkiSti());
		MvArea mvArea = backend.getMvAreaRepository().findSingleByPath(AreaPath.from(ELECTION_EVENT_ID));

		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setOperator(administrator);
		operatorRole.setRole(administratorRole);
		operatorRole.setMvElection(mvElection);
		operatorRole.setMvArea(mvArea);

		UserData userData = new UserData();
		userData.setOperatorRole(operatorRole);
		userData.setLocale(locale);
		return userData;
	}

	private UserData createOpptellingsansvarligUserData(Locale nbNoLocale, ElectionEvent electionEvent) {
		Operator operator = createOperator("22014456789", "Kari", "Valgansvarlig", electionEvent);
		Role opptellingsansvarligKommuneRole = backend.getRoleRepository().findByElectionEventAndId(electionEvent, "valgansvarlig_kommune");
		MvElection mvElection = backend.getMvElectionRepository().finnEnkeltMedSti(OSLO_ELECTION_PATH.tilValghierarkiSti());
		MvArea mvArea = backend.getMvAreaRepository().findSingleByPath(OSLO_MUNICIPALITY_AREA_PATH);

		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setOperator(operator);
		operatorRole.setRole(opptellingsansvarligKommuneRole);
		operatorRole.setMvElection(mvElection);
		operatorRole.setMvArea(mvArea);

		UserData userData = new UserData();
		userData.setOperatorRole(operatorRole);
		userData.setLocale(nbNoLocale);

		return userData;
	}

	private Operator createOperator(String operatorId, String firstName, String lastName, ElectionEvent electionEvent) {
		Operator operator = new Operator();
		operator.setElectionEvent(electionEvent);
		operator.setId(operatorId);
		operator.setActive(true);
		operator.setNameLine(firstName + " " + lastName);
		operator.setFirstName(firstName);
		operator.setLastName(lastName);

		entityManager.persist(operator);

		return operator;
	}

	private void setMvAreaOnUserData(UserData userData, MvArea mvArea) {
		OperatorRole operatorRole = userData.getOperatorRole();
		operatorRole.setMvArea(mvArea);

		entityManager.persist(operatorRole);
	}

	private Locale getNbNoLocale() {
		return backend.getLocaleRepository().findById("nb-NO");
	}

	static final class BoroughContestObjects {
		private Locale nbNoLocale;
		private UserData adminUserData;
		private ElectionEvent electionEvent;
		private ElectionGroup electionGroup;
		private Election election;
		private Map<String, Contest> contestIdToContestMap = new HashMap<>();
		private UserData opptellingsansvarligUserData;

		void registerNewlyCreatedContests(List<NewlyCreatedContest> newlyCreatedContests) {
			for (NewlyCreatedContest newlyCreatedContest : newlyCreatedContests) {
				contestIdToContestMap.put(newlyCreatedContest.contestId, newlyCreatedContest.contest);
			}
		}

		ElectionEvent getElectionEvent() {
			return electionEvent;
		}

		Election getElection() {
			return election;
		}

		public UserData getOpptellingsansvarligUserData() {
			return opptellingsansvarligUserData;
		}
	}

	private static final class NewlyCreatedContest {
		private final String contestId;
		private final Contest contest;

		NewlyCreatedContest(String contestId, Contest contest) {
			this.contestId = contestId;
			this.contest = contest;
		}
	}
}

