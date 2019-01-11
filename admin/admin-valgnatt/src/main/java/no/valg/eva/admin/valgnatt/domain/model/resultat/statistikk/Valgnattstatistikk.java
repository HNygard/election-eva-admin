package no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import no.valg.eva.admin.voting.domain.model.Stemmegivningsstatistikk;

public class Valgnattstatistikk {

	private final Stemmegivningsstatistikk stemmegivningsstatistikk;
	private final Stemmeseddelstatistikk stemmeseddelstatistikk;

	public Valgnattstatistikk(Stemmegivningsstatistikk stemmegivningsstatistikk, Stemmeseddelstatistikk stemmeseddelstatistikk) {
		this.stemmegivningsstatistikk = stemmegivningsstatistikk;
		this.stemmeseddelstatistikk = stemmeseddelstatistikk;
	}

	public Stemmeseddelstatistikk getStemmeseddelstatistikk() {
		return stemmeseddelstatistikk;
	}

	public JsonObject asJsonObject() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("stg-forhånd-godkjente", stemmegivningsstatistikk.getGodkjenteFhsg());
		builder.add("stg-valgting-godkjente", stemmegivningsstatistikk.getGodkjenteVtsg());
		builder.add("stg-forhånd-forkastede", stemmegivningsstatistikk.getForkastedeFhsg());
		builder.add("stg-valgting-forkastede", stemmegivningsstatistikk.getForkastedeVtsg());
		builder.add("sts-forhånd-godkjente", stemmeseddelstatistikk.getGodkjenteForhåndsstemmesedler());
		builder.add("sts-valgting-godkjente", stemmeseddelstatistikk.getGodkjenteValgtingsstemmesedler());
		builder.add("sts-forhånd-forkastede", stemmeseddelstatistikk.getForkastedeForhåndsstemmesedler());
		builder.add("sts-valgting-forkastede", stemmeseddelstatistikk.getForkastedeValgtingsstemmesedler());
		
		return builder.build();
	}

}
