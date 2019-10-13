package no.evote.service.configuration;

import static no.valg.eva.admin.common.AreaPath.from;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Brukere_Roller;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Manntall;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.configuration.ElectionRef;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

import com.google.common.base.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "MvAreaService")


@Default
@Remote(MvAreaService.class)
public class MvAreaServiceEjb implements MvAreaService {
	public static final Function<MvArea, AreaPath> AREA_PATH_FUNCTION = new Function<MvArea, AreaPath>() {
		@Override
		public AreaPath apply(MvArea mvArea) {
			return mvArea != null ? from(mvArea.getAreaPath()) : null;
		}
	};

	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;

	@Override
	@Security(accesses = { Aggregert_Brukere_Roller, Aggregert_Manntall }, type = READ)
	public MvArea findByPk(UserData userData, Long pk) {
		return mvAreaRepository.findByPk(pk);
	}

	@Override
	@SecurityNone
	public MvArea findRoot(Long eepk) {
		return mvAreaRepository.findRoot(eepk);
	}

	@Override
	@SecurityNone
	public List<MvArea> findByPathAndChildLevel(MvArea mvArea) {
		return mvAreaRepository.findByPathAndChildLevel(mvArea);
	}

	@Override
	@SecurityNone
	public List<MvArea> findByPathAndLevel(String path, int level) {
		return mvAreaRepository.findByPathAndLevel(path, level);
	}

	@Override
	@SecurityNone
	public List<MvArea> findByPathAndLevel(ValggeografiSti valggeografiSti, AreaLevelEnum level) {
		return mvAreaRepository.findByPathAndLevel(valggeografiSti.areaPath(), level);
	}

	@Override
	@SecurityNone
	@Deprecated
	public MvArea findSingleByPath(String path) {
		return mvAreaRepository.findSingleByPath(path);
	}

	@Override
	@SecurityNone
	public MvArea findSingleByPath(AreaPath path) {
		return mvAreaRepository.findSingleByPath(path);
	}

	@Override
	@SecurityNone
	public MvArea findSingleByPath(ValggeografiSti valggeografiSti) {
		return mvAreaRepository.findSingleByPath(valggeografiSti.areaPath());
	}

	@Override
	@SecurityNone
	public MvArea findSingleByPath(String electionEventId, AreaPath path) {
		return mvAreaRepository.findSingleByPath(electionEventId, path);
	}

	@Override
	@SecurityNone
	public List<AreaLevel> findAllAreaLevels(UserData userData) {
		return mvAreaRepository.findAllAreaLevels();
	}

	@Override
	@Security(accesses = Aggregert_Stemmegiving, type = READ)
	public MvArea findByMunicipalityAndPollingPlaceId(UserData userData, Long pk, String id) {
		return mvAreaRepository.findSingleByPollingPlaceIdAndMunicipalityPk(id, pk);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public List<ValgdistriktSti> findValgdistriktStierByValgStiWhereAllListProposalsAreApproved(UserData userData, ValgSti valgSti) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(valgSti);
		ElectionRef electionRef = new ElectionRef(mvElection.getElection().getPk());

		List<Contest> contests = mvAreaRepository.findContestsByElectionWhereAllBallotsAreProcessed(electionRef);
		return contests
				.stream()
				.map(contest -> ValghierarkiSti.valgdistriktSti(contest.electionPath()))
				.collect(Collectors.toList());
	}
}
