package no.valg.eva.admin.rbac.repository;

import lombok.NonNull;
import no.evote.security.UserData;
import no.evote.util.Wrapper;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.Role;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;

@Default
@ApplicationScoped
public class OperatorRepository extends BaseRepository {

    private static final int PARAM_3 = 3;

    public OperatorRepository() {
    }

    public OperatorRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    // * Finders *//

    public Operator findByPk(final long pk) {
        return super.findEntityByPk(Operator.class, pk);
    }

    public Operator findByElectionEventsAndId(final Long electionEventPk, final String operatorId) {
        TypedQuery<Operator> query = getEm().createNamedQuery("Operator.findByElectionEventAndId", Operator.class).setParameter("operatorId", operatorId)
                .setParameter("eventPk", electionEventPk);
        if (query.getResultList().isEmpty()) {
            return null;
        }
        return query.getSingleResult();
    }

    public List<Operator> findAll() {
        return super.findAllEntities(Operator.class);
    }

    public List<Operator> findOperatorsById(final String id) {
        TypedQuery<Operator> query = getEm().createNamedQuery("Operator.findById", Operator.class).setParameter("id", id);
        return query.getResultList();
    }

    public List<Operator> findOperatorsByName(@NonNull Long electionEventPk, @NonNull String nameLine) {
        TypedQuery<Operator> query = getEm().createNamedQuery("Operator.findByName", Operator.class);
        query.setParameter("electionEventPk", electionEventPk.intValue());
        query.setParameter("nameLine", nameLine);
        return query.getResultList();
    }

    public List<Operator> findOperatorsByNameAndArea(final UserData userData, final MvArea mvArea, String nameSearchString) {
        TypedQuery<Operator> query = getEm().createNamedQuery("Operator.findByNameAndArea", Operator.class)
                .setParameter(1, mvArea.getPk())
                .setParameter(2, userData.getElectionEventPk().intValue())
                .setParameter(PARAM_3, nameSearchString);
        return query.getResultList();
    }

    public List<Operator> findOperatorsWithAccess(final UserData userData, final MvArea mvArea, final Access access) {
        TypedQuery<Operator> query = getEm().createNamedQuery("OperatorRole.findOperatorsWithAccess", Operator.class)
                .setParameter("mvAreaPk", mvArea.getPk())
                .setParameter("electionEventPk", userData.getElectionEventPk())
                .setParameter("accessPath", access.getPath());
        return query.getResultList();
    }

    public boolean hasOperator(final String uid) {
        final Wrapper<Boolean> wrapper = new Wrapper<>(false);

        Session session = (Session) getEm().getDelegate();
        session.doWork(con -> {
            try (PreparedStatement stmt = con.prepareStatement("select 1 from operator where operator_id = ? limit 1;")) {
                stmt.setString(1, uid);

                try (ResultSet res = stmt.executeQuery()) {
                    if (res.next()) {
                        wrapper.setValue(true);
                    }
                }
            }
        });

        return wrapper.getValue();
    }

    // * Insertions *//

    public Operator create(final UserData userData, final Operator operator) {
        return super.createEntity(userData, operator);
    }

    // * Deletions *//

    public void delete(final UserData userData, final Operator operator) {
        super.deleteEntity(userData, Operator.class, operator.getPk());
    }

    List<Operator> findOperatorsWithRolesIn(Set<Role> roleFilter) {
        return getEm().createNamedQuery("Operator.findOperatorsWithRolesIn", Operator.class)
                .setParameter("roles", roleFilter)
                .getResultList();
    }
}
