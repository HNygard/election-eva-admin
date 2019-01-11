package no.evote.service.counting;

import no.valg.eva.admin.counting.domain.model.VoteCount;

/**
 * Databærer for tellinger opprettet i importen.  Hvis det er lokalt fordel på krets, vil også en urnetelling bli opprettet automatisk.
 */
class Tellinger {
	
	private final VoteCount urnetelling;
	private final VoteCount forelopigEllerEndeligTelling;

	Tellinger(VoteCount urnetelling, VoteCount forelopigEllerEndeligTelling) {
		this.urnetelling = urnetelling;
		this.forelopigEllerEndeligTelling = forelopigEllerEndeligTelling;
	}

	VoteCount getUrnetelling() {
		return urnetelling;
	}

	VoteCount getForelopigEllerEndeligTelling() {
		return forelopigEllerEndeligTelling;
	}
}
