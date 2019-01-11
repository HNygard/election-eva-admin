package no.valg.eva.admin.frontend.rbac.ctrls;

import java.util.Arrays;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.RoleItem;

public class BaseRbacTest extends BaseFrontendTest {

	static final RoleItem ROLE_VALGADMIN = roleItem("valgadmin", AreaLevelEnum.ROOT);
	static final RoleItem ROLE_VALGADMIN_KOMMUNE = roleItem("valgansvarlig_kommune");
	static final RoleItem ROLE_FORHANDSTEMME_MOTTAKER = roleItem("stemmemottak_forhånd");
	static final RoleItem ROLE_ANSVARLIG_URNETELLING = roleItem("ansvarlig_listeforslag");

	static final PollingPlaceArea AREA_VALG = area("Karolines testvalg", "752900");
	static final PollingPlaceArea AREA_OPPLAND = area("Oppland", "752900.47.05");
	static final PollingPlaceArea AREA_LUNNER = area("Lunner", "752900.47.05.0533", PollingPlaceType.ELECTION_DAY_VOTING);
	static final PollingPlaceArea AREA_LUNNER_DISTRICT = area("Lunner district", "752900.47.05.0533.053300.0000");
	static final PollingPlaceArea AREA_AMBULERENDE_PLACE = area("Ambulerende", "752900.47.05.0533.053300.0000.0001", PollingPlaceType.ADVANCE_VOTING);
	static final PollingPlaceArea AREA_LUNNER_OMSORGSSENTER_PLACE = area("Lunner omsorgssenter", "752900.47.05.0533.053300.0000.0002");

	static final String BRUKER_ID_1 = "11223344556";
	static final String BRUKER_ID_2 = "22334455667";
	static final String BRUKER_ID_3 = "33445566778";

	static RoleItem roleItem(String roleId) {
		return roleItem(roleId, AreaLevelEnum.MUNICIPALITY);
	}

	static RoleItem roleItem(String roleId, AreaLevelEnum level) {
		return roleItem(roleId, false, Arrays.asList(level));
	}

	static RoleItem roleItem(String roleId, boolean userSupport) {
		return roleItem(roleId, userSupport, Arrays.asList(AreaLevelEnum.MUNICIPALITY));
	}

	static RoleItem roleItem(String roleId, boolean userSupport, List<AreaLevelEnum> levels) {
		return new RoleItem(roleId, "@role[" + roleId + "].name", userSupport, null, levels);
	}

	static PollingPlaceArea area(String name, String areaPath) {
		return area(name, areaPath, PollingPlaceType.NOT_APPLICABLE);
	}

	static PollingPlaceArea area(String name, String areaPath, PollingPlaceType type) {
		return new PollingPlaceArea(AreaPath.from(areaPath), name, type);
	}

	Operator operator(String personId, List<RoleAssociation> associations) {
		Operator operator = new Operator(personId, "first", "last", "", "", true);
		operator.addAllRoleAssociations(associations);
		return operator;
	}

	RoleAssociation roleAssociation(RoleItem role, PollingPlaceArea area) {
		return new RoleAssociation(role, area);
	}
}
