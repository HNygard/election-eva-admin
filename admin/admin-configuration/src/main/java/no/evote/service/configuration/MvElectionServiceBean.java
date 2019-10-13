package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

import com.google.common.collect.Multimap;

@Default
@ApplicationScoped
public class MvElectionServiceBean {
	@Inject
	private ContestAreaRepository contestAreaRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;

	public MvElectionServiceBean() {

	}

	public boolean hasElectionsWithElectionTypeMinimal(MvElectionMinimal mvElectionMinimal, ElectionType electionType) {
		MvElection mvElection = mvElectionRepository.findByPk(mvElectionMinimal.getPk());
		return mvElectionRepository.hasElectionsWithElectionType(mvElection, electionType);
	}

	@Deprecated
	public MvElectionMinimal findSingleByPathMinimal(UserData userData, String path) {
		return findSingleByPathMinimal(userData, ElectionPath.from(path));
	}

	public MvElectionMinimal findSingleByPathMinimal(UserData userData, ElectionPath electionPath) {
		MvElection result = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		if (result == null) {
			return null;
		} else {
			return getMvElectionMinimal(userData, result);
		}
	}

	public MvElectionMinimal getMvElectionMinimal(UserData userData, MvElection mvElection) {
		boolean isContestOnMyLevelOrBelow = false;
		if (mvElection.getElectionLevel() == ElectionLevelEnum.CONTEST.getLevel()) {
			// don't need to check if user has access on top level
			isContestOnMyLevelOrBelow = userData.getOperatorRole().getMvArea().getAreaLevel() == 0
					|| contestAreaRepository.isContestOnOrBelowArea(mvElection.getContest().getPk(), userData.getOperatorRole().getMvArea().getPath());
		}
		return new MvElectionMinimal(mvElection, isContestOnMyLevelOrBelow);
	}

	public List<MvElectionMinimal> findByPathAndChildLevelMinimal(UserData userData, Long mvElectionPk, boolean includeContestsAboveMyLevel) {
		List<MvElectionMinimal> mvElectionMinimalList = new ArrayList<>();
		MvElection parentMvElection = mvElectionRepository.findByPk(mvElectionPk);
		List<MvElection> mvElectionList = mvElectionRepository.findByPathAndChildLevel(parentMvElection);

		// if we are not finding contests, we don't need to apply contestarea filter
		if (parentMvElection.getElectionLevel() != 2) {
			for (MvElection mvElection : mvElectionList) {
				mvElectionMinimalList.add(new MvElectionMinimal(mvElection, false));
			}
		} else {
			// first need to find all contest pks, in order to find all contestareas
			List<Long> contestPks = new ArrayList<>();
			for (MvElection mvElection : mvElectionList) {
				contestPks.add(mvElection.getContest().getPk());
			}

			if (!contestPks.isEmpty()) {
				// get a map containing contestPks as keys and a collection of mvArea paths as values
				Multimap<Long, String> contestMvAreaPaths = contestAreaRepository.getContestMvAreaPaths(contestPks);
				for (MvElection mvElection : mvElectionList) {
					Long contestPk = mvElection.getContest().getPk();
					Collection<String> mvAreaPaths = contestMvAreaPaths.get(contestPk);
					if (isContestOnMyLevelOrBelow(userData, mvAreaPaths)) {
						mvElectionMinimalList.add(new MvElectionMinimal(mvElection, true));
					} else if (includeContestsAboveMyLevel && isContestAboveMyLevel(userData, mvAreaPaths)) {
						mvElectionMinimalList.add(new MvElectionMinimal(mvElection, false));
					}
				}
			}
		}
		return mvElectionMinimalList;
	}

	private boolean isContestOnMyLevelOrBelow(final UserData userData, final Collection<String> mvAreaPaths) {
		for (String mvAreaPath : mvAreaPaths) {
			if (mvAreaPath.startsWith(userData.getOperatorRole().getMvArea().getPath())) {
				return true;
			}
		}
		return false;
	}

	private boolean isContestAboveMyLevel(final UserData userData, final Collection<String> mvAreaPaths) {
		for (String mvAreaPath : mvAreaPaths) {
			if (!userData.getOperatorRole().getMvArea().getPath().startsWith(mvAreaPath)) {
				return false;
			}
		}
		return true;
	}
}
