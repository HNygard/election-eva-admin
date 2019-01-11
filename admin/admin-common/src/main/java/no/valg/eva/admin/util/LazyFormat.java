package no.valg.eva.admin.util;

/**
 * Small wrapper for lazy evaluation of <code>String.format</code>, so that we don't need to unnecessarily format strings all the time. To use it, pass
 * <code>Object</code>, and call <code>toString</code> at the <b>very last moment</b> when you need it to be evaluated.
 */
public final class LazyFormat {

	private LazyFormat() {
		// Intentionally empty
	}

	public static Object format(final String s, final Object... o) {
		return new Object() {
			@Override
			public String toString() {
				return String.format(s, o);
			}
		};
	}
}
