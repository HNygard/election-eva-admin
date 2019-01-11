package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valg_Valgdistrikt;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.ElectionLevelEnum;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "ContestAreaService")
@Remote(ContestAreaService.class)
public class ContestAreaServiceEjb implements ContestAreaService {
	@Inject
	private ContestAreaRepository contestAreaRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valgdistrikt, type = WRITE)
	public ContestArea create(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST, areaLevelDynamic = true) ContestArea contestArea) {
		checkExistingOnContest(contestArea);
		checkExistingOnElection(contestArea);
		return contestAreaRepository.create(userData, contestArea);
	}

	private void checkExistingOnContest(ContestArea contestArea) {
		List<ContestArea> areas = contestAreaRepository.findContestAreasForContest(contestArea.getContest().getPk());
		for (ContestArea cArea : areas) {
			if (cArea.getMvArea().equals(contestArea.getMvArea())) {
				String areaName = getAreaLevel(contestArea.getMvArea()).getName();
				String[] params = { areaName, contestArea.getMvArea().toString(), contestArea.getContest().getName() };
				throw new EvoteException(ErrorCode.ERROR_CODE_0450_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_AREA, null, params);
			}
		}
	}

	private void checkExistingOnElection(ContestArea contestArea) {
		if (!contestAreaRepository.findContestAreaForElectionAndMvArea(contestArea.getContest().getElection().getPk(), contestArea.getMvArea().getPk())
				.isEmpty()) {
			String areaName = getAreaLevel(contestArea.getMvArea()).getName();
			String[] params = { areaName, contestArea.getMvArea().toString(), contestArea.getContest().getElection().getName() };
			throw new EvoteException(ErrorCode.ERROR_CODE_0451_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_ELECTION, null, params);
		}
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valgdistrikt, type = WRITE)
	public void delete(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST, entity = ContestArea.class) Long pk) {
		ContestArea area = contestAreaRepository.findByPk(pk);
		// This should be placed in domain model.
		if (area.isParentArea()) {
			Set<ContestArea> areas = new HashSet<>(contestAreaRepository.findContestAreasForContest(area.getContest().getPk()));
			areas.remove(area);
			for (ContestArea contestArea : areas) {
				if (contestArea.isChildArea()) {
					throw new EvoteException("@election.contest.contest_area.delete_child_before_parent");
				}
			}
		}
		contestAreaRepository.delete(userData, pk);
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valgdistrikt, type = WRITE)
	public ContestArea update(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST, entity = ContestArea.class) ContestArea contestArea) {
		assertParentChildRules(contestArea);
		return contestAreaRepository.update(userData, contestArea);
	}

	@Override
	@SecurityNone
	public List<ContestArea> findContestAreasForContest(Long contestPk) {
		return contestAreaRepository.findContestAreasForContest(contestPk);
	}

	@Override
	@Security(accesses = { Konfigurasjon_Geografi, Aggregert_Opptelling }, type = READ)
	public List<ContestArea> findContestAreasForContestPath(UserData userData, ElectionPath contestPath) {
		contestPath.assertContestLevel();
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		return contestMvElection.getContest().getContestAreaList();
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public List<ContestArea> findContestAreasForElectionPath(UserData userData, ElectionPath electionPath) {
		electionPath.assertElectionLevel();
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		return contestAreaRepository.findByElection(mvElection.getElection());
	}

	private void assertParentChildRules(ContestArea contestArea) {
		// This should be placed in domain model.
		List<ContestArea> contestAreas = getContestAreas(contestArea);
		boolean childFound = false;
		boolean parentFound = false;
		for (ContestArea area : contestAreas) {
			if (area.isParentArea()) {
				if (parentFound) {
					// More than 1 parent is not allowed
					throw new EvoteException("@election.contest.contest_area.duplicate.parent_area");
				}
				parentFound = true;
			} else if (area.isChildArea()) {
				childFound = true;
			}
		}
		if (childFound && !parentFound) {
			throw new EvoteException("@election.contest.contest_area.orphan_not_allowed");
		}
	}

	private List<ContestArea> getContestAreas(ContestArea contestArea) {
		Set<ContestArea> contestAreas = new HashSet<>();
		if (contestArea.getPk() != null) {
			contestAreas.add(contestArea);
		}
		contestAreas.addAll(contestAreaRepository.findContestAreasForContest(contestArea.getContest().getPk()));
		List<ContestArea> result = new ArrayList<>(contestAreas);
		if (contestArea.getPk() == null) {
			result.add(contestArea);
		}
		return result;
	}

	private AreaLevel getAreaLevel(MvArea mvArea) {
		return mvAreaRepository.findAreaLevelById(Integer.toString(mvArea.getAreaLevel()));
	}
}
