package no.evote.util;

/**
 * Generic wrapper class - can for instance be used when you specify an anonymous inner class that has to set a value for the surrounding code (which isn't
 * possible when the variable is immutable).
 * @param <T>
 */
public class Wrapper<T> {
	private T value;

	public Wrapper() {
		this.value = null;
	}

	public Wrapper(final T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(final T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
