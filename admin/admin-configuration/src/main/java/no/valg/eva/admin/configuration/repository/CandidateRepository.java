package no.valg.eva.admin.configuration.repository;

import no.evote.constants.AreaLevelEnum;
import no.evote.model.views.CandidateAudit;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.MaritalStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Default
@ApplicationScoped
public class CandidateRepository extends BaseRepository {
    private static final String BPK = "bpk";

    protected CandidateRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    @SuppressWarnings("unused")
    public CandidateRepository() {
    }

    public void deleteCandidate(UserData userData, Long proposalPersonPk) {
        super.deleteEntity(userData, Candidate.class, proposalPersonPk);
    }

    public Candidate findCandidateByBallotAndOrder(Long ballotPk, int displayOrder) {
        try {
            final Query query = getEm().createNamedQuery("Candidate.findByBallotAndOrder").setParameter(BPK, ballotPk).setParameter("order", displayOrder);
            return (Candidate) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }

    public Candidate findCandidateByPk(Long pk) {
        return super.findEntityByPk(Candidate.class, pk);
    }

    public Candidate findCandidateByBallotAndId(Long bpk, String id) {
        try {
            final Query query = getEm().createNamedQuery("Candidate.findByBallotAndId").setParameter(BPK, bpk).setParameter("id", id);
            return (Candidate) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }

    public List<CandidateAudit> getCandidateAuditByBallot(Long ballotPk) {
        Query query = getEm().createNamedQuery("CandidateAudit.byBallot").setParameter("ballotPk", ballotPk);
        return query.getResultList();
    }

    public List<Candidate> findByBelowDisplayOrder(Long ballotPk, int displayOrder) {
        return getEm()
                .createNamedQuery("Candidate.findByBelowDisplayOrder", Candidate.class)
                .setParameter("ballotPk", ballotPk)
                .setParameter("displayOrder", displayOrder)
                .getResultList();
    }

    public List<Candidate> findByIdInOtherBallot(String id, Long ballotPk, Long electionPk) {
        return getEm()
                .createNamedQuery("Candidate.findByIdInOtherBallot", Candidate.class)
                .setParameter("id", id)
                .setParameter(BPK, ballotPk)
                .setParameter("epk", electionPk)
                .getResultList();
    }

    public List<Candidate> findByIdInAnotherElection(String id, Long ballotPk, Long electionGroupPk) {
        return getEm()
                .createNamedQuery("Candidate.findByIdInAnotherElection", Candidate.class)
                .setParameter("id", id)
                .setParameter(BPK, ballotPk)
                .setParameter("egpk", electionGroupPk)
                .getResultList();
    }

    public Candidate createCandidate(UserData userData, Candidate candidate) {
        return super.createEntity(userData, candidate);
    }

    public Candidate updateCandidate(UserData userData, Candidate candidate) {
        return super.updateEntity(userData, candidate);
    }

    public List<Candidate> findByAffiliation(Long affiliationPk) {
        return getEm()
                .createNamedQuery("Candidate.findByAffiliation", Candidate.class)
                .setParameter("pk", affiliationPk)
                .getResultList();
    }

    public List<Candidate> findCandidateByBallotAndDisplayOrderRange(Long ballotPk, int displayOrderFrom, int displayOrderTo) {
        return getEm()
                .createNamedQuery("Candidate.findCandidateByBallotAndDisplayOrderRange", Candidate.class)
                .setParameter(BPK, ballotPk)
                .setParameter("displayOrderFrom", displayOrderFrom)
                .setParameter("displayOrderTo", displayOrderTo)
                .getResultList();
    }

    public List<Candidate> updateCandidates(List<Candidate> candidates) {
        List<Candidate> updatedEntities = new ArrayList<>();
        for (final Candidate entity : candidates) {
            Candidate updatedEntity = getEm().merge(entity);
            updatedEntities.add(updatedEntity);
        }
        return updatedEntities;
    }

    public List<Candidate> findCandidatesForOtherApprovedBallotsInSameContest(Long pkForBallotToNotIncludeCandidatesFor) {
        Query query = getEm().createNamedQuery("Candidate.findCandidatesForOtherBallotsInSameContest").setParameter("ballotPk",
                pkForBallotToNotIncludeCandidatesFor);
        // noinspection unchecked
        return (List<Candidate>) query.getResultList();
    }

    public List<Candidate> findCandidateAtCountyOrBelow(String candidateId, AreaPath areaPath) {
        AreaPath areaLevelPath = areaPath.toAreaLevelPath(AreaLevelEnum.COUNTY);
        return getEm().createNamedQuery("Candidate.findCandidateAtOrBelowArea", Candidate.class)
                .setParameter("candidateId", candidateId)
                .setParameter("areaPath", areaLevelPath.path())
                .getResultList();
    }

    public void deleteAllCandidates(UserData userData, List<Candidate> candidateList) {
        deleteEntities(userData, candidateList);
    }

    public MaritalStatus findMaritalStatusById(String id) {
        return findEntityById(MaritalStatus.class, id);
    }

    public List<Candidate> findCandidatesMatchingName(AreaPath areaPath, String nameLine) {
        return getEm().createNamedQuery("Candidate.findCandidatesMatchingName", Candidate.class)
                .setParameter(1, areaPath.path())
                .setParameter(2, nameLine)
                .getResultList();
    }

    public List<Candidate> findCandidateAtOrBelowArea(String candidateId, AreaPath areaPath) {
        return getEm().createNamedQuery("Candidate.findCandidateAtOrBelowArea", Candidate.class)
                .setParameter("candidateId", candidateId)
                .setParameter("areaPath", areaPath.path())
                .getResultList();
    }
}
