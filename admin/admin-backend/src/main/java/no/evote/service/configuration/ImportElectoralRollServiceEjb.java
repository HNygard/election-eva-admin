package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Import;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

import no.evote.constants.ElectionLevelEnum;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

import org.jboss.ejb3.annotation.TransactionTimeout;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "ImportElectoralRollService")
@Remote(ImportElectoralRollService.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ImportElectoralRollServiceEjb implements ImportElectoralRollService {
	@Inject
	private FullElectoralRollImporter fullElectoralRollImporter;
	@Resource
	private SessionContext context;

	@Override
	@Security(accesses = Manntall_Import, type = READ)
	public void validateImportFile(UserData userData, ElectionEvent electionEvent, String filePath) {
		fullElectoralRollImporter.validateImportFile(electionEvent, filePath);
	}

	/**
	 * Main service method for performing a preliminary full (complete) import of electoral roll.
	 */
	@Override
	@Security(accesses = Manntall_Import, type = WRITE)
	@Asynchronous
	@TransactionTimeout(value = 3, unit = TimeUnit.DAYS)
	public void preliminaryFullImportElectoralRoll(
			UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT) ElectionEvent electionEvent, String filePath) {
		fullElectoralRollImporter.fullImportElectoralRoll(userData, electionEvent, filePath, context, true);
	}

	/**
	 * Main service method for performing a final full (complete) import of electoral roll.
	 */
	@Override
	@Security(accesses = Manntall_Import, type = WRITE)
	@Asynchronous
	@TransactionTimeout(value = 3, unit = TimeUnit.DAYS)
	public void finalFullImportElectoralRoll(
			UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT) ElectionEvent electionEvent, String filePath) {
        fullElectoralRollImporter.fullImportElectoralRoll(userData, electionEvent, filePath, context, false);
	}
}
