package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Responsibility;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ResponsibleOfficerRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.mvArea;
import static no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType.CANDIDATE_NAME;
import static no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType.POLLING_PLACE_ELECTORAL_BOARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponsibilityValidationDomainServiceBoardMemberConflictTest extends MockUtilsTestCase {

    private static final String REPORTING_UNIT_NAME_LINE = "Reporting unit name line";
    private static final String RESPONSIBILITY_PROPERTY = "@responsibility[%s].name";
    private static final String MUNICIPALITY_NAME = "Municipality name";
    private static final String MV_AREA_MUNICIPALITY = "790001.47.07.0701";
    private static final String PARTY_NAME = "Testparti";
    private static final String CONTEST_NAME = "TestContest";
    private static final String NAME_LINE = "Fornavn Mellomnavn Etternavn";
    private static final String FIRST_NAME = "Fornavn";
    private static final String MIDDLE_NAME = "Mellomnavn";
    private static final String LAST_NAME = "Etternavn";
    private static final String EMPTY_STRING = "";
    private static final String MUNICIPALITY_AREA_LEVEL_NAME = "@area_level[3].name.lowercase";

    private ResponsibilityValidationDomainService responsibilityValidationDomainService;
    private ResponsibleOfficerRepository responsibleOfficerRepository;
    private ContestRepository contestRepository;
    private Candidate candidate;
    private Affiliation affiliation;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        responsibilityValidationDomainService = initializeMocks(ResponsibilityValidationDomainService.class);
        responsibleOfficerRepository = getInjectMock(ResponsibleOfficerRepository.class);
        contestRepository = getInjectMock(ContestRepository.class);
        when(contestRepository.getReference(any(), any())).thenReturn(contest());
        candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
        affiliation = mock(Affiliation.class, RETURNS_DEEP_STUBS);
    }

    @Test
    public void checkIfCandidateHasBoardMemberOrRoleConflict_givenNoRoleAndListProposalValidation_returnsNoResponsibilityConflict() {
        givenIsValidatePollingPlaceElectoralBoardAndListProposal(false);

        List<ResponsibilityConflict> responsibilityConflicts = responsibilityValidationDomainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);

        assertThat(responsibilityConflicts.size()).isZero();
    }

    private void givenIsValidatePollingPlaceElectoralBoardAndListProposal(boolean b) {
        when(affiliation.getBallot().getContest().getElection().getElectionGroup().isValidatePollingPlaceElectoralBoardAndListProposal()).thenReturn(b);
    }

    @Test
    public void checkIfCandidateHasBoardMemberOrRoleConflict_givenNoResponsibleOfficersMatchingName_returnsNoResponsibilityConflict() {
        givenIsValidatePollingPlaceElectoralBoardAndListProposal(true);
        givenFindResponsibleOfficersMatchingName(emptyList());

        List<ResponsibilityConflict> responsibilityConflicts = responsibilityValidationDomainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);

        assertThat(responsibilityConflicts.size()).isZero();
    }

    private void givenFindResponsibleOfficersMatchingName(List<ResponsibleOfficer> objects) {
        when(responsibleOfficerRepository.findResponsibleOfficersMatchingName(any(), any())).thenReturn(objects);
    }

    @Test
    public void checkIfCandidateHasBoardMemberOrRoleConflict_givenEmptyName_returnsEmptyList() {
        givenIsValidatePollingPlaceElectoralBoardAndListProposal(true);
        givenCandidateName(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);

        List<ResponsibilityConflict> responsibilityConflicts = responsibilityValidationDomainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);

        assertThat(responsibilityConflicts).isEmpty();
    }

    private void givenCandidateName(String firstName, String middleName, String lastName) {
        when(candidate.getFirstName()).thenReturn(firstName);
        when(candidate.getMiddleName()).thenReturn(middleName);
        when(candidate.getLastName()).thenReturn(lastName);
    }

    @Test(dataProvider = "boardMemberTestData")
    public void checkIfCandidateHasBoardMemberOrRoleConflict_givenResponsibleOfficersMatchingName_returnsResponsibilityConflict(
            ResponsibilityId responsibilityId, int expectedNumberOfConflicts) {
        givenIsValidatePollingPlaceElectoralBoardAndListProposal(true);
        givenCandidateName(FIRST_NAME, MIDDLE_NAME, LAST_NAME);
        givenFindResponsibleOfficersMatchingName(singletonList(responsibleOfficer(responsibilityId)));

        List<ResponsibilityConflict> responsibilityConflicts = responsibilityValidationDomainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);

        assertThat(responsibilityConflicts.size()).isEqualTo(expectedNumberOfConflicts);
        if (expectedNumberOfConflicts > 0) {
            ResponsibilityConflict responsibilityConflict = responsibilityConflicts.get(0);
            assertThat(responsibilityConflict.getType()).isEqualTo(POLLING_PLACE_ELECTORAL_BOARD);
            assertThat(responsibilityConflict.getMessageArguments()).isEqualTo(messageArgumentsForBoardMember(responsibilityId));
        }
    }

    private ResponsibleOfficer responsibleOfficer(ResponsibilityId responsibilityId) {
        ResponsibleOfficer responsibleOfficer = new ResponsibleOfficer();
        responsibleOfficer.setReportingUnit(reportingUnit());
        responsibleOfficer.setFirstName(FIRST_NAME);
        responsibleOfficer.setMiddleName(MIDDLE_NAME);
        responsibleOfficer.setLastName(LAST_NAME);
        responsibleOfficer.updateNameLine();
        responsibleOfficer.setResponsibility(Responsibility.builder()
                .id(responsibilityId.getId())
                .build());
        return responsibleOfficer;
    }

    private ReportingUnit reportingUnit() {
        ReportingUnit reportingUnit = new ReportingUnit();
        reportingUnit.setMvArea(mvArea(MV_AREA_MUNICIPALITY, MUNICIPALITY_NAME));
        reportingUnit.setNameLine(REPORTING_UNIT_NAME_LINE);
        return reportingUnit;
    }

    private List<String> messageArgumentsForBoardMember(ResponsibilityId responsibilityId) {
        return asList(NAME_LINE, format(RESPONSIBILITY_PROPERTY, responsibilityId.getId()), MUNICIPALITY_NAME, MUNICIPALITY_AREA_LEVEL_NAME);
    }

    @Test(dataProvider = "boardMemberTestData")
    public void checkIfBoardMemberHasCandidateConflict_givenResponsibilityAndMatchingNames_returnsConflictForNonAdministrativeResponsilibites(
            ResponsibilityId responsibilityId, int expectedNumberOfConflicts) {
        when(getInjectMock(CandidateRepository.class).findCandidatesMatchingName(any(), any())).thenReturn(listOfCandidates());

        List<ResponsibilityConflict> conflicts = responsibilityValidationDomainService.checkIfBoardMemberHasCandidateConflict(FIRST_NAME, MIDDLE_NAME, LAST_NAME, AreaPath.from("000000.00"));

        assertThat(conflicts.size()).isEqualTo(expectedNumberOfConflicts);
        if (expectedNumberOfConflicts > 0) {
            ResponsibilityConflict conflict = conflicts.get(0);
            assertThat(conflict.getType()).isEqualTo(CANDIDATE_NAME);
            assertThat(conflict.getMessageArguments()).isEqualTo(messageArgumentsForCandidate());
        }
    }

    @DataProvider
    private Object[][] boardMemberTestData() {
        return new Object[][]{
                {ResponsibilityId.LEDER, 1},
                {ResponsibilityId.NESTLEDER, 1},
                {ResponsibilityId.MEDLEM, 1},
                {ResponsibilityId.VARAMEDLEM, 1},
                {ResponsibilityId.SEKRETAER, 1},
        };
    }

    private List<String> messageArgumentsForCandidate() {
        return asList(NAME_LINE, PARTY_NAME, CONTEST_NAME, MUNICIPALITY_AREA_LEVEL_NAME);
    }


    private List<Candidate> listOfCandidates() {
        return singletonList(candidate());
    }

    private Candidate candidate() {
        return Candidate.builder()
                .nameLine(NAME_LINE)
                .ballot(ballot())
                .build();
    }

    private Ballot ballot() {
        return Ballot.builder()
                .affiliation(affiliation())
                .contest(contest())
                .build();
    }

    private Affiliation affiliation() {
        return Affiliation.builder()
                .party(party())
                .build();
    }

    private Party party() {
        return Party.builder()
                .name(PARTY_NAME)
                .build();
    }

    private Contest contest() {
        return Contest.builder()
                .name(CONTEST_NAME)
                .contestAreaSet(contestAreaSet())
                .build();
    }
    
    private Set<ContestArea> contestAreaSet() {
        Set<ContestArea> set = new HashSet<>();
        set.add(contestArea());
        return set;
    }

    private ContestArea contestArea() {
        return ContestArea.builder()
                .mvArea(mvArea(MV_AREA_MUNICIPALITY, MUNICIPALITY_NAME))
                .build();
    }
}
