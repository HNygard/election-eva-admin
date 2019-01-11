package no.valg.eva.admin.frontend.kontekstvelger;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class KontekstvelgerRad {
	private String id;
	private boolean visId;
	private String verdi;

	public KontekstvelgerRad() {
		// CDI
	}

	public KontekstvelgerRad(String id, boolean visId, String verdi) {
		this.id = id;
		this.visId = visId;
		this.verdi = verdi;
	}

	public static <T, R extends KontekstvelgerRad> List<R> kontekstvelgerRader(List<T> list, Function<T, R> mapper) {
		return list.stream().map(mapper).collect(toList());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isVisId() {
		return visId;
	}

	public void setVisId(boolean visId) {
		this.visId = visId;
	}

	public String getVerdi() {
		return verdi;
	}

	public void setVerdi(String verdi) {
		this.verdi = verdi;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("visId", visId)
				.append("verdi", verdi)
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
		KontekstvelgerRad that = (KontekstvelgerRad) o;
		return new EqualsBuilder()
				.append(visId, that.visId)
				.append(id, that.id)
				.append(verdi, that.verdi)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(id)
				.append(visId)
				.append(verdi)
				.toHashCode();
	}
}
