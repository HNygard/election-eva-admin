package no.valg.eva.admin.common.counting.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;

import no.valg.eva.admin.common.ElectionPath;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AntallStemmesedlerLagtTilSideForValg implements Serializable {
	private ElectionPath electionPath;
	private String navn;
	private int antallStemmesedler;

	public AntallStemmesedlerLagtTilSideForValg(ElectionPath electionPath, String navn, int antallStemmesedler) {
		this.electionPath = electionPath;
		this.navn = navn;
		this.antallStemmesedler = antallStemmesedler;
	}

	public ElectionPath getElectionPath() {
		return electionPath;
	}

	public String getNavn() {
		return navn;
	}

	public int getAntallStemmesedler() {
		return antallStemmesedler;
	}

	public void setAntallStemmesedler(int antallStemmesedler) {
		this.antallStemmesedler = antallStemmesedler;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AntallStemmesedlerLagtTilSideForValg that = (AntallStemmesedlerLagtTilSideForValg) o;
		return new EqualsBuilder()
				.append(antallStemmesedler, that.antallStemmesedler)
				.append(electionPath, that.electionPath)
				.append(navn, that.navn)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(electionPath)
				.append(navn)
				.append(antallStemmesedler)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("electionPath", electionPath)
				.append("navn", navn)
				.append("antallStemmesedler", antallStemmesedler)
				.toString();
	}
}
