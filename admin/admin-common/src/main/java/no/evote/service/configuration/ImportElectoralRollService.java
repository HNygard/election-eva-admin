package no.evote.service.configuration;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

public interface ImportElectoralRollService {

	void validateImportFile(UserData userData, ElectionEvent electionEvent, String filePath);
		
	void preliminaryFullImportElectoralRoll(UserData userData, final ElectionEvent electionEvent, final String filePath);

	void finalFullImportElectoralRoll(UserData userData, final ElectionEvent electionEvent, final String filePath);

}
