package no.valg.eva.admin.counting.domain.auditevents;

import java.util.HashMap;
import java.util.Map;

import no.valg.eva.admin.common.counting.model.CountQualifier;

public final class ThreadLocalVoteCountAuditDetailsMap {
	public static final ThreadLocalVoteCountAuditDetailsMap INSTANCE = new ThreadLocalVoteCountAuditDetailsMap();

	private final ThreadLocal<Map<CountQualifier, VoteCountAuditDetails>> mapHolder = new ThreadLocal<>();

	private ThreadLocalVoteCountAuditDetailsMap() {
	}

	public void put(CountQualifier qualifier, VoteCountAuditDetails voteCountAuditDetails) {
		map().put(qualifier, voteCountAuditDetails);
	}

	public VoteCountAuditDetails get(CountQualifier qualifier) {
		return map().get(qualifier);
	}

	public void clear() {
		mapHolder.remove();
	}

	private Map<CountQualifier, VoteCountAuditDetails> map() {
		Map<CountQualifier, VoteCountAuditDetails> map = mapHolder.get();
		if (map == null) {
			map = new HashMap<>();
			mapHolder.set(map);
		}
		return map;
	}
}
