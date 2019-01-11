package no.evote.service.backendmock;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Event;
import javax.enterprise.util.TypeLiteral;

/**
 * Event implementation that does nothing. Only for use in tests.
 *
 * @param <T> The event type
 */
public class FakeEvent<T> implements Event {
	@Override
	public void fire(Object event) {
	}

	@Override
	public Event select(Annotation... qualifiers) {
		return null;
	}

	@Override
	public Event<T> select(TypeLiteral subtype, Annotation... qualifiers) {
		return null;
	}

	@Override
	public Event<T> select(Class subtype, Annotation... qualifiers) {
		return null;
	}
}
