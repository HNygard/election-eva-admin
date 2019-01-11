package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.Contest;

public interface ContestService extends Serializable {

	Contest get(UserData userData, ElectionPath contestPath);

	Contest save(UserData userData, Contest contest);

	void delete(UserData userData, ElectionPath contestPath);
}
