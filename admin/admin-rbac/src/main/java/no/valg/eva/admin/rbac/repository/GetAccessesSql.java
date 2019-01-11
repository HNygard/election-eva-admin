package no.valg.eva.admin.rbac.repository;

import java.util.List;

import no.valg.eva.admin.rbac.domain.model.Role;

/**
 * Builds sql query for getting accesses.
 */
public class GetAccessesSql {

	private final String sql;

	public GetAccessesSql(final List<Role> roles) {
		StringBuilder searchString = new StringBuilder(
				"SELECT a.access_path FROM access a JOIN role_access_all_active ra1 ON a.access_pk = ra1.access_pk WHERE ra1.role_pk in (")
				.append(rolePks(roles.toArray(new Role[0]))).append(")");
		sql = searchString.toString();
	}

	private String rolePks(final Role[] roles) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < roles.length; i++) {
			sb.append(roles[i].getPk());
			if (i < roles.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public String getSql() {
		return sql;
	}
}
