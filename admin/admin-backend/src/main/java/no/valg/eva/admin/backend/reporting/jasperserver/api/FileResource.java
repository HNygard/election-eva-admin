package no.valg.eva.admin.backend.reporting.jasperserver.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "resource")
public class FileResource {
	private String name;
	private FileReference fileReference;

	public FileResource() {
	}

	public FileResource(final String name, final FileReference fileReference) {
		this.name = name;
		this.fileReference = fileReference;
	}

	public FileResource(final FileReference file) {
		this.fileReference = file;
	}

	public FileReference getFileReference() {
		return fileReference;
	}

	public void setFileReference(final FileReference fileReference) {
		this.fileReference = fileReference;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FileResource that = (FileResource) o;
		if (fileReference != null ? !fileReference.equals(that.fileReference) : that.fileReference != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return fileReference != null ? fileReference.hashCode() : 0;
	}
}
