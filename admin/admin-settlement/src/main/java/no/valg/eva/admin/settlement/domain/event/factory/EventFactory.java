package no.valg.eva.admin.settlement.domain.event.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import no.valg.eva.admin.settlement.domain.event.listener.EventListener;

public abstract class EventFactory<L extends EventListener> {
	protected final List<L> eventListeners = new ArrayList<>();

	public void addEventListener(L listener) {
		eventListeners.add(listener);
	}

	protected void fireEvents(Consumer<L> action) {
		eventListeners.forEach(action);
	}
}
