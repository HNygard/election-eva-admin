package no.evote.service.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.application.ElectionMapper;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

@Default
@ApplicationScoped
public class ElectionServiceBean {
	@Inject
	private ContestServiceBean contestService;
	@Inject
	private ElectionRepository electionRepository;
	@Inject
	private ElectionMapper electionMapper;
	@Inject
	private MvElectionRepository mvElectionRepository;

	public ElectionServiceBean() {

	}
	public ElectionServiceBean(ContestServiceBean contestService, ElectionRepository electionRepository,
			ElectionMapper electionMapper, MvElectionRepository mvElectionRepository) {
		this.contestService = contestService;
		this.electionRepository = electionRepository;
		this.electionMapper = electionMapper;
		this.mvElectionRepository = mvElectionRepository;
	}

	public SaveElectionResponse create(UserData userData, no.valg.eva.admin.common.configuration.model.election.Election election) {
		ElectionGroup electionGroup = mvElectionRepository.finnEnkeltMedSti(election.getParentElectionPath().tilValghierarkiSti()).getElectionGroup();
		if (electionGroup.hasElectionWithId(election.getId())) {
			return SaveElectionResponse.withIdNotUniqueError();
		}

		Election electionSaved = electionRepository.create(userData, electionMapper.toEntity(election));
		if (election.isAutoGenerateContests()) {
			contestService.createContestsForElection(userData, electionSaved);
		}
		return SaveElectionResponse.ok().setVersionedObject(electionMapper.toCommonObject(electionSaved));
	}

	public no.valg.eva.admin.common.configuration.model.election.Election get(ElectionPath electionPath) {
		return electionMapper.toCommonObject(electionRepository.findSingleByPath(electionPath));
	}

	/**
	 * Checks user's access rights and updates election.
	 * @param userData contains access rights
	 * @param newElection election to update
	 */
	public SaveElectionResponse update(UserData userData, no.valg.eva.admin.common.configuration.model.election.Election newElection) {
		Election existingElection = electionRepository.findByPk(newElection.getElectionRef().getPk());
		existingElection.checkVersion(newElection);
		if (hasIdChanged(newElection, existingElection)) {
			if (existingElection.getElectionGroup().hasElectionWithId(newElection.getId())) {
				return SaveElectionResponse.withIdNotUniqueError();
			}
		}
		Election updatedElection = electionMapper.updateEntity(existingElection, newElection);
		Election updated = electionRepository.update(userData, updatedElection);
		return SaveElectionResponse.ok().setVersionedObject(electionMapper.toCommonObject(updated));
	}

	private boolean hasIdChanged(no.valg.eva.admin.common.configuration.model.election.Election commonElection, Election existingElection) {
		return !existingElection.getId().equals(commonElection.getId());
	}
}
