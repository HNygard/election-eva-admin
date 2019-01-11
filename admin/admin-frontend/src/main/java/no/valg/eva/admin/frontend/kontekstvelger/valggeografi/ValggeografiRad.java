package no.valg.eva.admin.frontend.kontekstvelger.valggeografi;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerRad;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ValggeografiRad<S extends ValggeografiSti> extends KontekstvelgerRad {
	private S sti;

	public ValggeografiRad() {
		// CDI
	}

	public ValggeografiRad(Valggeografi<S> valggeografi) {
		super(valggeografi.id(), true, valggeografi.navn());
		this.sti = valggeografi.sti();
	}

	public S getSti() {
		return sti;
	}

	public void setSti(S sti) {
		this.sti = sti;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("sti", sti)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ValggeografiRad<?> that = (ValggeografiRad<?>) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(sti, that.sti)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(sti)
				.toHashCode();
	}
}
