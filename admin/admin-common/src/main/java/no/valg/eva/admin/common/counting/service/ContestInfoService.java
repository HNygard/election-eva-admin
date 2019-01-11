package no.valg.eva.admin.common.counting.service;

import java.io.Serializable;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;

/**
 * Interface for retrieving info about contests.
 */
public interface ContestInfoService extends Serializable {
	ContestInfo findContestInfoByPath(ElectionPath contestPath);

	ContestInfo findContestInfoByElectionAndArea(ElectionPath electionPath, AreaPath areaPath);

	ElectionPath findContestPathByElectionAndArea(UserData userData, ElectionPath electionPath, AreaPath areaPath);

	List<ContestInfo> contestOrElectionByAreaPath(AreaPath areaPath);

	/**
	 * @param areaPath
	 *            defines area under which contests are located. Contests may be related to subareas of this area, eg. municipalities in county or boroughs in
	 *            municipality
	 * @param electionPath
	 *            path to contest, used for sami election
	 * @param areaLevelFilter
	 *            if specified and not root, contests with this area level are returned
	 * @return contest info instances for contests
	 */
	List<ContestInfo> contestsByAreaAndElectionPath(UserData userData, AreaPath areaPath, ElectionPath electionPath, AreaLevelEnum areaLevelFilter);
	
	List<ContestInfo> electionsInElectionEvent(UserData userData, ElectionPath electionEventPath);
}
