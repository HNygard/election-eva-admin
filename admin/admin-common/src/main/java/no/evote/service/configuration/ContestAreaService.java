package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ContestArea;

public interface ContestAreaService extends Serializable {
	/**
	 * @deprecated Replaced by {@link #findContestAreasForContestPath(UserData, ElectionPath)}
	 */
	@Deprecated
	List<ContestArea> findContestAreasForContest(Long contestPk);

	List<ContestArea> findContestAreasForContestPath(UserData userData, ElectionPath contestPath);

	List<ContestArea> findContestAreasForElectionPath(UserData userData, ElectionPath electionPath);

	ContestArea create(UserData userData, ContestArea contestArea);

	void delete(UserData userData, Long pk);

	ContestArea update(UserData userData, ContestArea contestArea);
}
