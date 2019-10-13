package no.valg.eva.admin.valgnatt.domain.service.valgnattrapport;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.event.TellingEndrerStatus;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import no.valg.eva.admin.valgnatt.repository.ValgnattrapportRepository;

import org.apache.log4j.Logger;

/**
 * Håndter oppdaterting av korresponderende Valgnattrapport når en telling blir godkjent.
 */
@SuppressWarnings("unused")
@Default
@ApplicationScoped
public class TellingEndrerStatusDomainService {

	private static final Logger LOGGER = Logger.getLogger(TellingEndrerStatusDomainService.class);

	/** mapper kategori og qualifier til report type */
	private static Map<ReportTypeKey, ReportType> reportTypeMap = new HashMap<>();
	static {
		reportTypeMap.put(new ReportTypeKey(FO, PRELIMINARY), ReportType.STEMMESKJEMA_FF);
		reportTypeMap.put(new ReportTypeKey(FS, PRELIMINARY), ReportType.STEMMESKJEMA_FF);
		reportTypeMap.put(new ReportTypeKey(FO, FINAL), ReportType.STEMMESKJEMA_FE);
		reportTypeMap.put(new ReportTypeKey(FS, FINAL), ReportType.STEMMESKJEMA_FE);
		reportTypeMap.put(new ReportTypeKey(VO, PRELIMINARY), ReportType.STEMMESKJEMA_VF);
		reportTypeMap.put(new ReportTypeKey(VF, PRELIMINARY), ReportType.STEMMESKJEMA_VF);
		reportTypeMap.put(new ReportTypeKey(VS, PRELIMINARY), ReportType.STEMMESKJEMA_VF);
		reportTypeMap.put(new ReportTypeKey(VB, PRELIMINARY), ReportType.STEMMESKJEMA_VF);
		reportTypeMap.put(new ReportTypeKey(VO, FINAL), ReportType.STEMMESKJEMA_VE);
		reportTypeMap.put(new ReportTypeKey(VF, FINAL), ReportType.STEMMESKJEMA_VE);
		reportTypeMap.put(new ReportTypeKey(VS, FINAL), ReportType.STEMMESKJEMA_VE);
		reportTypeMap.put(new ReportTypeKey(VB, FINAL), ReportType.STEMMESKJEMA_VE);
	}

	private MvElectionRepository mvElectionRepository;
	private ValgnattrapportRepository valgnattrapportRepository;
	private RapporteringsområdeDomainService rapporteringsomradeDomainService;

	@Inject
	public TellingEndrerStatusDomainService(MvElectionRepository mvElectionRepository,
			ValgnattrapportRepository valgnattrapportRepository, RapporteringsområdeDomainService rapporteringsområdeDomainService) {
		this.mvElectionRepository = mvElectionRepository;
		this.valgnattrapportRepository = valgnattrapportRepository;
		this.rapporteringsomradeDomainService = rapporteringsområdeDomainService;
	}

	public void oppdaterValgnattrapport(@Observes TellingEndrerStatus tellingEndrerStatus) {
		if (opptellingSomIkkeRapporteresTilEvaResultat(tellingEndrerStatus)) {
			return;
		}

		ReportType reportType = reportTypeMap.get(new ReportTypeKey(tellingEndrerStatus.getCountCategory(), tellingEndrerStatus.getCountQualifier()));
		if (reportType == null) {
			LOGGER.warn("Fant ikke report type for " + tellingEndrerStatus.getCountCategory() + ", " + tellingEndrerStatus.getCountQualifier());
			return;
		}
		MvArea mvArea = rapporteringsomradeDomainService.kretsForRapportering(tellingEndrerStatus.getAreaPath());
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(tellingEndrerStatus.getContestPath().tilValghierarkiSti());

		Valgnattrapport valgnattrapport = valgnattrapportRepository.byContestReportTypeAndMvArea(mvElection.getContest(), reportType, mvArea);
		if (valgnattrapport == null) {
			LOGGER.debug("Fant ingen Valgnattrapport å oppdatere for " + tellingEndrerStatus);
			return;
		}
		LOGGER.debug("Fant valgnattrapport for " + tellingEndrerStatus);
		if (valgnattrapport.isOk()) {
			valgnattrapport.maaRapporteresPaaNytt();
			LOGGER.debug("Oppdaterte Valgnattrapport for " + tellingEndrerStatus);
		}
	}

	private boolean opptellingSomIkkeRapporteresTilEvaResultat(@Observes TellingEndrerStatus tellingEndrerStatus) {
		return EnumSet.of(FYLKESVALGSTYRET, OPPTELLINGSVALGSTYRET).contains(tellingEndrerStatus.getReportingUnitTypeId());
	}

	private static class ReportTypeKey {
		private final CountCategory countCategory;
		private final CountQualifier countQualifier;

		private ReportTypeKey(CountCategory countCategory, CountQualifier countQualifier) {
			this.countCategory = countCategory;
			this.countQualifier = countQualifier;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			ReportTypeKey that = (ReportTypeKey) o;

			if (countCategory != that.countCategory) {
				return false;
			}
			return countQualifier == that.countQualifier;
		}

		@Override
		public int hashCode() {
			int result = countCategory.hashCode();
			result = 31 * result + countQualifier.hashCode();
			return result;
		}
	}

}
