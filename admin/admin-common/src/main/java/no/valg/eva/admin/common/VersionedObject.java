package no.valg.eva.admin.common;

import java.io.Serializable;

/**
 * Abstract class for DTO objects for holding VersionedEntity info.
 * 
 * @see no.evote.model.VersionedEntity
 * */
public abstract class VersionedObject implements Serializable {

	private final int version;

	protected VersionedObject(int version) {
		this.version = version;
	}

	public int getVersion() {
		return version;
	}
}
