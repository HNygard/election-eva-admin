package no.valg.eva.admin.common.configuration.model.election;

import java.io.Serializable;

public class LocaleId implements Serializable {
	private final String id;

	public LocaleId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return "@locale[" + id + "].name";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		LocaleId localeId = (LocaleId) o;

		return !(id != null ? !id.equals(localeId.id) : localeId.id != null);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "LocaleId{id=" + id + "}";
	}
}
