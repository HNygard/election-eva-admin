package no.valg.eva.admin.frontend.kontekstvelger.valghierarki;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerRad;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ValghierarkiRad<S extends ValghierarkiSti> extends KontekstvelgerRad {
	private S sti;

	public ValghierarkiRad() {
		// CDI
	}

	public ValghierarkiRad(Valghierarki<S> valghierarki) {
		super(valghierarki.id(), false, valghierarki.navn());
		this.sti = valghierarki.sti();
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
		ValghierarkiRad<?> that = (ValghierarkiRad<?>) o;
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
