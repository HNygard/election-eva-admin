package no.valg.eva.admin.counting.domain.model.report;

/**
 * Types of report Admin sends to Valgnatt.
 */
public enum ReportType {
	PARTIER_OG_KANDIDATER, GEOGRAFI_STEMMEBERETTIGEDE, STEMMESKJEMA_FF, STEMMESKJEMA_FE, STEMMESKJEMA_VF, STEMMESKJEMA_VE, VALGOPPGJOR;
    
	public String textId() {
		return "@valgnatt.skjema[" + name().toLowerCase() + "]";
	}
	
	public boolean isStemmeskjema() {
		return this == STEMMESKJEMA_FE || this == STEMMESKJEMA_FF || this == STEMMESKJEMA_VE || this == STEMMESKJEMA_VF;
	}
	
	public boolean isForhaandsstemmeskjema() {
		return this == STEMMESKJEMA_FF || this == STEMMESKJEMA_FE;
	}
}
