package no.evote.presentation.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.evote.constants.VoteCountStatusEnum;
import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.log4j.Logger;

public final class EnumCache {
	private final Map<String, Object> propertCache = new HashMap<>();
	private final Map<String, Class<?>> baseCache = new HashMap<>();
	private static EnumCache staticEnumCache = null;
	private final Logger log = Logger.getLogger(EnumCache.class);

	public static EnumCache instance() {
		if (staticEnumCache == null) {
			staticEnumCache = new EnumCache();
		}
		return staticEnumCache;
	}

	private EnumCache() {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(VoteCountStatusEnum.class);
		classes.add(CountCategory.class);

		for (Class<?> clazz : classes) {
			try {
				baseCache.put(clazz.getSimpleName(), clazz);
				Method m = clazz.getMethod("values", (Class[]) null);
				Enum<?>[] valueList = (Enum[]) m.invoke(null, (Object[]) null);
				for (Enum<?> en : valueList) {
					propertCache.put(clazz.getSimpleName() + "." + en.name(), en);
				}
			} catch (Exception e) {
				log.error(clazz.getSimpleName(), e);
			}
		}
	}

	public Object getValueForKey(final String key) {
		return propertCache.get(key);
	}

	public Class<?> getClassForKey(final String key) {
		return baseCache.get(key);
	}
}
