package no.valg.eva.admin.backend.service.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

/**
 * This EJB verifies that the database schema is correct and has the latest database migrations
 */
@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class DatabaseSchemaCheckerBean {
	@Resource(mappedName = "java:/jdbc/evote")
	private DataSource evoteDatasource;

	@PostConstruct
	public void checkDatabaseMigrations() {
		Flyway flyway = initFlyway();

		int pendingMigrations = flyway.info().pending().length;
		if (pendingMigrations > 0) {
			throw new RuntimeException("There are " + pendingMigrations + " pending database migrations. Run Flyway migration before starting EVA Admin");
		}
	}

	private Flyway initFlyway() {
		Flyway flyway = new Flyway();
		flyway.setDataSource(evoteDatasource);
		flyway.setLocations("no/valg/eva/admin/database/migrations");
		flyway.setSchemas("admin", "audit");
		flyway.setOutOfOrder(true);
		return flyway;
	}
}
