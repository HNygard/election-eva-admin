package no.valg.eva.admin.util;

import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.test.BaseTakeTimeTest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EqualsHashCodeUtilTest extends BaseTakeTimeTest {

	private static final String NAME = "name";

	@Test
	public void testTwoNewObjectsEqual() {
		ElectionType e1 = createElectionType("ID", NAME);
		ElectionType e2 = createElectionType("ID", NAME);
		Assert.assertTrue(e1.equals(e2));
		Assert.assertTrue(e2.equals(e1));
		Assert.assertTrue(e1.hashCode() == e2.hashCode());
	}

	@Test
	public void testTwoNewObjectsNotEqual() {
		ElectionType e1 = createElectionType("ID", NAME);
		ElectionType e2 = createElectionType("id2", NAME);
		Assert.assertFalse(e1.equals(e2));
		Assert.assertFalse(e2.equals(e1));
		Assert.assertTrue(e1.hashCode() != e2.hashCode());
	}

	@Test
	public void testAgainstNullObject() {
		ElectionType e1 = createElectionType("ID", NAME);
		ElectionType e2 = null;
		Assert.assertFalse(e1.equals(e2));
	}

	@Test
	public void testAgainstNullField() {
		ElectionType e1 = createElectionType("ID", NAME);
		ElectionType e2 = createElectionType(null, NAME);
		Assert.assertFalse(e1.equals(e2));
		Assert.assertFalse(e2.equals(e1));
		Assert.assertTrue(e1.hashCode() != e2.hashCode());
	}

	@Test
	public void testAgainstSelf() {
		ElectionType e1 = createElectionType("ID", NAME);
		Assert.assertTrue(e1.equals(e1));
		Assert.assertTrue(e1.hashCode() == e1.hashCode());
	}

	private ElectionType createElectionType(final String id, final String name) {
		ElectionType electionType = new ElectionType();
		electionType.setId(id);
		electionType.setName(name);
		return electionType;
	}

}
