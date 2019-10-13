package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Beskyttet;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsmåter;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Administrere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "MvElectionService")



@Default
@Remote(MvElectionService.class)
public class MvElectionServiceEjb implements MvElectionService {
	@Inject
	private MvElectionServiceBean mvElectionService;
	@Inject
	private MvElectionRepository mvElectionRepository;

	@Override
	@SecurityNone
	public MvElection findByPk(Long pk) {
		return mvElectionRepository.findByPk(pk);
	}

	@Override
	@Security(accesses = Aggregert_Beskyttet, type = READ)
	public MvElection findRoot(UserData userData, long electionEvent) {
		return mvElectionRepository.findRoot(electionEvent);
	}

	@Override
	@Security(accesses = Konfigurasjon_Opptellingsmåter, type = READ)
	public List<MvElection> findByPathAndChildLevel(UserData userData, MvElection mvElection) {
		return mvElectionRepository.findByPathAndChildLevel(mvElection);
	}

	@Override
	@SecurityNone
	public List<MvElection> findByPathAndLevel(String path, int level) {
		return mvElectionRepository.findByPathAndLevel(path, level);
	}

	@Override
	@Security(accesses = Tilgang_Brukere_Administrere, type = READ)
	public List<MvElectionMinimal> findByPathAndLevel(UserData userData, ElectionPath electionPath, ElectionLevelEnum electionLevel) {
		List<MvElection> elections = mvElectionRepository.findByPathAndLevel(electionPath.path(), electionLevel.getLevel());
		return elections.stream().map(e -> mvElectionService.getMvElectionMinimal(userData, e)).collect(Collectors.toList());
	}

	@Override
	@SecurityNone
	public MvElection findSingleByPath(ElectionPath path) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(path.tilValghierarkiSti());
		mvElection.getContest().getFirstContestArea();
		return mvElection;
	}

	@Override
	@SecurityNone
	public MvElection findSingleByPath(ValghierarkiSti valghierarkiSti) {
		return mvElectionRepository.finnEnkeltMedSti(valghierarkiSti);
	}

	@Override
	@SecurityNone
	public boolean hasElectionsWithElectionType(MvElection mvElection, ElectionType electionType) {
		return mvElectionRepository.hasElectionsWithElectionType(mvElection, electionType);
	}

	@Override
	@SecurityNone
	public boolean hasElectionsWithElectionTypeMinimal(MvElectionMinimal mvElectionMinimal, ElectionType electionType) {
		return mvElectionService.hasElectionsWithElectionTypeMinimal(mvElectionMinimal, electionType);
	}

	@Override
	@SecurityNone
	public MvElectionMinimal findSingleByPathMinimal(UserData userData, String path) {
		return mvElectionService.findSingleByPathMinimal(userData, path);
	}

	@Override
	@SecurityNone
	public MvElectionMinimal getMvElectionMinimal(UserData userData, MvElection mvElection) {
		return mvElectionService.getMvElectionMinimal(userData, mvElection);
	}

	@Override
	@SecurityNone
	public boolean electionEventHasElectionOnBoroughLevel(ElectionPath electionPath) {
		return mvElectionRepository.findByPathAndLevel(electionPath, ElectionLevelEnum.CONTEST)
				.stream()
				.anyMatch(mvElection -> mvElection.getActualAreaLevel() == AreaLevelEnum.BOROUGH);
	}

	@Override
	@SecurityNone
	public List<MvElectionMinimal> findByPathAndChildLevelMinimal(UserData userData, Long mvElectionPk, boolean includeContestsAboveMyLevel) {
		return mvElectionService.findByPathAndChildLevelMinimal(userData, mvElectionPk, includeContestsAboveMyLevel);
	}
}
