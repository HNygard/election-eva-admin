package no.valg.eva.admin.counting.application;

import static no.evote.constants.ElectionLevelEnum.ELECTION_EVENT;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.service.contestinfo.ContestInfoDomainService;
import no.valg.eva.admin.counting.repository.ContestInfoRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * Service providing interface for getting contest info.
 */
@Stateless(name = "ContestInfoService")
@Remote(ContestInfoService.class)
@Default
public class ContestInfoApplicationService implements ContestInfoService {
	@Inject
	private ContestInfoRepository contestInfoRepository;

	@Inject
	private MvAreaRepository mvAreaRepository;

	@Inject
	private MvElectionRepository mvElectionRepository;

	@Inject
	private ContestInfoDomainService contestInfoDomainService;
	
	@Override
	@SecurityNone
	public ContestInfo findContestInfoByPath(ElectionPath contestPath) {
		contestPath.assertContestLevel();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		Contest mvElectionContest = mvElection.getContest();
		ContestArea contestArea = mvElectionContest.getContestAreaSet().iterator().next();
		return new ContestInfo(mvElection.getElectionPath(), mvElection.getElectionName(), mvElection.getContestName(), contestArea.getMvArea().getAreaPath());
	}

	@Override
	@SecurityNone
	public ContestInfo findContestInfoByElectionAndArea(ElectionPath electionPath, AreaPath areaPath) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		return contestInfoRepository.contestForElectionAndArea(mvElection.getElection(), mvArea);
	}

	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public ElectionPath findContestPathByElectionAndArea(UserData userData, ElectionPath electionPath, AreaPath areaPath) {
		return findContestInfoByElectionAndArea(electionPath, areaPath).getElectionPath();
	}

	@Override
	@SecurityNone
	public List<ContestInfo> contestOrElectionByAreaPath(AreaPath areaPath) {
		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		List<ContestInfo> list = contestInfoRepository.contestsForArea(mvArea.getPk());
		// If any boroughs, group them
		List<ContestInfo> result = new ArrayList<>();
		ContestInfo boroughGroup = null;
		for (ContestInfo info : list) {
			if (isContestLevel(info.getAreaLevel())) {
				if (isGroupLevel(info.getAreaLevel())) {
					// Only group Oslo borough contests, throw the rest
					if (boroughGroup == null) {
						boroughGroup = new ContestInfo(info.getElectionPath().toElectionPath().path(), info.getElectionName(), null,
								info.getAreaPath().toMunicipalityPath().path());
						result.add(boroughGroup);
					}
				} else {
					result.add(info);
				}
			}
		}
		return result;
	}

	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public List<ContestInfo> contestsByAreaAndElectionPath(UserData userData, AreaPath areaPath, ElectionPath electionPath, AreaLevelEnum areaLevelFilter) {
		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		return contestInfoDomainService.contestsByAreaAndElectionPath(mvArea, electionPath, areaLevelFilter);
	}

	@Override
	@SecurityNone
	public List<ContestInfo> electionsInElectionEvent(UserData userData, ElectionPath electionEventPath) {
		electionEventPath.assertLevel(ELECTION_EVENT);
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionEventPath.tilValghierarkiSti());
		return mvElection.getElectionEvent().elections().stream()
				.map(election -> new ContestInfo(election.electionPath(), election.getName(), "", AreaPath.from(electionEventPath.getElectionEventId())))
				.collect(Collectors.toList());
	}

	private boolean isContestLevel(AreaLevelEnum areaLevel) {
		return areaLevel.getLevel() <= AreaLevelEnum.BOROUGH.getLevel();
	}

	private boolean isGroupLevel(AreaLevelEnum areaLevel) {
		return areaLevel.getLevel() > AreaLevelEnum.MUNICIPALITY.getLevel();
	}
}
