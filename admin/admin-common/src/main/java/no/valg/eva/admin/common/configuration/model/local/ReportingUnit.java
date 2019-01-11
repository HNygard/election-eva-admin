package no.valg.eva.admin.common.configuration.model.local;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;

public class ReportingUnit extends VersionedObject {
	private final AreaPath areaPath;
	private final ReportingUnitTypeId type;
	private Long pk;
	private String nameLine;
	private String areaName;
	private String address;
	private String postalCode;
	private String postTown;

	public ReportingUnit(AreaPath areaPath, ReportingUnitTypeId type, int version) {
		super(version);
		this.areaPath = areaPath;
		this.type = type;
	}

	public String getNameLine() {
		return nameLine;
	}

	public void setNameLine(String nameLine) {
		this.nameLine = nameLine;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}

	public ReportingUnitTypeId getType() {
		return type;
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getName() {
		return "@reporting_unit_type[" + type.getId() + "].name";
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPostTown() {
		return postTown;
	}

	public void setPostTown(String postTown) {
		this.postTown = postTown;
	}

	public boolean hasAddressFields() {
		return !isEmpty(address) && !isEmpty(postalCode) && !isEmpty(postTown);
	}
}
