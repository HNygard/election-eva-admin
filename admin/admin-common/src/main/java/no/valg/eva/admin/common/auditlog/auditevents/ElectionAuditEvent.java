package no.valg.eva.admin.common.auditlog.auditevents;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.configuration.model.election.Election;

import org.joda.time.DateTime;

public class ElectionAuditEvent extends AuditEvent {
	private static final Process PROCESS = Process.CENTRAL_CONFIGURATION;
	private Election election;
	private ElectionPath electionPath;

	public ElectionAuditEvent(UserData userData, Election election, AuditEventTypes auditEventType,
			Outcome outcome, String detail) {
		super(userData, new DateTime(),
				AuditEventTypes.Save.equals(auditEventType) && election.getElectionRef() == null ? AuditEventTypes.Create : AuditEventTypes.Update,
				PROCESS, outcome, detail);
		this.election = election;
	}

	public ElectionAuditEvent(UserData userData, ElectionPath electionPath, AuditEventTypes auditEventType,
			Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, PROCESS, outcome, detail);
		this.electionPath = electionPath;
	}

	@Override
	public Class objectType() {
		return Election.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();

		if (AuditEventTypes.Delete.equals(eventType())) {
			builder.add("electionPath", electionPath.path());
		} else {
			builder.add("id", election.getId());
			builder.add("name", election.getName());
			if (election.getValgtype() != null) {
				builder.add("valgtype", election.getValgtype().name());
			}
			builder.add("electionType", election.getGenericElectionType().name());
			builder.add("endDateOfBirth", election.getEndDateOfBirth().toString());
			builder.add("singleArea", election.isSingleArea()); // kun ett geografisk område hver
			builder.add("penultimateRecount", election.isPenultimateRecount());
			builder.add("renumber", election.isRenumber());
			builder.add("renumberLimit", election.isRenumberLimit()); // begrensning på antall renummereringer

			builder.add("writein", election.isWritein()); // Slengere
			builder.add("writeinLocalOverride", election.isWriteinLocalOverride());
			builder.add("strikeout", election.isStrikeout()); // Tillatt med utstrykning av kandidater
			builder.add("personal", election.isPersonal()); // Personstemmer
			builder.add("candidatesInContestArea", election.isCandidatesInContestArea()); // Kandidater må tilhøre geografisk område til valgdistrikt
			builder.add("maxCandidateNameLength", election.getMaxCandidateNameLength());
			builder.add("maxCandidateResidenceProfessionLength", election.getMaxCandidateResidenceProfessionLength());
			builder.add("maxCandidatesAddition", election.getMaxCandidatesAddition());
			builder.add("maxCandidates", election.getMaxCandidates());
			builder.add("minCandidatesAddition", election.getMinCandidatesAddition());
			builder.add("minCandidates", election.getMinCandidates());

			if (election.getCandidateRankVoteShareThreshold() != null) {
				builder.add("candidateRankVoteShareThreshold", election.getCandidateRankVoteShareThreshold().toString()); // sperregrense
			}
			if (election.getBaselineVoteFactor() != null) {
				builder.add("baselineVoteFactor", election.getBaselineVoteFactor().toString()); // stemmefaktor til parti
			}
			if (election.getLevelingSeatsVoteShareThreshold() != null) {
				builder.add("", election.getLevelingSeatsVoteShareThreshold().toString()); // sperregrense for utjevningsmandater
			}
			if (election.getSettlementFirstDivisor() != null) {
				builder.add("settlementFirstDivisor", election.getSettlementFirstDivisor().toString()); // Sainte-lague
			}
			builder.add("levelingSeats", election.getLevelingSeats()); // utjevningsmandater
		}

		return builder.toJson();
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (AuditEventTypes.Delete.equals(auditEventType)) {
			return new Class[] { ElectionPath.class };
		}
		return new Class[] { Election.class };
	}

}
