package no.valg.eva.admin.configuration.application;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.common.configuration.model.election.GenericElectionType;
import no.valg.eva.admin.common.counting.model.configuration.ElectionRef;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

@Default
@ApplicationScoped
public class ElectionMapper {

	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private ElectionRepository electionRepository;

	public ElectionMapper() {

	}

	public ElectionMapper(MvElectionRepository mvElectionRepository, ElectionRepository electionRepository) {
		this.mvElectionRepository = mvElectionRepository;
		this.electionRepository = electionRepository;
	}

	public no.valg.eva.admin.configuration.domain.model.Election toEntity(Election election) {
		no.valg.eva.admin.configuration.domain.model.Election entity = new no.valg.eva.admin.configuration.domain.model.Election();
		entity.setPk(election.getElectionRef() != null ? election.getElectionRef().getPk() : null);
		return updateEntity(entity, election);
	}

	public no.valg.eva.admin.configuration.domain.model.Election updateEntity(no.valg.eva.admin.configuration.domain.model.Election entity, Election election) {
		entity.setAreaLevel(election.getAreaLevel());
		entity.setBaselineVoteFactor(election.getBaselineVoteFactor());
		entity.setCandidateRankVoteShareThreshold(election.getCandidateRankVoteShareThreshold());
		entity.setCandidatesInContestArea(election.isCandidatesInContestArea());
		entity.setId(election.getId());
		entity.setEndDateOfBirth(election.getEndDateOfBirth());
		entity.setLevelingSeats(election.getLevelingSeats());
		entity.setLevelingSeatsVoteShareThreshold(election.getLevelingSeatsVoteShareThreshold());
		entity.setName(election.getName());
		entity.setPenultimateRecount(election.isPenultimateRecount());
		entity.setPersonal(election.isPersonal());
		entity.setRenumber(election.isRenumber());
		entity.setRenumberLimit(election.isRenumberLimit());
		entity.setSettlementFirstDivisor(election.getSettlementFirstDivisor());
		entity.setSingleArea(election.isSingleArea());
		entity.setStrikeout(election.isStrikeout());
		entity.setValgtype(election.getValgtype());
		entity.setWritein(election.isWritein());
		entity.setWriteinLocalOverride(election.isWriteinLocalOverride());
		entity.setMaxCandidateNameLength(election.getMaxCandidateNameLength());
		entity.setMaxCandidateResidenceProfessionLength(election.getMaxCandidateResidenceProfessionLength());
		entity.setMinCandidates(election.getMinCandidates());
		entity.setMinCandidatesAddition(election.getMinCandidatesAddition());
		entity.setMaxCandidates(election.getMaxCandidates());
		entity.setMaxCandidatesAddition(election.getMaxCandidatesAddition());

		entity.setElectionGroup(mvElectionRepository.finnEnkeltMedSti(election.getParentElectionPath().tilValghierarkiSti()).getElectionGroup());
		entity.setElectionType(electionRepository.findElectionTypeById(election.getGenericElectionType().name()));
		return entity;
	}

	public Election toCommonObject(no.valg.eva.admin.configuration.domain.model.Election entity) {
		Election election = new Election(entity.getElectionGroup().electionPath(), entity.getAuditOplock());
		election.setElectionRef(new ElectionRef(entity.getPk()));
		election.setElectionGroupName(entity.getElectionGroup().getName());
		election.setAreaLevel(entity.getAreaLevel());
		election.setBaselineVoteFactor(entity.getBaselineVoteFactor());
		election.setCandidateRankVoteShareThreshold(entity.getCandidateRankVoteShareThreshold());
		election.setCandidatesInContestArea(entity.isCandidatesInContestArea());
		election.setId(entity.getId());
		election.setEndDateOfBirth(entity.getEndDateOfBirth());
		election.setLevelingSeats(entity.getLevelingSeats());
		election.setLevelingSeatsVoteShareThreshold(entity.getLevelingSeatsVoteShareThreshold());
		election.setName(entity.getName());
		election.setPenultimateRecount(entity.isPenultimateRecount());
		election.setPersonal(entity.isPersonal());
		election.setRenumber(entity.isRenumber());
		election.setRenumberLimit(entity.isRenumberLimit());
		election.setSettlementFirstDivisor(entity.getSettlementFirstDivisor());
		election.setSingleArea(entity.isSingleArea());
		election.setStrikeout(entity.isStrikeout());
		election.setValgtype(entity.getValgtype());
		election.setWritein(entity.isWritein());
		election.setWriteinLocalOverride(entity.isWriteinLocalOverride());
		election.setMaxCandidateNameLength(entity.getMaxCandidateNameLength());
		election.setMaxCandidateResidenceProfessionLength(entity.getMaxCandidateResidenceProfessionLength());
		election.setMinCandidates(entity.getMinCandidates());
		election.setMinCandidatesAddition(entity.getMinCandidatesAddition());
		election.setMaxCandidates(entity.getMaxCandidates());
		election.setMaxCandidatesAddition(entity.getMaxCandidatesAddition());

		election.setGenericElectionType(GenericElectionType.valueOf(entity.getElectionType().getId()));

		return election;
	}

	public ElectionDay toElectionDay(no.valg.eva.admin.configuration.domain.model.ElectionDay dbElectionDay) {
		ElectionDay result = new ElectionDay();
		result.setPk(dbElectionDay.getPk());
		result.setDate(dbElectionDay.getDate());
		result.setStartTime(dbElectionDay.getStartTime());
		result.setEndTime(dbElectionDay.getEndTime());
		return result;
	}
}
