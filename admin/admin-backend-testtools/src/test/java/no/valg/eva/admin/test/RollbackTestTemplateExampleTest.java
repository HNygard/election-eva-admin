package no.valg.eva.admin.test;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class RollbackTestTemplateExampleTest {
	@Test
	public void usingTemplate() {
		new RollbackTestTemplate() {
			@Override
			public void doInTransaction(final EntityManager entityManager) {
				Query query = entityManager.createQuery("SELECT count(*) FROM Aarsakskode");
				query.getResultList();
			}
		}.executeAndRollBack();
	}
}
