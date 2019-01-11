package no.valg.eva.admin.backend.reporting.jasperserver.api;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import no.valg.eva.admin.common.rbac.Accesses;

@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
public class FolderMetaData {
	private String access;

	public String getAccess() {
		return access;
	}
	
	public Accesses getAccesses() {
		return Accesses.valueOf(access);
	}

	public void setAccess(final String access) {
		this.access = access;
	}

	public FolderMetaData withAccess(final String access) {
		setAccess(access);
		return this;
	}
}
