package no.valg.eva.admin.frontend.i18n;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Wraps the MessageProvider for use in XHTML.
 */
@SuppressWarnings("rawtypes")
@Named("msgs")
@SessionScoped
public class FacesMessageWrapper implements Map, Serializable {
	@Inject
	private MessageProvider messageProvider;

	@Override
	public Object get(final Object key) {
		return messageProvider.get((String) key);
	}

	// Argh.. Ugly. Couldn't find out how to pass a String[] or String... from jsf/el
	public String get(final String key, final String param1) {
		return messageProvider.get(key, new String[] { param1 });
	}

	public String get(final String key, final String param1, final String param2) {
		return messageProvider.get(key, new String[] { param1, param2 });
	}

	public String getByElectionEvent(final String key, final Long electionEventPk) {
		return messageProvider.getByElectionEvent(key, electionEventPk);
	}

	public String getMultiple(final String multipleKeys) {
		String[] keys = multipleKeys.split("@");
		StringBuilder translation = new StringBuilder();
		for (String key : keys) {
			key = key.trim();
			if (key.length() > 0) {
				translation.append(get("@" + key));
				if (translation.charAt(translation.length() - 1) != '.') {
					translation.append(".");
				}
				translation.append(" ");
			}
		}

		return translation.toString();
	}

	/* The rest of the methods are not used so they are just dummy methods in order to conform with map interface */
	@Override
	public int size() {
		throw new IllegalAccessError();
	}

	@Override
	public boolean isEmpty() {
		throw new IllegalAccessError();
	}

	@Override
	public boolean containsKey(final Object key) {
		throw new IllegalAccessError();
	}

	@Override
	public boolean containsValue(final Object value) {
		throw new IllegalAccessError();
	}

	@Override
	public Object put(final Object key, final Object value) {
		throw new IllegalAccessError();
	}

	@Override
	public Object remove(final Object key) {
		throw new IllegalAccessError();
	}

	@Override
	public void putAll(final Map m) {
		throw new IllegalAccessError();
	}

	@Override
	public void clear() {
		throw new IllegalAccessError();
	}

	@Override
	public Set keySet() {
		throw new IllegalAccessError();
	}

	@Override
	public Collection values() {
		throw new IllegalAccessError();
	}

	@Override
	public Set entrySet() {
		throw new IllegalAccessError();
	}

}
