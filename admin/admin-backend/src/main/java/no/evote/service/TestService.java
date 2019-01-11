package no.evote.service;

import java.util.List;

import no.evote.model.Batch;
import no.evote.security.UserData;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.rbac.domain.model.Role;

public interface TestService {

	Role findByElectionEventAndId(ElectionEvent e, String id);

	void deleteFromVoter(UserData userData, Long electionEventPk);

	void deleteVoterImportBatch(UserData userData, Long voterImportBatchPk);

	void deleteVoter(UserData userData, Long voterPk);

	void deleteVotersByElectionEvent(UserData userData, Long electionEventPk);

	void deleteGeneratedElectoralRoll(UserData userData, String electionEventId);

	void deleteGeneratedEML(UserData userData, String electionEventId);

	void deleteContest(UserData userData, Contest contest);

	Contest createContest(UserData userData, Contest contest);

	void deleteElectionEvent(UserData userData, Long pk);

	List<ReportingUnitType> findAllReportingUnitTypes();

	Voter findVoterByPk(UserData userData, Long pk);

	void removeAllBatches(Jobbkategori category);

	<T> T createEntity(UserData userData, T entity);

	<T> void deleteEntity(UserData userData, T entity);

	<T> T updateEntity(UserData userData, T entity);

	Batch createBatchForGenerateVoterNumber(UserData userData);

}
