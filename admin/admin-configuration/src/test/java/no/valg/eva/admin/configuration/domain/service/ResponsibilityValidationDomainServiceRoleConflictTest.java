package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.municipality;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.mvArea;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponsibilityValidationDomainServiceRoleConflictTest extends MockUtilsTestCase {

    private static final String COUNTY_NAME = "County name";
    private static final String MUNICIPALITY_NAME = "Municipality name";
    private static final String POLLING_DISTRICT_NAME = "Polling district name";
    private static final String POLLING_PLACE_NAME = "Polling place name";
    private static final String CONFLICTING_ROLE_NAME = "conflicting_role";

    private static final MvArea MV_AREA_COUNTY = mvArea("790001.47.07", COUNTY_NAME);
    private static final MvArea MV_AREA_MUNICIPALITY = mvArea("790001.47.07.0701", MUNICIPALITY_NAME);
    private static final MvArea MV_AREA_BOROUGH = mvArea("790001.47.03.0301.000003", "Borough name");
    private static final MvArea MV_AREA_POLLING_DISTRICT = mvArea("790001.47.07.0701.000000.0001", POLLING_DISTRICT_NAME,
            municipality(MV_AREA_MUNICIPALITY.getMunicipalityId(), MV_AREA_MUNICIPALITY.getMunicipalityName()));
    private static final MvArea MV_AREA_POLLING_PLACE = mvArea("790001.47.07.0701.000000.0001.0001", POLLING_PLACE_NAME,
            municipality(MV_AREA_MUNICIPALITY.getMunicipalityId(), MV_AREA_MUNICIPALITY.getMunicipalityName()));

    private ResponsibilityValidationDomainService domainService;
    private RoleRepository roleRepository;
    private OperatorRoleRepository operatorRoleRepository;
    private Candidate candidate;
    private Affiliation affiliation;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        domainService = initializeMocks(ResponsibilityValidationDomainService.class);
        roleRepository = getInjectMock(RoleRepository.class);
        operatorRoleRepository = getInjectMock(OperatorRoleRepository.class);
        ContestRepository contestRepository = getInjectMock(ContestRepository.class);
        affiliation = mock(Affiliation.class, RETURNS_DEEP_STUBS);
        candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
        when(candidate.getId()).thenReturn("1234567812345");
        when(contestRepository.getReference(any(), any())).thenReturn(mock(Contest.class, RETURNS_DEEP_STUBS));
    }

    @Test
    public void checkIfCandidateHasBoardMemberOrRoleConflict_givenNoRoleAndListProposaValidation_returnsNoResponsibilityConflict() {
        givenIsValidateRoleAndListProposal(false);

        List<ResponsibilityConflict> responsibilityConflicts = domainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);

        assertThat(responsibilityConflicts.size()).isZero();
    }

    private void givenIsValidateRoleAndListProposal(boolean b) {
        when(affiliation.getBallot().getContest().getElection().getElectionGroup().isValidateRoleAndListProposal()).thenReturn(b);
    }

    @Test
    public void checkIfCandidateHasBoardMemberOrRoleConflict_givenNoRoleConflict_returnsNoResponsibilityConflict() {
        givenIsValidateRoleAndListProposal(true);
        givenFindRolesByCheckCandidateConflict(emptyList());

        List<ResponsibilityConflict> responsibilityConflicts = domainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);

        assertThat(responsibilityConflicts.size()).isZero();
    }

    private void givenFindRolesByCheckCandidateConflict(List<Role> objects) {
        when(roleRepository.findRolesByCheckCandidateConflict(any())).thenReturn(objects);
    }

    @Test
    public void checkIfCandidateHasBoardMemberOrRoleConflict_givenRoleConflictAndNoOperatorConflict_returnsNoResponsibilityConflict() {
        givenIsValidateRoleAndListProposal(true);
        givenFindRolesByCheckCandidateConflict(listOfRolesToCheck());
        givenFindConflictingOperatorRoles(emptyList());

        List<ResponsibilityConflict> responsibilityConflicts = domainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);

        assertThat(responsibilityConflicts.size()).isZero();
    }

    private void givenFindConflictingOperatorRoles(List<OperatorRole> objects) {
        when(operatorRoleRepository.findConflictingOperatorRoles(any(), any(), any())).thenReturn(objects);
    }

    @Test(dataProvider = "responsibilityConflicts")
    public void checkIfCandidateHasBoardMemberOrRoleConflict_givenRoleAndOperatorConflict_returnsResponsibilityConflict(
            MvArea mvArea, ResponsibilityConflictType responsibilityConflictType, List<String> messageArguments) {
        givenIsValidateRoleAndListProposal(true);
        givenFindRolesByCheckCandidateConflict(listOfRolesToCheck());
        givenFindConflictingOperatorRoles(listOfResponsibilityConlicts(mvArea));

        List<ResponsibilityConflict> responsibilityConflicts = domainService.checkIfCandidateHasBoardMemberOrRoleConflict(candidate, affiliation);

        ResponsibilityConflict responsibilityConflict = responsibilityConflicts.get(0);
        assertThat(responsibilityConflict.getType()).isEqualTo(responsibilityConflictType);
        assertThat(responsibilityConflict.getMessageArguments()).isEqualTo(messageArguments);

    }

    @DataProvider
    public Object[][] responsibilityConflicts() {
        return new Object[][]{
                {MV_AREA_COUNTY, ResponsibilityConflictType.ROLE_COUNTY, Arrays.asList(CONFLICTING_ROLE_NAME, COUNTY_NAME)},
                {MV_AREA_MUNICIPALITY, ResponsibilityConflictType.ROLE_MUNICIPALITY, Arrays.asList(CONFLICTING_ROLE_NAME, MUNICIPALITY_NAME)},
                {MV_AREA_BOROUGH, ResponsibilityConflictType.ROLE, singletonList(CONFLICTING_ROLE_NAME)},
                {MV_AREA_POLLING_DISTRICT, ResponsibilityConflictType.ROLE_POLLING_DISTRICT, Arrays.asList(CONFLICTING_ROLE_NAME, POLLING_DISTRICT_NAME, MUNICIPALITY_NAME)},
                {MV_AREA_POLLING_PLACE, ResponsibilityConflictType.ROLE_POLLING_PLACE, Arrays.asList(CONFLICTING_ROLE_NAME, POLLING_PLACE_NAME, MUNICIPALITY_NAME)}
        };
    }

    private List<Role> listOfRolesToCheck() {
        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setId(CONFLICTING_ROLE_NAME);
        role.setName(CONFLICTING_ROLE_NAME);
        roles.add(role);
        return roles;
    }

    private List<OperatorRole> listOfResponsibilityConlicts(MvArea mvArea) {
        List<OperatorRole> operatorRoles = new ArrayList<>();
        OperatorRole operatorRole = new OperatorRole();
        operatorRole.setMvArea(mvArea);
        operatorRoles.add(operatorRole);
        return operatorRoles;
    }
}
