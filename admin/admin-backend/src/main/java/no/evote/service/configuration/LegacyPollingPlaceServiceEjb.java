package no.evote.service.configuration;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.model.views.PollingPlaceVoting;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.PollingPlaceAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Konfigurasjon_Grunnlagsdata;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Manntall;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "LegacyPollingPlaceService")


@Default
@Remote(LegacyPollingPlaceService.class)
public class LegacyPollingPlaceServiceEjb implements LegacyPollingPlaceService {
	@Inject
    private PollingPlaceDomainService pollingPlaceApplicationService;
	@Inject
	private PollingPlaceRepository pollingPlaceRepository;

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = PollingPlaceAuditEvent.class, eventType = Create)
	public PollingPlace create(UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.POLLING_DISTRICT) PollingPlace pollingPlace) {
		return pollingPlaceApplicationService.create(userData, pollingPlace);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = PollingPlaceAuditEvent.class, eventType = Update)
	public PollingPlace update(UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.POLLING_PLACE) PollingPlace pollingPlace) {
		// Checks if the electionDayVoting already exists
		if (pollingPlace.isElectionDayVoting()) {
			PollingPlace pollingPlaceByElectionDayVoting = pollingPlaceRepository.findPollingPlaceByElectionDayVoting(pollingPlace.getPollingDistrict()
					.getPk());
			if (pollingPlaceByElectionDayVoting != null && !pollingPlaceByElectionDayVoting.getPk().equals(pollingPlace.getPk())) {
				throw new EvoteException("@common.message.evote_application_exception.DUPLICATE_ELECTION_DAY_VOTING");
			}
		}

		return pollingPlaceRepository.update(userData, pollingPlace);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = PollingPlaceAuditEvent.class, eventType = Delete)
	public void delete(UserData userData, PollingPlace pollingPlace) {
		pollingPlaceRepository.delete(userData, pollingPlace.getPk());
	}

	@Override
	@Security(accesses = { Konfigurasjon_Geografi, Aggregert_Konfigurasjon_Grunnlagsdata }, type = READ)
	public PollingPlace findByPk(UserData userData, Long pk) {
		return pollingPlaceRepository.findByPk(pk);
	}

	@Override
	@Security(accesses = { Konfigurasjon_Geografi, Aggregert_Konfigurasjon_Grunnlagsdata }, type = READ)
	public PollingPlace findPollingPlaceById(UserData userData, Long pollingDistrictPk, String id) {
		return pollingPlaceRepository.findPollingPlaceById(pollingDistrictPk, id);
	}

	@Override
	@Security(accesses = { Aggregert_Manntall, Aggregert_Stemmegiving }, type = READ)
	public List<PollingPlaceVoting> findAdvancedPollingPlaceByMunicipality(UserData userData, Long electionEventPk, String municipalityId) {
		return pollingPlaceRepository.findAdvancedPollingPlaceByMunicipality(electionEventPk, municipalityId);
	}
}
