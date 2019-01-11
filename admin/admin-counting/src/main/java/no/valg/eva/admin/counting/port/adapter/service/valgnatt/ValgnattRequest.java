package no.valg.eva.admin.counting.port.adapter.service.valgnatt;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import no.valg.eva.admin.counting.port.adapter.service.valgnatt.ssb2013.Admin;
import no.valg.eva.admin.counting.port.adapter.service.valgnatt.ssb2013.Set;

@XmlRootElement(name = "XML4DR")
public class ValgnattRequest {

	private String formId;
	private String electionName;
	private Admin admin;
	private final List<Set> setList = new ArrayList<>();

	public ValgnattRequest() {
	}

	public ValgnattRequest(String formId, String electionName) {
		this.formId = formId;
		this.electionName = electionName;
	}

	public void setFormId(final String formId) {
		this.formId = formId;
	}

	public void setAdmin(final Admin admin) {
		this.admin = admin;
	}

	public void addSet(final Set set) {
		setList.add(set);
	}

	@XmlAttribute
	public String getFormId() {
		return formId;
	}

	@XmlAttribute
	public String getElectionName() {
		return electionName;
	}

	@XmlElement(name = "Admin")
	public Admin getAdmin() {
		return admin;
	}

	@XmlElement(name = "Set")
	public List<Set> getSetList() {
		return setList;
	}
}
