package no.valg.eva.admin.rbac.domain.model;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * RBAC: Role granted to an operator, with additional constraints specified by links into the election and area hierarchies
 */
@Entity
@Table(name = "operator_role", uniqueConstraints = @UniqueConstraint(columnNames = {"operator_pk", "role_pk", "mv_election_pk", "mv_area_pk"}))
@AttributeOverride(name = "pk", column = @Column(name = "operator_role_pk"))
@NamedQueries({
        @NamedQuery(name = "OperatorRole.findOperatorRoles", query = "SELECT o FROM OperatorRole o WHERE o.operator = :operator"),
        @NamedQuery(name = "OperatorRole.findUnique", query = "SELECT o FROM OperatorRole o WHERE o.role.pk = :role AND o.operator.pk = :operator "
                + "AND o.mvArea.pk = :mvArea AND o.mvElection.pk = :mvElection"),
        @NamedQuery(name = "OperatorRole.findUserCountForRole", query = "SELECT COUNT(DISTINCT operator.pk) FROM OperatorRole o WHERE o.role.pk = :role"),
        @NamedQuery(name = "OperatorRole.findOperatorRolesAtArea", query = "SELECT o FROM OperatorRole o WHERE o.mvArea = :mvArea"),
        @NamedQuery(
                name = "OperatorRole.findOperatorRolesForOperatorAndArea",
                query = "SELECT o FROM OperatorRole o WHERE o.operator = :operator AND o.mvArea = :mvArea")})
// @formatter:off
@NamedNativeQueries({
        @NamedNativeQuery(name = "OperatorRole.findOperatorRolesGivingOperatorAccess", query = "SELECT DISTINCT opr2.* FROM mv_area mva3 "
                + "JOIN mv_area mva on mva.mv_area_pk = mva3.mv_area_pk "
                + "JOIN mv_area mva2 ON ((public.text2ltree(mva.area_path) OPERATOR(public.@>) public.text2ltree(mva2.area_path))) "
                + "JOIN operator_role opr2 on mva2.mv_area_pk = opr2.mv_area_pk "
                + "WHERE mva3.mv_area_pk = :mvAreaPk AND opr2.operator_pk = :operatorPk AND opr2.role_pk IN (SELECT role_pk FROM role_access_all WHERE "
                + "election_event_pk = :electionEventPk AND access_pk = :accessPk)", resultClass = OperatorRole.class),
        @NamedNativeQuery(name = "OperatorRole.findOperatorsWithAccess", query = "SELECT * FROM operator WHERE operator_pk IN "
                + "(SELECT DISTINCT opr2.operator_pk FROM mv_area mva3 "
                + "JOIN mv_area mva on mva.mv_area_pk = mva3.mv_area_pk "
                + "JOIN mv_area mva2 ON ((public.text2ltree(mva.area_path) OPERATOR(public.@>) public.text2ltree(mva2.area_path))) "
                + "JOIN operator_role opr2 on mva2.mv_area_pk = opr2.mv_area_pk "
                + "WHERE mva3.mv_area_pk = :mvAreaPk AND opr2.role_pk IN (SELECT role_pk FROM role_access_all WHERE "
                + "election_event_pk = :electionEventPk AND access_path = :accessPath))", resultClass = Operator.class),
        @NamedNativeQuery(name = "OperatorRole.findDescOperatorsRoles", query = "SELECT DISTINCT opr2.* FROM mv_area mva3 "
                + "JOIN mv_area mva on mva.mv_area_pk = mva3.mv_area_pk "
                + "JOIN mv_area mva2 ON ((public.text2ltree(mva.area_path) OPERATOR(public.@>) public.text2ltree(mva2.area_path))) "
                + "JOIN operator_role opr2 on mva2.mv_area_pk = opr2.mv_area_pk " + "WHERE mva3.mv_area_pk = :mvAreaPk", resultClass = OperatorRole.class),
        @NamedNativeQuery(name = "OperatorRole.findOperatorRolesForOperatorAtOrBelowMvArea", query = "SELECT DISTINCT opr2.* FROM mv_area mva3 "
                + "JOIN mv_area mva on mva.mv_area_pk = mva3.mv_area_pk "
                + "JOIN mv_area mva2 ON ((public.text2ltree(mva.area_path) OPERATOR(public.@>) public.text2ltree(mva2.area_path))) "
                + "JOIN operator_role opr2 on mva2.mv_area_pk = opr2.mv_area_pk "
                + "WHERE mva3.mv_area_pk = :mvAreaPk AND opr2.operator_pk = :operatorPk", resultClass = OperatorRole.class),
        @NamedNativeQuery(name = "OperatorRole.findConflictingOperatorRole", query =
                "select opr.* from mv_area mva " +
                        "join mv_area mva2 ON text2ltree(mva2.area_path) <@ text2ltree(mva.area_path) " +
                        "join operator_role opr on mva2.mv_area_pk = opr.mv_area_pk " +
                        "join operator o on opr.operator_pk = o.operator_pk " +
                        "join role r on opr.role_pk = r.role_pk " +
                        "where o.operator_id = :operatorId " +
                        "  and mva.mv_area_pk = :areaPk " +
                        "  and r.role_id = :roleId",
                resultClass = OperatorRole.class)
})

// @formatter:on
public class OperatorRole extends VersionedEntity implements java.io.Serializable, Comparable<OperatorRole> {

    private MvArea mvArea;
    private Operator operator;
    private MvElection mvElection;
    private Role role;
    private boolean isInOwnHierarchy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mv_area_pk", nullable = false)
    public MvArea getMvArea() {
        return this.mvArea;
    }

    public void setMvArea(final MvArea mvArea) {
        this.mvArea = mvArea;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_pk", nullable = false)
    public Operator getOperator() {
        return this.operator;
    }

    public void setOperator(final Operator operator) {
        this.operator = operator;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mv_election_pk", nullable = false)
    public MvElection getMvElection() {
        return this.mvElection;
    }

    public void setMvElection(final MvElection mvElection) {
        this.mvElection = mvElection;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_pk", nullable = false)
    public Role getRole() {
        return this.role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() == obj.getClass()) {
            return ((OperatorRole) obj).getRole().getPk().equals(getRole().getPk()) && ((OperatorRole) obj).getOperator().equals(getOperator())
                    && ((OperatorRole) obj).getMvArea().getPk().equals(getMvArea().getPk())
                    && ((OperatorRole) obj).getMvElection().getPk().equals(getMvElection().getPk());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getRole().hashCode() + getOperator().hashCode() + getMvArea().hashCode() + getMvElection().hashCode();
    }

    @Override
    public int compareTo(final OperatorRole o) {
        return this.getRole().compareTo(o.getRole());
    }

    @Transient
    public boolean isInOwnHierarchy() {
        return isInOwnHierarchy;
    }

    public void setInOwnHierarchy(final boolean isInOwnHierarchy) {
        this.isInOwnHierarchy = isInOwnHierarchy;
    }

    @Transient
    public Boolean isEnabledForSecurityLevel(int securityLevel) {
        return role.isActive() && role.getAccumulatedSecLevel() <= securityLevel && operator.isActive();
    }
}
