package no.valg.eva.admin.frontend.kontekstvelger;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.io.Serializable;
import java.util.StringTokenizer;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Kontekst implements Serializable {
	private static final String VALGHIERARKI = "hierarki";
	private static final String VALGGEOGRAFI = "geografi";
	private static final String COUNT_CATEGORY = "countCategory";

	private ValghierarkiSti valghierarkiSti;
	private ValggeografiSti valggeografiSti;
	private CountCategory countCategory;

	public Kontekst() {
	}

	public Kontekst(Kontekst k1, Kontekst k2) {
		valghierarkiSti = k1.valghierarkiSti != null ? k1.valghierarkiSti : k2.valghierarkiSti;
		valggeografiSti = k1.valggeografiSti != null ? k1.valggeografiSti : k2.valggeografiSti;
		countCategory = k1.countCategory != null ? k1.countCategory : k2.countCategory;
	}

	public static Kontekst deserialize(String s) {
		if (isEmpty(s)) {
			return null;
		}
		StringTokenizer tokens = new StringTokenizer(s, "|");
		Kontekst result = null;
		while (tokens.hasMoreTokens()) {
			String key = tokens.nextToken();
			if (tokens.hasMoreTokens()) {
				String value = tokens.nextToken();
				switch (key) {
				case VALGGEOGRAFI:
					result = result == null ? new Kontekst() : result;
					result.setValggeografiSti(ValggeografiSti.fra(AreaPath.from(value)));
					break;
				case VALGHIERARKI:
					result = result == null ? new Kontekst() : result;
					result.setValghierarkiSti(ValghierarkiSti.fra(ElectionPath.from(value)));
					break;
					case COUNT_CATEGORY:
						result = result == null ? new Kontekst() : result;
						result.setCountCategory(CountCategory.fromId(value));
						break;
				default:
					result = null;
				}
			} else {
				result = null;
			}
		}
		return result;
	}

	public String serialize() {
		StringBuilder result = new StringBuilder();
		if (valghierarkiSti != null) {
			wrap(result, VALGHIERARKI, valghierarkiSti.toString());
		}
		if (valggeografiSti != null) {
			wrap(result, VALGGEOGRAFI, valggeografiSti.toString());
		}
		if (countCategory != null) {
			wrap(result, COUNT_CATEGORY, countCategory.toString());
		}
		return result.length() == 0 ? null : result.toString();
	}

	public ValghierarkiSti getValghierarkiSti() {
		return valghierarkiSti;
	}

	public void setValghierarkiSti(ValghierarkiSti valghierarkiSti) {
		this.valghierarkiSti = valghierarkiSti;
	}

	public ValggruppeSti valggruppeSti() {
		return (ValggruppeSti) valghierarkiSti;
	}

	public ValggeografiSti getValggeografiSti() {
		return valggeografiSti;
	}

	public void setValggeografiSti(ValggeografiSti valggeografiSti) {
		this.valggeografiSti = valggeografiSti;
	}

	public CountCategory getCountCategory() {
		return countCategory;
	}

	public void setCountCategory(CountCategory countCategory) {
		this.countCategory = countCategory;
	}

	public KommuneSti kommuneSti() {
		if (valggeografiSti == null) {
			return null;
		}
		return ValggeografiSti.kommuneSti(valggeografiSti.areaPath());
	}

	private void wrap(StringBuilder builder, String key, String value) {
		if (builder.length() > 0) {
			builder.append("|");
		}
		builder.append(key).append("|").append(value).toString();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("valghierarkiSti", valghierarkiSti)
				.append("valggeografiSti", valggeografiSti)
				.append("countCategory", countCategory)
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
		Kontekst kontekst = (Kontekst) o;
		return new EqualsBuilder()
				.append(valghierarkiSti, kontekst.valghierarkiSti)
				.append(valggeografiSti, kontekst.valggeografiSti)
				.append(countCategory, kontekst.countCategory)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(valghierarkiSti)
				.append(valggeografiSti)
				.append(countCategory)
				.toHashCode();
	}

	public boolean harVerdiFor(KontekstvelgerElement.Type type) {
		switch (type) {
			case HIERARKI:
				return valghierarkiSti != null;
			case GEOGRAFI:
				return valggeografiSti != null;
			case OPPTELLINGSKATEGORI:
				return countCategory != null;
			default:
				return false;
		}
	}

	public Object verdiFor(KontekstvelgerElement.Type type) {
		switch (type) {
			case HIERARKI:
				return valghierarkiSti;
			case GEOGRAFI:
				return valggeografiSti;
			case OPPTELLINGSKATEGORI:
				return countCategory;
			default:
				return null;
		}
	}

	public Kontekst settVerdi(KontekstvelgerElement.Type type, Object verdi) {
		switch (type) {
			case HIERARKI:
				valghierarkiSti = (ValghierarkiSti) verdi;
				break;
			case GEOGRAFI:
				valggeografiSti = (ValggeografiSti) verdi;
				break;
			case OPPTELLINGSKATEGORI:
				countCategory = (CountCategory) verdi;
				break;
			default:
				throw new IllegalArgumentException(format("ugyldig type: %s", type));
		}
		return this;
	}
}
