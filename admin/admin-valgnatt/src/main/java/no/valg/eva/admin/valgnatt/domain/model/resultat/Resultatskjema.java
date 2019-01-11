package no.valg.eva.admin.valgnatt.domain.model.resultat;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.auditevents.valgnatt.ValgnattAuditable;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk.Valgnattstatistikk;
import no.valg.eva.admin.valgnatt.domain.model.resultat.stemmetall.Stemmetall;

public class Resultatskjema implements ValgnattAuditable {

	private final ResultatType resultatType;
	private final Valgtype valgtype;
	private final String electionYear;
	protected final MvArea mvArea;
	private final List<Stemmetall> stemmetallList;
	private final Valgnattstatistikk valgnattstatistikk;
	private final ValgnattskjemaJsonBuilderFactory valgnattskjemaJsonBuilderFactory = new ValgnattskjemaJsonBuilderFactory();

	public Resultatskjema(ResultatType resultatType, MvArea mvArea, MvElection valgdistrikt, List<Stemmetall> stemmetallList, Valgnattstatistikk valgnattstatistikk) {
		this.resultatType = resultatType;
		this.mvArea = mvArea;
		this.valgtype = valgdistrikt.getElection().getValgtype();
		this.electionYear = valgdistrikt.electionYear();
		this.stemmetallList = stemmetallList;
		this.valgnattstatistikk = valgnattstatistikk;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = jsonBuilder();
		return builder.toJson();
	}

	protected JsonBuilder jsonBuilder() {
		JsonBuilder builder = valgnattskjemaJsonBuilderFactory.createJsonBuilder(resultatType.toString(), mvArea.getElectionEventId(), mvArea.getElectionEventName(),
				valgtype, electionYear);

		builder.add("kommune", mvArea.getMunicipalityId() + " (" + mvArea.getMunicipalityName() + ")");
		builder.add("stemmekrets", mvArea.getPollingDistrictId() + " (" + mvArea.getPollingDistrictName() + ")");

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Stemmetall stemmetall : stemmetallList) {
			arrayBuilder.add(stemmetall.asJsonObject());
		}

		builder.add("stemmer", arrayBuilder.build());
		builder.add("statistikk", valgnattstatistikk.asJsonObject());
		return builder;
	}
}
