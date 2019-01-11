package no.valg.eva.admin.felles.valggeografi.model;

import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Kommune extends Valggeografi<KommuneSti> {
	private final boolean elektroniskManntall;

	public Kommune(KommuneSti sti, String navn, boolean elektroniskManntall) {
		super(sti, navn);
		this.elektroniskManntall = elektroniskManntall; 
	}

	@Override
	public ValggeografiNivaa nivaa() {
		return ValggeografiNivaa.KOMMUNE;
	}

	public boolean harIkkeElektroniskManntall() {
		return !elektroniskManntall;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Kommune kommune = (Kommune) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(elektroniskManntall, kommune.elektroniskManntall)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(elektroniskManntall)
				.toHashCode();
	}
}
