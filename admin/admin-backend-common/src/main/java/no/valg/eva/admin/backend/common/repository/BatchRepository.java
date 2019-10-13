package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.TypedQuery;

import no.evote.model.Batch;
import no.evote.model.BatchStatus;
import no.evote.security.UserData;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;

@Default
@ApplicationScoped
public class BatchRepository extends BaseRepository {
	public BatchRepository() {

	}

	public Batch findByPk(Long pk) {
		return super.findEntityByPk(Batch.class, pk);
	}

	public Batch update(UserData userData, Batch batch) {
		return super.updateEntity(userData, batch);
	}

	public BatchStatus findBatchStatusById(Integer id) {
		return super.findEntityById(BatchStatus.class, id);
	}

	public List<Batch> findByElectionEventIdAndCategory(String electionEventId, Jobbkategori category) {
		TypedQuery<Batch> query = getEm()
				.createNamedQuery("Batch.findByElectionEventIdAndCategory", Batch.class)
				.setParameter("electionEventId", electionEventId)
				.setParameter("category", category);
		return query.getResultList();
	}

	public Batch findUniqueBatch(int id, Long electionEventPk, Jobbkategori category) {
		TypedQuery<Batch> query = getEm()
				.createNamedQuery("Batch.findBatchUnique", Batch.class)
				.setParameter("id", id)
				.setParameter("electionEventPk", electionEventPk)
				.setParameter("category", category);
		return query.getSingleResult();
	}

	public List<Batch> listMyBatches(Long opk, Jobbkategori category) {
		return getEm().createNamedQuery("Batch.findByOperator", Batch.class)
				.setParameter("opk", opk)
				.setParameter("category", category)
				.getResultList();
	}

	public Batch create(UserData userData, Batch batch) {
		return super.createEntity(userData, batch);
	}

	public BatchStatus findBatchStatusById(int id) {
		return super.findEntityById(BatchStatus.class, id);
	}

	public void lockBatchTable() {
		getEm().createNativeQuery("LOCK batch IN ACCESS EXCLUSIVE MODE").executeUpdate();
	}

	public void delete(UserData userData, Long batchPk) {
		super.deleteEntity(userData, Batch.class, batchPk);
	}

	/**
	 * Removes all batches registered with the specified access path
	 */
	public void deleteAllWithCategory(Jobbkategori category) {
		getEm()
				.createQuery("DELETE FROM Batch b WHERE b.pk IN (SELECT b2.pk FROM Batch b2 where b2.category = ?1)")
				.setParameter(1, category)
				.executeUpdate();
	}
}
