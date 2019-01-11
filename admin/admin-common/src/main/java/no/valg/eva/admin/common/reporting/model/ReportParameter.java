package no.valg.eva.admin.common.reporting.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ReportParameter implements Serializable, Cloneable {
	private boolean inferred;
	private Object defaultValue;
	private String id;
	private String description;
	private String label;
	private String type;
	private Set<ReportParameter> dependentParameters;
	private ReportParameter parent;
	private Object parentValue;
	private boolean mandatory;
	private boolean fixed;

	public ReportParameter(String id, String label, String description, String type) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.type = type;
	}

	private ReportParameter(String id, String label, String description, boolean inferred, String type, Object defaultValue,
			Set<ReportParameter> dependentParameters,
			Object parentValue, boolean mandatory) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.inferred = inferred;
		this.type = type;
		this.defaultValue = defaultValue;
		this.dependentParameters = dependentParameters;
		this.parentValue = parentValue;
		this.mandatory = mandatory;
	}

	public ReportParameter(String id, String type, String defaultValue) {
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isInferred() {
		return inferred;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setInferred(boolean inferred) {
		this.inferred = inferred;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Gets (if any) the parameter that is dependent of the value of this one. E.g. Country->County
	 */
	public Set<ReportParameter> getDependentParameters() {
		return dependentParameters != null ? new HashSet<>(dependentParameters) : null;
	}

	@Override
	protected ReportParameter clone() throws CloneNotSupportedException {
		return new ReportParameter(id, label, description, inferred, type, defaultValue, getDependentParameters(), parentValue, mandatory);
	}

	public void addDependentParameter(ReportParameter next) {
		if (dependentParameters == null) {
			dependentParameters = new HashSet<>(0);
		}
		dependentParameters.add(next);
	}

	public void setParentValue(Object parentValue) {
		this.parentValue = parentValue;
	}

	public Object getParentValue() {
		return parentValue;
	}

	public ReportParameter getParent() {
		return parent;
	}

	public void setParent(ReportParameter parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getId()).append(" ");
		if (description != null) {
			stringBuilder.append(description);
		}
		if (parentValue != null) {
			stringBuilder.append(" (parent value: ").append(getParentValue()).append(")");
		}
		return stringBuilder.toString();
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Set<ReportParameter> getDescendingParameters() {
		if (CollectionUtils.isNotEmpty(getDependentParameters())) {
			HashSet<ReportParameter> reportParameters = new HashSet<>(getDependentParameters());
			for (ReportParameter reportParameter : reportParameters) {
				reportParameters.addAll(reportParameter.getDescendingParameters());
			}
			return reportParameters;
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReportParameter that = (ReportParameter) o;

		return new EqualsBuilder()
				.append(id, that.id)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(id)
				.toHashCode();
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public boolean isFixed() {
		return fixed;
	}

	public boolean isNumber() {
		return "number".equals(type);
	}
}
