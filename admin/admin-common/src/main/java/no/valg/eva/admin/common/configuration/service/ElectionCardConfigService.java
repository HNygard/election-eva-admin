package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;

public interface ElectionCardConfigService extends Serializable {

	ElectionCardConfig findElectionCardByArea(UserData userData, AreaPath path);

	ElectionCardConfig save(UserData userData, ElectionCardConfig electionCard);

}
