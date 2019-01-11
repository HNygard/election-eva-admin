package no.valg.eva.admin.common.auditlog;

import java.util.Objects;
import java.util.Set;

import no.valg.eva.admin.common.Process;

import com.google.common.collect.ImmutableSet;

public enum SimpleAuditEventType implements AuditEventType {
	OperatorLoggedOut(Process.AUTHENTICATION, setOf(Outcome.Success)),
	OperatorSelectedRole(Process.AUTHENTICATION, setOf(Outcome.Success)),
	SubConfigEvent(Process.CENTRAL_CONFIGURATION, setOf(Outcome.Success)),
	AccessDeniedInBackend(Process.AUTHORIZATION, setOf(Outcome.GenericError));

	private final Process process;
	private final Set<Outcome> validOutcomes;

	SimpleAuditEventType(Process process, Set<Outcome> validOutcomes) {
		this.process = Objects.requireNonNull(process);
		this.validOutcomes = Objects.requireNonNull(validOutcomes);

		if (validOutcomes.size() < 1) {
			throw new IllegalStateException("There must be at least one valid outcome");
		}
	}

	public Process process() {
		return process;
	}

	public boolean hasSingleOutcome() {
		return validOutcomes.size() == 1;
	}

	public Outcome getSingleOutcome() {
		if (!hasSingleOutcome()) {
			throw new IllegalStateException("AuditEventType " + name() + " has multiple outcomes");
		}
		return validOutcomes.iterator().next();
	}

	public boolean isValidOutcome(Outcome outcome) {
		return validOutcomes.contains(outcome);
	}

	private static ImmutableSet<Outcome> setOf(Outcome... validOutcomes) {
		return ImmutableSet.<Outcome> builder().add(validOutcomes).build();
	}
}
