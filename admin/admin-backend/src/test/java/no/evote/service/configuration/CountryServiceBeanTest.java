package no.evote.service.configuration;

import java.util.List;

import javax.persistence.PersistenceException;

import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = { TestGroups.REPOSITORY })
public class CountryServiceBeanTest extends AreaBaseTest {
	@BeforeMethod
	public void init() {
		setElectionEvent(buildElectionEvent());
		createElectionEvent(getElectionEvent());
	}

	@Test
	public void testCreate() {
		setCountry(buildCountry(getElectionEvent()));

		getCountryServiceBean().create(rbacTestFixture.getUserData(), getCountry());

		Assert.assertTrue(getCountry().getPk() != null);
		Assert.assertTrue(getCountry().getPk() > 0);
		Country tmpCountry = getCountryServiceBean().findByPk(getCountry().getPk());
		Assert.assertTrue(tmpCountry.getPk().equals(getCountry().getPk()));
	}

	@Test
	public void countryShouldNotHaveCounties() {
		setCountry(buildCountry(getElectionEvent()));
		createCountry(getCountry());

		List<Country> countries = getCountryService().getCountriesWithoutCounties(getElectionEvent().getPk());
		Assert.assertEquals(countries.size(), 1);
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testCreateDuplicateId() {
		setCountry(buildCountry(getElectionEvent()));
		createCountry(getCountry());

		Country dupe = buildCountry(getElectionEvent());
		getCountryServiceBean().create(rbacTestFixture.getUserData(), dupe);
	}

	@Test
	public void testUpdate() {
		setCountry(buildCountry(getElectionEvent()));
		createCountry(getCountry());

		String updatedName = "updatedName";
		getCountry().setName(updatedName);

		Country updatedCountry = getCountryServiceBean().update(rbacTestFixture.getUserData(), getCountry());

		Assert.assertNotNull(updatedCountry);
		Assert.assertTrue(updatedCountry.getName().equals(updatedName));
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testUpdateDuplicateId() {
		// Creates two Country entries
		Country country1 = buildCountry(getElectionEvent());
		country1.setId("31");
		createCountry(country1);

		Country country2 = buildCountry(getElectionEvent());
		country2.setId("32");
		createCountry(country2);

		// Tries to update country1 with the same id as country2
		country2.setId("31");
		getCountryServiceBean().update(rbacTestFixture.getUserData(), country2);
	}

	@Test
	public void testFindCountryById() {
		setCountry(buildCountry(getElectionEvent()));
		createCountry(getCountry());

		Country country = getCountryServiceBean().findCountryById(getElectionEvent().getPk(), getCountry().getId());

		Assert.assertNotNull(country);
		Assert.assertTrue(country.getId().equals(getCountry().getId()));
		Assert.assertTrue(getCountryServiceBean().findCountryById(getElectionEvent().getPk(), "88") == null);
	}

	@Test
	public void testFindCountryByIdNotFound() {
		Country country = getCountryServiceBean().findCountryById(getElectionEvent().getPk(), "X");

		Assert.assertNull(country);
	}
}

