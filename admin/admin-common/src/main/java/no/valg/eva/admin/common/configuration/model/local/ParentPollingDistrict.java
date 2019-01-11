package no.valg.eva.admin.common.configuration.model.local;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;

public class ParentPollingDistrict extends PollingDistrict {

	private List<RegularPollingDistrict> children = new ArrayList<>();

	public ParentPollingDistrict(AreaPath path) {
		this(path, 0);
	}

	public ParentPollingDistrict(AreaPath path, int version) {
		super(path, PollingDistrictType.PARENT, version);
	}

	public void addChild(RegularPollingDistrict district) {
		children.add(district.toChild());
	}

	public List<RegularPollingDistrict> getChildren() {
		return children;
	}

	@Override
	public boolean isValid() {
		if (children.isEmpty()) {
			return false;
		}
		for (RegularPollingDistrict district : children) {
			if (!district.isValid()) {
				return false;
			}
		}
		return super.isValid();
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
