package no.valg.eva.admin.common;

import java.io.Serializable;

/**
 * General service request for finding data by primary key
 */
public class FindByIdRequest implements Serializable {

	private final String id;

	public FindByIdRequest(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
