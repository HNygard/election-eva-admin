package no.valg.eva.admin.common.auditlog;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.SecurityLevel;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.model.election.GenericElectionType;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.rbac.BuypassOperator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.ContactInfo;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.configuration.domain.model.Responsibility;
import no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AuditLogTestsObjectMother {
    public static final String UID = "221100123456";
    public static final SecurityLevel SECURITY_LEVEL = SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC;
    public static final Locale LOCALE = new Locale();
    public static final String ROLE = "valgansvarlig_kommune";
    public static final String ROLE_NAME = "RoleName";
    public static final AreaPath AREA_PATH = AreaPath.from("950000.47.03.0301");
    public static final AreaPath AREA_PATH_1 = AreaPath.from("950000.47.03.0301.030101.0101");
    public static final AreaPath AREA_PATH_2 = AreaPath.from("950000.47.03.0301.030101.0102");
    public static final ElectionPath ELECTION_PATH = ElectionPath.from("950000.01");
    public static final LocalDate ELECTION_DAY_1 = new LocalDate(2015, 9, 13);
    public static final LocalDate ELECTION_DAY_2 = new LocalDate(2015, 9, 14);
    public static final int ELECTION_YEAR = 2015;
    public static final int MONTH_7 = 7;
    public static final String ELECTION_EVENT_ID = "950000";
    public static final String POLLING_PLACE_ID = "0010";
    public static final String BOROUGH_ID = "000001";
    public static final String MUNICIPALITY_ID = "0001";
    public static final String COUNTY_ID = "01";
    public static final String COUNTRY_ID = "47";
    public static final String POLLING_DISTRICT_ID = "0001";
    public static final String FIRST_NAME = "Fornavn";
    public static final String MIDDLE_NAME = "Mellomnavn";
    public static final String LAST_NAME = "Etternavn";
    public static final String PHONE = "99999999";
    public static final String EMAIL = "test@jpro.no";
    public static final String POSTAL_CODE = "9999";
    public static final Manntallsnummer MANNTALLSNUMMER = new Manntallsnummer("123456789080");
    public static final String AARSAKSKODE = "26";

    public static final InetAddress INET_ADDRESS;
    public static final String POLLING_PLACE_NAME = "PollingPlace";
    public static final String ADDRESS_LINE_1 = "Address Line 1";
    public static final String ADDRESS_LINE_2 = "Address Line 2";
    public static final String ADDRESS_LINE_3 = "Address Line 3";
    public static final String GPS_COORDINATES = "59.9156281,10.7259915,15";
    public static final String POLLING_PLACE_INFO_TEXT = "Info text";
    public static final String POST_TOWN = "Town";
    public static final int REPORTING_UNIT_TYPE_ID_4 = 4;
    public static final String NAME_LINE = "Test Testesen";
    public static final String MUNICIPALITY_NAME = "Municipality";
    public static final String LOCALE_ID = "NO-nb";
    public static final LocalTime START_TIME;
    public static final LocalTime END_TIME;
    public static final String START_TIME_STRING = "09:00";
    public static final String END_TIME_STRING = "09:00";
    public static final String TELEPHONE_NUMBER = "12345678";
    private static final String ELECTION_ID = "01";
    private static final String ELECTION_NAME = "Kommunestyrevalg";

    static {
        try {
            INET_ADDRESS = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        START_TIME = DateTimeFormat.forPattern("HH:mm").parseLocalTime(START_TIME_STRING);
        END_TIME = DateTimeFormat.forPattern("HH:mm").parseLocalTime(END_TIME_STRING);
    }

    public UserData createUserData() {
        return createUserData(createOperatorRole());
    }

    public UserData createUserData(OperatorRole operatorRole) {
        UserData userData = new UserData(UID, SECURITY_LEVEL, LOCALE, INET_ADDRESS);
        userData.setOperatorRole(operatorRole);
        return userData;
    }

    public ElectionEvent createElectionEvent() {
        ElectionEvent electionEvent = new ElectionEvent(1L);
        electionEvent.setId(ELECTION_EVENT_ID);
        ElectionEventStatus electionEventStatus = new ElectionEventStatus();
        electionEventStatus.setId(ElectionEventStatusEnum.CENTRAL_CONFIGURATION.id());
        electionEvent.setElectionEventStatus(electionEventStatus);
        electionEvent.getElectionDays().add(createElectionDay(ELECTION_DAY_1));
        electionEvent.getElectionDays().add(createElectionDay(ELECTION_DAY_2));

        return electionEvent;
    }

    public ElectionDay createElectionDay(LocalDate date) {
        ElectionDay electionDay = new ElectionDay();
        electionDay.setPk(1L);
        electionDay.setDate(date);
        electionDay.setStartTime(new LocalTime(8, 0));
        electionDay.setEndTime(new LocalTime(23, 59));
        return electionDay;

    }

    public ElectionDay createElectionDay() {
        return createElectionDay(ELECTION_DAY_2);
    }

    public Contest createContest() {

        Contest contest = new Contest();
        contest.setName("Kommune- og fylkestingsvalget 2015");
        contest.setMaxCandidates(31);
        contest.setMinCandidates(2);
        contest.setMaxWriteIn(6);
        contest.setNumberOfPositions(25);
        contest.setMaxRenumber(3);
        contest.setMinProposersNewParty(2);
        contest.setMinProposersOldParty(1);
        return contest;

    }

    public no.valg.eva.admin.common.configuration.model.election.Contest createCommonContest() {
        Contest entity = createContest();
        Election election = new Election(ElectionPath.from("111111"));
        election.setElectionPath(election.getParentElectionPath().add("22"));
        no.valg.eva.admin.common.configuration.model.election.Contest result = new no.valg.eva.admin.common.configuration.model.election.Contest(election);
        result.setId("33");
        result.setName(entity.getName());
        result.getListProposalData().setMaxCandidates(entity.getMaxCandidates());
        result.getListProposalData().setMinCandidates(entity.getMinCandidates());
        result.getListProposalData().setMaxWriteIn(entity.getMaxWriteIn());
        result.getListProposalData().setNumberOfPositions(entity.getNumberOfPositions());
        result.getListProposalData().setMaxRenumber(entity.getMaxRenumber());
        result.getListProposalData().setMinProposersNewParty(entity.getMinProposersNewParty());
        result.getListProposalData().setMinProposersOldParty(entity.getMinProposersOldParty());
        return result;
    }

    public Set<Locale> createLocales() {
        Set<Locale> locales = new LinkedHashSet<>();
        locales.add(createLocale("nn-NO"));
        locales.add(createLocale("nb-NO"));
        return locales;
    }

    private Locale createLocale(String id) {
        Locale locale = new Locale();
        locale.setId(id);
        return locale;
    }

    public OperatorRole createOperatorRole() {
        Operator operator = new Operator();
        operator.setElectionEvent(createElectionEvent());

        Role role = new Role();
        role.setId(ROLE);
        role.setName(ROLE_NAME);

        MvArea mvArea = getMvArea();
        mvArea.setAreaLevel(AreaLevelEnum.MUNICIPALITY.getLevel());
        mvArea.setAreaPath(AREA_PATH.path());
        mvArea.setMunicipalityName(MUNICIPALITY_NAME);

        MvElection mvElection = new MvElection();
        mvElection.setElectionEventId(ELECTION_EVENT_ID);
        mvElection.setElectionPath(ELECTION_PATH.path());

        OperatorRole operatorRole = new OperatorRole();
        operatorRole.setOperator(operator);
        operatorRole.setRole(role);
        operatorRole.setMvArea(mvArea);
        operatorRole.setMvElection(mvElection);

        return operatorRole;
    }

    public no.valg.eva.admin.common.rbac.Operator createOperator() {
        return new no.valg.eva.admin.common.rbac.Operator(new Person(new PersonId(UID), null, FIRST_NAME, null, LAST_NAME, null));
    }

    public Operator createDomainOperator() {
        Operator operator = new Operator();
        operator.setFirstName(FIRST_NAME);
        operator.setLastName(LAST_NAME);
        operator.setTelephoneNumber(TELEPHONE_NUMBER);
        operator.setEmail(EMAIL);
        operator.setId(UID);
        return operator;
    }

    public List<ElectionVoteCountCategory> createElectionVoteCountCategories() {
        List<ElectionVoteCountCategory> electionVoteCountCategories = new ArrayList<>();
        VoteCountCategory voteCountCategory = new VoteCountCategory();
        voteCountCategory.setId("VO");

        ElectionGroup electionGroup = new ElectionGroup();
        electionGroup.setId("01");

        ElectionVoteCountCategory electionVoteCountCategoryVO = new ElectionVoteCountCategory();
        electionVoteCountCategoryVO.setElectionGroup(electionGroup);
        electionVoteCountCategoryVO.setVoteCountCategory(voteCountCategory);
        electionVoteCountCategoryVO.setCountCategoryEditable(true);
        electionVoteCountCategoryVO.setCountCategoryEnabled(true);
        electionVoteCountCategoryVO.setTechnicalPollingDistrictCountConfigurable(true);
        electionVoteCountCategoryVO.setSpecialCover(true);
        electionVoteCountCategories.add(electionVoteCountCategoryVO);

        return electionVoteCountCategories;
    }

    public no.valg.eva.admin.common.configuration.model.election.ElectionGroup createElectionGroup() {
        no.valg.eva.admin.common.configuration.model.election.ElectionGroup electionGroup = new no.valg.eva.admin.common.configuration.model.election.ElectionGroup(
                ElectionPath.from("111111"));
        electionGroup.setId("01");
        electionGroup.setName("Kommune- og fylkestingsvalg");
        return electionGroup;
    }

    public Collection<RoleAssociation> createRoleAssociations() {
        Collection<RoleAssociation> roleAssociations = new ArrayList<>();
        roleAssociations.add(createRoleAssociation(createRoleItem("r1", "role_1", false), createArea(AREA_PATH_1, "area_1")));
        roleAssociations.add(createRoleAssociation(createRoleItem("r2", "role_2", false), createArea(AREA_PATH_2, "area_2")));
        return roleAssociations;
    }

    private RoleAssociation createRoleAssociation(RoleItem roleItem, PollingPlaceArea area) {
        return new RoleAssociation(roleItem, area);
    }

    private PollingPlaceArea createArea(AreaPath path, String name) {
        return new PollingPlaceArea(path, name);
    }

    private RoleItem createRoleItem(String id, String name, boolean userSupport) {
        return new RoleItem(id, name, userSupport, null, new ArrayList<AreaLevelEnum>());
    }

    public ContactInfo createContactInfo() {
        return new ContactInfo(PHONE, EMAIL);
    }

    public Borough createBorough() {
        return new Borough(BOROUGH_ID, "Borough", createMunicipality());
    }

    public Municipality createMunicipality() {
        Municipality municipality = new Municipality(MUNICIPALITY_ID, MUNICIPALITY_NAME, createCounty());
        Locale locale = new Locale();
        locale.setId(LOCALE_ID);
        municipality.setLocale(locale);
        return municipality;
    }

    public County createCounty() {
        County county = new County(COUNTY_ID, "County", createCountry());
        Locale locale = new Locale();
        locale.setId(LOCALE_ID);
        county.setLocale(locale);
        return county;
    }

    public Country createCountry() {
        return new Country(COUNTRY_ID, "Norge", createElectionEvent());
    }

    public PollingDistrict createPollingDistrict() {
        return new PollingDistrict(POLLING_DISTRICT_ID, "PollingDistrict", createBorough());
    }

    public int pk() {
        return 1;
    }

    public PollingPlace createPollingPlace() {
        PollingPlace pollingPlace = new PollingPlace(POLLING_PLACE_ID, POLLING_PLACE_NAME, POSTAL_CODE, createPollingDistrict());
        pollingPlace.setAddressLine1(ADDRESS_LINE_1);
        pollingPlace.setAddressLine2(ADDRESS_LINE_2);
        pollingPlace.setAddressLine3(ADDRESS_LINE_3);
        pollingPlace.setGpsCoordinates(GPS_COORDINATES);
        pollingPlace.setInfoText(POLLING_PLACE_INFO_TEXT);
        pollingPlace.setPostTown(POST_TOWN);
        return pollingPlace;
    }

    public Election createElection() {
        Election election = new Election(ELECTION_PATH);
        election.setId(ELECTION_ID);
        election.setName(ELECTION_NAME);
        election.setValgtype(Valgtype.STORTINGSVALG);
        election.setGenericElectionType(GenericElectionType.R);
        election.setEndDateOfBirth(new LocalDate(ELECTION_YEAR, MONTH_7, 1));
        return election;
    }

    public Voter createVoter() {
        Voter voter = new Voter();
        voter.setId(UID);
        voter.setNameLine(NAME_LINE);
        voter.setAddressLine1(ADDRESS_LINE_1);
        voter.setCountryId(COUNTRY_ID);
        voter.setCountyId(COUNTY_ID);
        voter.setMunicipalityId(MUNICIPALITY_ID);
        voter.setBoroughId(BOROUGH_ID);
        voter.setPollingDistrictId(POLLING_DISTRICT_ID);
        voter.setMvArea(getMvArea());
        voter.setAarsakskode(AARSAKSKODE);
        return voter;
    }

    protected MvArea getMvArea() {
        MvArea mvArea = new MvArea();
        mvArea.setElectionEventId(ELECTION_EVENT_ID);
        return mvArea;
    }

    public ResponsibleOfficer createResponsibleOfficer() {
        ResponsibleOfficer responsibleOfficer = new ResponsibleOfficer();
        responsibleOfficer.setFirstName(FIRST_NAME);
        responsibleOfficer.setMiddleName(MIDDLE_NAME);
        responsibleOfficer.setLastName(LAST_NAME);
        Responsibility responsibility = new Responsibility();
        responsibility.setId("4");
        responsibleOfficer.setResponsibility(responsibility);
        responsibleOfficer.setDisplayOrder(1);
        responsibleOfficer.setAddressLine1(ADDRESS_LINE_1);
        responsibleOfficer.setAddressLine2(ADDRESS_LINE_2);
        responsibleOfficer.setAddressLine3(ADDRESS_LINE_3);
        responsibleOfficer.setPostalCode(POSTAL_CODE);
        responsibleOfficer.setPostTown(POST_TOWN);
        responsibleOfficer.setEmail(EMAIL);
        responsibleOfficer.setTelephoneNumber(PHONE);
        return responsibleOfficer;
    }

    public ReportingUnit createReportingUnit() {
        ReportingUnitType reportingUnitType = new ReportingUnitType();
        reportingUnitType.setId(REPORTING_UNIT_TYPE_ID_4);
        ReportingUnit reportingUnit = new ReportingUnit();
        reportingUnit.setNameLine("Stemmested");
        reportingUnit.setReportingUnitType(reportingUnitType);
        reportingUnit.setAddressLine1(ADDRESS_LINE_1);
        reportingUnit.setAddressLine2(ADDRESS_LINE_2);
        reportingUnit.setAddressLine3(ADDRESS_LINE_3);
        reportingUnit.setEmail(EMAIL);
        reportingUnit.setTelephoneNumber(PHONE);
        reportingUnit.setPostalCode(POSTAL_CODE);
        reportingUnit.setPostTown(POST_TOWN);

        return reportingUnit;
    }

    public OpeningHours createOpeningHours() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setStartTime(START_TIME);
        openingHours.setEndTime(END_TIME);
        openingHours.setPollingPlace(createPollingPlace());
        openingHours.setElectionDay(createElectionDay());
        return openingHours;
    }

    public Voter createUpdateVoter() {
        Voter voter = createVoter();
        voter.setNameLine(NAME_LINE);
        voter.setEndringstype('E');
        return voter;
    }

    public List<BuypassOperator> createBuypassOperatorList() {
        List<BuypassOperator> operators = new ArrayList<>();
        BuypassOperator b = new BuypassOperator();
        PersonId p = new PersonId("12345678901");
        b.setFnr(p);
        b.setBuypassKeySerialNumber("9578-4050-000000000");
        operators.add(b);
        return operators;
    }

    public Voting createVoting(Voter voter, String votingCategoryId) {
        Voting voting = new Voting();
        voting.setVoter(voter);

        VotingCategory votingCategory = new VotingCategory();
        votingCategory.setId(votingCategoryId);
        voting.setVotingCategory(votingCategory);

        MvArea electoralRollArea = voter.getMvArea();
        electoralRollArea.setAreaPath(AREA_PATH_1.path());
        voting.setMvArea(electoralRollArea);

        PollingPlace pollingPlace = new PollingPlace();
        pollingPlace.setId("0001");
        voting.setPollingPlace(pollingPlace);

        return voting;
    }

    public Voting createVoting() {
        return createVoting(createVoter(), "FI");
    }

    public byte[] createByteArray(int length) {
        return new byte[length];
    }
}
