package no.valg.eva.admin.voting.domain.model;

import java.io.Serializable;

import no.valg.eva.admin.felles.melding.Alvorlighetsgrad;

public class VelgerMelding implements Serializable {
	private Alvorlighetsgrad alvorlighetsgrad = Alvorlighetsgrad.INFO;
	private VelgerMeldingType velgerMeldingType;
	private Object data;
	private VelgerMelding tilleggsMelding;

	public VelgerMelding(VelgerMeldingType velgerMeldingType) {
		this.velgerMeldingType = velgerMeldingType;
	}

	public VelgerMelding(VelgerMeldingType velgerMeldingType, Alvorlighetsgrad alvorlighetsgrad) {
		this(velgerMeldingType, alvorlighetsgrad, null);
	}

	public VelgerMelding(VelgerMeldingType velgerMeldingType, Alvorlighetsgrad alvorlighetsgrad, Object data) {
		this.velgerMeldingType = velgerMeldingType;
		this.alvorlighetsgrad = alvorlighetsgrad;
		this.data = data;
	}

	public VelgerMeldingType getVelgerMeldingType() {
		return velgerMeldingType;
	}

	public Alvorlighetsgrad getAlvorlighetsgrad() {
		return alvorlighetsgrad;
	}

	public Object getData() {
		return data;
	}

	public VelgerMelding getTilleggsMelding() {
		return tilleggsMelding;
	}

	public void setTilleggsMelding(VelgerMelding tilleggsMelding) {
		this.tilleggsMelding = tilleggsMelding;
	}
}
