package no.valg.eva.admin.common;

import no.evote.constants.PollingPlaceType;

public class PollingPlaceArea extends Area {
	private PollingPlaceType pollingPlaceType;

	public PollingPlaceArea(AreaPath areaPath, String name) {
		this(areaPath, name, PollingPlaceType.NOT_APPLICABLE);
	}

	public PollingPlaceArea(AreaPath areaPath, String name, PollingPlaceType pollingPlaceType) {
        super(areaPath, name);
		this.pollingPlaceType = pollingPlaceType;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}
	
	public boolean contains(PollingPlaceArea area) {
		return this.areaPath.contains(area.getAreaPath());
	}

	public String getName() {
		return name;
	}

	public PollingPlaceType getPollingPlaceType() {
		return pollingPlaceType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PollingPlaceArea area = (PollingPlaceArea) o;

		if (pollingPlaceType != area.pollingPlaceType) {
			return false;
		}
		if (!areaPath.equals(area.areaPath)) {
			return false;
		}
		if (!name.equals(area.name)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = areaPath.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + (pollingPlaceType.ordinal());
		return result;
	}

	@Override
	public String toString() {
		return "Area{"
				+ "areaPath=" + areaPath
				+ ", name='" + name + '\''
				+ ", pollingPlaceType=" + pollingPlaceType.name()
				+ '}';
	}
}
