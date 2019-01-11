package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.application.ElectionGroupMapper;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;

public class ElectionGroupServiceBean {

	@Inject
	private ElectionRepository electionRepository;
	@Inject
	private ElectionGroupRepository electionGroupRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private ElectionGroupMapper electionGroupMapper;

	public ElectionGroup get(ElectionPath electionGroupPath) {
		return ElectionGroupMapper.toElectionGroup(electionGroupRepository.findSingleByPath(electionGroupPath));
	}

	public SaveElectionResponse create(UserData userData, ElectionGroup electionGroup) {
		ElectionEvent electionEvent = electionEventRepository.findById(electionGroup.getParentElectionPath().getElectionEventId());
		if (electionEvent.hasElectionGroupsWithId(electionGroup.getId())) {
			return SaveElectionResponse.withIdNotUniqueError();
		}

		no.valg.eva.admin.configuration.domain.model.ElectionGroup created = electionGroupRepository.create(userData,
				electionGroupMapper.toEntity(electionGroup));
		return SaveElectionResponse.ok().setVersionedObject(ElectionGroupMapper.toElectionGroup(created));
	}

	public SaveElectionResponse update(UserData userData, ElectionGroup electionGroup) {
		no.valg.eva.admin.configuration.domain.model.ElectionGroup existing = electionGroupRepository.findByPk(electionGroup.getElectionGroupRef().getPk());
		existing.checkVersion(electionGroup);
		if (hasIdChanged(electionGroup, existing)) {
			if (existing.getElectionEvent().hasElectionGroupsWithId(electionGroup.getId())) {
				return SaveElectionResponse.withIdNotUniqueError();
			}
		}

		no.valg.eva.admin.configuration.domain.model.ElectionGroup updated = electionGroupRepository.update(userData, electionGroupMapper.updateEntity(
				existing, electionGroup));
		return SaveElectionResponse.ok().setVersionedObject(ElectionGroupMapper.toElectionGroup(updated));
	}

	public List<ElectionGroup> getElectionGroupsWithoutElections(Long electionEventPk) {
		List<ElectionGroup> withoutElections = new ArrayList<>();
		List<no.valg.eva.admin.configuration.domain.model.ElectionGroup> groups = electionGroupRepository.getElectionGroupsSorted(electionEventPk);
		for (no.valg.eva.admin.configuration.domain.model.ElectionGroup group : groups) {
			if (electionRepository.findElectionsByElectionGroup(group.getPk()).isEmpty()) {
				withoutElections.add(ElectionGroupMapper.toElectionGroup(group));
			}
		}
		return withoutElections;
	}

	private boolean hasIdChanged(ElectionGroup commonElection, no.valg.eva.admin.configuration.domain.model.ElectionGroup existingElection) {
		return !existingElection.getId().equals(commonElection.getId());
	}
}
