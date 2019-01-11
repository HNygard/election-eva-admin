package no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Stemmeseddelstatistikk {

	private int godkjenteForhåndsstemmesedler;
	private int godkjenteValgtingsstemmesedler;
	private int forkastedeForhåndsstemmesedler;
	private int forkastedeValgtingsstemmesedler;

	public Stemmeseddelstatistikk(int godkjenteForhåndsstemmesedler, int godkjenteValgtingsstemmesedler, int forkastedeForhåndsstemmesedler,
			int forkastedeValgtingsstemmesedler) {
		this.godkjenteForhåndsstemmesedler = godkjenteForhåndsstemmesedler;
		this.godkjenteValgtingsstemmesedler = godkjenteValgtingsstemmesedler;
		this.forkastedeForhåndsstemmesedler = forkastedeForhåndsstemmesedler;
		this.forkastedeValgtingsstemmesedler = forkastedeValgtingsstemmesedler;
	}

	public JsonObject asJsonObject() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("sts-forhånd-godkjente", godkjenteForhåndsstemmesedler);
		builder.add("sts-valgting-godkjente", godkjenteValgtingsstemmesedler);
		builder.add("sts-forhånd-forkastede", forkastedeForhåndsstemmesedler);
		builder.add("sts-valgting-forkastede", forkastedeValgtingsstemmesedler);
		return builder.build();
	}

	public int getGodkjenteForhåndsstemmesedler() {
		return godkjenteForhåndsstemmesedler;
	}

	public int getGodkjenteValgtingsstemmesedler() {
		return godkjenteValgtingsstemmesedler;
	}

	public int getForkastedeForhåndsstemmesedler() {
		return forkastedeForhåndsstemmesedler;
	}

	public int getForkastedeValgtingsstemmesedler() {
		return forkastedeValgtingsstemmesedler;
	}
}
