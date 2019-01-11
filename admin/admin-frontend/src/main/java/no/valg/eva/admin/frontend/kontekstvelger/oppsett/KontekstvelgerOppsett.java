package no.valg.eva.admin.frontend.kontekstvelger.oppsett;

import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.GEOGRAFI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.HIERARKI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.OPPTELLINGSKATEGORI;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class KontekstvelgerOppsett {
	private List<KontekstvelgerElement> elementer = new ArrayList<>();

	public static KontekstvelgerOppsett deserialize(String s) {
		KontekstvelgerOppsett result = new KontekstvelgerOppsett();
		for (String cfg : kontekstvelgerKonfigs(s)) {
			result.getElementer().add(KontekstvelgerElement.deserialize(cfg));
		}
		return result;
	}

	static List<String> kontekstvelgerKonfigs(String s) {
		List<String> result = new ArrayList<>();
		if (s == null || !s.startsWith("[") && !s.endsWith("]")) {
			throw invalidSerial(s);
		}
		int index = 0;
		Deque<Character> stack = new ArrayDeque<>();
		StringBuilder builder = new StringBuilder();
		while (index < s.length()) {
			char c = s.charAt(index);
			if ('[' == c) {
				stack.add(c);
				if (stack.size() > 1) {
					builder.append(c);
				}
			} else if (']' == c && !stack.isEmpty()) {
				stack.pop();
				if (stack.isEmpty()) {
					result.add(builder.toString());
					builder = new StringBuilder();
				} else {
					builder.append(c);
				}
			} else {
				builder.append(c);
			}
			index++;

		}
		if (builder.length() != 0 || result.isEmpty()) {
			throw invalidSerial(s);
		}
		return result;
	}

	private static IllegalArgumentException invalidSerial(String s) {
		return new IllegalArgumentException("Invalid ContextPickerSetup serialized string '" + s + "'");
	}

	public String serialize() {
		StringBuilder result = new StringBuilder();
		for (KontekstvelgerElement cfg : elementer) {
			result.append("[").append(cfg.serialize()).append("]");
		}
		return result.toString();
	}

	public void leggTil(KontekstvelgerElement config) {
		elementer.add(config);
	}

	public List<KontekstvelgerElement> getElementer() {
		return elementer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		KontekstvelgerOppsett that = (KontekstvelgerOppsett) o;
		return new EqualsBuilder()
				.append(elementer, that.elementer)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(elementer)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("elementer", elementer)
				.toString();
	}

	public List<KontekstvelgerElement.Type> typerForKontekst() {
		EnumSet<KontekstvelgerElement.Type> kontekstTyper = EnumSet.of(HIERARKI, GEOGRAFI, OPPTELLINGSKATEGORI);
		return elementer.stream()
				.map(KontekstvelgerElement::getType)
				.filter(kontekstTyper::contains)
				.collect(toList());
	}
}
