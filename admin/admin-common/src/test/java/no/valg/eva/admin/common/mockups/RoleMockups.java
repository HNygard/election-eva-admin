package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.ElectionEventMockups.electionEvent;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.ROLE_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Role;

public final class RoleMockups {

	public static final long ROLE_PK = ROLE_PK_SERIES + 1;

	public static Role role(final ElectionEvent electionEvent) {
		Role role = new Role();
		role.setPk(ROLE_PK);
		role.setElectionEvent(electionEvent);
		return role;
	}

	public static Role defaultRole() {
		return role(electionEvent());
	}

	private RoleMockups() {
		// no instances allowed
	}
}
