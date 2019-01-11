package no.valg.eva.admin.common.rbac;

import static java.util.Objects.requireNonNull;
import static no.evote.constants.ElectionLevelEnum.CONTEST;

import java.util.List;

import no.valg.eva.admin.common.configuration.model.election.Contest;
import no.valg.eva.admin.common.configuration.status.ContestStatus;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.rbac.application.PersonMapper;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

public class OperatorMapper {
	protected OperatorMapper() {
	}

	public static RoleAssociation toRoleAssociation(OperatorRole operatorRole) {
		Role role = operatorRole.getRole();
		MvArea mvArea = operatorRole.getMvArea();
		RoleAssociation result = new RoleAssociation(toRoleViewObject(role), mvArea.toViewObject());
		if (role.getElectionLevel() != null && role.getElectionLevel() == CONTEST && operatorRole.getMvElection().getContest() != null) {
			result.setContest(mapContest(operatorRole.getMvElection().getContest()));
		}
		return result;
	}

	private static RoleItem toRoleViewObject(Role role) {
		return new RoleItem(role.getId(), role.getName(), role.getTranslatedName(), role.isUserSupport(), role.getElectionLevel(), role.levelsAsEnums());
	}

	public static Operator toViewOperatorWithRoleAssociations(no.valg.eva.admin.rbac.domain.model.Operator operator, List<OperatorRole> operatorRoles) {
		requireNonNull(operator);

		Operator operatorWithRoleAssociations = PersonMapper.toOperator(operator);
		for (OperatorRole operatorRole : operatorRoles) {
			RoleAssociation roleAssociation = toRoleAssociation(operatorRole);
			operatorWithRoleAssociations.addRoleAssociation(roleAssociation);
		}

		return operatorWithRoleAssociations;
	}

	private static Contest mapContest(no.valg.eva.admin.configuration.domain.model.Contest dbContest) {
		return new no.valg.eva.admin.common.configuration.model.election.Contest(
				dbContest.getPk(),
				dbContest.getId(),
				dbContest.getName(),
				ContestStatus.fromId(dbContest.getContestStatus().getId()),
				dbContest.getPenultimateRecount(),
				dbContest.getEndDateOfBirth(),
				null,
				dbContest.getAuditOplock());
	}
}
