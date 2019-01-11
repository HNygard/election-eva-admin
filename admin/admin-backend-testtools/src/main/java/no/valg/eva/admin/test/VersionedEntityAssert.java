package no.valg.eva.admin.test;

import static org.assertj.core.internal.Objects.instance;

import no.evote.model.VersionedEntity;

import org.assertj.core.api.AbstractAssert;
import org.joda.time.DateTime;

public class VersionedEntityAssert extends AbstractAssert<VersionedEntityAssert, VersionedEntity> {

	private VersionedEntityAssert(final VersionedEntity actual) {
		super(actual, VersionedEntityAssert.class);
	}

	public static VersionedEntityAssert assertThat(final VersionedEntity actual) {
		return new VersionedEntityAssert(actual);
	}

	public VersionedEntityAssert hasPk() {
		Long pk = actual.getPk();
		instance().assertNotNull(info, pk);
		return this;
	}

	public VersionedEntityAssert hasAuditTimestamp() {
		DateTime auditTimestamp = actual.getAuditTimestamp();
		instance().assertNotNull(info, auditTimestamp);
		return this;
	}

	public VersionedEntityAssert hasAuditOperationEqualTo(final String expectedAuditOperation) {
		String auditOperation = actual.getAuditOperation();
		instance().assertEqual(info, auditOperation, expectedAuditOperation);
		return this;
	}

	public VersionedEntityAssert hasAuditOplockEqualTo(final int expectedOplock) {
		int auditOplock = actual.getAuditOplock();
		instance().assertEqual(info, auditOplock, expectedOplock);
		return this;
	}
}
