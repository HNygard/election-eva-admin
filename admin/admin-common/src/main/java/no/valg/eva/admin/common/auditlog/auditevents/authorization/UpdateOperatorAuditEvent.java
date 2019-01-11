package no.valg.eva.admin.common.auditlog.auditevents.authorization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.Collection;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;

import org.joda.time.DateTime;

public class UpdateOperatorAuditEvent extends AuditEvent {

	private final Operator operator;
	private AreaPath areaPath;
	private final Collection<RoleAssociation> addedRoles;
	private final Collection<RoleAssociation> deletedRoles;

	public UpdateOperatorAuditEvent(UserData userData, Operator operator, AreaPath areaPath, Collection<RoleAssociation> addedRoles,
			Collection<RoleAssociation> deletedRoles, AuditEventTypes auditEventTypes, Outcome outcome, String detail) {
		super(userData, new DateTime(), auditEventTypes, Process.AUTHORIZATION, outcome, detail);
		this.operator = operator;
		this.areaPath = areaPath;
		this.addedRoles = addedRoles;
		this.deletedRoles = deletedRoles;
	}

	@Override
	public Class objectType() {
		return Operator.class;
	}

	@Override
	public String toJson() {
		JsonBuilder builder = new JsonBuilder();
		builder.add("personId", operator.getPersonId().getId());
		builder.add("phone", operator.getTelephoneNumber());
		builder.add("email", operator.getEmail());
		builder.add("active", operator.isActive());
		builder.add("keySerialNumber", operator.getKeySerialNumber());
		builder.add("areaPath", areaPath != null ? areaPath.path() : null);
		builder.add("addedRoles", toJson(addedRoles));
		builder.add("deletedRoles", toJson(deletedRoles));
		return builder.toJson();
	}

	private JsonArray toJson(Collection<RoleAssociation> roleAssociations) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (RoleAssociation roleAssociation : roleAssociations) {
			JsonBuilder builder = new JsonBuilder();
			builder.add("areaPath", roleAssociation.getArea().getAreaPath().path());
			builder.add("areaName", roleAssociation.getArea().getName());
			builder.add("roleId", roleAssociation.getRole().getRoleId());
			builder.add("roleName", defaultIfBlank(roleAssociation.getRole().getTranslatedName(), roleAssociation.getRole().getRoleName()));
			arrayBuilder.add(builder.asJsonObject());
		}
		return arrayBuilder.build();
	}

	public static Class[] objectClasses(AuditEventType auditEventType) {
		return new Class[] { Operator.class, AreaPath.class, Collection.class, Collection.class };
	}
}
