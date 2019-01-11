package no.valg.eva.admin.rbac.application;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.validation.OperatorValidationManual;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.auditevents.authorization.BuypassKeySerialNumbersAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.authorization.ContactInfoChangedAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.authorization.DeleteOperatorAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.authorization.UpdateOperatorAuditEvent;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.common.rbac.BuypassOperator;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.OperatorMapper;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.common.rbac.service.ContactInfo;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.rbac.domain.OperatorDomainService;
import no.valg.eva.admin.rbac.domain.RoleAreaService;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.operator.OperatorFactory;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.util.CSVUtil;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Manntall_Søk;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Administrere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

@Remote(AdminOperatorService.class)
@Stateless(name = "AdminOperatorService")
@NoArgsConstructor
public class AdminOperatorApplicationService implements AdminOperatorService {
    @Inject
    private OperatorRepository operatorRepository;
    @Inject
    private OperatorRoleRepository operatorRoleRepository;
    @Inject
    private MvElectionRepository mvElectionRepository;
    @Inject
    private MvAreaRepository mvAreaRepository;
    @Inject
    private RoleRepository roleRepository;
    @Inject
    private RoleAreaService roleAreaService;
    @Inject
    private OperatorDomainService operatorDomainService;
    @Inject
    private VoterRepository voterRepository;
    private Validator validator;

    private static final String BUYPASS_SERIAL_NUMBER_PATTERN = "[0-9]{4}-[0-9]{4}-[0-9]{9}";
    private Pattern pattern = Pattern.compile(BUYPASS_SERIAL_NUMBER_PATTERN);

    private Operator operatorById(UserData userData, PersonId operatorId) {
        no.valg.eva.admin.rbac.domain.model.Operator operator = operatorDomainService.operatorByElectionEventAndId(userData.electionEvent(), operatorId);
        MvArea mvArea = findMvArea(AreaPath.from(userData.getOperatorMvArea().getAreaPath()));
        List<OperatorRole> operatorRoles = operatorDomainService.findAllOperatorRolesForOperatorInArea(operator, mvArea, userData);

        return OperatorMapper.toViewOperatorWithRoleAssociations(operator, operatorRoles);
    }

    @Override
    @Security(accesses = {Aggregert_Manntall_Søk, Tilgang_Brukere_Administrere}, type = READ)
    public Operator operatorOrVoterById(UserData userData, PersonId personId) {
        if (isOperator(userData, personId)) {
            return operatorById(userData, personId);
        }
        Voter voter = voterRepository.voterOfId(personId.getId(), userData.getElectionEventPk());

        Operator operator = null;
        if (voter != null) {
            operator = new Operator(PersonMapper.toPerson(voter));
        }
        return operator;
    }

    @Override
    @Security(accesses = Tilgang_Brukere_Administrere, type = READ)
    public Collection<Person> operatorsByName(UserData userData, String name) {
        Map<PersonId, Person> users = new HashMap<>();

        List<no.valg.eva.admin.rbac.domain.model.Operator> operators = operatorRepository.findOperatorsByName(userData.getElectionEventPk(), name);
        for (no.valg.eva.admin.rbac.domain.model.Operator operator : operators) {
            users.put(new PersonId(operator.getId()), PersonMapper.toOperator(operator));
        }

        List<Voter> voters = voterRepository.votersByName(userData.getElectionEventPk(), name);

        for (Voter voter : voters) {
            PersonId personId = new PersonId(voter.getId());
            if (!users.containsKey(personId)) {
                users.put(personId, PersonMapper.toPerson(voter));
            }
        }

        return new ArrayList<>(users.values()); // Problem med serialisering i WildFly uten new ArrayList..
    }

    @Override
    @Security(accesses = Tilgang_Brukere_Administrere, type = READ)
    public List<Operator> operatorsInArea(UserData userData, AreaPath areaPath) {

        if (areaPath != null && areaPath.isCountryLevel()) {
            areaPath = areaPath.toRootPath();
        }

        MvArea mvArea = findMvArea(requireNonNull(areaPath));
        List<OperatorRole> operatorsRoles = operatorDomainService.operatorRolesInArea(mvArea, userData);
        return OperatorViewToDomainMapper.toOperatorWithRoleAssociations(operatorsRoles);
    }

    @Override
    @Security(accesses = Tilgang_Brukere_Administrere, type = READ)
    public List<no.valg.eva.admin.rbac.domain.model.Operator> operatorsInAreaByName(UserData userData, @NonNull AreaPath areaPath, String searchNameString) {
        MvArea mvArea = findMvArea(areaPath);
        return operatorRepository.findOperatorsByNameAndArea(userData, mvArea, searchNameString);
    }

    @Override
    @Security(accesses = Tilgang_Brukere_Administrere, type = WRITE)
    @AuditLog(eventClass = UpdateOperatorAuditEvent.class, eventType = AuditEventTypes.Update)
    public Operator updateOperator(UserData userData, Operator operator, AreaPath areaPath,
                                   Collection<RoleAssociation> addedRoleAssociations, Collection<RoleAssociation> deletedRoleAssociations) {

        assertUserDoesNotUpdateOwnRoleAssociations(userData, operator, addedRoleAssociations, deletedRoleAssociations);

        no.valg.eva.admin.rbac.domain.model.Operator operatorEntity;
        if (isOperator(userData, operator.getPersonId())) {
            operatorEntity = operatorDomainService.operatorByElectionEventAndId(userData.electionEvent(), operator.getPersonId());
        } else {
            ElectionEvent electionEvent = userData.getOperator().getElectionEvent();
            operatorEntity = operatorRepository.create(userData, OperatorFactory.create(electionEvent, operator));
        }
        operatorEntity.setTelephoneNumber(operator.getTelephoneNumber());
        operatorEntity.setEmail(operator.getEmail());
        operatorEntity.setActive(operator.isActive());
        operatorEntity.setKeySerialNumber(operator.getKeySerialNumber());
        if (operator.getAddress() != null) {
            operatorEntity.setAddressLine1(operator.getAddress().getAddressLine1());
            operatorEntity.setPostalCode(operator.getAddress().getPostalCode());
            operatorEntity.setPostTown(operator.getAddress().getPostTown());
        } else {
            operatorEntity.setAddressLine1(null);
            operatorEntity.setPostalCode(null);
            operatorEntity.setPostTown(null);
        }

        validate(operatorEntity);

        MvArea mvArea = findMvArea(areaPath);
        List<OperatorRole> existingOperatorRoles = operatorDomainService.findAllOperatorRolesForOperatorInArea(operatorEntity, mvArea, userData);

        ignoreRoleAssociationsThatAreBothNewAndDeleted(addedRoleAssociations, deletedRoleAssociations);
        deleteRoleAssociations(userData, existingOperatorRoles, deletedRoleAssociations);
        MvElection mvElection = requireNonNull(userData.getOperatorRole().getMvElection());
        addRoleAssociations(userData, operatorEntity, mvElection, existingOperatorRoles, addedRoleAssociations);
        return operatorById(userData, operator.getPersonId());
    }

    private void validate(no.valg.eva.admin.rbac.domain.model.Operator operatorEntity) {
        Set<ConstraintViolation<no.valg.eva.admin.rbac.domain.model.Operator>> constraintViolations = getValidator().validate(operatorEntity,
                OperatorValidationManual.class);
        if (!constraintViolations.isEmpty()) {
            ConstraintViolation<no.valg.eva.admin.rbac.domain.model.Operator> v = constraintViolations.iterator().next();
            String msg = v.getMessageTemplate();
            if (msg.startsWith("{@") && msg.endsWith("}")) {
                msg = msg.substring(1, msg.length() - 1);
            }
            throw new EvoteException(msg);
        }
    }

    /**
     * Objects in both collections are removed from both collections.
     * <p/>
     * Package visibility for testability.
     */
    void ignoreRoleAssociationsThatAreBothNewAndDeleted(Collection<RoleAssociation> newRoleAssociations,
                                                        Collection<RoleAssociation> deletedRoleAssociations) {
        Collection<RoleAssociation> originalNewRoleAssociations = new ArrayList<>(newRoleAssociations);

        newRoleAssociations.removeAll(deletedRoleAssociations);
        deletedRoleAssociations.removeAll(originalNewRoleAssociations);
    }

    private void assertUserDoesNotUpdateOwnRoleAssociations(UserData userData, Operator operator,
                                                            Collection<RoleAssociation> newRoleAssociations, Collection<RoleAssociation> deletedRoleAssociations) {
        if (!newRoleAssociations.isEmpty() || !deletedRoleAssociations.isEmpty()) {
            if (userData.getOperator().getId().equals(operator.getPersonId().getId())) {
                throw new EvoteException(new UserMessage("@rbac.role.kanIkkeEndreEgneRoller"));
            }
        }
    }

    private MvArea findMvArea(AreaPath areaPath) {
        MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
        if (mvArea == null) {
            throw new EvoteException("AreaPath is unknown: " + areaPath.path());
        }
        return mvArea;
    }

    private boolean isOperator(UserData userData, PersonId personId) {
        return operatorRepository.findByElectionEventsAndId(userData.getElectionEventPk(), personId.getId()) != null;
    }

    private void deleteRoleAssociations(UserData userData, List<OperatorRole> existingOperatorRoles, Collection<RoleAssociation> roleAssociationsToDelete) {
        for (OperatorRole operatorRole : existingOperatorRoles) {
            if (roleAssociationsToDelete.contains(OperatorMapper.toRoleAssociation(operatorRole))) {
                operatorRoleRepository.delete(userData, operatorRole);
            }
        }
    }

    private void addRoleAssociations(UserData userData, no.valg.eva.admin.rbac.domain.model.Operator operator, MvElection mvElection,
                                     List<OperatorRole> existingOperatorRoles,
                                     Collection<RoleAssociation> newRoleAssociations) {
        OperatorViewToDomainMapper operatorMapper = OperatorViewToDomainMapper.getInstance(mvAreaRepository, roleRepository);

        for (RoleAssociation newRoleAssociation : newRoleAssociations) {
            boolean operatorHasRoleAssociation = false;
            for (OperatorRole existingOr : existingOperatorRoles) {
                if (hasRoleAssociation(existingOr, newRoleAssociation)) {
                    operatorHasRoleAssociation = true;
                    break;
                }
            }
            if (!operatorHasRoleAssociation) {
                MvElection myMvElection = mvElection;
                if (newRoleAssociation.getElectionPath() != null) {
                    myMvElection = mvElectionRepository.finnEnkeltMedSti(newRoleAssociation.getElectionPath().tilValghierarkiSti());
                }
                OperatorRole newOperatorRole = operatorMapper.toOperatorRoleForOperator(newRoleAssociation, operator, myMvElection);
                operatorRoleRepository.create(userData, newOperatorRole);
            }
        }
    }

    private boolean hasRoleAssociation(OperatorRole operatorRole, RoleAssociation roleAssociation) {
        boolean mvAreaMatch = operatorRole.getMvArea().getAreaPath().toString().equals(roleAssociation.getArea().getAreaPath().toString());
        boolean roleMatch = operatorRole.getRole().getId().toString().equals(roleAssociation.getRole().getRoleId().toString());
        boolean electionMatch = roleAssociation.getElectionPath() == null || operatorRole.getMvElection().getElection() == null
                || roleAssociation.getElectionPath().equals(operatorRole.getMvElection().electionPath());
        return mvAreaMatch && roleMatch && electionMatch;
    }

    @Override
    @Security(accesses = Tilgang_Brukere_Administrere, type = WRITE)
    @AuditLog(eventClass = DeleteOperatorAuditEvent.class, eventType = AuditEventTypes.Delete)
    public void deleteOperator(UserData userData, Operator operator) {

        assertUserDoesNotDeleteSelf(userData, operator);

        AreaPath usersSelectedArea = AreaPath.from(userData.getOperatorMvArea().getAreaPath());
        MvArea mvArea = findMvArea(usersSelectedArea);

        no.valg.eva.admin.rbac.domain.model.Operator operatorToDelete = operatorDomainService.operatorByElectionEventAndId(userData.electionEvent(),
                operator.getPersonId());

        operatorDomainService.deleteOperatorInArea(userData, operatorToDelete, mvArea);
    }

    private void assertUserDoesNotDeleteSelf(UserData userData, Person operator) {
        if (userData.getOperator().getId().equals(operator.getPersonId().getId())) {
            throw new EvoteException("User cannot delete self");
        }
    }

    @Override
    @Security(accesses = Tilgang_Brukere_Administrere, type = READ)
    public Collection<RoleItem> assignableRolesForArea(UserData userData, AreaPath areaPath) {
        if (userData.getOperatorAreaPath().isRootLevel()) {
            return roleAreaService.findAssignableRolesForOperatorRole(userData.getOperatorRole());
        }
        return roleAreaService.assignableRolesForArea(areaPath, userData.getElectionEventPk());
    }

    @Override
    @Security(accesses = Tilgang_Brukere_Administrere, type = READ)
    public Map<RoleItem, List<PollingPlaceArea>> areasForRole(UserData userData, AreaPath areaPath) {
        return roleAreaService.areasForRole(areaPath, userData.electionEvent());
    }

    @Override
    @SecurityNone
    public ContactInfo contactInfoForOperator(UserData userData) {
        no.valg.eva.admin.rbac.domain.model.Operator operator = operatorRepository.findByPk(userData.getOperator().getPk());
        return new ContactInfo(operator.getTelephoneNumber(), operator.getEmail());
    }

    @Override
    @SecurityNone
    @AuditLog(eventClass = ContactInfoChangedAuditEvent.class, eventType = AuditEventTypes.ContactInfoChanged)
    public void updateContactInfoForOperator(UserData userData, ContactInfo contactInfo) {
        operatorRepository.findByPk(userData.getOperator().getPk()).updateContactInfo(contactInfo);
    }

    @Override
    @Security(accesses = Accesses.Import_Buypass, type = WRITE)
    @AuditLog(eventClass = BuypassKeySerialNumbersAuditEvent.class, eventType = AuditEventTypes.Update, objectSource = AuditedObjectSource.ReturnValue)
    public List<BuypassOperator> updateBuypassKeySerialNumbers(UserData userData, byte[] contents) {
        List<BuypassOperator> oppdaterte = new ArrayList<>();

        if (contents == null || contents.length == 0) {
            return oppdaterte;
        }
        try {
            return getBuypassOperators(contents, oppdaterte);
        } catch (IOException e) {
            throw new EvoteException(e.getMessage(), e);
        }

    }

    private List<BuypassOperator> getBuypassOperators(byte[] contents, List<BuypassOperator> oppdaterte) throws IOException {
        List<List<String>> rows = CSVUtil.getRowsFromFile(new ByteArrayInputStream(contents), 0, ";", EvoteConstants.CHARACTER_SET_ISO);

        for (List<String> row : rows) {
            String fnr = row.get(0);
            PersonId p = new PersonId(fnr);
            String buypassSerialNumber = row.get(1);
            List<no.valg.eva.admin.rbac.domain.model.Operator> operatorsById = operatorRepository.findOperatorsById(fnr);

            if (operatorsById != null && operatorsById.size() > 0 && buypassSerialNumber != null && validateBuypassNumber(buypassSerialNumber)) {
                BuypassOperator b = new BuypassOperator();
                b.setFnr(p);
                b.setBuypassKeySerialNumber(buypassSerialNumber);
                oppdaterte.add(b);
                for (no.valg.eva.admin.rbac.domain.model.Operator operator : operatorsById) {
                    operator.setKeySerialNumber(buypassSerialNumber);
                }
            }
        }

        return oppdaterte;
    }

    private boolean validateBuypassNumber(String buypassSerialnumber) {
        return pattern.matcher(buypassSerialnumber).matches();
    }

    Validator getValidator() {
        if (validator == null) {
            validator = Validation.buildDefaultValidatorFactory().getValidator();
        }
        return validator;
    }

    void setValidator(Validator validator) {
        this.validator = validator;
    }
}
