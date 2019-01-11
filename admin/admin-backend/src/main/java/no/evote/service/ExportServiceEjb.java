package no.evote.service;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_EML_Behandle;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_EML_Last_Ned;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.BatchInfoDto;
import no.evote.model.Batch;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Security;

/**
 * Exports EML and electoral roll as zip files.
 * 
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "ExportService")
@Remote(ExportService.class)
public class ExportServiceEjb implements ExportService {
	@Inject
	private ExportServiceBean exportService;

	@Override
	@Security(accesses = Konfigurasjon_EML_Behandle, type = WRITE)
	public void generateEML(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT) Long electionEventPk) {
		exportService.generateEML(userData, electionEventPk);
	}

	@Override
	@Security(accesses = Konfigurasjon_EML_Last_Ned, type = READ)
	public byte[] getGeneratedEML(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT, entity = Batch.class) Long batchPk) {
		return exportService.getGeneratedEML(batchPk);
	}

	@Override
	@Security(accesses = Konfigurasjon_EML_Behandle, type = READ)
	public boolean validateGeneratedEML(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT, entity = Batch.class) Long batchPk) {
		return exportService.validateGeneratedEML(batchPk);
	}

	@Override
	@Security(accesses = Konfigurasjon_EML_Last_Ned, type = READ)
	public List<BatchInfoDto> getGeneratedEMLBatches(UserData userData, String electionEventId) {
		return exportService.getGeneratedEMLBatches(electionEventId);
	}
}
