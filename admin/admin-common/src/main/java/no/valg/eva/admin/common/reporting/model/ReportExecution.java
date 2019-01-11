package no.valg.eva.admin.common.reporting.model;

import static java.util.Collections.EMPTY_MAP;

import java.io.Serializable;
import java.util.Map;

public class ReportExecution implements Serializable {
	private byte[] content;
	private String exportId;
	private String reportName;
	private Map<String, String> arguments = EMPTY_MAP;
	private Map<String, String> parameterLabels = EMPTY_MAP;
	private String fileName;
	private String format;
	private String requestId;
	private boolean failed;

	public ReportExecution(String requestId, String exportId, String reportName, String fileName, String format, Map<String, String> arguments,
			Map<String, String> parameterLabels) {
		this.requestId = requestId;
		this.exportId = exportId;
		this.reportName = reportName;
		this.fileName = fileName;
		this.format = format;
		this.arguments = arguments;
		this.parameterLabels = parameterLabels;
	}

	public ReportExecution(String requestId, byte[] content, String reportName, String fileName, String format, Map<String, String> arguments,
			Map<String, String> parameterLabels) {
		this.content = content;
		this.reportName = reportName;
		this.fileName = fileName;
		this.format = format;
		this.requestId = requestId;
		this.arguments = arguments;
		this.parameterLabels = parameterLabels;
	}

	public ReportExecution(byte[] content, String reportName, String fileName, String format) {
		this.content = content;
		this.reportName = reportName;
		this.fileName = fileName;
		this.format = format;
	}

	public ReportExecution(byte[] content, String reportName, String fileName, String format, Map<String, String> parameters,
			Map<String, String> parameterLabels) {
		this.content = content;
		this.reportName = reportName;
		this.fileName = fileName;
		this.format = format;
		this.arguments = parameters;
		this.parameterLabels = parameterLabels;
	}

	public ReportExecution(ReportExecution rx, byte[] content) {
		this(rx.requestId, rx.exportId, rx.reportName, rx.fileName, rx.format, rx.arguments, rx.parameterLabels);
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public String getReportName() {
		return reportName;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}

	public String getFileName() {
		return fileName;
	}

	public Map<String, String> getParameterLabels() {
		return parameterLabels;
	}

	public String getFormat() {
		return format;
	}

	public boolean isReady() {
		return content != null;
	}

	public String getRequestId() {
		return requestId;
	}

	public String getExportId() {
		return exportId;
	}

	public ReportExecution failed() {
		this.failed = true;
		return this;
	}

	public boolean isFailed() {
		return failed;
	}
}
