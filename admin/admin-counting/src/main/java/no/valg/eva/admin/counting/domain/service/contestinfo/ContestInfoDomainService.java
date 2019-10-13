package no.valg.eva.admin.counting.domain.service.contestinfo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.repository.ContestInfoRepository;

/**
 * Forretningsregler knyttet til hvilke contest-er som er relevante for et gitt område og valgnivå.
 */
@Default
@ApplicationScoped
public class ContestInfoDomainService {

	@Inject
	private ContestInfoRepository contestInfoRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;

	public ContestInfoDomainService() {

	}

	public ContestInfoDomainService(ContestInfoRepository contestInfoRepository, MvElectionRepository mvElectionRepository) {
		this.contestInfoRepository = contestInfoRepository;
		this.mvElectionRepository = mvElectionRepository;
	}

	public List<ContestInfo> contestsByAreaAndElectionPath(MvArea mvArea, ElectionPath electionPath, AreaLevelEnum areaLevelFilter) {
		List<ContestInfo> contestInfoList;
		if (electionPath.getLevel() == ElectionLevelEnum.ELECTION_EVENT) {
			contestInfoList = contestInfoRepository.contestsForArea(mvArea.getPk());
		} else {
			MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
			contestInfoList = Collections.singletonList(contestInfoRepository.contestForSamiElection(mvElection.getContest()));
		}

		return contestInfoList.stream()
				.filter(new ContestInfoPredicate(areaLevelFilter))
				.collect(Collectors.toList());

	}
}
