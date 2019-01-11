package no.valg.eva.admin.backend.reporting.jasperserver.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FileReference {
	private String uri;

	public FileReference() {
	}

	public FileReference(final String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FileReference that = (FileReference) o;
		if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return uri != null ? uri.hashCode() : 0;
	}
}
