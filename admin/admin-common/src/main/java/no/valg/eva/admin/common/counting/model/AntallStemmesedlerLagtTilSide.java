package no.valg.eva.admin.common.counting.model;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class AntallStemmesedlerLagtTilSide implements Serializable {
	private AreaPath municipalityPath;
	private List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList;
	private boolean lagringAvAntallStemmesedlerLagtTilSideMulig;

	public AntallStemmesedlerLagtTilSide(AreaPath municipalityPath, List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList,
										 boolean lagringAvAntallStemmesedlerLagtTilSideMulig) {
		this.municipalityPath = municipalityPath;
		this.antallStemmesedlerLagtTilSideForValgList = antallStemmesedlerLagtTilSideForValgList;
		this.lagringAvAntallStemmesedlerLagtTilSideMulig = lagringAvAntallStemmesedlerLagtTilSideMulig;
	}

	public AntallStemmesedlerLagtTilSide(AreaPath municipalityPath, AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg,
										 boolean lagringAvAntallStemmesedlerLagtTilSideMulig) {
		this.municipalityPath = municipalityPath;
		this.antallStemmesedlerLagtTilSideForValgList = singletonList(antallStemmesedlerLagtTilSideForValg);
		this.lagringAvAntallStemmesedlerLagtTilSideMulig = lagringAvAntallStemmesedlerLagtTilSideMulig;
	}

	public AreaPath getMunicipalityPath() {
		return municipalityPath;
	}

	public List<AntallStemmesedlerLagtTilSideForValg> getAntallStemmesedlerLagtTilSideForValgList() {
		return antallStemmesedlerLagtTilSideForValgList;
	}
	
	public int getTotaltAntallStemmesedlerLagtTilSideForValg() {
		return antallStemmesedlerLagtTilSideForValgList
				.stream()
				.mapToInt(AntallStemmesedlerLagtTilSideForValg::getAntallStemmesedler)
				.sum();
	}

	public int getAntallStemmesedlerLagtTilSideForValgdistrikt(Contest contest) {
		AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg =
				antallStemmesedlerLagtTilSideForValgList.stream()
						.filter(a -> a.getElectionPath().equals(contest.electionPath()))
						.findFirst()
						.orElse(new AntallStemmesedlerLagtTilSideForValg(contest.electionPath(), contest.getName(), 0));
		return antallStemmesedlerLagtTilSideForValg.getAntallStemmesedler();
	}

	public boolean isLagringAvAntallStemmesedlerLagtTilSideMulig() {
		return lagringAvAntallStemmesedlerLagtTilSideMulig;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AntallStemmesedlerLagtTilSide that = (AntallStemmesedlerLagtTilSide) o;
		return new EqualsBuilder()
				.append(lagringAvAntallStemmesedlerLagtTilSideMulig, that.lagringAvAntallStemmesedlerLagtTilSideMulig)
				.append(municipalityPath, that.municipalityPath)
				.append(antallStemmesedlerLagtTilSideForValgList, that.antallStemmesedlerLagtTilSideForValgList)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(municipalityPath)
				.append(antallStemmesedlerLagtTilSideForValgList)
				.append(lagringAvAntallStemmesedlerLagtTilSideMulig)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("municipalityPath", municipalityPath)
				.append("antallStemmesedlerLagtTilSideForValgList", antallStemmesedlerLagtTilSideForValgList)
				.append("lagringAvAntallStemmesedlerLagtTilSideMulig", lagringAvAntallStemmesedlerLagtTilSideMulig)
				.toString();
	}
}
