package no.valg.eva.admin.counting.port.adapter.service.valgnatt.ssb2013;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * First element in XML to SSB.
 */
@XmlType
public class Admin {

	@XmlElement(name = "Custom")
	private Custom custom;

	public Admin() {
	}

	public Admin(final Custom custom) {
		this.custom = custom;
	}
}
