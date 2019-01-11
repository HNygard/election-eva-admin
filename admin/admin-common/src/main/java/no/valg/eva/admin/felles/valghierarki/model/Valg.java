package no.valg.eva.admin.felles.valghierarki.model;

import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Valg extends Valghierarki<ValgSti> {
	private final ValggeografiNivaa valggeografiNivaa;
	private final boolean enkeltOmrade;
	private final String valggruppeNavn;

	public Valg(ValgSti sti, String navn, ValggeografiNivaa valggeografiNivaa, boolean enkeltOmrade, String valggruppeNavn) {
		super(sti, navn);
		this.valggeografiNivaa = valggeografiNivaa;
		this.enkeltOmrade = enkeltOmrade;
		this.valggruppeNavn = valggruppeNavn;
	}

	@Override
	public ValghierarkiNivaa nivaa() {
		return ValghierarkiNivaa.VALG;
	}

	@Override
	public ValggeografiNivaa valggeografiNivaa() {
		return valggeografiNivaa;
	}

	public boolean isEnkeltOmrade() {
		return enkeltOmrade;
	}

	public String valggruppeNavn() {
		return valggruppeNavn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Valg valg = (Valg) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(enkeltOmrade, valg.enkeltOmrade)
				.append(valggeografiNivaa, valg.valggeografiNivaa)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(valggeografiNivaa)
				.append(enkeltOmrade)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("valggeografiNivaa", valggeografiNivaa)
				.append("enkeltOmrade", enkeltOmrade)
				.toString();
	}
}
