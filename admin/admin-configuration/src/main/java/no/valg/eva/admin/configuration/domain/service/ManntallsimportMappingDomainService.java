package no.valg.eva.admin.configuration.domain.service;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.VoterAuditEvent;
import no.valg.eva.admin.configuration.domain.event.ManntallsimportFullfortEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.manntall.OmraadeMapping;
import no.valg.eva.admin.configuration.repository.ManntallsimportMappingRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;

import org.apache.log4j.Logger;

/**
 * Tjenester som hjelper til med å mappe velgere til annen geografi enn i manntallsimport
 */
@Default
@ApplicationScoped
public class ManntallsimportMappingDomainService {

	private static final Logger LOG = Logger.getLogger(ManntallsimportMappingDomainService.class);

	private VoterRepository voterRepository;
	private MvAreaRepository mvAreaRepository;
	private AuditLogServiceBean auditLogService;
	private ManntallsimportMappingRepository manntallsimportMappingRepository;
	private List<Voter> velgerQueue;
	private List<AuditEvent> auditEventQueue;

	@Inject
	public ManntallsimportMappingDomainService(ManntallsimportMappingRepository manntallsimportMappingRepository, VoterRepository voterRepository,
											   MvAreaRepository mvAreaRepository, AuditLogServiceBean auditLogService) {
		this.manntallsimportMappingRepository = manntallsimportMappingRepository;
		this.voterRepository = voterRepository;
		this.mvAreaRepository = mvAreaRepository;
		this.auditLogService = auditLogService;
	}

	public void flyttVelgereTilKonfigurertKrets(@Observes ManntallsimportFullfortEvent event) {
		flyttVelgereTilKonfigurertKrets(event.getUserData(), event.getValghendelse());
	}

	/**
	 * Med utgangspunkt i mappingreglene konfigurert for oppgitt valghendelse, mappes velgerne på kretsbasis for hver regel. Dette er et bevisst valg, siden
	 * størrelsen på kretser som oftest bare er noen tusen velgere.
	 */
	private void flyttVelgereTilKonfigurertKrets(UserData userData, ElectionEvent electionEvent) {
		for (OmraadeMapping omraadeMapping : finnMappingForValghendelse(electionEvent)) {
			kjorBatchMedFlyttingForEnKrets(userData, omraadeMapping);
		}
	}

	private List<OmraadeMapping> finnMappingForValghendelse(ElectionEvent electionEvent) {
		List<OmraadeMapping> mappingerFunnet = manntallsimportMappingRepository.finnForValghendelse(electionEvent);
		LOG.debug("Fant " + mappingerFunnet.size() + " mappinger for valghendelsen " + electionEvent.getId());
		return mappingerFunnet;
	}

	private void kjorBatchMedFlyttingForEnKrets(UserData userData, OmraadeMapping omraadeMapping) {
		initialiserBatch();
		flyttVelgereIKrets(userData, omraadeMapping);
		commitBatch(userData, omraadeMapping);
	}

	private void initialiserBatch() {
		velgerQueue = new ArrayList<>();
		auditEventQueue = new ArrayList<>();
	}

	private void flyttVelgereIKrets(UserData userData, OmraadeMapping omraadeMapping) {
		List<Voter> manntallsoppfoeringer = voterRepository.findByOmraadesti(omraadeMapping.getFraOmraadesti());
		LOG.debug("Fant " + manntallsoppfoeringer.size() + " for krets " + omraadeMapping.getFraOmraade());
		for (Voter velger : manntallsoppfoeringer) {
			flyttVelgerMedAuditLog(velger, userData, omraadeMapping);
		}
	}

	private void flyttVelgerMedAuditLog(Voter velger, UserData userData, OmraadeMapping omraadeMapping) {
		velgerQueue.add(oppdatereVelgerIhhtMapping(velger, omraadeMapping));
		auditEventQueue.add(auditlogMappingVelger(userData, velger, omraadeMapping));
	}

	private Voter oppdatereVelgerIhhtMapping(Voter velger, OmraadeMapping omraadeMapping) {
		AreaPath omraadesti = omraadeMapping.getTilOmraadesti();
		velger.setCountryId(omraadesti.getCountryId());
		velger.setCountyId(omraadesti.getCountyId());
		velger.setMunicipalityId(omraadesti.getMunicipalityId());
		velger.setBoroughId(omraadesti.getBoroughId());
		velger.setPollingDistrictId(omraadesti.getPollingDistrictId());
		velger.setMvArea(mvAreaRepository.findSingleByPath(omraadesti));
		if (velger.getPollingStation() != null) {
			LOG.warn("Rode var satt for velger '" + velger.getId() + "' i forbindelse med mapping av krets. Dette er ikke forventet. Rodeverdi er fjernet.");
			velger.setPollingStation(null);
		}
		return velger;
	}

	private AuditEvent auditlogMappingVelger(UserData userData, Voter velger, OmraadeMapping omraadeMapping) {
		return new VoterAuditEvent(userData, velger, AuditEventTypes.Update, Outcome.Success, "Velger flyttet til krets " + omraadeMapping.getTilOmraade());
	}

	private void commitBatch(UserData userData, OmraadeMapping omraadeMapping) {
		commitVelgerQueue(userData, omraadeMapping);
		commitAuditlogEventQueue();
	}

	private void commitVelgerQueue(UserData userData, OmraadeMapping omraadeMapping) {
		voterRepository.updateVoters(userData, velgerQueue);
		LOG.info("Flyttet " + velgerQueue.size() + " velgere til krets " + omraadeMapping.getTilOmraade());
		velgerQueue.clear();
	}

	private void commitAuditlogEventQueue() {
		for (AuditEvent auditEvent : auditEventQueue) {
			auditLogService.addToAuditTrail(auditEvent);
		}
		auditEventQueue.clear();
	}
}
