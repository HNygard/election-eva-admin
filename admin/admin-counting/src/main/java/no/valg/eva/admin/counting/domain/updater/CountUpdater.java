package no.valg.eva.admin.counting.domain.updater;

import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

public interface CountUpdater<T> {
	void applyUpdates(VoteCount voteCount, T count, VoteCountStatus voteCountStatus);
}
