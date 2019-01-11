package no.valg.eva.admin.common.counting.model.valgnatt;

import java.io.Serializable;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.counting.domain.model.report.ReportType;

import org.joda.time.DateTime;

/**
 * Metadata for en rapportering til EVA Resultat (Valgnatt).  Eksempler på rapporteringer er Stemmeskjema og Oppgjorsskjema.
 */
public class Valgnattrapportering implements Serializable {

	static final String SENDING = "Sending";
	static final String OK = "OK";
	public static final String RESENDES = "RESENDES";
	public static final String NOT_SENT = "NOT_SENT";

	private final AreaPath areaPath;
	private final String areaName;
	private final ElectionPath contestPath;
	private final ElectionPath electionPath;
	private final ReportType reportType;
	private final ValgnattrapportPk valgnattrapportPk;
    private final boolean 	klarForRapportering;
    
    private DateTime auditTimestamp;
    private String status;

	public static class ValgnattrapportPk implements Serializable {
		private final Long valgnattrapportPk;

		public ValgnattrapportPk(Long valgnattrapportPk) {
			this.valgnattrapportPk = valgnattrapportPk;
		}

		public Long getValgnattrapportPk() {
			return valgnattrapportPk;
		}
	}
	
    public Valgnattrapportering(AreaPath areaPath, String areaName, ElectionPath contestPath, ElectionPath electionPath,
								ReportType reportType, ValgnattrapportPk valgnattrapportPk, boolean klarForRapportering, DateTime auditTimestamp, String status) {
		this.areaPath = areaPath;
        this.areaName = areaName;
		this.contestPath = contestPath;
		this.electionPath = electionPath;
		this.reportType = reportType;
		this.valgnattrapportPk = valgnattrapportPk;
        this.klarForRapportering = klarForRapportering;
        this.auditTimestamp = auditTimestamp;
		this.status = status;
	}

	public void oppdaterStatusSendt() {
		status = SENDING;
		auditTimestamp = new DateTime();
	}
	
	public boolean isGeografiStemmeberettigede() {
		return ReportType.GEOGRAFI_STEMMEBERETTIGEDE.equals(reportType);
	}

	public boolean isPartierOgKandidater() {
		return reportType != null && reportType == ReportType.PARTIER_OG_KANDIDATER;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}
    
	public ElectionPath getContestPath() {
		return contestPath;
	}

	public ElectionPath getElectionPath() {
		return electionPath;
	}

	public ReportType getReportType() {
		return reportType;
	}

	public String getStatus() {
		return status;
	}

	public DateTime getAuditTimestamp() {
		return auditTimestamp;
	}

	public ValgnattrapportPk getValgnattrapportPk() {
		return valgnattrapportPk;
	}

    public boolean isKlarForRapportering() {
        return klarForRapportering;
    }
    
    public boolean isEndelig() {
        return reportType == ReportType.STEMMESKJEMA_FE || reportType == ReportType.STEMMESKJEMA_VE;
    }

    public boolean isForelopig() {
        return reportType == ReportType.STEMMESKJEMA_FF || reportType == ReportType.STEMMESKJEMA_VF;
    }
    
    public boolean isValgting() {
        return reportType == ReportType.STEMMESKJEMA_VF || reportType == ReportType.STEMMESKJEMA_VE;
    }

    public boolean isForhand() {
        return reportType == ReportType.STEMMESKJEMA_FF || reportType == ReportType.STEMMESKJEMA_FE;
    }
    
    public boolean isKretsvis() {
        return areaPath.isPollingDistrictLevel();
    }

    public String getAreaName() {
        return areaName;
    }

	public boolean isSendt() {
		return OK.equals(status);
	}

	public boolean isNotSendt() {
		return !OK.equals(status);
	}
	
	public boolean kanRapporteres() {
		return klarForRapportering && ("RESENDES".equals(status) || "NOT_SENT".equals(status));
	}

	public boolean ferdigRapportert() {
		return "OK".equals(status);
	}
}
