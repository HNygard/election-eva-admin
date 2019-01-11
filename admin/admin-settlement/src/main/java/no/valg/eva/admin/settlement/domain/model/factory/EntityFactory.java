package no.valg.eva.admin.settlement.domain.model.factory;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.settlement.domain.consumer.EntityConsumer;

public abstract class EntityFactory<T extends EntityFactory, C extends EntityConsumer> {
	protected final List<C> consumers = new ArrayList<>();

	public T addConsumer(C consumer) {
		consumers.add(consumer);
		return self();
	}

	protected void updateConsumers() {
		consumers.forEach(this::updateConsumer);
	}

	protected abstract void updateConsumer(C c);

	protected abstract T self();
}
