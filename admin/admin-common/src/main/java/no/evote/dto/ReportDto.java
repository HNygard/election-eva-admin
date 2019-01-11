package no.evote.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

@XmlRootElement(name = "report")
@XmlType
public class ReportDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid;
	private String fileName;
	private LocalDate executionDate;
	private LocalTime executionTime;
	private String state;
	private String format;
	private String displayName;

	private String templateName;
    private String uri;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public LocalDate getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(LocalDate executionDate) {
		this.executionDate = executionDate;
	}

	public String getState() {
		return state;
	}

	public void setState(final String state) {
		this.state = state;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(final String format) {
		this.format = format;
	}

	public LocalTime getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(LocalTime executionTime) {
		this.executionTime = executionTime;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(final String templateName) {
		this.templateName = templateName;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
