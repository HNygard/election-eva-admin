package no.evote.service.configuration;

import java.util.List;

import javax.persistence.PersistenceException;

import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.REPOSITORY })
public class CountyServiceTest extends AreaBaseTest {
	@BeforeMethod(alwaysRun = true)
	public void init() {
		// Creates election event
		setElectionEvent(buildElectionEvent());
		createElectionEvent(getElectionEvent());

		// Creates country
		setCountry(buildCountry(getElectionEvent()));
		createCountry(getCountry());
	}

	@Test
	public void testCreate() {
		setCounty(buildCounty(getCountry()));

		getCountyServiceBean().create(rbacTestFixture.getUserData(), getCounty());

		Assert.assertTrue(getCounty().getPk() != null);
		Assert.assertTrue(getCounty().getPk() > 0);
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testCreateDuplicateId() {
		setCounty(buildCounty(getCountry()));
		getCountyServiceBean().create(rbacTestFixture.getUserData(), getCounty());

		County dupe = buildCounty(getCountry());
		getCountyServiceBean().create(rbacTestFixture.getUserData(), dupe);
	}

	@Test
	public void testUpdate() {
		setCounty(buildCounty(getCountry()));
		createCounty(getCounty());
		String updatedName = "updatedName";
		getCounty().setName(updatedName);

		County updatedCounty = getCountyRepository().update(rbacTestFixture.getUserData(), getCounty());

		Assert.assertNotNull(updatedCounty);
		Assert.assertTrue(updatedCounty.getName().equals(updatedName));
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testUpdateDuplicateId() {
		County county1 = buildCounty(getCountry());
		county1.setId("01");
		createCounty(county1);

		County county2 = buildCounty(getCountry());
		county2.setId("02");
		createCounty(county2);

		county2.setId("01");
		getCountyRepository().update(rbacTestFixture.getUserData(), county2);
	}

	@Test
	public void testFindCountyById() {
		setCounty(buildCounty(getCountry()));
		createCounty(getCounty());

		County county = getCountyServiceBean().findCountyById(getCountry().getPk(), getCounty().getId());

		Assert.assertNotNull(county);
		Assert.assertTrue(county.getId().equals(getCounty().getId()));
		Assert.assertTrue(getCountyServiceBean().findCountyById(getCountry().getPk(), "88") == null);
	}

	@Test
	public void testFindCountyByPk() {
		setCounty(buildCounty(getCountry()));
		createCounty(getCounty());

		County county = getCountyRepository().findByPk(getCounty().getPk());

		Assert.assertNotNull(county);
		Assert.assertTrue(county.getId().equals(getCounty().getId()));
	}

	@Test
	public void shouldBeNoCountiesWithoutMunicipalities() {
		Assert.assertEquals(getCountyServiceBean().getCountiesWithoutMunicipalities(getElectionEvent().getPk()).size(), 0);
	}

	@Test
	public void countyShouldNotHaveMunicipalities() {
		setCounty(buildCounty(getCountry()));

		List<Country> countries = getCountryService().getCountriesWithoutCounties(getElectionEvent().getPk());
		Assert.assertEquals(countries.size(), 1);

		createCounty(getCounty());

		countries = getCountryService().getCountriesWithoutCounties(getElectionEvent().getPk());
		Assert.assertEquals(countries.size(), 0);

		Assert.assertEquals(getCountyServiceBean().getCountiesWithoutMunicipalities(getElectionEvent().getPk()).size(), 1);
	}

	@Test
	public void testFindCountyByIdNotFound() {
		County county = getCountyServiceBean().findCountyById(getCountry().getPk(), "X");

		Assert.assertNull(county);
	}
}
