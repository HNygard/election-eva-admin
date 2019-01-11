package no.evote.service.configuration;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public interface BallotService extends Serializable {
	
	void updateBallotStatus(UserData userData, Affiliation affiliation, BallotStatus ballotStatus);

	void delete(UserData userData, Ballot ballot);

	Ballot findByPk(UserData userData, Long pk);

	BallotStatus findBallotStatusById(UserData userData, int id);
}
