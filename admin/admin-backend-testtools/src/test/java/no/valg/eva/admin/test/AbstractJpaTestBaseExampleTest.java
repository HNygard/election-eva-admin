package no.valg.eva.admin.test;

import static org.testng.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class AbstractJpaTestBaseExampleTest extends AbstractJpaTestBase {
	@Test
	public void smoketest() {
		EntityManager em = getEntityManager();
		Query query = em.createQuery("SELECT count(*) FROM Aarsakskode");
		Long result = (Long) query.getSingleResult();
		assertTrue(result > 0);
	}
}
