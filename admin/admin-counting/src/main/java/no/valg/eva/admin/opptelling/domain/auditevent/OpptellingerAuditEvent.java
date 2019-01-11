package no.valg.eva.admin.opptelling.domain.auditevent;

import static java.util.Arrays.copyOf;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.util.Arrays;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

import org.joda.time.DateTime;

public class OpptellingerAuditEvent extends AuditEvent {
	private ValghierarkiSti valghierarkiSti;
	private ValggeografiSti valggeografiSti;
	private CountCategory[] countCategories;
	private Styretype[] styretyper;

	@SuppressWarnings("unused")
	public OpptellingerAuditEvent(
			UserData userData, ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, CountCategory[] countCategories, Styretype[] styretyper,
			AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.COUNTING, outcome, detail);
		this.valghierarkiSti = valghierarkiSti;
		this.valggeografiSti = valggeografiSti;
		this.countCategories = countCategories == null ? null : copyOf(countCategories, countCategories.length);
		this.styretyper = styretyper == null ? null : copyOf(styretyper, styretyper.length);
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (auditEventType == null) {
			throw new IllegalArgumentException("auditEventType must not be null");
		}
		if (auditEventType == AuditEventTypes.DeletedAllInArea) {
			return new Class[]{ValghierarkiSti.class, ValggeografiSti.class, CountCategory[].class, Styretype[].class};
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Class objectType() {
		return VoteCount.class;
	}

	@Override
	public String toJson() {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("valghierarkiSti", valghierarkiSti.toString())
				.add("valggeografiSti", valggeografiSti.toString());
		if (isNotEmpty(countCategories)) {
			jsonBuilder.add("countCategories", Arrays.toString(countCategories));
		}
		if (isNotEmpty(styretyper)) {
			jsonBuilder.add("styretyper", Arrays.toString(styretyper));
		}
		return jsonBuilder.toJson();
	}
}
