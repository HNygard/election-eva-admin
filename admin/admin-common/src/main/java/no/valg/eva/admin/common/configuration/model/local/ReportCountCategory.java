package no.valg.eva.admin.common.configuration.model.local;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.counting.constants.CountingMode;

public class ReportCountCategory extends VersionedObject {

	private no.valg.eva.admin.common.counting.model.CountCategory category;
	private List<CountingMode> countingModes = new ArrayList<>();
	private CountingMode countingMode = null;
	private boolean editable = false;

	public ReportCountCategory(no.valg.eva.admin.common.counting.model.CountCategory category, List<CountingMode> countingModes) {
		this(category, countingModes, 0);
	}

	public ReportCountCategory(no.valg.eva.admin.common.counting.model.CountCategory category, List<CountingMode> countingModes, int version) {
		super(version);
		this.category = category;
		this.countingModes = countingModes;
	}

	public no.valg.eva.admin.common.counting.model.CountCategory getCategory() {
		return category;
	}

	public List<CountingMode> getCountingModes() {
		return countingModes;
	}

	public void setCountingModes(List<CountingMode> countingModes) {
		this.countingModes = countingModes;
	}

	public CountingMode getCountingMode() {
		return countingMode;
	}

	public void setCountingMode(CountingMode countingMode) {
		this.countingMode = countingMode;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}
