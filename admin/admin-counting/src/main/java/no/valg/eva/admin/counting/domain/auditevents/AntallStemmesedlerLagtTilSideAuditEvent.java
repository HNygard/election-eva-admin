package no.valg.eva.admin.counting.domain.auditevents;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSideForValg;

import org.joda.time.DateTime;

public class AntallStemmesedlerLagtTilSideAuditEvent extends AuditEvent {
	private AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide;

	public AntallStemmesedlerLagtTilSideAuditEvent(UserData userData, AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide,
												   AuditEventTypes auditEventType, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventType, Process.COUNTING, outcome, detail);
		this.antallStemmesedlerLagtTilSide = antallStemmesedlerLagtTilSide;
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		if (auditEventType == null) {
			throw new IllegalArgumentException("auditEventType must not be null");
		}
		if (auditEventType == AuditEventTypes.Save) {
			return new Class[]{AntallStemmesedlerLagtTilSide.class};
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Class objectType() {
		return AntallStemmesedlerLagtTilSide.class;
	}

	@Override
	public String toJson() {
		AreaPath municipalityPath = antallStemmesedlerLagtTilSide.getMunicipalityPath();
		return new JsonBuilder()
				.add("kommuneSti", municipalityPath.toString())
				.add("antallStemmesedlerLagtTilSideForValg", antallStemmesedlerLagtTilSideForValg(antallStemmesedlerLagtTilSide))
				.toJson();
	}

	private JsonArray antallStemmesedlerLagtTilSideForValg(AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList = antallStemmesedlerLagtTilSide.getAntallStemmesedlerLagtTilSideForValgList();
		for (AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg : antallStemmesedlerLagtTilSideForValgList) {
			arrayBuilder.add(antallStemmesedlerLagtTilSideForValg(antallStemmesedlerLagtTilSideForValg));
		}
		return arrayBuilder.build();
	}

	private JsonObject antallStemmesedlerLagtTilSideForValg(AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg) {
		return new JsonBuilder()
				.add("valghierarkiSti", antallStemmesedlerLagtTilSideForValg.getElectionPath().toString())
				.add("navn", antallStemmesedlerLagtTilSideForValg.getNavn())
				.add("antallStemmesedler", antallStemmesedlerLagtTilSideForValg.getAntallStemmesedler())
				.asJsonObject();
	}
}
