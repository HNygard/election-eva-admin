package no.valg.eva.admin.frontend.rbac.ctrls;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

public final class RoleSorter implements Serializable {
	private RoleSorter() {
	}

	public interface SortKey<T> {
		String getSortKey(T o);

		default void setTranslated(T o, String translated) {
		}
	}

	public static <T> void sortTranslated(MessageProvider messageProvider, List<T> roleList, SortKey<T> sortKey) {
		Collections.sort(roleList, new Comparator<T>() {
			private final Map<String, String> translationCache = new HashMap();

			private String getTranslatedName(final String sortKey) {
				String translation = translationCache.get(sortKey);
				if (translation == null) {
					translation = messageProvider.get(sortKey);
					translationCache.put(sortKey, translation);
				}

				return translation;
			}

			@Override
			public int compare(final T t1, final T t2) {
				sortKey.setTranslated(t1, getTranslatedName(sortKey.getSortKey(t1)));
				sortKey.setTranslated(t2, getTranslatedName(sortKey.getSortKey(t2)));
				return getTranslatedName(sortKey.getSortKey(t1)).compareTo(getTranslatedName(sortKey.getSortKey(t2)));
			}
		});
	}
}
