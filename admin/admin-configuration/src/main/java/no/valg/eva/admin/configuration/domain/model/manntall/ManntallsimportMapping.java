package no.valg.eva.admin.configuration.domain.model.manntall;

import java.util.ArrayList;
import java.util.List;

public class ManntallsimportMapping {
	private List<OmraadeMapping> mappedeOmraader = new ArrayList<>();

	public List<OmraadeMapping> getMappedeOmraader() {
		return mappedeOmraader;
	}

	public void setMappedeOmraader(List<OmraadeMapping> mappedeOmraader) {
		this.mappedeOmraader = mappedeOmraader;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ManntallsimportMapping that = (ManntallsimportMapping) o;

		return mappedeOmraader != null ? mappedeOmraader.equals(that.mappedeOmraader) : that.mappedeOmraader == null;
	}

	@Override
	public int hashCode() {
		return mappedeOmraader != null ? mappedeOmraader.hashCode() : 0;
	}
}
