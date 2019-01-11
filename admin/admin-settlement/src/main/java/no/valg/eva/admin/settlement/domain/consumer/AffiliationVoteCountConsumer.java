package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;

public interface AffiliationVoteCountConsumer extends EntityConsumer {
	void consume(AffiliationVoteCount affiliationVoteCount);
}
