package no.valg.eva.admin.common.configuration.model.party;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.valg.eva.admin.common.Area;
import no.valg.eva.admin.common.AreaPath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
public class Parti implements Serializable {

	@Getter @Setter private String id;
	@Getter @Setter private Partikategori partikategori;
	@Getter @Setter private Integer partikode;
	@Getter @Setter private boolean godkjent;
	@Getter @Setter private boolean forenkletBehandling;
	@Getter @Setter private Long partyPk;
	@Getter @Setter private List<Area> omrader = new ArrayList<>();
	@Getter @Setter private String oversattNavn;

	public Parti(Partikategori partikategori, String id) {
		this.partikategori = partikategori;
		this.id = id;
	}

	public void leggTilOmraadeHvisLokalt(AreaPath areaPath) {
		if (this.getPartikategori() == Partikategori.LOKALT) {
			this.getOmrader().add(new Area(areaPath, ""));
		}
	}
}
