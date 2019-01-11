package no.valg.eva.admin.configuration.domain.model;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

public class ResponsibleOfficerTest {

	@Test
	public void testNameLine() {
		ResponsibleOfficer responsibleOfficer = new ResponsibleOfficer();
		responsibleOfficer.setLastName("Nordmann");
		responsibleOfficer.setFirstName("Ola");
		responsibleOfficer.setMiddleName("B");
		responsibleOfficer.updateNameLine();
		assertEquals("Nordmann Ola B", responsibleOfficer.getNameLine());
	}

}
