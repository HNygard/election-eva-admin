package no.evote.service.configuration;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;

public interface LegacyContestService extends Serializable {

	Contest findByPk(UserData userData, Long pk);

	Contest findByElectionPath(UserData userData, ElectionPath electionPath);
}
