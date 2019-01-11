package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import org.joda.time.DateTime;

import java.util.Set;

public class ElectionEventAuditEvent extends AuditEvent {
	private final ElectionEvent electionEvent;
	private final Set<Locale> locales;

	//           auditlogges, og dermed kommer både "to" og "from" med
	public ElectionEventAuditEvent(UserData userData, ElectionEvent electionEventTo, ElectionEvent electionEventFrom, Set<Locale> locales,
								   AuditEventTypes crudType, Outcome outcome, String detail) {
		this(userData, electionEventTo, locales, crudType, outcome, detail);
	}

	public ElectionEventAuditEvent(UserData userData, ElectionEvent electionEvent, Set<Locale> locales,
								   AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.CENTRAL_CONFIGURATION, outcome, detail);
		this.electionEvent = electionEvent;
		this.locales = locales;
	}

	@Override
	public String toJson() {
		JsonBuilder objectBuilder = new JsonBuilder();
		objectBuilder.add("id", electionEvent.getId());
		objectBuilder.add("name", electionEvent.getName());
		objectBuilder.addLocales("locales", locales);
		objectBuilder.add("status", electionEvent.getElectionEventStatusEnum().name());

		objectBuilder.add("theme", electionEvent.getTheme());
		objectBuilder.add("demoElection", electionEvent.isDemoElection());

		objectBuilder.addDate("electoralRollCutOffDate", electionEvent.getElectoralRollCutOffDate());

		objectBuilder.addDate("votingCardElectoralRollDate", electionEvent.getVotingCardElectoralRollDate());
		objectBuilder.addDate("votingCardDeadline", electionEvent.getVotingCardDeadline());
		objectBuilder.addDate("voterNumbersAssignedDate", electionEvent.getVoterNumbersAssignedDate());

		objectBuilder.add("electoralRollLinesPerPage", electionEvent.getElectoralRollLinesPerPage());
		objectBuilder.add("voterImportDirName", electionEvent.getVoterImportDirName());
		objectBuilder.add("voterImportMunicipality", electionEvent.isVoterImportMunicipality());

		// electionEvent.getElectionDays() is not preloaded, and will not be logged, to avoid LazyInitializationException.

		return objectBuilder.toJson();
	}

	@Override
	public Class objectType() {
		return ElectionEvent.class;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.Create.equals(auditEventType)) {
			return new Class[]{ElectionEvent.class, ElectionEvent.class, Set.class};
		} else if (AuditEventTypes.Update.equals(auditEventType)) {
			return new Class[] { ElectionEvent.class, Set.class };
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
