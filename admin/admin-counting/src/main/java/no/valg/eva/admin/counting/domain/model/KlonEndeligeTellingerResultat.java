package no.valg.eva.admin.counting.domain.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;

public class KlonEndeligeTellingerResultat implements Serializable {
	private final Map<Fylkeskommune, Map<Kommune, CountingMode>> fylkeskommunerKommunerOgCountingModes;
	private final List<KommuneSti> kommunerSkippet;

	public KlonEndeligeTellingerResultat(Map<Fylkeskommune, Map<Kommune, CountingMode>> fylkeskommunerKommunerOgCountingModes,
										 List<KommuneSti> kommunerSkippet) {
		this.fylkeskommunerKommunerOgCountingModes = fylkeskommunerKommunerOgCountingModes;
		this.kommunerSkippet = kommunerSkippet;
	}

	public String toHtml() {
		StringBuilder resultat = new StringBuilder();
		resultat.append("<ul>");
		for (Fylkeskommune fylkeskommune : fylkeskommunerKommunerOgCountingModes.keySet()) {
			resultat.append("<li>");
			resultat.append(fylkeskommune.id());
			resultat.append(" - ");
			resultat.append(fylkeskommune.navn());
			toHtml(resultat, fylkeskommunerKommunerOgCountingModes.get(fylkeskommune));
			resultat.append("</li>");
		}
		resultat.append("</ul>");
		return resultat.toString();
	}

	private void toHtml(StringBuilder resultat, Map<Kommune, CountingMode> kommunerCountingModes) {
		resultat.append("<ul>");
		for (Kommune kommune : kommunerCountingModes.keySet()) {
			resultat.append("<li>");
			resultat.append(kommune.id());
			resultat.append(" - ");
			resultat.append(kommune.navn());
			resultat.append(" - ");
			resultat.append(opptellingsmaate(kommunerCountingModes, kommune));
			if (kommunerSkippet.contains(kommune.sti())) {
				resultat.append(" (SKIPPET)");
			}
			resultat.append("</li>");
		}
		resultat.append("</ul>");
	}

	private String opptellingsmaate(Map<Kommune, CountingMode> kommunerCountingModes, Kommune kommune) {
		CountingMode countingMode = kommunerCountingModes.get(kommune);
		switch (countingMode) {
			case BY_POLLING_DISTRICT:
				return "Lokalt fordelt på krets";
			case CENTRAL:
				return "Sentralt samlet";
			case CENTRAL_AND_BY_POLLING_DISTRICT:
				return "Sentralt fordelt på krets";
			default:
				throw new IllegalStateException("uventet opptellingsmaate: " + countingMode);
		}
	}
}
