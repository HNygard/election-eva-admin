package no.evote.service.configuration;

import java.util.List;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.REPOSITORY })
public class PollingDistrictServiceTest extends AreaBaseTest {

	@BeforeMethod(alwaysRun = true)
	public void init() {
		setElectionEvent(buildElectionEvent());
		getElectionEvent().getElectionGroups().add(buildElectionGroup());
		createElectionEvent(getElectionEvent());

		setCountry(buildCountry(getElectionEvent()));
		createCountry(getCountry());

		setCounty(buildCounty(getCountry()));
		createCounty(getCounty());

		setMunicipality(buildMunicipality(getCounty()));
		createMunicipality(getMunicipality());

		setBorough(buildBorough(getMunicipality()));
		createBorough(getBorough());
	}

	@Test
	public void testCreate() {
		setPollingDistrict(buildPollingDistrict(getBorough()));

		getPollingDistrictRepository().create(rbacTestFixture.getUserData(), getPollingDistrict());

		Assert.assertTrue(getPollingDistrict().getPk() != null);
		Assert.assertTrue(getPollingDistrict().getPk() > 0);
	}

	@Test
	public void pollingDistrictsShouldNotHaveVoters() {
		setPollingDistrict(buildPollingDistrict(getBorough()));
		Assert.assertFalse(getBoroughService().getBoroughsWithoutPollingDistricts(getElectionEvent().getPk()).isEmpty());

		getPollingDistrictRepository().create(rbacTestFixture.getUserData(), getPollingDistrict());
		Assert.assertTrue(getBoroughService().getBoroughsWithoutPollingDistricts(getElectionEvent().getPk()).isEmpty());
		Assert.assertFalse(getPollingDistrictRepository().getPollingDistrictsWithoutVoters(getElectionEvent().getPk()).isEmpty());
	}

	@Test(expectedExceptions = ConstraintViolationException.class)
	public void testCreateIdTooLong() {
		setPollingDistrict(buildPollingDistrict(getBorough()));
		getPollingDistrict().setId("12345");

		getPollingDistrictRepository().create(rbacTestFixture.getUserData(), getPollingDistrict());
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testCreateDuplicateId() {
		setPollingDistrict(buildPollingDistrict(getBorough()));
		getPollingDistrictRepository().create(rbacTestFixture.getUserData(), getPollingDistrict());

		getPollingDistrictRepository().create(rbacTestFixture.getUserData(), buildPollingDistrict(getBorough()));
	}

	@Test
	public void testUpdate() {
		setPollingDistrict(buildPollingDistrict(getBorough()));
		createPollingDistrict(getPollingDistrict());
		String updatedName = "updatedName";
		getPollingDistrict().setName(updatedName);

		PollingDistrict updatedPollingDistrict = getPollingDistrictRepository().update(rbacTestFixture.getUserData(), getPollingDistrict());

		Assert.assertNotNull(updatedPollingDistrict);
		Assert.assertTrue(updatedPollingDistrict.getName().equals(updatedName));
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testUpdateDuplicateId() {
		PollingDistrict pollingDistrict1 = buildPollingDistrict(getBorough());
		pollingDistrict1.setId("2210");
		createPollingDistrict(pollingDistrict1);
		PollingDistrict pollingDistrict2 = buildPollingDistrict(getBorough());
		pollingDistrict2.setId("2211");
		createPollingDistrict(pollingDistrict2);

		pollingDistrict2.setId("2210");
		getPollingDistrictRepository().update(rbacTestFixture.getUserData(), pollingDistrict2);
	}

	@Test
	public void testFindPollingDistrictById() {
		setPollingDistrict(buildPollingDistrict(getBorough()));
		createPollingDistrict(getPollingDistrict());

		PollingDistrict pollingDistrict = getPollingDistrictRepository().findPollingDistrictById(getBorough().getPk(), getPollingDistrict().getId());

		Assert.assertNotNull(pollingDistrict);
		Assert.assertTrue(pollingDistrict.getId().equals(getPollingDistrict().getId()));
	}

	@Test
	public void testFindPollingDistrictByIdNotFound() {
		PollingDistrict pollingDistrict = getPollingDistrictRepository().findPollingDistrictById(getBorough().getPk(), "X");

		Assert.assertNull(pollingDistrict);
	}

	@Test
	public void testFindPollingDistrictsForParent() {
		// Creates a parent polling district
		PollingDistrict pollingDistrict1 = buildPollingDistrict(getBorough());
		pollingDistrict1.setId("2207");
		pollingDistrict1.setParentPollingDistrict(true);
		createPollingDistrict(pollingDistrict1);

		// Creates two polling districts within parent
		PollingDistrict pollingDistrict2 = buildPollingDistrict(getBorough());
		pollingDistrict2.setId("2208");
		pollingDistrict2.setPollingDistrict(pollingDistrict1);
		createPollingDistrict(pollingDistrict2);

		PollingDistrict pollingDistrict3 = buildPollingDistrict(getBorough());
		pollingDistrict3.setId("2209");
		pollingDistrict3.setPollingDistrict(pollingDistrict1);
		createPollingDistrict(pollingDistrict3);

		List<PollingDistrict> pollingDistrictsForParentList = getPollingDistrictRepository().findPollingDistrictsForParent(pollingDistrict1);

		Assert.assertNotNull(pollingDistrictsForParentList);
		Assert.assertEquals(pollingDistrictsForParentList.size(), 2);
		for (PollingDistrict pollingDistrict : pollingDistrictsForParentList) {
			Assert.assertEquals(pollingDistrict.isParentPollingDistrict(), false);
			Assert.assertNotNull(pollingDistrict.getPollingDistrict());
			Assert.assertEquals(pollingDistrict.getPollingDistrict().getPk(), pollingDistrict1.getPk());
		}

		// Cleanup
		getPollingDistrictRepository().deleteParentPollingDistrict(rbacTestFixture.getUserData(), pollingDistrict1);
	}

	@Test
	public void testDeleteParentPollingDistrict() {
		// Creates a parent polling district
		PollingDistrict pollingDistrict1 = buildPollingDistrict(getBorough());
		pollingDistrict1.setId("2201");
		pollingDistrict1.setParentPollingDistrict(true);
		createPollingDistrict(pollingDistrict1);

		// Creates two polling districts within parent
		PollingDistrict pollingDistrict2 = buildPollingDistrict(getBorough());
		pollingDistrict2.setId("2202");
		pollingDistrict2.setPollingDistrict(pollingDistrict1);
		createPollingDistrict(pollingDistrict2);

		PollingDistrict pollingDistrict3 = buildPollingDistrict(getBorough());
		pollingDistrict3.setId("2203");
		pollingDistrict3.setPollingDistrict(pollingDistrict1);
		createPollingDistrict(pollingDistrict3);

		// Deletes parent polling district
		getPollingDistrictRepository().deleteParentPollingDistrict(rbacTestFixture.getUserData(), pollingDistrict1);

		// Retrives the updated polling districts
		pollingDistrict1 = getPollingDistrictRepository().findByPk(pollingDistrict1.getPk());
		pollingDistrict2 = getPollingDistrictRepository().findByPk(pollingDistrict2.getPk());
		pollingDistrict3 = getPollingDistrictRepository().findByPk(pollingDistrict3.getPk());

		Assert.assertNull(pollingDistrict1);
		Assert.assertNotNull(pollingDistrict2);
		Assert.assertNull(pollingDistrict2.getPollingDistrict());
		Assert.assertNotNull(pollingDistrict3);
		Assert.assertNull(pollingDistrict3.getPollingDistrict());
	}

	@Test
	public void testGetMunicipalityProxy() {
		PollingDistrict pollingDistrict1 = buildPollingDistrict(getBorough());
		pollingDistrict1.setMunicipality(false);
		getPollingDistrictRepository().create(rbacTestFixture.getUserData(), pollingDistrict1);
		Assert.assertFalse(getPollingDistrictRepository().municipalityProxyExists(getMunicipality().getPk()));

		pollingDistrict1.setMunicipality(true);
		getPollingDistrictRepository().update(rbacTestFixture.getUserData(), pollingDistrict1);
		Assert.assertTrue(getPollingDistrictRepository().municipalityProxyExists(getMunicipality().getPk()));
	}

	private ElectionGroup buildElectionGroup() {
		ElectionGroup electionGroup = new ElectionGroup();
		electionGroup.setElectionEvent(getElectionEvent());
		electionGroup.setId("01");
		electionGroup.setName(getElectionEvent().getName());
		electionGroup.setAdvanceVoteInBallotBox(true);
		electionGroup.setElectronicMarkoffs(true);
		return electionGroup;
	}
}
