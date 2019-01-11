package no.valg.eva.admin.felles.sti;

import static java.lang.String.format;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class Sti<F extends Sti<? extends Sti>> implements Serializable {
	private static final String STI_FORMAT = "%s.%s";

	private Pattern patternForValideringAvId;
	private F forelderSti;
	private String sisteId;

	protected Sti(Pattern patternForValideringAvId, F forelderSti, String sisteId) {
		this.patternForValideringAvId = patternForValideringAvId;
		this.forelderSti = forelderSti;
		this.sisteId = sisteId;
	}

	protected void validerNull(Sti sti, String feilmelding) {
		if (sti == null) {
			throw new IllegalArgumentException(feilmelding);
		}
	}

	protected void validerPattern(String id, String feilmelding) {
		if (id == null || !patternForValideringAvId.matcher(id).matches()) {
			throw new IllegalArgumentException(format(feilmelding, id));
		}
	}

	public F forelderSti() {
		return forelderSti;
	}

	public String sisteId() {
		return sisteId;
	}

	public boolean likEllerUnder(Sti sti) {
		return equals(sti) || forelderSti() != null && forelderSti().likEllerUnder(sti);
	}

	public String toString() {
		return forelderSti != null ? format(STI_FORMAT, forelderSti, sisteId) : sisteId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Sti that = (Sti) o;
		return new EqualsBuilder()
				.append(forelderSti(), that.forelderSti())
				.append(sisteId(), that.sisteId())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(forelderSti())
				.append(sisteId())
				.toHashCode();
	}
}
