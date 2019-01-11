package no.valg.eva.admin.backend.reporting.jasperserver.api;

import static java.lang.String.format;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a representation of <a
 * href="http://community.jaspersoft.com/documentation/jasperreports-server-web-services-guide/v550/running-report-asynchronously">the POST body used to execute
 * a report in JasperServer</a>
 */
@XmlRootElement(name = "reportExecutionRequest")
public class JasperExecutionRequest {
	private String reportUnitUri;
	private Boolean async = false;
	private String outputFormat = "pdf";

	private List<JasperExecutionRequest.ReportParameter> parameters;

	public JasperExecutionRequest() {
	}

	public JasperExecutionRequest(final String reportUnitUri, final List<ReportParameter> parameters) {
		this.reportUnitUri = reportUnitUri;
		// make sure uri starts with a slash
		if (!this.reportUnitUri.startsWith("/")) {
			this.reportUnitUri = "/" + this.reportUnitUri;
		}
		this.parameters = parameters;
	}

	public JasperExecutionRequest asPdf() {
		this.outputFormat = "pdf";
		return this;
	}

	public JasperExecutionRequest as(final String format) {
		this.outputFormat = format;
		return this;
	}

	public String getReportUnitUri() {
		return reportUnitUri;
	}

	public void setReportUnitUri(final String reportUnitUri) {
		this.reportUnitUri = reportUnitUri;
	}

	public Boolean getAsync() {
		return async;
	}

	public void setAsync(final Boolean async) {
		this.async = async;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(final String outputFormat) {
		this.outputFormat = outputFormat;
	}

	@XmlElementWrapper(name = "parameters")
	@XmlElement(name = "reportParameter")
	public List<ReportParameter> getParameters() {
		return parameters;
	}

	public void setParameters(final List<ReportParameter> parameters) {
		this.parameters = parameters;
	}

	public JasperExecutionRequest async(boolean async) {
		setAsync(async);
		return this;
	}

	public static class ReportParameter {
		private String name;
		private List<String> values;

		public ReportParameter() {
		}

		public ReportParameter(final String name, final List<String> values) {
			this.name = name;
			this.values = values;
		}

		@XmlAttribute
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public List<String> getValue() {
			return values;
		}

		public void setValue(final List<String> values) {
			this.values = values;
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

			return !(name != null ? !name.equals(that.name) : that.name != null) && !(values != null ? !values.equals(that.values) : that.values != null);
		}

		@Override
		public int hashCode() {
			int result = name != null ? name.hashCode() : 0;
			result = 31 * result + (values != null ? values.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return format("ReportParameter{name='%s', values=%s}", name, values);
		}
	}
}
