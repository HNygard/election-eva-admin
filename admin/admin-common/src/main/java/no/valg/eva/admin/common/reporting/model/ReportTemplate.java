package no.valg.eva.admin.common.reporting.model;

import static com.google.common.collect.ImmutableList.copyOf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.valg.eva.admin.backend.reporting.jasperserver.InselectableParameterValueForMvArea;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class ReportTemplate implements Serializable, Cloneable, Comparable<ReportTemplate> {
	private String reportUri;
	private String reportName;
	private String reportDescription;
	private String filenamePattern;
	private List<ReportParameter> parameters;
	private List<String> fileFormats;
	private Set<Integer> areaLevel;
	private String areaPathMask;
	private boolean hidden;
	private boolean async;
	private boolean maybePreGenerated;
	private Map<Pair<String, String>, InselectableParameterValueForMvArea> unselectableParameterValues;

	public ReportTemplate(final String reportUri, final String reportName, final String reportDescription, final String filenamePattern,
			final List<ReportParameter> parameters, List<String> fileFormats, Set<Integer> areaLevel, String areaPathMask,
			boolean hidden, boolean async, boolean maybePreGenerated, Map<Pair<String, String>, InselectableParameterValueForMvArea> unselectableParameterValues) {
		this.reportUri = reportUri;
		this.reportName = reportName;
		this.reportDescription = reportDescription;
		this.filenamePattern = filenamePattern;
		this.parameters = parameters;
		this.fileFormats = fileFormats;
		this.areaLevel = areaLevel;
		this.areaPathMask = areaPathMask;
		this.hidden = hidden;
		this.async = async;
		this.maybePreGenerated = maybePreGenerated;
		this.unselectableParameterValues = unselectableParameterValues;
	}

	public ReportTemplate withParameterValues(final Map<String, Object> valueMap) {
		if (parameters != null) {
			for (ReportParameter parameter : parameters) {
				if (valueMap.containsKey(parameter.getId())) {
					Object defaultValue = valueMap.get(parameter.getId());
					parameter.setDefaultValue(defaultValue);
					parameter.setInferred(true);
					// also tell descendant parameter about this' value
					Set<ReportParameter> dependentParameters = parameter.getDependentParameters();
					if (dependentParameters != null) {
						for (ReportParameter dependingParameter : dependentParameters) {
							dependingParameter.setParentValue(defaultValue);
							dependingParameter.setParent(parameter);
						}
					}
				}
			}
		}
		return this;
	}

	/**
	 * Sets the dependentParameters relationship between parameters
	 */
	public ReportTemplate withRelationships(ReportTemplate reportTemplate, String[] pathParameters, Map<String, Integer> parameterPathOrder) {
		Collection<ReportParameter> reportTemplateParameters = reportTemplate.getParameters();
		Map<String, ReportParameter> nameToParamMap = new HashMap<>(reportTemplateParameters.size());
		for (ReportParameter parameter : reportTemplateParameters) {
			nameToParamMap.put(parameter.getId(), parameter);
		}
		setRelationshipsForHierarchy(nameToParamMap, pathParameters, parameterPathOrder);
		return reportTemplate;
	}

	private void setRelationshipsForHierarchy(Map<String, ReportParameter> nameToParamMap, String[] pathParameters, Map<String, Integer> parameterPathOrder) {
		for (Map.Entry<String, ReportParameter> param : nameToParamMap.entrySet()) {
			// find next parameter in sequence if any
			Integer pathPosition = parameterPathOrder.get(param.getKey());
			if (pathPosition != null && pathPosition < pathParameters.length - 1) {
				ReportParameter childParam = nameToParamMap.get(pathParameters[pathPosition + 1]);
				if (childParam != null) {
					ReportParameter parentParam = param.getValue();
					parentParam.addDependentParameter(childParam);
					childParam.setParent(parentParam);
				}
			}
		}
	}

	public String getFilenamePattern() {
		return filenamePattern;
	}

	public void setFilenamePattern(final String filenamePattern) {
		this.filenamePattern = filenamePattern;
	}

	public String getReportUri() {
		return reportUri;
	}

	public void setReportUri(final String reportUri) {
		this.reportUri = reportUri;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(final String reportName) {
		this.reportName = reportName;
	}

	public String getReportDescription() {
		return reportDescription;
	}

	public void setReportDescription(final String reportDescription) {
		this.reportDescription = reportDescription;
	}

	public List<ReportParameter> getParameters() {
		return parameters;
	}

	public void setParameters(final List<ReportParameter> parameters) {
		this.parameters = parameters;
	}

	public List<String> getFileFormats() {
		return fileFormats;
	}

	public void setFileFormats(List<String> fileFormats) {
		this.fileFormats = fileFormats;
	}

	public Set<Integer> getAreaLevels() {
		return areaLevel;
	}

	public String getAreaPathMask() {
		return areaPathMask;
	}

	public boolean isHidden() {
		return hidden;
	}

	public boolean isAsync() {
		return async;
	}

	public boolean isMaybePreGenerated() {
		return maybePreGenerated;
	}

	public Map<Pair<String, String>, InselectableParameterValueForMvArea> getUnselectableParameterValues() {
		return unselectableParameterValues;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReportTemplate that = (ReportTemplate) o;

		if (areaLevel != null ? !areaLevel.equals(that.areaLevel) : that.areaLevel != null) {
			return false;
		}
		if (areaPathMask != null ? !areaPathMask.equals(that.areaPathMask) : that.areaPathMask != null) {
			return false;
		}
		if (fileFormats != null ? !fileFormats.equals(that.fileFormats) : that.fileFormats != null) {
			return false;
		}
		if (filenamePattern != null ? !filenamePattern.equals(that.filenamePattern) : that.filenamePattern != null) {
			return false;
		}
		if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) {
			return false;
		}
		if (reportDescription != null ? !reportDescription.equals(that.reportDescription) : that.reportDescription != null) {
			return false;
		}
		if (reportName != null ? !reportName.equals(that.reportName) : that.reportName != null) {
			return false;
		}
		return reportUri != null ? reportUri.equals(that.reportUri) : that.reportUri == null;
	}

	@Override
	public int hashCode() {
		int result = reportUri != null ? reportUri.hashCode() : 0;
		result = 31 * result + (reportName != null ? reportName.hashCode() : 0);
		result = 31 * result + (reportDescription != null ? reportDescription.hashCode() : 0);
		result = 31 * result + (filenamePattern != null ? filenamePattern.hashCode() : 0);
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		result = 31 * result + (fileFormats != null ? fileFormats.hashCode() : 0);
		result = 31 * result + (areaLevel != null ? areaLevel.hashCode() : 0);
		result = 31 * result + (areaPathMask != null ? areaPathMask.hashCode() : 0);
		return result;
	}

	@Override
	public ReportTemplate clone() throws CloneNotSupportedException {
		return new ReportTemplate(reportUri, reportName, reportDescription, filenamePattern, new ArrayList<>(Collections2.transform(parameters,
				new Function<ReportParameter, ReportParameter>() {
					@Override
					public ReportParameter apply(ReportParameter input) {
						try {
							return input.clone();
						} catch (CloneNotSupportedException e) {
							throw new RuntimeException(e);
						}
					}
				})), copyOf(fileFormats), areaLevel, areaPathMask, hidden, async, maybePreGenerated, unselectableParameterValues);
	}

	@Override
	public int compareTo(ReportTemplate other) {
		return this.getReportUri().compareTo(other.getReportUri());
	}

}
