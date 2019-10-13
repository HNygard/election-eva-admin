package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsmåter;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionVoteCountCategoryAuditEvent;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.repository.ElectionVoteCountCategoryRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "ElectionVoteCountCategoryService")



@Default
@Remote(ElectionVoteCountCategoryService.class)
public class ElectionVoteCountCategoryServiceEjb implements ElectionVoteCountCategoryService {
	@Inject
	private ElectionVoteCountCategoryRepository electionVoteCountCategoryRepository;

	@Override
	@Security(accesses = Konfigurasjon_Opptellingsmåter, type = READ)
	public List<ElectionVoteCountCategory> findElectionVoteCountCategories(UserData userData, ElectionGroup electionGroup,
			CountCategory... excludedCategories) {
		return electionVoteCountCategoryRepository.findElectionVoteCountCategories(electionGroup, excludedCategories);
	}

	@Override
	@Security(accesses = Konfigurasjon_Opptellingsmåter, type = WRITE)
	@AuditLog(eventClass = ElectionVoteCountCategoryAuditEvent.class, eventType = AuditEventTypes.Update)
	public void update(UserData userData, List<ElectionVoteCountCategory> electionVoteCountCategories) {
		electionVoteCountCategoryRepository.update(userData, electionVoteCountCategories);
	}

}
