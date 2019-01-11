package no.evote.service;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import no.evote.dto.BatchInfoDto;
import no.evote.model.BaseEntity;
import no.evote.model.Batch;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BatchRepository;
import no.valg.eva.admin.backend.common.repository.GenericRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoterImportBatchRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.RoleRepository;

/**
 * This class contains method that are only used by integration tests, mostly for cleanup after tests.
 */
@Stateless(name = "TestService")
@Remote(TestService.class)
public class TestServiceEjb implements TestService {
	@Inject
	private ExportServiceBean exportService;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private ContestRepository contestRepository;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private VoterImportBatchRepository voterImportBatchRepository;
	@Inject
	private BatchRepository batchRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private GenericRepository genericRepository;
	@Inject
	private RoleRepository roleRepository;
	@Inject
	private BatchServiceBean batchService;

	@Override
	public Role findByElectionEventAndId(ElectionEvent ee, String id) {
		return roleRepository.findByElectionEventAndId(ee, id);
	}

	@Override
	public Contest createContest(UserData userData, Contest contest) {
		return contestRepository.create(userData, contest);
	}

	@Override
	public void deleteContest(UserData userData, Contest contest) {
		contestRepository.delete(userData, contest.getPk());
	}

	@Override
	public void deleteFromVoter(UserData userData, Long electionEventPk) {
		voterRepository.deleteVotersByElectionEvent(electionEventPk);
	}

	@Override
	public void deleteVoterImportBatch(UserData userData, Long voterImportBatchPk) {
		voterImportBatchRepository.delete(userData, voterImportBatchPk);
	}

	@Override
	public void deleteVoter(UserData userData, Long voterPk) {
		voterRepository.delete(userData, voterPk);
	}

	@Override
	public void deleteVotersByElectionEvent(UserData userData, Long electionEventPk) {
		List<Voter> voters = voterRepository.findVotersByElectionEvent(electionEventPk);
		voterRepository.delete(userData, voters);
	}

	@Override
	public void deleteGeneratedEML(UserData userData, String electionEventId) {
		for (BatchInfoDto batchInfo : exportService.getGeneratedEMLBatches(electionEventId)) {
			batchRepository.delete(userData, batchInfo.getPk());
		}
	}

	@Override
	public void deleteGeneratedElectoralRoll(UserData userData, String electionEventId) {
		for (BatchInfoDto batchInfo : exportService.getGeneratedElectoralRollBatches(electionEventId)) {
			batchRepository.delete(userData, batchInfo.getPk());
		}
	}

	@Override
	public void deleteElectionEvent(UserData userData, Long pk) {
		electionEventRepository.delete(userData, pk);
	}

	@Override
	public List<ReportingUnitType> findAllReportingUnitTypes() {
		return reportingUnitRepository.findAllReportingUnitTypes();
	}

	@Override
	public Voter findVoterByPk(UserData userData, Long pk) {
		return voterRepository.findByPk(pk);
	}

	/**
	 * Removes all batches registered with the specified access path
	 */
	@Override
	public void removeAllBatches(Jobbkategori category) {
		batchRepository.deleteAllWithCategory(category);
	}

	@Override
	public <T> T createEntity(UserData userData, T entity) {
		return genericRepository.create(userData, entity);
	}

	@Override
	public <T> void deleteEntity(UserData userData, T entity) {
		genericRepository.delete(userData, entity.getClass(), ((BaseEntity) entity).getPk());
	}

	@Override
	public <T> T updateEntity(UserData userData, T entity) {
		return genericRepository.update(userData, entity);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Batch createBatchForGenerateVoterNumber(UserData userData) {
		return batchService.createBatchForGenerateVoterNumber(userData);
	}
}
