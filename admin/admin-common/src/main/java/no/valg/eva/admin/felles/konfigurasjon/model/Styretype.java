package no.valg.eva.admin.felles.konfigurasjon.model;

import static java.lang.String.format;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;

import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;

public enum Styretype {
	VALGHENDELSESSTYRET(0, VALGHENDELSE, ValggeografiNivaa.VALGHENDELSE),
	OPPTELLINGSVALGSTYRET(1, VALGDISTRIKT, null),
	RIKSVALGSTYRET(2, VALGGRUPPE, LAND),
	FYLKESVALGSTYRET(3, VALG, FYLKESKOMMUNE),
	VALGSTYRET(4, VALGGRUPPE, KOMMUNE),
	BYDELSVALGSTYRET(5, VALG, BYDEL),
	STEMMESTYRET(6, VALGHENDELSE, STEMMEKRETS);

	private final int id;
	private final String navn;
	private final ValghierarkiNivaa valghierarkiNivaa;
	private final ValggeografiNivaa valggeografiNivaa;

	Styretype(int id, ValghierarkiNivaa valghierarkiNivaa, ValggeografiNivaa valggeografiNivaa) {
		this.id = id;
		this.navn = format("@reporting_unit_type[%d].name", id);
		this.valghierarkiNivaa = valghierarkiNivaa;
		this.valggeografiNivaa = valggeografiNivaa;
	}

	public int id() {
		return id;
	}

	public String navn() {
		return navn;
	}

	public ValghierarkiNivaa valghierarkiNivaa() {
		return valghierarkiNivaa;
	}

	public ValggeografiNivaa valggeografiNivaa() {
		return valggeografiNivaa;
	}
}
