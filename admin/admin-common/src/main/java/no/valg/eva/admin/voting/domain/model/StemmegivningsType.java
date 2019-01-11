package no.valg.eva.admin.voting.domain.model;

public enum StemmegivningsType {
	FORHANDSSTEMME_ORDINAER, FORHANDSSTEMME_KONVOLUTTER_SENTRALT, FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER, VALGTINGSTEMME_ORDINAER, VALGTINGSTEMME_KONVOLUTTER_SENTRALT;

	public boolean isForhand() {
		return isForhandOrinaer() || isForhandSentralt() || isForhandSentInnkomne();
	}

	public boolean isForhandOrinaer() {
		return this == FORHANDSSTEMME_ORDINAER;
	}

	public boolean isForhandSentralt() {
		return this == FORHANDSSTEMME_KONVOLUTTER_SENTRALT;
	}

	public boolean isForhandSentInnkomne() {
		return this == FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER;
	}

	public boolean isForhandIkkeSentInnkomne() {
		return isForhand() && !isForhandSentInnkomne();
	}

	public boolean isValgting() {
		return isValgtingOrdinaere() || isValgtingSentralt();
	}

	public boolean isValgtingOrdinaere() {
		return this == VALGTINGSTEMME_ORDINAER;
	}

	public boolean isValgtingSentralt() {
		return this == VALGTINGSTEMME_KONVOLUTTER_SENTRALT;
	}
}
