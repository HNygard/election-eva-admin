package no.valg.eva.admin.frontend.kontekstvelger.oppsett;

import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.FILTER;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.NIVAER;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.TJENESTE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.VARIANT;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.GEOGRAFI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.HIERARKI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.OPPTELLINGSKATEGORI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.SIDE;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.EnumMap;
import java.util.Map;
import java.util.StringTokenizer;

import no.valg.eva.admin.felles.model.Nivaa;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class KontekstvelgerElement {
	private Type type;
	private Map<Parameter, String> konfigurasjon = new EnumMap<>(Parameter.class);

	private KontekstvelgerElement(Type type) {
		this.type = type;
	}

	public static KontekstvelgerElement side(String uri) {
		return new KontekstvelgerElement(SIDE).leggTil(Parameter.URI, uri);
	}

	public static KontekstvelgerElement hierarki(ValghierarkiNivaa... valghierarkiNivaaer) {
		return new KontekstvelgerElement(HIERARKI).leggTil(NIVAER, Nivaa.toString(valghierarkiNivaaer));
	}

	public static KontekstvelgerElement geografi(ValggeografiNivaa... valggeografiNivaaer) {
		return new KontekstvelgerElement(GEOGRAFI).leggTil(NIVAER, Nivaa.toString(valggeografiNivaaer));
	}

	public static KontekstvelgerElement opptellingskategori() {
		return new KontekstvelgerElement(OPPTELLINGSKATEGORI);
	}

	public static KontekstvelgerElement deserialize(String s) {
		if (isEmpty(s)) {
			throw invalidSerial(s);
		}
		StringTokenizer tokens = new StringTokenizer(s, "|");
		String sType = tokens.nextToken();
		KontekstvelgerElement result = new KontekstvelgerElement(Type.fra(sType));
		while (tokens.hasMoreTokens()) {
			String navn = tokens.nextToken();
			if (!tokens.hasMoreTokens()) {
				throw invalidSerial(s);
			}
			String value = tokens.nextToken();
			result.leggTil(Parameter.fra(navn), value);
		}
		return result;
	}

	private static IllegalArgumentException invalidSerial(String s) {
		return new IllegalArgumentException("Invalid ContextPickerCfg serialized string '" + s + "'");
	}

	public KontekstvelgerElement medTjeneste(ValghierarkiTjeneste valgHierarkiTjeneste) {
		if (type == HIERARKI) {
			return leggTil(TJENESTE, valgHierarkiTjeneste.name());
		}
		throw new IllegalArgumentException("Valghierarkitjeneste kan kun knyttes til et hierarki-element");
	}

	public KontekstvelgerElement medTjeneste(ValggeografiTjeneste valggeografiTjeneste) {
		if (type == GEOGRAFI) {
			return leggTil(TJENESTE, valggeografiTjeneste.name());
		}
		throw new IllegalArgumentException("Valggeografitjeneste kan kun knyttes til et geografi-element");
	}

	public KontekstvelgerElement medFilter(ValggeografiFilter valggeografiFilter) {
		if (type == GEOGRAFI) {
			return leggTil(FILTER, valggeografiFilter.name());
		}
		throw new IllegalArgumentException("Valggeografifilter kan kun knyttes til et geografi-element");
	}

	public KontekstvelgerElement medVariant(ValggeografiVariant valggeografiVariant) {
		if (type == GEOGRAFI) {
			return leggTil(VARIANT, valggeografiVariant.name());
		}
		throw new IllegalArgumentException("Valggeografivariant kan kun knyttes til et geografi-element");
	}

	public KontekstvelgerElement leggTil(Parameter parameter, String value) {
		konfigurasjon.put(parameter, value);
		return this;
	}

	public String serialize() {
		StringBuilder result = new StringBuilder();
		result.append(type.navn());
		for (Parameter parameter : konfigurasjon.keySet()) {
			result.append('|').append(parameter.navn()).append("|").append(konfigurasjon.get(parameter));
		}
		return result.toString();
	}

	public Type getType() {
		return type;
	}

	public String get(Parameter parameter) {
		return konfigurasjon.get(parameter);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		KontekstvelgerElement that = (KontekstvelgerElement) o;
		return new EqualsBuilder()
				.append(type, that.type)
				.append(konfigurasjon, that.konfigurasjon)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(type)
				.append(konfigurasjon)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("type", type)
				.append("konfigurasjon", konfigurasjon)
				.toString();
	}

	public enum Type {
		SIDE, HIERARKI, GEOGRAFI, OPPTELLINGSKATEGORI;

		static Type fra(String navn) {
			return valueOf(navn.toUpperCase());
		}

		String navn() {
			return name().toLowerCase();
		}
	}

	public enum Parameter {
		URI, NIVAER, TJENESTE, FILTER, VARIANT;

		static Parameter fra(String navn) {
			return valueOf(navn.toUpperCase());
		}

		String navn() {
			return name().toLowerCase();
		}
	}
}
