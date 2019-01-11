package no.valg.eva.admin.common.reporting.model;

import java.io.Serializable;

public class SelectableReportParameterValue implements Serializable {
	private String valueId;
	private String label;

	public SelectableReportParameterValue(String valueId, String label) {
		this.valueId = valueId;
		this.label = label;
	}

	public SelectableReportParameterValue(String label) {
		this.label = label;
	}

	public String getValueId() {
		return valueId;
	}

	public String getLabel() {
		return label;
	}
}
