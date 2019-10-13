package no.valg.eva.admin.configuration.application;

import no.evote.constants.ElectionLevelEnum;
import no.evote.security.UserData;
import no.evote.service.configuration.ElectionGroupServiceBean;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionGroupAuditEvent;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsvalgstyrer;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valg_Valggruppe;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ElectionGroupService")


@Default
@Remote(ElectionGroupService.class)
public class ElectionGroupApplicationService implements ElectionGroupService {

	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private ElectionGroupServiceBean electionGroupService;
	@Inject
	private ElectionGroupRepository electionGroupRepository;

	@Override
	@Security(accesses = { Konfigurasjon_Grunnlagsdata_Redigere, Konfigurasjon_Opptellingsvalgstyrer }, type = READ)
	public List<ElectionGroup> getElectionGroups(UserData userData) {
		ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());
		return mvElectionRepository.findByPathAndLevel(electionEvent.electionPath(), ElectionLevelEnum.ELECTION_GROUP)
				.stream()
				.map(ElectionGroupMapper::toElectionGroup)
				.collect(Collectors.toList());
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valggruppe, type = READ)
	public ElectionGroup get(UserData userData, ElectionPath electionGroupPath) {
		return electionGroupService.get(electionGroupPath);
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valggruppe, type = WRITE)
	@AuditLog(eventClass = ElectionGroupAuditEvent.class, eventType = AuditEventTypes.Save)
	public SaveElectionResponse save(UserData userData, ElectionGroup electionGroup) {
		if (electionGroup.getElectionGroupRef() == null) {
			return electionGroupService.create(userData, electionGroup);
		} else {
			return electionGroupService.update(userData, electionGroup);
		}
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valggruppe, type = WRITE)
	@AuditLog(eventClass = ElectionGroupAuditEvent.class, eventType = AuditEventTypes.Delete)
	public void delete(UserData userData, ElectionPath electionGroupPath) {
		no.valg.eva.admin.configuration.domain.model.ElectionGroup group = electionGroupRepository.findSingleByPath(electionGroupPath);
		electionGroupRepository.delete(userData, group.getPk());
	}

	@Override
	@SecurityNone
	public boolean isScanningEnabled(UserData userData) {
		return userData.getOperatorMvElection().getElectionEvent().isScanningEnabledInElectionGroup();
	}
}
