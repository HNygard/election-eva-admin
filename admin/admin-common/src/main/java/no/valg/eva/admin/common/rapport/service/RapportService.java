package no.valg.eva.admin.common.rapport.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;

public interface RapportService extends Serializable {

	List<ValghendelsesRapport> rapporterForValghendelse(UserData userData, ElectionPath electionEventPath);

	List<ValghendelsesRapport> rapporterForBruker(UserData userData, ElectionPath electionEventPath);

	List<ValghendelsesRapport> lagre(UserData userData, ElectionPath electionEventPath, List<ValghendelsesRapport> rapporter);

}
