package no.valg.eva.admin.backend.reporting.jasperserver.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InputControl {
	private String description;
	private String uri;
	private String label;
	private DataTypeReference dataTypeReference;

	public InputControl() {
	}

	public InputControl(final String label, final String description, final String uri, final String dataTypeReferenceUri) {
		this.description = description;
		this.uri = uri;
		this.label = label;
		this.dataTypeReference = new DataTypeReference(dataTypeReferenceUri);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(final String uri) {
		this.uri = uri;
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

	public DataTypeReference getDataTypeReference() {
		return dataTypeReference;
	}

	public void setDataTypeReference(final DataTypeReference dataTypeReference) {
		this.dataTypeReference = dataTypeReference;
	}

	public static class DataTypeReference {
		public DataTypeReference() {
		}

		private String uri;

		public DataTypeReference(final String dataTypeReferenceUri) {
			this.uri = dataTypeReferenceUri;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(final String uri) {
			this.uri = uri;
		}
	}
}
