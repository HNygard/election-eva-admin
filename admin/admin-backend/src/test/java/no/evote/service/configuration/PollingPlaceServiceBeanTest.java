package no.evote.service.configuration;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.test.TestGroups;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;


@Test(groups = { TestGroups.REPOSITORY })
public class PollingPlaceServiceBeanTest extends AreaBaseTest {
	private static final String PD1 = "0501";
	private static final String PD2 = "0502";
	private MvAreaRepository mvAreaRepository;
	private PollingPlaceRepository pollingPlaceRepository;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		setElectionEvent(buildElectionEvent());
		createElectionEvent(getElectionEvent());

		setCountry(buildCountry(getElectionEvent()));
		createCountry(getCountry());

		setCounty(buildCounty(getCountry()));
		createCounty(getCounty());

		setMunicipality(buildMunicipality(getCounty()));
		createMunicipality(getMunicipality());

		setBorough(buildBorough(getMunicipality()));
		createBorough(getBorough());

		setPollingDistrict(buildPollingDistrict(getBorough()));
		createPollingDistrict(getPollingDistrict());

		mvAreaRepository = backend.getMvAreaRepository();
		pollingPlaceRepository = backend.getPollingPlaceRepository();
	}

	@Test
	public void testCreate() {
		setPollingPlace(buildPollingPlace(getPollingDistrict()));

		pollingPlaceApplicationService.create(rbacTestFixture.getUserData(), getPollingPlace());

		Assert.assertTrue(getPollingPlace().getPk() != null);
		Assert.assertTrue(getPollingPlace().getPk() > 0);
	}

	@Test(expectedExceptions = ConstraintViolationException.class)
	public void testCreateIdTooLong() {
		setPollingPlace(buildPollingPlace(getPollingDistrict()));
		getPollingPlace().setId("12345");

		pollingPlaceApplicationService.create(rbacTestFixture.getUserData(), getPollingPlace());
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testCreateDuplicateId() {
		setPollingPlace(buildPollingPlace(getPollingDistrict()));
		pollingPlaceApplicationService.create(rbacTestFixture.getUserData(), getPollingPlace());

		// Tries to insert duplicate
		pollingPlaceApplicationService.create(rbacTestFixture.getUserData(), buildPollingPlace(getPollingDistrict()));
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testCreateDuplicateElectionDayVoting() {
		PollingPlace pollingPlace1 = buildPollingPlace(getPollingDistrict());
		pollingPlace1.setId(PD1);
		pollingPlace1.setElectionDayVoting(true);
		createPollingPlace(pollingPlace1);
		PollingPlace pollingPlace2 = buildPollingPlace(getPollingDistrict());
		pollingPlace2.setId(PD2);
		pollingPlace2.setElectionDayVoting(true);

		// Tries to insert duplicate
		pollingPlaceApplicationService.create(rbacTestFixture.getUserData(), pollingPlace2);
	}

	@Test
	public void testUpdate() {
		setPollingPlace(buildPollingPlace(getPollingDistrict()));
		createPollingPlace(getPollingPlace());
		String updatedName = "updatedName";
		getPollingPlace().setName(updatedName);

		PollingPlace updatedPollingPlace = pollingPlaceRepository.update(rbacTestFixture.getUserData(), getPollingPlace());

		Assert.assertNotNull(updatedPollingPlace);
		Assert.assertTrue(updatedPollingPlace.getName().equals(updatedName));
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testUpdateDuplicateId() {
		PollingPlace pollingPlace1 = buildPollingPlace(getPollingDistrict());
		pollingPlace1.setId(PD1);
		createPollingPlace(pollingPlace1);
		PollingPlace pollingPlace2 = buildPollingPlace(getPollingDistrict());
		pollingPlace2.setId(PD2);
		createPollingPlace(pollingPlace2);
		pollingPlace2.setId(PD1); // Tries to update pollingPlace1 with the same id as pollingPlace2

		pollingPlaceRepository.update(rbacTestFixture.getUserData(), pollingPlace2);
	}

	@Test(expectedExceptions = PersistenceException.class)
	public void testUpdateDuplicateElectionDayVoting() {
		PollingPlace pollingPlace1 = buildPollingPlace(getPollingDistrict());
		pollingPlace1.setId(PD1);
		pollingPlace1.setElectionDayVoting(true);
		createPollingPlace(pollingPlace1);
		PollingPlace pollingPlace2 = buildPollingPlace(getPollingDistrict());
		pollingPlace2.setId(PD2);
		pollingPlace2.setElectionDayVoting(false);
		createPollingPlace(pollingPlace2);
		pollingPlace2.setElectionDayVoting(true);

		pollingPlaceRepository.update(rbacTestFixture.getUserData(), pollingPlace2);
	}

	@Test
	public void testFindPollingPlaceById() {
		setPollingPlace(buildPollingPlace(getPollingDistrict()));
		createPollingPlace(getPollingPlace());

		PollingPlace pollingPlace = pollingPlaceRepository.findPollingPlaceById(getPollingDistrict().getPk(), getPollingPlace().getId());

		Assert.assertNotNull(pollingPlace);
		Assert.assertTrue(pollingPlace.getId().equals(getPollingPlace().getId()));
	}

	@Test
	public void testFindPollingPlaceByIdNotFound() {
		PollingPlace pollingPlace = pollingPlaceRepository.findPollingPlaceById(getPollingDistrict().getPk(), "X");

		Assert.assertNull(pollingPlace);
	}

	@Test
	public void testStoreValidInfoText() {
		PollingPlace pollingPlace = createValidPollingPlace("1992");
		pollingPlace.setInfoText("test valid data");
		pollingPlace = pollingPlaceRepository.create(rbacTestFixture.getUserData(), pollingPlace);
		try {
			Assert.assertEquals(pollingPlace.getInfoText(), "test valid data");
		} finally {
			pollingPlaceRepository.delete(rbacTestFixture.getUserData(), pollingPlace.getPk());
		}
	}

	@Test
	public void testStoreXSSInfoText() {
		PollingPlace pollingPlace = createValidPollingPlace("1992");
		pollingPlace.setInfoText("<script>alert('X')</script>");
		pollingPlace = pollingPlaceRepository.create(rbacTestFixture.getUserData(), pollingPlace);
		try {
			Assert.assertEquals(pollingPlace.getInfoText(), "");
		} finally {
			pollingPlaceRepository.delete(rbacTestFixture.getUserData(), pollingPlace.getPk());
		}
	}

	private PollingPlace createValidPollingPlace(final String id) {
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setId(id);
		pollingPlace.setPostalCode("0000");
		pollingPlace.setPollingDistrict(getPollingDistrict());
		pollingPlace.setName("pollingplacename");

		return pollingPlace;
	}

	public MvAreaRepository getMvAreaRepository() {
		return mvAreaRepository;
	}
}

