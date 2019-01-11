package no.evote.presentation.cache;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

/**
 * Convenience class for caching of electionevents.
 */
@ApplicationScoped
public class ElectionEventCache implements Serializable {

	@Inject
	private EntityCache entityCache;

	public ElectionEvent get(final UserData userData, final Long pk) {
		return entityCache.get(userData, ElectionEvent.class, pk);
	}

	public void remove(final Long pk) {
		entityCache.remove(ElectionEvent.class, pk);
	}
}
