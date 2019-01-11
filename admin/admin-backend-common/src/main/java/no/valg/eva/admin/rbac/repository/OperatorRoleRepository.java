package no.valg.eva.admin.rbac.repository;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.rbac.AreaAndElectionLevelVerifier;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

public class OperatorRoleRepository extends BaseRepository {
    private static final String OPERATOR = "operator";
    private static final String MV_AREA_PK = "mvAreaPk";
    private static final String MV_AREA = "mvArea";
    private static final String OPERATOR_PK = "operatorPk";
    private static final String MV_ELECTION = "mvElection";
    private static final String ROLE = "role";
    private static final String ELECTION_EVENT_PK = "electionEventPk";
    private static final String ACCESS_PK = "accessPk";
    private static final String OPERATOR_ID = "operatorId";
    private static final String AREA_PK = "areaPk";
    private static final String ROLE_ID = "roleId";
    private final AreaAndElectionLevelVerifier verifier = AreaAndElectionLevelVerifier.getInstance();

    /**
     * Default constructor, for use by CDI container.
     */
    public OperatorRoleRepository() {
    }

    /**
     * Constructor for use in tests.
     */
    public OperatorRoleRepository(EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * Returns a list containing all OperatorRoles assigned to the Operator
     */
    public List<OperatorRole> getOperatorRoles(Operator operator) {
        TypedQuery<OperatorRole> query = getEm().createNamedQuery("OperatorRole.findOperatorRoles", OperatorRole.class).setParameter(OPERATOR, operator);
        List<OperatorRole> operatorRoles = query.getResultList();
        Collections.sort(operatorRoles);
        return operatorRoles;
    }

    /**
     * Returns a list with all OperatorRoles that is linked to MvArea descending from the mvArea parameter.
     * <p/>
     * The order of OperatorRoles is nondeterministic when multiple objects refer to the same role. See {@link OperatorRole#compareTo(OperatorRole)}.
     */
    public List<OperatorRole> findDescOperatorsRoles(MvArea mvArea) {
        TypedQuery<OperatorRole> query = getEm().createNamedQuery("OperatorRole.findDescOperatorsRoles", OperatorRole.class).setParameter(MV_AREA_PK, mvArea.getPk());
        List<OperatorRole> operatorRoles = query.getResultList();
        Collections.sort(operatorRoles);
        return operatorRoles;
    }

    /**
     * Returns an operator's role associations at an exact mvArea. Can be used to restrict view for county administrators, so that they don't see municipality
     * users.
     *
     * @param mvArea the area to search for OperatorRoles
     * @return a list with all OperatorRoles that are linked to the MvArea
     */
    public List<OperatorRole> operatorRolesAtArea(MvArea mvArea) {
        TypedQuery<OperatorRole> query = getEm()
                .createNamedQuery("OperatorRole.findOperatorRolesAtArea", OperatorRole.class)
                .setParameter(MV_AREA, mvArea);
        List<OperatorRole> operatorRoles = query.getResultList();
        Collections.sort(operatorRoles);
        return operatorRoles;
    }

    /**
     * @param mvArea   the area from where to descend down the area hierarchy
     * @param operator the operator to find role associations for (not necessarily the the administrator performing the operation)
     * @return a list with all OperatorRoles that are linked to MvArea and Operator, descending from the MvArea
     */
    public List<OperatorRole> operatorRolesForOperatorAtOrBelowMvArea(Operator operator, MvArea mvArea) {
        TypedQuery<OperatorRole> query = getEm()
                .createNamedQuery("OperatorRole.findOperatorRolesForOperatorAtOrBelowMvArea", OperatorRole.class)
                .setParameter(MV_AREA_PK, mvArea.getPk())
                .setParameter(OPERATOR_PK, operator.getPk());
        List<OperatorRole> operatorRoles = query.getResultList();
        Collections.sort(operatorRoles);
        return operatorRoles;
    }

    /**
     * Returns role associations for an operator at an exact mvArea. Can be used to restrict view for county administrators, so that they don't see municipality
     * users.
     *
     * @param mvArea the level in the area hierarchy to search for OperatorRoles
     * @return a list with all OperatorRoles that are linked to the MvArea
     */
    public List<OperatorRole> operatorRolesForOperatorAtOwnLevel(Operator operator, MvArea mvArea) {
        TypedQuery<OperatorRole> query = getEm().createNamedQuery("OperatorRole.findOperatorRolesForOperatorAndArea", OperatorRole.class)
                .setParameter(OPERATOR, operator).setParameter(MV_AREA, mvArea);
        List<OperatorRole> operatorRoles = query.getResultList();
        Collections.sort(operatorRoles);
        return operatorRoles;
    }

    public OperatorRole create(UserData userData, OperatorRole operatorRole) {
        verifier.verifyAreaAndElectionLevels(userData, operatorRole);
        return createEntity(userData, operatorRole);
    }

    public OperatorRole update(UserData userData, OperatorRole operatorRole) {
        verifier.verifyAreaAndElectionLevels(userData, operatorRole);
        return updateEntity(userData, operatorRole);
    }

    public void delete(UserData userData, List<OperatorRole> or) {
        for (OperatorRole operatorRole : or) {
            verifier.verifyAreaAndElectionLevels(userData, operatorRole);
        }
        deleteEntities(userData, or);
    }

    public void delete(UserData userData, Long pk) {
        deleteEntity(userData, OperatorRole.class, pk);
    }

    public void delete(UserData userData, OperatorRole operatorRole) {
        verifier.verifyAreaAndElectionLevels(userData, operatorRole);
        deleteEntity(userData, OperatorRole.class, operatorRole.getPk());
    }

    public List<OperatorRole> findAll() {
        List<OperatorRole> operatorRoles = super.findAllEntities(OperatorRole.class);
        Collections.sort(operatorRoles);
        return operatorRoles;
    }

    public OperatorRole findByPk(Long operatorRolePk) {
        return super.findEntityByPk(OperatorRole.class, operatorRolePk);
    }

    /**
     * Finds the OperatorRole represented by the parameters
     */
    public OperatorRole findUnique(Role role, Operator operator, MvArea mvArea, MvElection mvElection) {
        TypedQuery<OperatorRole> query = getEm()
                .createNamedQuery("OperatorRole.findUnique", OperatorRole.class)
                .setParameter(ROLE, role.getPk())
                .setParameter(OPERATOR, operator.getPk())
                .setParameter(MV_AREA, mvArea.getPk())
                .setParameter(MV_ELECTION, mvElection.getPk());
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns the number of Role objects that the Operator has.
     */
    public Long findUserCountForRole(Long rolePk) {
        TypedQuery<Long> query = getEm().createNamedQuery("OperatorRole.findUserCountForRole", Long.class).setParameter(ROLE, rolePk);
        return query.getSingleResult();
    }

    public List<OperatorRole> findOperatorRolesGivingOperatorAccess(Long electionEventPk, MvArea mvArea, Operator operator, Access access) {

        TypedQuery<OperatorRole> query = getEm().createNamedQuery("OperatorRole.findOperatorRolesGivingOperatorAccess", OperatorRole.class)
                .setParameter(MV_AREA_PK, mvArea.getPk())
                .setParameter(ELECTION_EVENT_PK, electionEventPk);
        query.setParameter(ACCESS_PK, access.getPk());
        query.setParameter(OPERATOR_PK, operator.getPk());

        return query.getResultList();
    }

    public List<OperatorRole> findConflictingOperatorRoles(PersonId personId, MvArea mvArea, Role role) {
        return getEm()
                .createNamedQuery("OperatorRole.findConflictingOperatorRole", OperatorRole.class)
                .setParameter(OPERATOR_ID, personId.getId())
                .setParameter(AREA_PK, mvArea.getPk())
                .setParameter(ROLE_ID, role.getId())
                .getResultList();
    }
}
