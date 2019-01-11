package no.valg.eva.admin.configuration.domain.service;

import java.util.Collection;

import javax.inject.Inject;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

public class ContestAreaDomainService {
	private final MvElectionRepository mvElectionRepository;

	@Inject
	public ContestAreaDomainService(MvElectionRepository mvElectionRepository) {
		this.mvElectionRepository = mvElectionRepository;
	}

	public Collection<ContestArea> contestAreasFor(ElectionPath contestPath) {
		contestPath.assertContestLevel();
		return mvElectionRepository
				.finnEnkeltMedSti(contestPath.tilValghierarkiSti())
				.getContest()
				.getContestAreaSet();
	}
}
