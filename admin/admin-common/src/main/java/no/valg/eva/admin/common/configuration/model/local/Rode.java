package no.valg.eva.admin.common.configuration.model.local;

import java.io.Serializable;

/* DEV-NOTE: Denne klassen skulle gjerne vært immutable, men fordi GUI-verdier bindes til denne klassen, så må det være settere */
public class Rode implements Serializable {

	private String id;
	private String fra;
	private String til;
	private int antallVelgere;

	public Rode(String id, String fra, String til, int antallVelgere) {
		validate(id);
		
		this.id = id.toUpperCase();
		this.fra = fra.toUpperCase();
		this.til = til.toUpperCase();
		this.antallVelgere = antallVelgere;
	}

	private void validate(String id) {
		if (id == null) {
			throw new IllegalArgumentException("ID må ha en verdi");
		}
	}

	public Rode(String id, String fra, String til) {
		this(id, fra, til, 0);
	}

	public Rode(String fra, String til, int antallVelgere) {
		this(fra + " - " + til, fra, til, antallVelgere);
	}

	public Rode(String fra, String til) {
		this(fra, til, 0);
	}

	public String getId() {
		return id;
	}

	public String getFra() {
		return fra;
	}

	public void setFra(final String fra) {
		this.fra = fra;
	}

	public String getTil() {
		return til;
	}

	public void setTil(final String til) {
		this.til = til;
	}

	public int getAntallVelgere() {
		return antallVelgere;
	}

	public void setAntallVelgere(final int antallVelgere) {
		this.antallVelgere = antallVelgere;
	}

	@Override
	public String toString() {
		return fra + " - " + til;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fra == null) ? 0 : fra.hashCode());
		result = prime * result + ((til == null) ? 0 : til.hashCode());
		result = prime * result + antallVelgere;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		Rode other = (Rode) obj;

		if (fra == null) {
			if (other.fra != null) {
				return false;
			}
		} else if (!fra.equals(other.fra)) {
			return false;
		}
		if (til == null) {
			if (other.til != null) {
				return false;
			}
		} else if (!til.equals(other.til)) {
			return false;
		}
		
		return antallVelgere == other.antallVelgere;
	}

	public Rode leggTilVelgere(int antallEkstraVelgere) {
		return new Rode(id, fra, til, antallVelgere + antallEkstraVelgere);
	}
}
