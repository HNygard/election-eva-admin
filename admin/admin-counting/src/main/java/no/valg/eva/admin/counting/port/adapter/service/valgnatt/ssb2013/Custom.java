package no.valg.eva.admin.counting.port.adapter.service.valgnatt.ssb2013;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Custom element. Child of Admin with attribute sendid set to 'Skjemanavn_Kommunenr_ååååmmdd_ttmmss'.
 * 
 */
@XmlType
public class Custom {

	@XmlAttribute
	private String name;

	@XmlValue
	private String sendid;

	public Custom() {
	}

	public Custom(final String name, final String sendid) {
		this.name = name;
		this.sendid = sendid;
	}
}
