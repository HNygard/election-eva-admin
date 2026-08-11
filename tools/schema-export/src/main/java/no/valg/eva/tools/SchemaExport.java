package no.valg.eva.tools;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.Target;

/**
 * Writes the PostgreSQL DDL for EVA Admin's entity model to a file.
 *
 * The entity list is supplied by generate.sh, derived from the @Entity
 * annotations in the release source. That is deliberately not taken from any
 * persistence.xml: no shipped persistence unit covers the whole model. The
 * admin-backend test unit lists 117 classes, the union of every shipped unit
 * reaches 119, and the source has 120 -- LegacyPollingDistrict appears in none
 * of them. See docs/findings/2026-08-11-schema-generated-from-entities.md
 *
 * Uses Hibernate's SchemaExport rather than Persistence.generateSchema, because
 * the latter builds a SessionFactory and a SessionFactory demands a JDBC
 * connection even when only generating a script. Nothing here contacts a
 * database.
 */
public final class SchemaExport {

	private SchemaExport() {
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("usage: SchemaExport <entity-class-list> <output.sql>");
			System.exit(2);
		}
		File classList = new File(args[0]);
		String target = args[1];

		List<String> classNames = readEntityClassNames(classList);
		System.out.println("Entity classes found in the release source: " + classNames.size());

		StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect")
				// Without this Hibernate tries to read JDBC metadata to work out
				// defaults, which would mean opening a connection.
				.applySetting("hibernate.temp.use_jdbc_metadata_defaults", "false")
				.build();

		MetadataSources sources = new MetadataSources(registry);
		for (String className : classNames) {
			sources.addAnnotatedClass(Class.forName(className));
		}

		MetadataImplementor metadata = (MetadataImplementor) sources.buildMetadata();

		org.hibernate.tool.hbm2ddl.SchemaExport export = new org.hibernate.tool.hbm2ddl.SchemaExport(metadata);
		export.setOutputFile(target);
		export.setDelimiter(";");
		export.setFormat(true);
		export.execute(Target.SCRIPT, org.hibernate.tool.hbm2ddl.SchemaExport.Type.CREATE);

		if (!export.getExceptions().isEmpty()) {
			for (Object e : export.getExceptions()) {
				System.err.println("schema export problem: " + e);
			}
			System.exit(1);
		}

		System.out.println("Wrote " + target);
	}

	private static List<String> readEntityClassNames(File classList) throws Exception {
		List<String> names = new ArrayList<>();
		for (String line : Files.readAllLines(classList.toPath(), StandardCharsets.UTF_8)) {
			String trimmed = line.trim();
			if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
				names.add(trimmed);
			}
		}
		return names;
	}
}
