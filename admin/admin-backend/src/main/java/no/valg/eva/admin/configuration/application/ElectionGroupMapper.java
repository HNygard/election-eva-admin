package no.valg.eva.admin.configuration.application;

import lombok.NoArgsConstructor;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.common.counting.model.configuration.ElectionRef;
import no.valg.eva.admin.configuration.domain.model.MvElection;

import javax.inject.Inject;

@NoArgsConstructor // For testing
public class ElectionGroupMapper {

	@Inject
	private ElectionEventRepository electionEventRepository;

	@Inject
	public ElectionGroupMapper(ElectionEventRepository electionEventRepository) {
		this.electionEventRepository = electionEventRepository;
	}

	public no.valg.eva.admin.configuration.domain.model.ElectionGroup toEntity(ElectionGroup electionGroup) {
		no.valg.eva.admin.configuration.domain.model.ElectionGroup result = new no.valg.eva.admin.configuration.domain.model.ElectionGroup();

		result.setElectionEvent(electionEventRepository.findById(electionGroup.getParentElectionPath().getElectionEventId()));
		result.setId(electionGroup.getId());
		result.setName(electionGroup.getName());
		result.setAdvanceVoteInBallotBox(electionGroup.isAdvanceVoteInBallotBox());
		result.setElectronicMarkoffs(electionGroup.isElectronicMarkoffs());
		result.setScanningPermitted(electionGroup.isScanningPermitted());
		result.setValidateRoleAndListProposal(electionGroup.isValidateRoleAndListProposal());
		result.setValidatePollingPlaceElectoralBoardAndListProposal(electionGroup.isValidatePollingPlaceElectoralBoardAndListProposal());
		return result;
	}

	public no.valg.eva.admin.configuration.domain.model.ElectionGroup updateEntity(no.valg.eva.admin.configuration.domain.model.ElectionGroup entity,
			ElectionGroup electionGroup) {
		entity.setElectionEvent(electionEventRepository.findById(electionGroup.getParentElectionPath().getElectionEventId()));
		entity.setId(electionGroup.getId());
		entity.setName(electionGroup.getName());
		entity.setAdvanceVoteInBallotBox(electionGroup.isAdvanceVoteInBallotBox());
		entity.setElectronicMarkoffs(electionGroup.isElectronicMarkoffs());
		entity.setScanningPermitted(electionGroup.isScanningPermitted());
		entity.setValidateRoleAndListProposal(electionGroup.isValidateRoleAndListProposal());
		entity.setValidatePollingPlaceElectoralBoardAndListProposal(electionGroup.isValidatePollingPlaceElectoralBoardAndListProposal());
		return entity;
	}

	public static ElectionGroup toElectionGroup(MvElection mvElection) {
		mvElection.electionPath().assertElectionGroupLevel();
		return toElectionGroup(mvElection.getElectionGroup());
	}

	public static ElectionGroup toElectionGroup(no.valg.eva.admin.configuration.domain.model.ElectionGroup dbElectionGroup) {
		ElectionGroup result = new ElectionGroup(dbElectionGroup.getElectionEvent().electionPath(), dbElectionGroup.getAuditOplock());
		if (dbElectionGroup.getPk() != null) {
			result.setElectionGroupRef(new ElectionRef(dbElectionGroup.getPk()));
		}
		result.setElectionEventName(dbElectionGroup.getElectionEvent().getName());
		result.setId(dbElectionGroup.getId());
		result.setName(dbElectionGroup.getName());
		result.setAdvanceVoteInBallotBox(dbElectionGroup.isAdvanceVoteInBallotBox());
		result.setElectronicMarkoffs(dbElectionGroup.isElectronicMarkoffs());
		result.setScanningPermitted(dbElectionGroup.isScanningPermitted());
		result.setValidateRoleAndListProposal(dbElectionGroup.isValidateRoleAndListProposal());
		result.setValidatePollingPlaceElectoralBoardAndListProposal(dbElectionGroup.isValidatePollingPlaceElectoralBoardAndListProposal());
		return result;
	}
}
