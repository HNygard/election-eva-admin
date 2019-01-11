package no.valg.eva.admin.common.configuration.model.local;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.configuration.model.Displayable;

public class ElectionCardConfig extends VersionedObject implements Displayable {

	public static final String REGEX_INFO_TEXT = "[^;]*";

	private final ReportingUnit reportingUnit;
	private String infoText;
	private List<ElectionDayPollingPlace> places = new ArrayList<>();

	public ElectionCardConfig(ReportingUnit reportingUnit) {
		this(reportingUnit, 0);
	}

	public ElectionCardConfig(ReportingUnit reportingUnit, int municipalityVersion) {
		super(municipalityVersion);
		this.reportingUnit = reportingUnit;
	}

	@Override
	public String display() {
		if (reportingUnit.getAreaPath().isRootLevel()) {
			return reportingUnit.getNameLine();
		}
		return reportingUnit.getAreaPath().getLeafId() + "-" + reportingUnit.getAreaName();
	}

	public ReportingUnit getReportingUnit() {
		return reportingUnit;
	}

	public String getInfoText() {
		return infoText;
	}

	public void setInfoText(String infoText) {
		this.infoText = infoText;
	}

	public List<ElectionDayPollingPlace> getPlaces() {
		return places;
	}

	public void setPlaces(List<ElectionDayPollingPlace> places) {
		this.places = places;
	}
}
