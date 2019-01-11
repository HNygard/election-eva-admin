package no.valg.eva.admin.rbac.application;

import no.evote.exception.EvoteException;
import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.valg.eva.admin.common.Address;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.BuypassOperator;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.ContactInfo;
import no.valg.eva.admin.common.test.data.AreaPathTestData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.rbac.RbacTestdataFactory;
import no.valg.eva.admin.rbac.domain.OperatorDomainService;
import no.valg.eva.admin.rbac.domain.RoleAreaService;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.util.IOUtil;
import org.joda.time.LocalDate;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

public class AdminOperatorApplicationServiceTest extends MockUtilsTestCase {

    private static final AreaPath AREA_PATH = AreaPath.from("424242.01");
    private static final String OPERATOR_ID = "010119598765";
    private static final String VOTER_ID = "020229598765";
    private static final String NAME = "Kristine Bogen";
    private static final long ELECTION_EVENT_PK = 42L;

    private AdminOperatorApplicationService service;
    private OperatorRoleRepository operatorRoleRepositoryMock;
    private OperatorDomainService operatorDomainServiceMock;
    private RbacTestdataFactory testdataFactory;
    private OperatorRepository operatorRepositoryMock;
    private MvAreaRepository mvAreaRepositoryMock;
    private VoterRepository voterRepositoryMock;
    private RoleAreaService roleAreaServiceMock;
    private RoleRepository roleRepositoryMock;
    private UserData userDataMock;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        service = initializeMocks(AdminOperatorApplicationService.class);
        testdataFactory = new RbacTestdataFactory(ELECTION_EVENT_PK);

        operatorRoleRepositoryMock = getInjectMock(OperatorRoleRepository.class);
        operatorDomainServiceMock = getInjectMock(OperatorDomainService.class);
        operatorRepositoryMock = getInjectMock(OperatorRepository.class);
        mvAreaRepositoryMock = getInjectMock(MvAreaRepository.class);
        roleAreaServiceMock = getInjectMock(RoleAreaService.class);
        voterRepositoryMock = getInjectMock(VoterRepository.class);
        roleRepositoryMock = getInjectMock(RoleRepository.class);
        userDataMock = mock(UserData.class, RETURNS_DEEP_STUBS);

        Validator validator = mock(Validator.class);
        service.setValidator(validator);
    }

    @Test
    public void usersByNameIsFoundFromOperatorsAndVoters() {
        UserData userData = this.userDataMock;
        when(userData.getElectionEventPk()).thenReturn(ELECTION_EVENT_PK);
        List<no.valg.eva.admin.rbac.domain.model.Operator> operators = createOperators();
        when(operatorRepositoryMock.findOperatorsByName(ELECTION_EVENT_PK, NAME)).thenReturn(operators);
        List<Voter> voters = createVoters(VOTER_ID);
        when(voterRepositoryMock.votersByName(ELECTION_EVENT_PK, NAME)).thenReturn(voters);

        Collection<Person> persons = service.operatorsByName(userData, NAME);
        assertThat(persons).hasSize(2);
    }

    @Test
    public void whenUserIsBothVoterAndOperatorUsersByNameContainsOperator() {
        UserData userData = this.userDataMock;
        when(userData.getElectionEventPk()).thenReturn(ELECTION_EVENT_PK);
        List<no.valg.eva.admin.rbac.domain.model.Operator> operators = createOperators();
        when(operatorRepositoryMock.findOperatorsByName(ELECTION_EVENT_PK, NAME)).thenReturn(operators);
        List<Voter> voters = createVoters(OPERATOR_ID); // same id as operator
        when(voterRepositoryMock.votersByName(ELECTION_EVENT_PK, NAME)).thenReturn(voters);

        Collection<Person> persons = service.operatorsByName(userData, NAME);
        assertThat(persons).hasSize(1);
    }

    private List<no.valg.eva.admin.rbac.domain.model.Operator> createOperators() {
        List<no.valg.eva.admin.rbac.domain.model.Operator> operators = new ArrayList<>();
        no.valg.eva.admin.rbac.domain.model.Operator operator = new no.valg.eva.admin.rbac.domain.model.Operator();
        operator.setId(OPERATOR_ID);
        operator.setFirstName("Ole");
        operator.setLastName("Operator");
        operators.add(operator);
        return operators;
    }

    private List<Voter> createVoters(String id) {
        List<Voter> voters = new ArrayList<>();
        Voter voter = createVoter(id);
        voters.add(voter);
        return voters;
    }

    private Voter createVoter(String id) {
        Voter voter = new Voter();
        voter.setId(id);
        voter.setFirstName("Viggo");
        voter.setLastName("Voter");
        return voter;
    }

    @Test
    public void assignableRolesForAreaAreFoundFromRepository() {
        List<RoleItem> roleItems = new ArrayList<>();
        when(roleRepositoryMock.assignableRolesForArea(AREA_PATH.getLevel(), ELECTION_EVENT_PK)).thenReturn(roleItems);
        assertEquals(service.assignableRolesForArea(userDataMock, AREA_PATH), roleItems);
    }

    @Test
    public void assignableRolesForArea_withRootUser_verifyOperatorRoleServiceExecuted() {
        UserData userDataMock = mock(UserData.class, RETURNS_DEEP_STUBS);
        when(userDataMock.getOperatorAreaPath().isRootLevel()).thenReturn(true);

        service.assignableRolesForArea(userDataMock, AREA_PATH);

        verify(roleAreaServiceMock).findAssignableRolesForOperatorRole(userDataMock.getOperatorRole());
    }

    @Test
    public void updateOperator_updatesOperatorFields() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);
        userData.setAccessCache(mock(AccessCache.class));

        no.valg.eva.admin.rbac.domain.model.Operator operatorEntity = mock(no.valg.eva.admin.rbac.domain.model.Operator.class);

        when(operatorDomainServiceMock.operatorByElectionEventAndId(userData.electionEvent(), new PersonId(OPERATOR_ID))).thenReturn(operatorEntity);
        when(operatorRepositoryMock.findByElectionEventsAndId(ELECTION_EVENT_PK, OPERATOR_ID)).thenReturn(operatorEntity);
        when(mvAreaRepositoryMock.findSingleByPath(AREA_PATH)).thenReturn(selectedMvArea);
        when(operatorRoleRepositoryMock.operatorRolesForOperatorAtOwnLevel(operatorEntity, selectedMvArea)).thenReturn(EMPTY_LIST);
        Operator operator = createOperator();
        when(operatorEntity.getId()).thenReturn(operator.getPersonId().getId());
        when(operatorEntity.getFirstName()).thenReturn(operator.getFirstName());
        when(operatorEntity.getLastName()).thenReturn(operator.getLastName());

        service.updateOperator(userData, operator, AREA_PATH, EMPTY_LIST, EMPTY_LIST);

        verify(operatorEntity).setEmail("ola-new@norge.no");
        verify(operatorEntity).setTelephoneNumber("99988777");
    }

    @Test
    public void updateOperator_updatesOperatorRoles() {
        final String newRoleId = "role3";
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);
        userData.setAccessCache(mock(AccessCache.class));
        no.valg.eva.admin.rbac.domain.model.Operator operatorEntity = testdataFactory.createOperator(OPERATOR_ID, "Ola", "Nordmann", "ola@norge.no", "11122333",
                electionEvent);
        OperatorRole existingToKeep = testdataFactory.createOperatorRole(operatorEntity, "role1", selectedMvArea, false);
        OperatorRole existingToDelete = testdataFactory.createOperatorRole(operatorEntity, "role2", selectedMvArea, false);
        Role role3 = testdataFactory.createRole(newRoleId, electionEvent, false);

        when(operatorDomainServiceMock.operatorByElectionEventAndId(userData.electionEvent(), new PersonId(OPERATOR_ID))).thenReturn(operatorEntity);
        when(operatorRepositoryMock.findByElectionEventsAndId(ELECTION_EVENT_PK, OPERATOR_ID)).thenReturn(operatorEntity);
        when(mvAreaRepositoryMock.findSingleByPath(AREA_PATH)).thenReturn(selectedMvArea);
        when(roleRepositoryMock.findByElectionEventAndId(electionEvent, existingToKeep.getRole().getId())).thenReturn(existingToKeep.getRole());
        when(roleRepositoryMock.findByElectionEventAndId(electionEvent, existingToDelete.getRole().getId())).thenReturn(existingToDelete.getRole());
        when(roleRepositoryMock.findByElectionEventAndId(electionEvent, newRoleId)).thenReturn(role3);
        when(operatorDomainServiceMock.findAllOperatorRolesForOperatorInArea(operatorEntity, selectedMvArea, userData)).thenReturn(
                asList(existingToKeep, existingToDelete));

        Operator operator = createOperator();

        RoleAssociation newRoleAssociation = new RoleAssociation(new RoleItem(newRoleId, newRoleId, false, null, new ArrayList<>()),
                selectedMvArea.toViewObject());
        RoleAssociation roleAssociationToDelete = new RoleAssociation(toRoleItem(existingToDelete.getRole()), existingToDelete.getMvArea().toViewObject());

        service.updateOperator(userData, operator, AREA_PATH, singletonList(newRoleAssociation), singletonList(roleAssociationToDelete));

        ArgumentCaptor<OperatorRole> operatorRoleArgumentCaptor = ArgumentCaptor.forClass(OperatorRole.class);

        verify(operatorRoleRepositoryMock).create(eq(userData), operatorRoleArgumentCaptor.capture());
        verify(operatorRoleRepositoryMock).delete(userData, existingToDelete);

        OperatorRole capturedOperatorRole = operatorRoleArgumentCaptor.getValue();
        assertThat(capturedOperatorRole.getOperator()).isEqualTo(operatorEntity);
        assertThat(capturedOperatorRole.getMvArea()).isEqualTo(selectedMvArea);
        assertThat(capturedOperatorRole.getRole().getId()).isEqualTo(newRoleId);
    }

    private RoleItem toRoleItem(Role role) {
        return new RoleItem(role.getId(), role.getName(), role.isUserSupport(), role.getElectionLevel(), role.levelsAsEnums());
    }

    @Test(enabled = false)
    public void updateOperatorShouldDeleteOperatorIfAllOperatorRolesAreRemoved() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);
        userData.setAccessCache(mock(AccessCache.class));
        no.valg.eva.admin.rbac.domain.model.Operator operatorEntity = testdataFactory.createOperator(OPERATOR_ID, "Ola", "Nordmann", "ola@norge.no", "11122333",
                electionEvent);
        OperatorRole existingToDelete = testdataFactory.createOperatorRole(operatorEntity, "role1", selectedMvArea, false);

        when(operatorDomainServiceMock.operatorByElectionEventAndId(userData.electionEvent(), new PersonId(OPERATOR_ID))).thenReturn(operatorEntity);
        when(mvAreaRepositoryMock.findSingleByPath(AREA_PATH)).thenReturn(selectedMvArea);
        when(roleRepositoryMock.findByElectionEventAndId(electionEvent, existingToDelete.getRole().getId())).thenReturn(existingToDelete.getRole());
        when(operatorDomainServiceMock.findAllOperatorRolesForOperatorInArea(operatorEntity, selectedMvArea, userData)).thenReturn(
                singletonList(existingToDelete));

        Operator operator = toOperatorPerson(operatorEntity);
        RoleAssociation roleAssociationToDelete = new RoleAssociation(toRoleItem(existingToDelete.getRole()), existingToDelete.getMvArea().toViewObject());

        service.updateOperator(userData, operator, AREA_PATH, EMPTY_LIST, singletonList(roleAssociationToDelete));

        verify(operatorRoleRepositoryMock).delete(userData, existingToDelete);
        verify(operatorDomainServiceMock).deleteOperatorIfNoMoreOperatorRoles(userData, operatorEntity);
    }

    private Operator toOperatorPerson(no.valg.eva.admin.rbac.domain.model.Operator operatorEntity) {
        return new Operator(new Person(new PersonId(operatorEntity.getId()), null, operatorEntity.getFirstName(), "", operatorEntity.getLastName(),
                null).withEmail(operatorEntity.getEmail()).withTelephoneNumber(operatorEntity.getTelephoneNumber()));
    }

    @Test
    public void updateCreatesNewOperatorInstanceIfUnknownOperator() {
        OperatorRole operatorRoleMock = mock(OperatorRole.class);
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);

        no.valg.eva.admin.rbac.domain.model.Operator operatorMock = mock(no.valg.eva.admin.rbac.domain.model.Operator.class);
        when(userDataMock.getOperator()).thenReturn(operatorMock);
        when(userDataMock.getElectionEventPk()).thenReturn(ELECTION_EVENT_PK);
        when(userDataMock.getOperatorRole()).thenReturn(operatorRoleMock);
        when(operatorRoleMock.getMvElection()).thenReturn(mock(MvElection.class));
        when(operatorDomainServiceMock.operatorByElectionEventAndId(any(ElectionEvent.class), eq(new PersonId(OPERATOR_ID)))).thenReturn(operatorMock);
        when(mvAreaRepositoryMock.findSingleByPath(AREA_PATH)).thenReturn(mock(MvArea.class));
        when(operatorRepositoryMock.findByElectionEventsAndId(anyLong(), anyString())).thenReturn(null);
        when(operatorRepositoryMock.create(any(UserData.class), any(no.valg.eva.admin.rbac.domain.model.Operator.class))).thenReturn(operatorMock);
        when(userDataMock.getOperatorMvArea()).thenReturn(selectedMvArea);

        Operator operator = new Operator(new Person(new PersonId(OPERATOR_ID), new LocalDate(), "Ola", null, "Nordmann", new Address("", "", "", "", "", ""))
                .withEmail("ola-new@norge.no").withTelephoneNumber("99988777"));
        when(operatorMock.getId()).thenReturn(operator.getPersonId().getId());
        when(operatorMock.getFirstName()).thenReturn(operator.getFirstName());
        when(operatorMock.getLastName()).thenReturn(operator.getLastName());

        service.updateOperator(userDataMock, operator, AREA_PATH, EMPTY_LIST, EMPTY_LIST);

        verify(operatorRepositoryMock).create(any(UserData.class), any(no.valg.eva.admin.rbac.domain.model.Operator.class));
    }

    @Test
    void updateOperator_unknownAreaPath_throwsEvoteException() {
        UserData userDataMock = this.userDataMock;
        no.valg.eva.admin.rbac.domain.model.Operator operatorMock = mock(no.valg.eva.admin.rbac.domain.model.Operator.class);
        when(userDataMock.getOperator()).thenReturn(operatorMock);
        when(mvAreaRepositoryMock.findSingleByPath(AREA_PATH)).thenReturn(null);
        when(operatorDomainServiceMock.operatorByElectionEventAndId(any(), eq(new PersonId(OPERATOR_ID)))).thenReturn(operatorMock);
        when(operatorRepositoryMock.findByElectionEventsAndId(anyLong(), anyString())).thenReturn(operatorMock);

        Operator operator = new Operator(new Person(new PersonId(OPERATOR_ID), new LocalDate(), "Ola", null, "Nordmann", new Address("", "", "", "", "", ""))
                .withEmail("ola-new@norge.no").withTelephoneNumber("99988777"));

        try {
            service.updateOperator(userDataMock, operator, AREA_PATH, EMPTY_LIST, EMPTY_LIST);
            fail("Expected EvoteException");
        } catch (EvoteException e) {
            assertThat(e.getMessage()).contains(AREA_PATH.path());
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shallThrowNpeForNullArguments_2() {
        assertFindAllOperatorsThrowsNpeForNullArguments(userDataMock, null);
    }

    @Test
    public void testOperatorsInArea_givenCountryAreaPathLevel_verifiesAreaPathRoot() {
        AreaPath areaPath = AreaPathTestData.AREA_PATH_111111_11;

        MvArea mvArea = createMock(MvArea.class);
        MvAreaRepository mvAreaRepository = getInjectMock(MvAreaRepository.class);
        when(mvAreaRepository.findSingleByPath(any(AreaPath.class))).thenReturn(mvArea);

        service.operatorsInArea(userDataMock, areaPath);

        verify(mvAreaRepository).findSingleByPath(areaPath.toRootPath());
        verify(getInjectMock(OperatorDomainService.class)).operatorRolesInArea(mvArea, userDataMock);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shallThrowNpeForNullArguments_3() {
        assertUpdateOperatorThrowsNpeForNullArguments(
                null, mock(AreaPath.class), mock(Operator.class), EMPTY_LIST, EMPTY_LIST);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shallThrowNpeForNullArguments_4() {
        assertUpdateOperatorThrowsNpeForNullArguments(
                userDataMock, null, mock(Operator.class), EMPTY_LIST, EMPTY_LIST);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shallThrowNpeForNullArguments_5() {
        assertUpdateOperatorThrowsNpeForNullArguments(
                userDataMock, mock(AreaPath.class), null, EMPTY_LIST, EMPTY_LIST);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shallThrowNpeForNullArguments_6() {
        assertUpdateOperatorThrowsNpeForNullArguments(
                userDataMock, mock(AreaPath.class), mock(Operator.class), null, EMPTY_LIST);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shallThrowNpeForNullArguments_7() {
        assertUpdateOperatorThrowsNpeForNullArguments(
                userDataMock, mock(AreaPath.class), mock(Operator.class), EMPTY_LIST, null);
    }

    private void assertFindAllOperatorsThrowsNpeForNullArguments(UserData userData, AreaPath areaPath) {
        service.operatorsInArea(userData, areaPath);
    }

    @Test
    public void testOperatorOrVoterById_givenExistingOperator_verifiesOperatorById() {
        PersonId personId = createMock(PersonId.class);

        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        UserData userData = testdataFactory.createUserData(testdataFactory.createMvArea(AREA_PATH, electionEvent));

        String operatorId = "operatorId";
        no.valg.eva.admin.rbac.domain.model.Operator domainOperator = no.valg.eva.admin.rbac.domain.model.Operator.builder()
                .id(operatorId)
                .firstName("firstName")
				.lastName("lastName")
                .build();

        when(operatorRepositoryMock.findByElectionEventsAndId(any(), any())).thenReturn(domainOperator);
        when(operatorDomainServiceMock.operatorByElectionEventAndId(any(), any())).thenReturn(domainOperator);

        Operator operator = service.operatorOrVoterById(userData, personId);
        assertNotNull(operator);
        assertEquals(operator.getPersonId().getId(), operatorId);
    }

    @Test
    public void testOperatorOrVoterById_givenNotExistingOperator_verifiesVoterById() {
        PersonId personId = createMock(PersonId.class);

        Voter voter = createVoter("theId");
        when(operatorRepositoryMock.findByElectionEventsAndId(any(), any())).thenReturn(null);
        when(voterRepositoryMock.voterOfId(any(), any())).thenReturn(voter);

        Operator operator = service.operatorOrVoterById(userDataMock, personId);
        assertNotNull(operator);
        assertEquals(operator.getPersonId().getId(), voter.getId());
    }

    private void assertUpdateOperatorThrowsNpeForNullArguments(UserData userData, AreaPath areaPath, Operator operator,
                                                               List<RoleAssociation> newRoleAssociations, List<RoleAssociation> deletedRoleAssociations) {
        service.updateOperator(userData, operator, areaPath, newRoleAssociations, deletedRoleAssociations);
    }

    @Test
    public void shallIgnoreRoleAssociationsThatAreBothNewAndDeleted() {
        RoleAssociation onlyNew = new RoleAssociation(mock(RoleItem.class), mock(PollingPlaceArea.class));
        RoleAssociation onlyDeleted = new RoleAssociation(mock(RoleItem.class), mock(PollingPlaceArea.class));
        RoleAssociation inBoth = new RoleAssociation(mock(RoleItem.class), mock(PollingPlaceArea.class));

        List<RoleAssociation> newRoleAssociations = new ArrayList<>(asList(onlyNew, inBoth));
        List<RoleAssociation> deletedRoleAssociations = new ArrayList<>(asList(onlyDeleted, inBoth));

        service.ignoreRoleAssociationsThatAreBothNewAndDeleted(newRoleAssociations, deletedRoleAssociations);

        assertThat(newRoleAssociations).containsOnly(onlyNew);
        assertThat(deletedRoleAssociations).containsOnly(onlyDeleted);
    }

    @Test
    public void deleteKnownOperator() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        UserData userData = testdataFactory.createUserData(testdataFactory.createMvArea(AREA_PATH, electionEvent));
        userData.setAccessCache(mock(AccessCache.class));
        MvArea usersSelectedArea = userData.getOperatorMvArea();
        AreaPath selectedAreaPath = AreaPath.from(usersSelectedArea.getAreaPath());
        no.valg.eva.admin.rbac.domain.model.Operator operatorEntity = testdataFactory.createOperator(OPERATOR_ID, "Ola", "Nordmann", "ola@norge.no", "99988777",
                electionEvent);

        when(mvAreaRepositoryMock.findSingleByPath(selectedAreaPath)).thenReturn(usersSelectedArea);
        when(operatorDomainServiceMock.operatorByElectionEventAndId(userData.electionEvent(), new PersonId(OPERATOR_ID))).thenReturn(operatorEntity);

        Operator operator = createOperator();

        service.deleteOperator(userData, operator);

        verify(operatorDomainServiceMock).deleteOperatorInArea(userData, operatorEntity, usersSelectedArea);
    }

    private Operator createOperator() {
        return new Operator(new Person(new PersonId(OPERATOR_ID), new LocalDate(), "Ola", null, "Nordmann", null).withEmail("ola-new@norge.no")
                .withTelephoneNumber("99988777"));
    }

    @Test
    public void deleteUnknownOperatorShouldThrowEvoteException() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea mvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(testdataFactory.createMvArea(AREA_PATH, electionEvent));
        AreaPath usersSelectedArea = AreaPath.from(userData.getOperatorMvArea().getAreaPath());

        when(mvAreaRepositoryMock.findSingleByPath(usersSelectedArea)).thenReturn(mvArea);
        when(operatorRepositoryMock.findByElectionEventsAndId(ELECTION_EVENT_PK, OPERATOR_ID)).thenReturn(null);
        doThrow(new EvoteException(OPERATOR_ID)).when(operatorDomainServiceMock).operatorByElectionEventAndId(any(ElectionEvent.class), any(PersonId.class));
        Operator operator = createOperator();

        try {
            service.deleteOperator(userData, operator);
            fail("Expected EvoteException");
        } catch (EvoteException e) {
            assertThat(e.getMessage()).contains(OPERATOR_ID);
        }

    }

    @Test
    public void userCannotDeleteHerself() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);

        Operator operator = toOperatorPerson(userData.getOperator());

        try {
            service.deleteOperator(userData, operator);
        } catch (EvoteException e) {
            assertThat(e.getMessage()).isEqualTo("User cannot delete self");
        }
    }

    @Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@rbac.role.kanIkkeEndreEgneRoller")
    public void userCannotUpdateOperatorRolesForHerself() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);

        Operator operator = toOperatorPerson(userData.getOperator());

        List<RoleAssociation> newRoleAssociations = singletonList(new RoleAssociation(new RoleItem("role1", "role1", false, null, new ArrayList<>()),
                new PollingPlaceArea(AreaPath.from("424242.01"), "Testland")));

        service.updateOperator(userData, operator, AREA_PATH, newRoleAssociations, Collections.emptyList());
    }

    @Test
    public void fetchContactInfoForUser() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);
        String email = "ola@norge.no";
        String telephoneNumber = "99988777";
        no.valg.eva.admin.rbac.domain.model.Operator operatorEntity = testdataFactory.createOperator(OPERATOR_ID, "Ola", "Nordmann", email, telephoneNumber,
                electionEvent);
        when(operatorRepositoryMock.findByPk(anyLong())).thenReturn(operatorEntity);
        ContactInfo contactInfo = service.contactInfoForOperator(userData);
        assertNotNull(contactInfo);
        assertEquals(contactInfo.getPhone(), telephoneNumber);
        assertEquals(contactInfo.getEmail(), email);
    }

    @Test
    public void updateContactInfoForUser() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);
        no.valg.eva.admin.rbac.domain.model.Operator mockOperator = mock(no.valg.eva.admin.rbac.domain.model.Operator.class, RETURNS_DEEP_STUBS);
        when(operatorRepositoryMock.findByPk(anyLong())).thenReturn(mockOperator);
        String phone = "99999999";
        String email = "a@b.com";
        service.updateContactInfoForOperator(userData, new ContactInfo(phone, email));

        verify(mockOperator).updateContactInfo(new ContactInfo(phone, email));
    }

    @Test
    public void updateBuypassKeySerialNumers_withEmptyFile_returns0() {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);

        List<BuypassOperator> operators = service.updateBuypassKeySerialNumbers(userData, null);

        assertEquals(operators.size(), 0);
    }

    @Test
    public void updateBuypassKeySerialNumers_withNonEmptyFile_returnsNumberOfUpdatedOperators() throws IOException {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);
        no.valg.eva.admin.rbac.domain.model.Operator mockOperator = mock(no.valg.eva.admin.rbac.domain.model.Operator.class, RETURNS_DEEP_STUBS);
        List<no.valg.eva.admin.rbac.domain.model.Operator> mockOperators = new ArrayList<>();
        mockOperators.add(mockOperator);
        byte[] bytes = readResourceAsUtf8("no/valg/eva/admin/rbac/service/impl/buypass-test.txt");
        when(operatorRepositoryMock.findOperatorsById(anyString())).thenReturn(mockOperators);

        List<BuypassOperator> operators = service.updateBuypassKeySerialNumbers(userData, bytes);

        assertEquals(operators.size(), 1);
    }

    @Test
    public void updateBuypassKeySerialNumers_withNonValidData_returns0() throws IOException {
        ElectionEvent electionEvent = testdataFactory.createElectionEvent(ELECTION_EVENT_PK);
        MvArea selectedMvArea = testdataFactory.createMvArea(AREA_PATH, electionEvent);
        UserData userData = testdataFactory.createUserData(selectedMvArea);
        no.valg.eva.admin.rbac.domain.model.Operator mockOperator = mock(no.valg.eva.admin.rbac.domain.model.Operator.class, RETURNS_DEEP_STUBS);
        List<no.valg.eva.admin.rbac.domain.model.Operator> mockOperators = new ArrayList<>();
        mockOperators.add(mockOperator);
        byte[] bytes = readResourceAsUtf8("no/valg/eva/admin/rbac/service/impl/buypass-invalidformat.txt");
        when(operatorRepositoryMock.findOperatorsById(anyString())).thenReturn(mockOperators);

        List<BuypassOperator> operators = service.updateBuypassKeySerialNumbers(userData, bytes);

        assertEquals(operators.size(), 0);
    }

    private byte[] readResourceAsUtf8(final String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            return IOUtil.getBytes(is);
        }
    }

}
