package no.valg.eva.admin.backend.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.evote.model.views.ForeignEarlyVoting;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.auditevents.CentrallyRegisteredVotingAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.DeleteVotingAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.DeleteVotingsInAreaAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.UpdateAdvanceVotingsApprovedAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.UpdateElectionDayVotingsApprovedAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.UpdateVotingAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.VotingAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRepository;
import no.valg.eva.admin.voting.service.VotingServiceBean;
import org.joda.time.LocalDate;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Rapport;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving_Prøving;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving_Registrer;
import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Stemmegiving;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Forhånd_Registrer;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Forhånd_Registrer_Sent_Innkommet;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Forhånd_Registrer_Sentralt;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Oversikt_Forkastede;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Oversikt_Til_Andre_Kommuner;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Valgting_Registrer;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Valgting_Registrer_Sentralt;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "VotingService")


@Default
@Remote(VotingService.class)
public class VotingServiceEjb implements VotingService {
    private static final long serialVersionUID = 7946562029498331749L;
    @Inject
	private VotingServiceBean votingService;
	@Inject
	private VotingRepository votingRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;

	@Override
	@Security(accesses = { Aggregert_Rapport }, type = READ)
	public long countUnapprovedAdvanceVotings(UserData userData, AreaPath areaPath) {
		return votingRepository.countUnapprovedAdvanceVotings(areaPath);
	}

	@Override
	@Security(accesses = { Aggregert_Stemmegiving_Prøving, Stemmegiving_Oversikt_Forkastede }, type = WRITE)
	@AuditLog(eventClass = UpdateVotingAuditEvent.class, eventType = AuditEventTypes.Update, objectSource = AuditedObjectSource.ReturnValue)
	public Voting update(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_GROUP) Voting voting) {
		return votingRepository.update(userData, voting);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
	@AuditLog(eventClass = UpdateVotingAuditEvent.class, eventType = AuditEventTypes.Update, objectSource = AuditedObjectSource.ReturnValue)
	public Voting updateAdvanceVotingApproved(UserData userData, Voting votingForApproval) {
		return votingService.updateAdvanceVotingApproved(userData, votingForApproval);
	}

	/**
	 * Retrieve all votings for a voter in an election group
	 */
	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
	public List<Voting> getVotingsByElectionGroupAndVoter(UserData userData, Long voterPk, Long electionGroupPk) {
		return votingRepository.getVotingsByElectionGroupAndVoter(voterPk, electionGroupPk);
	}

	/**
	 * Retrieve all votings for a voter in an election group
	 */
	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = READ)
	public List<Voting> getVotingsByElectionGroupVoterAndMunicipality(UserData userData, Long voterPk, Long electionGroupPk, Long municipalityPk) {
		return votingRepository.getVotingsByElectionGroupVoterAndMunicipality(voterPk, electionGroupPk, municipalityPk);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = READ)
	public List<Voting> getRejectedVotingsByElectionGroupAndMunicipality(UserData userData, ValggruppeSti valggruppeSti, KommuneSti kommuneSti) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(valggruppeSti);
		return votingRepository.getRejectedVotingsByElectionGroupAndMunicipality(kommuneSti.kommuneId(), mvElection.getElectionGroup().getPk());
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Registrer, type = WRITE)
	@AuditLog(eventClass = DeleteVotingAuditEvent.class, eventType = AuditEventTypes.Delete, objectSource = AuditedObjectSource.Parameters)
	public void delete(UserData userData, Long pk) {
		votingRepository.delete(userData, pk);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Registrer, type = WRITE)
	@AuditLog(eventClass = DeleteVotingAuditEvent.class, eventType = AuditEventTypes.Delete, objectSource = AuditedObjectSource.Parameters)
	public void delete(UserData userData, Voting voting) {
		votingRepository.delete(userData, voting.getPk());
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving, type = READ)
	public List<VotingDto> findVotingStatistics(UserData userData, Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate, LocalDate endDate, int votingNumberStart,
			int votingNumberEnd, boolean includeLateValidation, String[] votingCategories, boolean includeLateAdvanceVotings) {
		return votingService.findVotingStatistics(pollingPlacePk, municipalityPk, electionGroupPk, startDate, endDate, votingNumberStart, votingNumberEnd, includeLateValidation, votingCategories, includeLateAdvanceVotings);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = READ)
	public List<PickListItem> findAdvanceVotingPickList(UserData userData, Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate, LocalDate endDate, int votingNumberStart, int votingNumberEnd) {
		return votingService.findAdvanceVotingPickList(pollingPlacePk, municipalityPk, electionGroupPk, startDate, endDate, votingNumberStart, votingNumberEnd);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = READ)
	public List<PickListItem> findElectionDayVotingPickList(UserData userData, long municipalityPk, Long electionGroupPk, int votingNumberStart, int votingNumberEnd, String... votingCats) {
		return votingService.findElectionDayVotingPickList(municipalityPk, electionGroupPk, votingNumberStart, votingNumberEnd, votingCats);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
	@AuditLog(eventClass = UpdateAdvanceVotingsApprovedAuditEvent.class, eventType = AuditEventTypes.UpdateAll, objectSource = AuditedObjectSource.ParametersAndReturnValue)
	public int updateAdvanceVotingsApproved(
			UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.POLLING_PLACE, entity = PollingPlace.class) Long pollingPlacePk,
			@SecureEntity(areaLevel = AreaLevelEnum.MUNICIPALITY, entity = Municipality.class) long municipalityPk,
			@SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_GROUP, entity = ElectionGroup.class) Long electionGroupPk, LocalDate startDate,
			LocalDate endDate,
			int votingNumberStart, int votingNumberEnd) {
		return votingService.updateAdvanceVotingsApproved(pollingPlacePk, municipalityPk, electionGroupPk, startDate, endDate, votingNumberStart, votingNumberEnd);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = WRITE)
	@AuditLog(eventClass = UpdateElectionDayVotingsApprovedAuditEvent.class, eventType = AuditEventTypes.UpdateAll, objectSource = AuditedObjectSource.ParametersAndReturnValue)
	public int updateElectionDayVotingsApproved(UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.MUNICIPALITY, entity = Municipality.class) long municipalityPk,
			@SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_GROUP, entity = ElectionGroup.class) Long electionGroupPk,
			int votingNumberStart, int votingNumberEnd, String... votingCats) {
		return votingService.updateElectionDayVotingsApproved(municipalityPk, electionGroupPk, votingNumberStart, votingNumberEnd, votingCats);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Prøving, type = READ)
	public Voting findVotingByVotingNumber(UserData userData, ValggruppeSti valggruppeSti, KommuneSti kommuneSti, long votingNumber, boolean earlyVoting) {
		MvElection valggruppe = mvElectionRepository.finnEnkeltMedSti(valggruppeSti);
		MvArea kommune = mvAreaRepository.findSingleByPath(kommuneSti.areaPath());
		return votingRepository.findVotingByVotingNumber(kommune.getMunicipality().getPk(), valggruppe.getElectionGroup().getPk(), votingNumber, earlyVoting);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving, type = READ)
	public VotingCategory findVotingCategoryById(UserData userData, String id) {
		return votingRepository.findVotingCategoryById(id);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving_Registrer, type = READ)
	public List<VotingCategory> findAdvanceVotingCategories(UserData userData) {
		return votingRepository.findAdvanceVotingCategories();
	}

	@Override
	@Security(accesses = Stemmegiving_Oversikt_Til_Andre_Kommuner, type = READ)
	public List<ForeignEarlyVoting> findForeignEarlyVotingsSentFromMunicipality(UserData userData, ValggruppeSti valggruppeSti, KommuneSti kommuneSti) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(valggruppeSti);
		return votingRepository.findForeignEarlyVotingsSentFromMunicipality(mvElection.getElectionGroup().getPk(), kommuneSti.kommuneId());
	}

	@Override
	@Security(accesses = Beskyttet_Slett_Stemmegiving, type = WRITE)
	@AuditLog(eventClass = DeleteVotingsInAreaAuditEvent.class, eventType = AuditEventTypes.DeletedAllInArea, objectSource = AuditedObjectSource.Parameters)
	public void deleteVotings(UserData userData, @SecureEntity(electionLevelDynamic = true) MvElection mvElection, @SecureEntity(areaLevelDynamic = true) MvArea mvArea, Integer selectedVotingCategoryPk) {
		votingRepository.deleteVotings(mvElection, mvArea, selectedVotingCategoryPk);
	}

	@Override
	@Security(accesses = Beskyttet_Slett_Stemmegiving, type = WRITE)
	// Auditlogges ikke separat, siden kall alltid skjer sammen med deleteVotings
	public void deleteSeqVotingNumber(UserData userData, @SecureEntity(electionLevelDynamic = true) MvElection mvElection, @SecureEntity(areaLevelDynamic = true) MvArea mvArea) {
		votingRepository.deleteSeqVotingNumber(mvElection, mvArea);
	}

	@Override
	@Security(accesses = Beskyttet_Slett_Stemmegiving, type = READ)
	public List<VotingCategory> findAllVotingCategories(UserData userData) {
		return votingRepository.findAllVotingCategories();
	}

	@Override
	@Security(accesses = { Stemmegiving_Forhånd_Registrer, Stemmegiving_Forhånd_Registrer_Sent_Innkommet, Stemmegiving_Forhånd_Registrer_Sentralt }, type = WRITE)
	@AuditLog(eventClass = VotingAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ParametersAndReturnValue)
	public Voting markOffVoterAdvance(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Voter voter, boolean isVoterInLoggedInMunicipality, String selectedVotingCategoryId, String ballotBoxId, VotingPhase votingPhase) {
		return votingService.markOffVoterAdvance(userData, pollingPlace, electionGroup, voter, isVoterInLoggedInMunicipality, selectedVotingCategoryId, ballotBoxId, votingPhase);
	}

	@Override
	@Security(accesses = { Stemmegiving_Forhånd_Registrer_Sentralt, Stemmegiving_Valgting_Registrer_Sentralt }, type = WRITE)
	@AuditLog(eventClass = CentrallyRegisteredVotingAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ParametersAndReturnValue)
	public Voting registerVoteCentrally(UserData userData, ElectionGroup electionGroup, Voter voter, String selectedVotingCategoryId, MvArea currentMvArea, VotingPhase votingPhase) {
		return votingService.registerVoteCentrally(userData, electionGroup, voter, selectedVotingCategoryId, currentMvArea, votingPhase);
	}

	@Override
	@Security(accesses = { Stemmegiving_Valgting_Registrer, Stemmegiving_Valgting_Registrer_Sentralt }, type = WRITE)
	@AuditLog(eventClass = VotingAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ParametersAndReturnValue)
	public Voting markOffVoter(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Voter voterSearchResult, boolean isForeignVoting, VotingPhase votingPhase) {
		return votingService.markOffVoter(userData, pollingPlace, electionGroup, voterSearchResult, isForeignVoting, votingPhase);
	}

	@Override
	@Security(accesses = { Stemmegiving_Forhånd_Registrer, Stemmegiving_Forhånd_Registrer_Sent_Innkommet, Stemmegiving_Forhånd_Registrer_Sentralt }, type = WRITE)
	@AuditLog(eventClass = VotingAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ParametersAndReturnValue)
	public Voting markOffVoterAdvanceVoteInBallotBox(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Voter voter, boolean voterIsInLoggedInMunicipality, VotingPhase votingPhase) {
		return votingService.markOffVoterAdvanceVoteInBallotBox(userData, pollingPlace, electionGroup, voter, voterIsInLoggedInMunicipality, votingPhase);
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving, type = READ)
	public VelgerSomSkalStemme hentVelgerSomSkalStemme(UserData userData, StemmegivningsType stemmegivningsType, ElectionPath electionPath, AreaPath areaPath, Voter voter) {
		return votingService.hentVelgerSomSkalStemme(stemmegivningsType, electionPath, areaPath, voter);
	}
}
