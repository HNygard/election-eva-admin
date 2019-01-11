package no.valg.eva.admin.backend.service.impl;

import no.evote.service.configuration.MvAreaServiceBean;
import no.valg.eva.admin.configuration.repository.EligibilityRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;
import no.valg.eva.admin.voting.service.VotingServiceBean;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class DefaultVotingServiceProducer {

	@Inject
	private MvAreaServiceBean mvAreaService;
	@Inject
	private PollingDistrictRepository pollingDistrictRepository;
	@Inject
	private PollingPlaceRepository pollingPlaceRepository;
	@Inject
	private VotingRepository votingRepository;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private EligibilityRepository eligibilityRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;

	@Produces
	@Pojo
	public VotingServiceBean getService(final InjectionPoint ip) {
		return new VotingServiceBean(
				mvAreaService, pollingDistrictRepository, pollingPlaceRepository,
				votingRepository, voterRepository, eligibilityRepository, mvElectionRepository);
	}
}
