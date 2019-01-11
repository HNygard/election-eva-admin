package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.rbac.BuypassOperator;

import org.joda.time.DateTime;

public class BuypassKeySerialNumbersAuditEvent extends AuditEvent {

	private List<BuypassOperator> operators = new ArrayList<>();

	public BuypassKeySerialNumbersAuditEvent(UserData userData, List<BuypassOperator> buypassOperators, AuditEventTypes crudType, Outcome outcome, String detail) {
		super(userData, new DateTime(), crudType, Process.AUTHORIZATION, outcome, detail);
		operators = buypassOperators;
	}

	@Override
	public Class objectType() {
		return BuypassOperator.class;
	}

	@Override
	public String toJson() {
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

		if (operators != null) {

			for (BuypassOperator operator : operators) {
				JsonBuilder jsonBuilder = new JsonBuilder();
				jsonBuilder.add("fnr", operator.getFnr());
				jsonBuilder.add("buypass-nummer", operator.getBuypassKeySerialNumber());
				jsonArrayBuilder.add(jsonBuilder.asJsonObject());
			}
			return jsonArrayBuilder.build().toString();
		} else {
			return "Ingen importerte nummer";
		}
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { List.class };
	}

}
