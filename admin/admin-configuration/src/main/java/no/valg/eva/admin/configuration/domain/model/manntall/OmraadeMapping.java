package no.valg.eva.admin.configuration.domain.model.manntall;

import javax.validation.constraints.NotNull;

import no.valg.eva.admin.common.AreaPath;

public class OmraadeMapping {
	private final String fraOmraade;
	private final String tilOmraade;

	public OmraadeMapping(@NotNull String fraOmraade, @NotNull String tilOmraade) {
		this.fraOmraade = fraOmraade;
		this.tilOmraade = tilOmraade;
	}

	public String getFraOmraade() {
		return fraOmraade;
	}

	public AreaPath getFraOmraadesti() {
		return new AreaPath(fraOmraade);
	}

	public String getTilOmraade() {
		return tilOmraade;
	}

	public AreaPath getTilOmraadesti() {
		return new AreaPath(tilOmraade);
	}

	@Override
	public int hashCode() {
		int result = fraOmraade.hashCode();
		result = 31 * result + tilOmraade.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}

		OmraadeMapping that = (OmraadeMapping) o;

		return fraOmraade.equals(that.fraOmraade) && tilOmraade.equals(that.tilOmraade);

	}
}
