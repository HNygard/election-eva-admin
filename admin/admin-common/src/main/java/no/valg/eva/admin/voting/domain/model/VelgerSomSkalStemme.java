package no.valg.eva.admin.voting.domain.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.VotingCategory;

public class VelgerSomSkalStemme implements Serializable {
	private boolean kanRegistrereStemmegivning;
	private List<VotingCategory> stemmetypeListe;
	private Set<VelgerMelding> velgerMeldinger = new LinkedHashSet<>();

	public VelgerSomSkalStemme(List<VotingCategory> stemmetypeListe) {
		this.stemmetypeListe = stemmetypeListe;
		this.kanRegistrereStemmegivning = false;
	}

	public boolean isKanRegistrereStemmegivning() {
		return kanRegistrereStemmegivning;
	}

	public void setKanRegistrereStemmegivning(boolean kanRegistrereStemmegivning) {
		this.kanRegistrereStemmegivning = kanRegistrereStemmegivning;
	}

	public List<VotingCategory> getStemmetypeListe() {
		return stemmetypeListe;
	}

	public Set<VelgerMelding> getVelgerMeldinger() {
		return velgerMeldinger;
	}
}
