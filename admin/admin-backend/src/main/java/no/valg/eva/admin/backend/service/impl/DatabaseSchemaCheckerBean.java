package no.valg.eva.admin.backend.service.impl;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * This EJB verifies that the database schema is correct and has the latest database migrations
 */
@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class DatabaseSchemaCheckerBean {
	public static PGSimpleDataSource evoteDatasource;

	@PostConstruct
	public void checkDatabaseMigrations() {

        // <property name="javax.persistence.jdbc.driver" value="org.postgresql.Driver"/>
        // <property name="javax.persistence.jdbc.url" value="jdbc:postgresql://${testDatabaseHost}:${testDatabasePort}/${testDatabaseName}"/>
        // <property name="javax.persistence.jdbc.user" value="admin"/>
        // <property name="javax.persistence.jdbc.password" value="admin"/>
		evoteDatasource = new PGSimpleDataSource();

		//evoteDatasource.setDriverClassName("com.mysql.jdbc.Driver");
		//evoteDatasource.setUsername("username");
		evoteDatasource.setPassword("password");
		evoteDatasource.setUrl("jdbc:postgresql://<host>:<port>/<database>");
		//evoteDatasource.setMaxActive(10);
		//evoteDatasource.setMaxIdle(5);
		//evoteDatasource.setInitialSize(5);
		//evoteDatasource.setValidationQuery("SELECT 1");



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
