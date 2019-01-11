package no.evote.service;

import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_List_Slett_Velgere;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Generer_Mannntallsnummer;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Import;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Importer;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.model.Batch;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BatchRepository;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.counting.SaveUploadedCountAuditEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.counting.domain.auditevents.ImportUploadedCountAuditEvent;


@Stateless(name = "BatchService")
@Remote(BatchService.class)
public class BatchServiceEjb implements BatchService {
	@Inject
	private BatchServiceBean batchService;
	@Inject
	private BatchRepository batchRepository;

	@Override
	@Security(accesses = Opptelling_Importer, type = WRITE)
	@AuditLog(eventClass = SaveUploadedCountAuditEvent.class, eventType = AuditEventTypes.SaveUploadedCount)
	public Batch saveFile(UserData userData, byte[] file, String fileName, Jobbkategori category) {
		return batchService.saveFile(userData, file, fileName, category);
	}

	@Asynchronous
	@Override
	@Security(accesses = Opptelling_Importer, type = WRITE)
	@AuditLog(eventClass = ImportUploadedCountAuditEvent.class, eventType = AuditEventTypes.ImportUploadedCount)
	public void importFile(UserData userData, int id, Long electionEventPk, Jobbkategori category) {
		batchService.importFile(userData, id, electionEventPk, category);
	}

	@Override
	@SecurityNone
	public int checkStatus(long batchPk) {
		return batchService.checkStatus(batchPk);
	}

	@Override
	@Security(accesses = Opptelling_Importer, type = READ)
	public List<Batch> listMyBatches(UserData userData, Jobbkategori category) {
		return batchRepository.listMyBatches(userData.getOperator().getPk(), category);
	}

	@Override
	@Security(accesses = { Beskyttet_List_Slett_Velgere, Manntall_Import, Manntall_Generer_Mannntallsnummer }, type = READ)
	public List<Batch> listBatchesByEventAndCategory(UserData userData, Jobbkategori category, String electionEventId) {
		return batchService.listBatchesByEventAndCategory(category, electionEventId);
	}
}
