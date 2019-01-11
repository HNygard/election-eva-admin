package no.valg.eva.admin.counting.port.adapter.service.valgnatt.ssb2013;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Data elements wraps a Value element. Data has an attribute dataId that describes the value.
 */
@XmlType
public class Data {

	private String dataId;
	private String value;

	public Data() {
	}

	public Data(final String dataId) {
		this.dataId = dataId;
	}

	public void setDataId(final String dataId) {
		this.dataId = dataId;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@XmlAttribute
	public String getDataId() {
		return dataId;
	}

	@XmlElement(name = "Value")
	public String getValue() {
		return value;
	}
}
