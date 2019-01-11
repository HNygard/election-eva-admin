package no.valg.eva.admin.backend.util;

import static no.valg.eva.admin.backend.util.NativeSqlBuilder.createNativeSqlBuilder;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class NativeSqlBuilderTest {
	
	@Test
	public void build_withMinimumRequiredSql_returnsSql() {

		final String expected = "SELECT name FROM employee";

		final String actual = createNativeSqlBuilder()
				.select("name")
				.from("employee")
				.build();
		
		assertEquals(actual, expected);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void build_withoutRequiredSelectClause_throwsException() {
		
		createNativeSqlBuilder()
				.from("employee")
				.build();
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void build_withoutRequiredFromClause_throwsException() {

		createNativeSqlBuilder()
				.select("name")
				.build();
	}

	@Test
	public void build_withMultipleSelectArguments_returnsSql() {

		final String expected = "SELECT name, salary, age FROM employee";

		final String actual = createNativeSqlBuilder()
				.select("name")
				.select("salary")
				.select("age")
				.from("employee")
				.build();

		assertEquals(actual, expected);
	}


	@Test
	public void build_withMultipleFromArguments_returnsSql() {

		final String expected = "SELECT * FROM employee e, department AS d";

		final String actual = createNativeSqlBuilder()
				.select("*")
				.from("employee e")
				.from("department AS d")
				.build();

		assertEquals(actual, expected);
	}

	@Test
	public void build_withWhereClause_returnsSql() {

		final String expected = "SELECT name FROM employee WHERE salary > 10000";

		final String actual = createNativeSqlBuilder()
				.select("name")
				.from("employee")
				.where("salary > 10000")
				.build();

		assertEquals(actual, expected);
	}

	@Test
	public void build_withMultipleWhereArguments_returnsSql() {

		final String expected = "SELECT name FROM employee WHERE salary > 10000 AND name like 'Bob%' AND age <= 60";

		final String actual = createNativeSqlBuilder()
				.select("name")
				.from("employee")
				.where("salary > 10000")
				.where("name like 'Bob%'")
				.where("age <= 60")
				.build();

		assertEquals(actual, expected);
	}


	@Test
	public void build_withJoinClause_returnsSql() {

		final String expected = "SELECT e.name, d.name FROM employee e JOIN department d ON e.dep_id = d.id";

		final String actual = createNativeSqlBuilder()
				.select("e.name")
				.select("d.name")
				.from("employee e")
				.join("department d ON e.dep_id = d.id")
				.build();

		assertEquals(actual, expected);
	}

	@Test
	public void build_withMultipleJoinsArguments_returnsSql() {

		final String expected = "SELECT e.name, d.name, c.name FROM employee e JOIN department d ON e.dep_id = d.id JOIN country AS c ON d.country_id = c.id";

		final String actual = createNativeSqlBuilder()
				.select("e.name")
				.select("d.name")
				.select("c.name")
				.from("employee e")
				.join("department d ON e.dep_id = d.id")
				.join("country AS c ON d.country_id = c.id")
				.build();

		assertEquals(actual, expected);
	}
	
	@Test
	public void build_withOrderByClause_returnsSql() {

		final String expected = "SELECT name FROM employee ORDER BY hair_color ASC";

		final String actual = createNativeSqlBuilder()
				.select("name")
				.from("employee")
				.orderBy("hair_color")
				.build();

		assertEquals(actual, expected);
	}

	@Test
	public void build_withMultipleOrderByArguments_returnsSql() {

		final String expected = "SELECT name FROM employee ORDER BY hair_color ASC, salary DESC";

		final String actual = createNativeSqlBuilder()
				.select("name")
				.from("employee")
				.orderBy("hair_color")
				.orderBy("salary", false)
				.build();

		assertEquals(actual, expected);
	}

	@Test
	public void build_withDuplicateArguments_returnsSql() {

		final String expected = "SELECT e.name, d.name FROM employee e JOIN department d ON e.dep_id = d.id WHERE e.name = 'PER' GROUP BY e.age";
		
		final String actual = createNativeSqlBuilder()
				.select("e.name")
				.select("e.name")
				.select("e.name")
				.select("d.name")
				.from("employee e")
				.from("employee e")
				.join("department d ON e.dep_id = d.id")
				.join("department d ON e.dep_id = d.id")
				.join("department d ON e.dep_id = d.id")
				.where("e.name = 'PER'")
				.where("e.name = 'PER'")
				.where("e.name = 'PER'")
				.where("e.name = 'PER'")
				.groupBy("e.age")
				.groupBy("e.age")
				.groupBy("e.age")
				.build();
		
		assertEquals(actual, expected);
	}

	@Test
	public void build_withDuplicateUnorderedArguments_returnsSql() {

		final String expected = "SELECT e.name, d.name FROM employee e JOIN department d ON e.dep_id = d.id WHERE e.name = 'PER' GROUP BY e.age";

		final String actual = createNativeSqlBuilder()
				.select("e.name")
				.select("e.name")
				.join("department d ON e.dep_id = d.id")
				.select("d.name")
				.from("employee e")
				.from("employee e")
				.where("e.name = 'PER'")
				.join("department d ON e.dep_id = d.id")
				.join("department d ON e.dep_id = d.id")
				.where("e.name = 'PER'")
				.where("e.name = 'PER'")
				.groupBy("e.age")
				.where("e.name = 'PER'")
				.groupBy("e.age")
				.groupBy("e.age")
				.select("e.name")
				.build();

		assertEquals(actual, expected);
	}

	@Test
	public void build_withGroupByClause_returnsSql() {

		final String expected = "SELECT name FROM employee GROUP BY age";

		final String actual = createNativeSqlBuilder()
				.select("name")
				.from("employee")
				.groupBy("age")
				.build();
		
		assertEquals(actual, expected);
	}

	@Test
	public void build_withMultipleGroupByArguments_returnsSql() {

		final String expected = "SELECT name FROM employee GROUP BY age, hair_color, name";


		final String actual = createNativeSqlBuilder()
				.select("name")
				.from("employee")
				.groupBy("age")
				.groupBy("hair_color")
				.groupBy("name")
				.build();

		assertEquals(actual, expected);
	}
}
