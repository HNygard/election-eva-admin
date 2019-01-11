package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Manntall;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Manntall_Søk;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving_Registrer;
import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Ny_Manntallsinnlasting;
import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Velgere_Med_Område;
import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Velgere_Uten_Område;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Rediger_Person;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.model.SpesRegType;
import no.evote.model.Statuskode;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.AarsakskodeRepository;
import no.valg.eva.admin.backend.common.repository.SpesRegTypeRepository;
import no.valg.eva.admin.backend.common.repository.StatuskodeRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.ElectoralRollAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.SearchByIdAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.SearchByVoterNumberAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.electoralroll.VoterAuditEvent;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.Aarsakskode;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.VoterRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "VoterService")
@Remote(VoterService.class)
public class VoterServiceEjb implements VoterService {
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private VoterServiceBean voterService;
	@Inject
	private AarsakskodeRepository aarsakskodeRepository;
	@Inject
	private SpesRegTypeRepository spesRegTypeRepository;
	@Inject
	private StatuskodeRepository statuskodeRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;

	@Override
	@Security(accesses = Manntall_Rediger_Person, type = WRITE)
	@AuditLog(eventClass = VoterAuditEvent.class, eventType = AuditEventTypes.Create)
	public Voter create(UserData userData, Voter voter) {
		ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());
		if (!findByElectionEventAndId(userData, voter.getId(), electionEvent.getPk()).isEmpty()) {
			throw new EvoteException("@electoralRoll.ssnAlreadyInElectoralRoll");
		}
		return voterRepository.create(userData, voter);
	}

	@Override
	@Security(accesses = Manntall_Rediger_Person, type = WRITE)
	@AuditLog(eventClass = VoterAuditEvent.class, eventType = AuditEventTypes.Update)
	public Voter updateWithManualData(UserData userData, Voter updatedVoter) {
		return voterService.updateWithManualData(userData, updatedVoter);
	}

	@Override
	@Security(accesses = { Aggregert_Manntall_Søk, Aggregert_Stemmegiving_Registrer }, type = READ)
	@AuditLog(eventClass = SearchByIdAuditEvent.class, eventType = AuditEventTypes.SearchElectoralRoll)
	public List<Voter> findByElectionEventAndId(UserData userData, String id, Long electionEventPk) {
		return voterRepository.findByElectionEventAndId(electionEventPk, id);
	}

	@Override
	@Security(accesses = { Aggregert_Manntall_Søk, Aggregert_Stemmegiving_Registrer }, type = READ)
	@AuditLog(eventClass = SearchByVoterNumberAuditEvent.class, eventType = AuditEventTypes.SearchElectoralRoll)
	public List<Voter> findByManntallsnummer(UserData userData, Manntallsnummer manntallsnummer) {
		return voterRepository.findByVoterNumber(userData.electionEvent(), manntallsnummer.getKortManntallsnummer());
	}

	/**
	 * Builds a custom search based on the parameters. The search uses soundex on the name_line.
	 */
	@Override
	@Security(accesses = { Aggregert_Manntall_Søk, Aggregert_Stemmegiving_Registrer }, type = READ)
	@AuditLog(eventClass = VoterAuditEvent.class, eventType = AuditEventTypes.SearchElectoralRoll)
	public List<Voter> searchVoter(
			UserData userData, Voter voter, String countyId, String municipalityId, Integer maxResultSize, boolean approved, Long electionEventPk) {
		return voterRepository.searchVoter(voter, countyId, municipalityId, maxResultSize, approved, electionEventPk);
	}

	@Override
	@SecurityNone
	public List<Aarsakskode> findAllAarsakskoder() {
		return aarsakskodeRepository.findAll();
	}

	@Override
	@Security(accesses = { Aggregert_Stemmegiving, Aggregert_Manntall }, type = READ)
	public List<SpesRegType> findAllSpesRegTypes(UserData userData) {
		return spesRegTypeRepository.findAll();
	}

	@Override
	@Security(accesses = { Aggregert_Stemmegiving, Aggregert_Manntall }, type = READ)
	public List<Statuskode> findAllStatuskoder(UserData userData) {
		return statuskodeRepository.findAll();
	}

	@Override
	@Asynchronous
	@Security(accesses = Beskyttet_Slett_Velgere_Med_Område, type = WRITE)
	@AuditLog(eventClass = ElectoralRollAuditEvent.class, eventType = AuditEventTypes.DeletedAllInArea, objectSource = AuditedObjectSource.Parameters)
	public void deleteVoters(
			UserData userData, @SecureEntity(electionLevelDynamic = true) MvElection mvElection, @SecureEntity(areaLevelDynamic = true) MvArea mvArea) {
		voterService.deleteVoters(userData, mvElection, mvArea, false);
	}

	@Override
	@Asynchronous
	@Security(accesses = Beskyttet_Ny_Manntallsinnlasting, type = WRITE)
	@AuditLog(eventClass = ElectoralRollAuditEvent.class, eventType = AuditEventTypes.DeleteAll)
	public void prepareNewInitialLoad(UserData userData, MvElection mvElection, MvArea mvArea) {
		voterService.prepareNewInitialLoad(userData, mvElection, mvArea);
	}

	@Override
	@Security(accesses = Beskyttet_Slett_Velgere_Uten_Område, type = WRITE)
	@AuditLog(eventClass = ElectoralRollAuditEvent.class, eventType = AuditEventTypes.DeletedAllWithoutArea)
	public void deleteVotersWithoutMvArea(UserData userData, @SecureEntity(entity = ElectionEvent.class, areaLevel = AreaLevelEnum.ROOT) Long electionEventPk) {
		voterRepository.deleteVotersWithoutMvArea(electionEventPk);
	}

	/**
	 * Creates a voter with a fictitious SSN and
	 * 
	 * @return voter with a fictitious SSN
	 */
	@Override
	@Security(accesses = Aggregert_Stemmegiving_Registrer, type = WRITE)
	public Voter createFictitiousVoter(UserData userData, AreaPath municipalityPath) {
		return voterService.createFictitiousVoter(userData, municipalityPath);
	}
}
