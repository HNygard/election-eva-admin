package no.valg.eva.admin.backend.reporting.jasperserver.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * java version of Jasper report template
 */
@XmlRootElement(name = "reportUnit")
public class JasperReport {
	private String label;
	private String uri;
	private String description;
	private List<FileResource> resources = new ArrayList();
	private List<InputControl> inputControls = new ArrayList();

	public JasperReport() {
	}

	public JasperReport(final String label, final String uri) {
		this.label = label;
		this.uri = uri;
	}

	public JasperReport(final String label, final String uri, final String description) {
		this(label, uri);
		this.description = description;
	}

	public JasperReport(final String label, final String uri, final String description, final List<FileResource> fileResources) {
		this(label, uri, description);
		resources = fileResources;
	}

	public JasperReport(final String label, final String uri, final String description, final List<FileResource> fileResources,
			final List<InputControl> inputControls) {
		this(label, uri, description, fileResources);
		this.inputControls = inputControls;
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

	public String getUri() {
		return uri;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}

	@XmlElementWrapper(name = "resources")
	@XmlElement(name = "resource")
	public List<FileResource> getResources() {
		return resources;
	}

	public void setResources(final List<FileResource> resources) {
		this.resources = resources;
	}

	@XmlElementWrapper(name = "inputControls")
	@XmlElement(name = "inputControlReference")
	public List<InputControl> getInputControls() {
		return inputControls;
	}

	public void setInputControls(final List<InputControl> inputControls) {
		this.inputControls = inputControls;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JasperReport that = (JasperReport) o;
		return new EqualsBuilder()
				.append(this.description, that.description)
				.append(this.label, that.label)
				.append(this.uri, that.uri)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(label).append(uri).append(description).hashCode();
	}
}
