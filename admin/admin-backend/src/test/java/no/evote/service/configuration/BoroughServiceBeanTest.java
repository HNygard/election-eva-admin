package no.evote.service.configuration;

import javax.persistence.PersistenceException;
import javax.validation.ValidationException;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = { TestGroups.REPOSITORY })
public class BoroughServiceBeanTest extends AreaBaseTest {
	@BeforeMethod
	public void init() {
		// Creates election event
		setElectionEvent(buildElectionEvent());
		createElectionEvent(getElectionEvent());

		// Creates country
		setCountry(buildCountry(getElectionEvent()));
		createCountry(getCountry());

		// Creates county
		setCounty(buildCounty(getCountry()));
		createCounty(getCounty());

		// Creates municipality
		setMunicipality(buildMunicipality(getCounty()));
		createMunicipality(getMunicipality());
	}

	@Test
	public void testCreate() {
		// Builds a Borough entry
		setBorough(buildBorough(getMunicipality()));

		// Saves entry
		getBoroughService().create(rbacTestFixture.getUserData(), getBorough());

		// Asserts
		Assert.assertTrue(getBorough().getPk() != null);
		Assert.assertTrue(getBorough().getPk() > 0);
	}

	@Test
	public void testCreateIdTooLong() {
		try {
			// Builds a Borough entry
			setBorough(buildBorough(getMunicipality()));
			getBorough().setId("1234567");

			// Saves entry
			getBoroughService().create(rbacTestFixture.getUserData(), getBorough());
			Assert.fail();
		} catch (ValidationException e) {
		}
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testCreateDuplicateId() {
		// Creates a Borough entry
		setBorough(buildBorough(getMunicipality()));
		getBoroughService().create(rbacTestFixture.getUserData(), getBorough());

		// Tries to insert duplicate
		getBoroughService().create(rbacTestFixture.getUserData(), buildBorough(getMunicipality()));
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCreateDuplicateBoroughWholeMunicipality() {
		// Creates two Borough entries
		Borough borough1 = buildBorough(getMunicipality());
		borough1.setMunicipality1(true);
		borough1.setId("050505");
		createBorough(borough1);

		Borough borough2 = buildBorough(getMunicipality());
		borough2.setMunicipality1(true);
		borough2.setId("050605");

		// Tries to insert duplicate borough for whole municipality
		getBoroughService().create(rbacTestFixture.getUserData(), borough2);
	}

	@Test
	public void boroughShouldNotHavePollingDistricts() {
		// Creates a Borough entry
		setBorough(buildBorough(getMunicipality()));

		Assert.assertFalse(getMunicipalityService().getMunicipalitiesWithoutBoroughs(getElectionEvent().getPk()).isEmpty());
		createBorough(getBorough());

		Assert.assertTrue(getMunicipalityService().getMunicipalitiesWithoutBoroughs(getElectionEvent().getPk()).isEmpty());
		Assert.assertFalse(getBoroughService().getBoroughsWithoutPollingDistricts(getElectionEvent().getPk()).isEmpty());
	}

	@Test
	public void testUpdate() {
		// Creates a Borough entry
		setBorough(buildBorough(getMunicipality()));
		createBorough(getBorough());

		// Updates borough
		String updatedName = "updatedName";
		getBorough().setName(updatedName);

		// Saves entry
		Borough updatedBorough = getBoroughService().update(rbacTestFixture.getUserData(), getBorough());

		// Asserts
		Assert.assertNotNull(updatedBorough);
		Assert.assertTrue(updatedBorough.getName().equals(updatedName));
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testUpdateDuplicateId() {
		// Creates two Borough entries
		Borough borough1 = buildBorough(getMunicipality());
		borough1.setId("060606");
		createBorough(borough1);

		Borough borough2 = buildBorough(getMunicipality());
		borough2.setId("060607");
		createBorough(borough2);

		// Tries to update borough1 with the same id as borough2
		borough2.setId("060606");
		getBoroughService().update(rbacTestFixture.getUserData(), borough2);
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testUpdateDuplicateBoroughWholeMunicipality() {
		// Creates a "whole municipality" borough
		Borough borough1 = buildBorough(getMunicipality());
		borough1.setId("080808");
		borough1.setMunicipality1(true);
		createBorough(borough1);

		// Creates a "not whole municipality" borough
		Borough borough2 = buildBorough(getMunicipality());
		borough2.setId("080809");
		createBorough(borough2);

		// Tries to update borough2 to be a "whole municipality" borough
		borough2.setMunicipality1(true);
		getBoroughService().update(rbacTestFixture.getUserData(), borough2);
	}

	@Test
	public void testFindBoroughByPk() {
		// Creates a Borough entry
		setBorough(buildBorough(getMunicipality()));
		createBorough(getBorough());

		// Asserts
		Assert.assertTrue(getBorough().getPk().equals(genericTestRepository.findEntityByProperty(Borough.class, "pk", getBorough().getPk()).getPk()));
		Assert.assertFalse(getBorough().getPk().equals(genericTestRepository.findEntityByProperty(Borough.class, "pk", 99999999L)));
	}
}

