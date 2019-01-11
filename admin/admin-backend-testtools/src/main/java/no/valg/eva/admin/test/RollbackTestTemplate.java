package no.valg.eva.admin.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;

/**
 * Template for writing code that rolls back, for use in tests.
 * <p/>
 * Uses persistence unit <code>{@value #PERSISTENCE_UNIT_NAME}</code>.
 * 
 * @see EntityManagerFactoryUtil details on how the EntityManagerFactory is created
 */
public abstract class RollbackTestTemplate {
	private static final String PERSISTENCE_UNIT_NAME = "evotePU";
	private static final Logger LOGGER = Logger.getLogger(RollbackTestTemplate.class);

	/**
	 * This method will be invoked from {@link #executeAndRollBack}, according to the <a href="http://en.wikipedia.org/wiki/Template_method_pattern">Template Method
	 * Pattern</a>
	 * 
	 * @param entityManager
	 *            the EntityManager will be injected.
	 */
	public abstract void doInTransaction(final EntityManager entityManager);

	/**
	 * Invoke this method to perform work that shall be rolled back.
	 */
	public final void executeAndRollBack() {
		EntityManagerFactory entityManagerFactory = new EntityManagerFactoryUtil().createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		try {
			entityManager.getTransaction().begin();
			doInTransaction(entityManager);
		} finally {
			EntityTransaction transaction = entityManager.getTransaction();
			try {
				if (transaction.isActive()) {
					transaction.rollback();
				}
			} catch (PersistenceException e) {
				LOGGER.error("Failed to roll back transaction", e);
			}
			entityManager.close();
		}
	}

}
