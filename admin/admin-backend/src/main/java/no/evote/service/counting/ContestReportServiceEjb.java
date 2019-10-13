package no.evote.service.counting;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "ContestReportService")


@Default
@Remote(ContestReportService.class)
public class ContestReportServiceEjb implements ContestReportService {
	@Inject
	private ContestReportRepository contestReportRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public boolean hasContestReport(UserData userData, Long contestPk) {
		return contestReportRepository.hasContestReport(contestPk);
	}

	/**
	 * @return true if contest report exists for contest path and reporting unit area path, false otherwise
	 */
	@Override
	@Security(accesses = Aggregert_Opptelling, type = READ)
	public boolean hasContestReport(UserData userData, ElectionPath contestPath, AreaPath reportingUnitAreaPath) {
		contestPath.assertContestLevel();
		MvElection contestMvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
		ReportingUnit reportingUnit;
		if (contestMvElection.getActualAreaLevel() == AreaLevelEnum.BOROUGH) {
			reportingUnit = reportingUnitRepository.getReportingUnit(contestPath, contestMvElection.contestAreaPath());
		} else {
			reportingUnit = reportingUnitRepository.getReportingUnit(contestPath, reportingUnitAreaPath);
		}
		return contestReportRepository.hasContestReport(contestMvElection.getContest(), reportingUnit);
	}
}
