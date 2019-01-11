package no.valg.eva.admin.valgnatt.domain.model.resultat.stemmetall;

import static no.evote.constants.EvoteConstants.BALLOT_BLANK;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import no.evote.constants.EvoteConstants;

/**
 * Stemmetall for foreløpig eller endelig telling av forhåndsstemmer og valgtingsstemmer samt listestemmer. Kan representeres som JSON. Forhånd,
 * valgtingsstemmer og listestemmer kommer bare med i JSON representasjonen dersom det foreligger tall.
 */
public class Stemmetall {

	private final String partiId;
	private final int fhsForeløpig;
	private final int vtsForeløpig;
	private final int fhsEndelig;
	private final int vtsEndelig;
	private final boolean hasFhsForeløpig;
	private final boolean hasVtsForeløpig;
	private final boolean hasFhsEndelig;
	private final boolean hasVtsEndelig;
	private final Integer lis;

	public Stemmetall(String id, int fhsForeløpig, int vtsForeløpig, boolean hasFhsForeløpig, boolean hasVtsForeløpig, int fhsEndelig, int vtsEndelig,
			boolean hasFhsEndelig, boolean hasVtsEndelig, Integer lis) {
		if (BALLOT_BLANK.equals(id)) {
			this.partiId = EvoteConstants.VALGNATT_PARTY_ID_BLANKE;
		} else {
			this.partiId = id;
		}
		this.fhsForeløpig = fhsForeløpig;
		this.vtsForeløpig = vtsForeløpig;
		this.hasFhsForeløpig = hasFhsForeløpig;
		this.hasVtsForeløpig = hasVtsForeløpig;
		this.fhsEndelig = fhsEndelig;
		this.vtsEndelig = vtsEndelig;
		this.hasFhsEndelig = hasFhsEndelig;
		this.hasVtsEndelig = hasVtsEndelig;
		this.lis = lis;
	}

	public JsonObject asJsonObject() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("partikode", partiId);
		if (hasFhsForeløpig && !hasFhsEndelig) {
			builder.add("fhs-foreløpig", fhsForeløpig);
		}
		if (hasVtsForeløpig && !hasVtsEndelig) {
			builder.add("vts-foreløpig", vtsForeløpig);
		}
		if (hasFhsEndelig) {
			builder.add("fhs-endelig", fhsEndelig);
		}
		if (hasVtsEndelig) {
			builder.add("vts-endelig", vtsEndelig);
		}
		if (lis != null) {
			builder.add("lis", lis);
		}
		return builder.build();
	}

	public String getPartiId() {
		return partiId;
	}

	public int getFhsForeløpig() {
		return fhsForeløpig;
	}

	public int getVtsForeløpig() {
		return vtsForeløpig;
	}

	public Integer getLis() {
		return lis;
	}

	public int getFhsEndelig() {
		return fhsEndelig;
	}

	public int getVtsEndelig() {
		return vtsEndelig;
	}

}
