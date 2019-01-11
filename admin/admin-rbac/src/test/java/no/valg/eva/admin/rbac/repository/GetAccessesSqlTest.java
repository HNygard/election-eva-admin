package no.valg.eva.admin.rbac.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Role;

import org.testng.annotations.Test;

public class GetAccessesSqlTest {

	@Test
	public void sqlContainsRolePks() {
		String expectedSql = "SELECT a.access_path FROM access a JOIN role_access_all_active ra1 ON a.access_pk = ra1.access_pk WHERE ra1.role_pk in (3,2,1)";
		assertThat(new GetAccessesSql(roles()).getSql()).isEqualTo(expectedSql);
	}

	private List<Role> roles() {
		List<Role> roles = new ArrayList<>();
		
		roles.add(role(3L));
		roles.add(role(2L));
		roles.add(role(1L));
		
		return roles;
	}

	private Role role(final long pk) {
		Role role = new Role();
		role.setPk(pk);
		role.setElectionEvent(new ElectionEvent(1L));
		role.setId("" + pk);
		return role;
	}

}
