package no.valg.eva.admin.counting.application;

import static com.codepoetics.protonpack.StreamUtils.groupRuns;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.Collection;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.service.CountingOverviewService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.service.ContestAreaDomainService;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.service.countingoverview.CountingOverviewDomainService;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "CountingOverviewService")
@Default
@Remote(CountingOverviewService.class)
public class CountingOverviewApplicationService implements CountingOverviewService {
	private MvElectionRepository mvElectionRepository;
	private MvAreaRepository mvAreaRepository;
	private CountingOverviewDomainService countingOverviewDomainService;
	private ContestAreaDomainService contestAreaDomainService;

	public CountingOverviewApplicationService() {
		// CDI
	}

	@Inject
	public CountingOverviewApplicationService(CountingOverviewDomainService countingOverviewDomainService, MvElectionRepository mvElectionRepository,
			MvAreaRepository mvAreaRepository, ContestAreaDomainService contestAreaDomainService) {
		this.countingOverviewDomainService = countingOverviewDomainService;
		this.mvElectionRepository = mvElectionRepository;
		this.mvAreaRepository = mvAreaRepository;
		this.contestAreaDomainService = contestAreaDomainService;
	}

	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public List<ContestInfo> electionsFor(UserData userData, AreaPath areaPath) {
		if (userData.isElectionEventAdminUser()) {
			return electionsForFylkesvalgstyretOrValgstyret(userData.getOperatorElectionPath(), areaPath);
		}
		if (userData.isOpptellingsvalgstyret()) {
			return electionsForOpptellingsvalgstyret(userData);
		}
		if (userData.isFylkesvalgstyret() || userData.isValgstyret()) {
			return electionsForFylkesvalgstyretOrValgstyret(userData.getOperatorElectionPath(), userData.getOperatorAreaPath());
		}
		throw new IllegalArgumentException("user is neither representing opptellingsvalgstyret, fylkesvalgstyret or valgstyret");
	}

	private List<ContestInfo> electionsForOpptellingsvalgstyret(UserData userData) {
		MvElection mvElection = mvElectionRepository.findByPk(userData.getOperatorMvElection().getPk());
		return singletonList(new ContestInfo(mvElection.getElectionPath(), mvElection.getElectionName(), mvElection.getContestName(), null));
	}

	private List<ContestInfo> electionsForFylkesvalgstyretOrValgstyret(ElectionPath electionPath, AreaPath areaPath) {
		return groupRuns(
				mvElectionRepository
						.findContestsForElectionAndArea(electionPath, areaPath)
						.stream().sorted(this::compareAreaPath),
				this::compareAreaLevel)
				.map(list -> list.get(0))
				.map(this::contestInfo)
				.collect(toList());
	}

	private int compareAreaPath(MvElection mvElection1, MvElection mvElection2) {
		return mvElection1.getElectionPath().compareTo(mvElection2.getElectionPath());
	}

	private int compareAreaLevel(MvElection mvElection1, MvElection mvElection2) {
		return mvElection1.getAreaLevel() - mvElection2.getAreaLevel();
	}

	private ContestInfo contestInfo(MvElection mvElection) {
		ElectionPath contestPath = ElectionPath.from(mvElection.getElectionPath());
		String electionName = mvElection.getElectionName();
		if (mvElection.getActualAreaLevel() == BOROUGH) {
			return new ContestInfo(contestPath.toElectionPath(), electionName, null, null);
		}
		return new ContestInfo(contestPath, electionName, mvElection.getContestName(), null);
	}

	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public List<CountingOverviewRoot> countingOverviewsFor(UserData userData, ElectionPath electionPath, AreaPath areaPath) {
		if (userData.isElectionEventAdminUser()) {
			return countingOverviewsForElectionEventAdmin(electionPath, areaPath);
		}
		if (userData.isOpptellingsvalgstyret()) {
			return countingOverviewsForOpptellingsvalgstyret(userData);
		}
		if (userData.isFylkesvalgstyret()) {
			return countingOverviewsForFylkesvalgstyret(electionPath.tilValghierarkiSti().tilValgdistriktSti(), userData.getOperatorAreaPath());
		}
		if (userData.isValgstyret()) {
			return countingOverviewsForValgstyret(electionPath, userData.getOperatorAreaPath());
		}
		throw new IllegalArgumentException("user is neither representing opptellingsvalgstyret, fylkesvalgstyret or valgstyret");
	}

	private List<CountingOverviewRoot> countingOverviewsForElectionEventAdmin(ElectionPath electionPath, AreaPath areaPath) {
		if (areaPath.isCountyLevel()) {
			return countingOverviewsForFylkesvalgstyret(electionPath.tilValghierarkiSti().tilValgdistriktSti(), areaPath);
		}
		if (areaPath.isMunicipalityLevel()) {
			return countingOverviewsForValgstyret(electionPath, areaPath);
		}
		throw new IllegalArgumentException("area path must either on county or municipality");
	}

	private List<CountingOverviewRoot> countingOverviewsForValgstyret(ElectionPath electionPath, AreaPath areaPath) {
		if (electionPath.getLevel() == CONTEST) {
			return countingOverviewsForValgstyret(electionPath.tilValghierarkiSti().tilValgdistriktSti(), areaPath);
		} else {
			return countingOverviewsForValgstyret(electionPath.tilValghierarkiSti().tilValgSti(), areaPath);
		}
	}

	private List<CountingOverviewRoot> countingOverviewsForOpptellingsvalgstyret(UserData userData) {
		MvElection operatorMvElection = userData.getOperatorMvElection();
		ElectionPath contestPath = ElectionPath.from(operatorMvElection.getElectionPath());
		Collection<ContestArea> contestAreas = contestAreaDomainService.contestAreasFor(contestPath);
		long childAreaCount = contestAreas.stream().filter(ContestArea::isChildArea).count();
		return contestAreas
				.stream()
				.filter(contestArea -> includeArea(contestArea, childAreaCount))
				.sorted(this::orderByAreaPath)
				.map(countingOverviewDomainService::countingOverviewForOpptellingsvalgstyret)
				.collect(toList());
	}

	private boolean includeArea(ContestArea contestArea, long childAreaCount) {
		return contestArea.isParentArea() && childAreaCount > 0 || !contestArea.isChildArea() && !contestArea.isParentArea();
	}

	private int orderByAreaPath(ContestArea contestArea1, ContestArea contestArea2) {
		return contestArea1.getAreaPath().compareTo(contestArea2.getAreaPath());
	}

	private List<CountingOverviewRoot> countingOverviewsForFylkesvalgstyret(ValgdistriktSti valgdistriktSti, AreaPath areaPath) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(valgdistriktSti);
		Contest contest = mvElection.getContest();
		return mvAreaRepository
				.findByPathAndChildLevel(areaPath)
				.stream()
				.map(mvArea -> countingOverviewDomainService.countingOverviewForFylkesvalgstyret(contest, mvArea))
				.collect(toList());
	}

	private List<CountingOverviewRoot> countingOverviewsForValgstyret(ValgSti valgSti, AreaPath areaPath) {
		return countingOverviewsForValgstyret((ValghierarkiSti) valgSti, areaPath);
	}

	private List<CountingOverviewRoot> countingOverviewsForValgstyret(ValgdistriktSti valgdistriktSti, AreaPath areaPath) {
		return countingOverviewsForValgstyret((ValghierarkiSti) valgdistriktSti, areaPath);
	}

	private List<CountingOverviewRoot> countingOverviewsForValgstyret(ValghierarkiSti valghierarkiSti, AreaPath areaPath) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(valghierarkiSti);
		if (mvElection.getActualElectionLevel() == ELECTION && mvElection.getActualAreaLevel() == BOROUGH) {
			Election election = mvElection.getElection();
			return mvAreaRepository
					.findByPathAndChildLevel(areaPath)
					.stream()
					.filter(MvArea::isNotMunicipalityBorough)
					.map(mvArea -> countingOverviewDomainService.countingOverviewForValgstyret(election.contestRelatedTo(mvArea), mvArea))
					.collect(toList());
		}
		if (mvElection.getActualElectionLevel() == CONTEST) {
			Contest contest = mvElection.getContest();
			MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
			return singletonList(countingOverviewDomainService.countingOverviewForValgstyret(contest, mvArea));
		}
		throw new IllegalArgumentException(format("Ugyldig valghierarkisti! Stien er på valgnivå for et valg som ikke er på bydelsnivå: %s", valghierarkiSti));
	}
}
