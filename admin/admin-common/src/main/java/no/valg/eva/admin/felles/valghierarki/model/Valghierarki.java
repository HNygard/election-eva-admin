package no.valg.eva.admin.felles.valghierarki.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;

import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class Valghierarki<S extends ValghierarkiSti> implements Serializable {
	private final S sti;
	private final String navn;

	public Valghierarki(S sti, String navn) {
		this.sti = sti;
		this.navn = navn;
	}

	public abstract ValghierarkiNivaa nivaa();

	public S sti() {
		return sti;
	}

	public String id() {
		return sti.sisteId();
	}

	public String navn() {
		return navn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Valghierarki<?> that = (Valghierarki<?>) o;
		return new EqualsBuilder()
				.append(sti, that.sti)
				.append(navn, that.navn)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(sti)
				.append(navn)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("sti", sti)
				.append("navn", navn)
				.toString();
	}

	public abstract ValggeografiNivaa valggeografiNivaa();
}
