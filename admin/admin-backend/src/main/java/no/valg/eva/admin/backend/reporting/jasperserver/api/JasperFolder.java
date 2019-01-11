package no.valg.eva.admin.backend.reporting.jasperserver.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "folder")
public class JasperFolder {
	private String label;
	private String uri;

	public JasperFolder() {
	}

	public JasperFolder(final String uri, final String label) {
		this.uri = uri;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}
}
