package no.valg.eva.admin.backend.reporting.jasperserver.api;

import static com.google.common.collect.Lists.newArrayList;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.ReportMetaData.Format.PDF;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportMetaData {
	private String reportUri;
	private Boolean async = false;
	private String filenamePattern;
	private String reportName;
	private String description;

	@XmlElement(name = "areaLevel")
	private List<AreaLevel> areaLevels;
	private String areaPathMask;
	private Boolean hidden = null;
	private Boolean runNightly;
	@XmlElement(name = "mandatoryParameter")
	private List<String> mandatoryParameters;
	@XmlElement(name = "unselectableParameterValue")
	private List<UnselectableParameterValue> unselectableParameterValues;
	@XmlElement(name = "fixedParameterValue")
	private List<FixedParameterValue> fixedParameterValues;
	@XmlElement(name = "optionalPathParameter")
	private String optionalPathParameter;

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isHidden() {
		return Boolean.TRUE.equals(hidden);
	}

	public Boolean getRunNightly() {
		return runNightly;
	}

	public void setRunNightly(Boolean runNightly) {
		this.runNightly = runNightly;
	}

	@XmlElement(name = "format")
	private List<Format> formats = newArrayList(PDF);

	public List<Format> getFormats() {
		return formats;
	}

	public void setFormats(final List<Format> formats) {
		this.formats = formats;
	}

	public String getReportUri() {
		return reportUri;
	}

	public void setReportUri(final String reportUri) {
		this.reportUri = reportUri;
	}

	public Boolean getAsync() {
		return async;
	}

	public void setAsync(final Boolean async) {
		this.async = async;
	}

	public String getFilenamePattern() {
		return filenamePattern;
	}

	public void setFilenamePattern(final String filenamePattern) {
		this.filenamePattern = filenamePattern;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(final String reportName) {
		this.reportName = reportName;
	}

	public List<AreaLevel> getAreaLevels() {
		return areaLevels;
	}

	public void setAreaLevels(final List<AreaLevel> areaLevels) {
		this.areaLevels = areaLevels;
	}

	public List<String> getMandatoryParameters() {
		return mandatoryParameters;
	}

	public void setMandatoryParameters(List<String> mandatoryParameters) {
		this.mandatoryParameters = mandatoryParameters;
	}

	public String getOptionalPathParameter() {
		return optionalPathParameter;
	}

	public void setOptionalPathParameter(String optionalPathParameter) {
		this.optionalPathParameter = optionalPathParameter;
	}

	public List<UnselectableParameterValue> getUnselectableParameterValues() {
		return unselectableParameterValues;
	}

	public void setUnselectableParameterValues(List<UnselectableParameterValue> unselectableParameterValues) {
		this.unselectableParameterValues = unselectableParameterValues;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ReportMetaData that = (ReportMetaData) o;
		return new EqualsBuilder()
				.append(this.areaLevels, that.areaLevels)
				.append(this.async, that.async)
				.append(this.description, that.description)
				.append(this.filenamePattern, that.filenamePattern)
				.append(this.formats, that.formats)
				.append(this.reportName, that.reportName)
				.append(this.reportUri, that.reportUri)
				.append(this.areaPathMask, that.areaPathMask)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(reportUri)
				.append(async)
				.append(filenamePattern)
				.append(reportName)
				.append(description)
				.append(areaLevels)
				.append(formats)
				.append(areaPathMask)
				.hashCode();
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setAreaPathMask(String areaPathMask) {
		this.areaPathMask = areaPathMask;
	}

	public String getAreaPathMask() {
		return areaPathMask;
	}

	public void setFixedParameterValues(List<FixedParameterValue> fixedParameterValues) {
		this.fixedParameterValues = fixedParameterValues;
	}

	public List<FixedParameterValue> getFixedParameterValues() {
		return fixedParameterValues;
	}

	public enum Format {
		@XmlEnumValue("pdf")
		PDF("pdf"),
		@XmlEnumValue("csv")
		CSV("csv"),
		@XmlEnumValue("xls")
		XLS("xls"),
		@XmlEnumValue("xlsx")
		XLSX("xlsx");

		private final String name;

		Format(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public enum AreaLevel {
		@XmlEnumValue("electionEvent")
		ELECTION_EVENT,
		@XmlEnumValue("country")
		COUNTRY,
		@XmlEnumValue("county")
		COUNTY,
		@XmlEnumValue("municipality")
		MUNICIPALITY,
		@XmlEnumValue("borough")
		BOROUGH,
		@XmlEnumValue("pollingDistrict")
		POLLING_DISTRICT,
		@XmlEnumValue("pollingPlace")
		POLLING_PLACE
	}

	@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement
	public static class UnselectableParameterValue {
		private String userRoleMvAreaRegExp;
		private String parameter;
		private String value;

		public UnselectableParameterValue() {
		}

		public UnselectableParameterValue(String parameter, String value) {
			this.parameter = parameter;
			this.value = value;
		}

		public UnselectableParameterValue(String parameter, String value, String userRoleMvAreaRegExp) {
			this(parameter, value);
			this.userRoleMvAreaRegExp = userRoleMvAreaRegExp;
		}

		public String getParameter() {
			return parameter;
		}

		public void setParameter(String parameter) {
			this.parameter = parameter;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getUserRoleMvAreaRegExp() {
			return userRoleMvAreaRegExp;
		}

		public void setUserRoleMvAreaRegExp(String userRoleMvAreaRegExp) {
			this.userRoleMvAreaRegExp = userRoleMvAreaRegExp;
		}
	}

	@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement
	public static class FixedParameterValue {
		private String parameter;
		private String value;

		public FixedParameterValue() {
		}

		public FixedParameterValue(String parameter, String value) {
			this.parameter = parameter;
			this.value = value;
		}

		public String getParameter() {
			return parameter;
		}

		public String getValue() {
			return value;
		}
	}
}
