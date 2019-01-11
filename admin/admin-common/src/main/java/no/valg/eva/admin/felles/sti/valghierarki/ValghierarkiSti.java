package no.valg.eva.admin.felles.sti.valghierarki;

import static java.lang.String.format;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;

import java.util.regex.Pattern;

import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.felles.sti.Sti;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;

public abstract class ValghierarkiSti<F extends ValghierarkiSti<? extends ValghierarkiSti>> extends Sti<F> {
	protected ValghierarkiSti(Pattern patternForValideringAvId, F forelderSti, String sisteId) {
		super(patternForValideringAvId, forelderSti, sisteId);
	}

	@SuppressWarnings("unchecked")
	public static <S extends ValghierarkiSti> S fra(ElectionPath electionPath) {
		ElectionLevelEnum level = electionPath.getLevel();
		switch (level) {
			case ELECTION_EVENT:
				return (S) valghendelseSti(electionPath);
			case ELECTION_GROUP:
				return (S) valggruppeSti(electionPath);
			case ELECTION:
				return (S) valgSti(electionPath);
			case CONTEST:
				return (S) valgdistriktSti(electionPath);
			default:
				throw new IllegalArgumentException(format("ukjent nivå: %s", level));
		}
	}

	public static ValghendelseSti valghendelseSti(ElectionPath electionPath) {
		return new ValghendelseSti(electionPath.getElectionEventId());
	}

	public static ValggruppeSti valggruppeSti(ElectionPath electionPath) {
		return new ValggruppeSti(valghendelseSti(electionPath), electionPath.getElectionGroupId());
	}

	public static ValgSti valgSti(ElectionPath electionPath) {
		return new ValgSti(valggruppeSti(electionPath), electionPath.getElectionId());
	}

	public static ValgdistriktSti valgdistriktSti(ElectionPath electionPath) {
		return new ValgdistriktSti(valgSti(electionPath), electionPath.getContestId());
	}

	public abstract ValghendelseSti valghendelseSti();

	public ElectionPath electionPath() {
		return ElectionPath.from(toString());
	}

	public ValghierarkiNivaa nivaa() {
		if (this instanceof ValghendelseSti) {
			return VALGHENDELSE;
		}
		if (this instanceof ValggruppeSti) {
			return VALGGRUPPE;
		}
		if (this instanceof ValgSti) {
			return VALG;
		}
		if (this instanceof ValgdistriktSti) {
			return VALGDISTRIKT;
		}
		throw new IllegalStateException(format("ukjent stitype: %s", getClass()));
	}

	public ValgSti tilValgSti() {
		if (nivaa() == VALG) {
			return (ValgSti) this;
		}
		throw new IllegalStateException(format("forventet sti på nivå <%s>, men var på nivå <%s>", VALG, nivaa()));
	}

	public ValgdistriktSti tilValgdistriktSti() {
		if (nivaa() == VALGDISTRIKT) {
			return (ValgdistriktSti) this;
		}
		throw new IllegalStateException(format("forventet sti på nivå <%s>, men var på nivå <%s>", VALGDISTRIKT, nivaa()));
	}
}
