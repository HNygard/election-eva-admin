package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

public interface MvElectionService extends Serializable {

	MvElection findRoot(UserData userData, long electionEvent);

	List<MvElection> findByPathAndChildLevel(UserData userData, MvElection mvElection);

	@Deprecated
	List<MvElection> findByPathAndLevel(String path, int level);

	List<MvElectionMinimal> findByPathAndLevel(UserData userData, ElectionPath electionPath, ElectionLevelEnum electionLevel);

	@Deprecated
	MvElection findSingleByPath(ElectionPath path);

	MvElection findSingleByPath(ValghierarkiSti valghierarkiSti);

	boolean hasElectionsWithElectionType(MvElection mvElection, ElectionType electionType);

	MvElection findByPk(Long pk);

	MvElectionMinimal findSingleByPathMinimal(UserData userData, String path);

	List<MvElectionMinimal> findByPathAndChildLevelMinimal(UserData userData, Long mvElectionPk, boolean includeContestsAboveMyLevel);

	boolean hasElectionsWithElectionTypeMinimal(MvElectionMinimal mvElection, ElectionType electionTypeFilter);

	MvElectionMinimal getMvElectionMinimal(UserData userData, MvElection mvElection);
	
	boolean electionEventHasElectionOnBoroughLevel(ElectionPath electionPath);
}
