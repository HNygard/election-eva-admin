package no.valg.eva.admin.test;

import no.valg.eva.admin.backend.testtools.TestInitialContextFactory;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.transaction.TransactionSynchronizationRegistry;
import java.security.Security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Superclass for TestNG-based repository tests. Default behaviour is to run the test in a transaction and roll back. This may be changed by overriding
 * {@link #shallRunInTransaction()} and {@link #shallRollBackTransaction()}.
 * <p/>
 * Persistence unit <code>{@value #PERSISTENCE_UNIT_NAME}</code> is used.
 */
public abstract class AbstractJpaTestBase extends MockUtilsTestCase {
    private static final String PERSISTENCE_UNIT_NAME = "evotePU";
    private static EntityManagerFactory entityManagerFactory;
    private static final Logger LOGGER = Logger.getLogger(AbstractJpaTestBase.class);

    private EntityManager entityManager;

    @BeforeGroups(groups = {TestGroups.REPOSITORY})
    public void konfigurerBouncyCastleProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeSuite(groups = TestGroups.REPOSITORY)
    public static synchronized void initializeEntityManagerFactory() {
        entityManagerFactory = new EntityManagerFactoryUtil().createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }

	@BeforeMethod(groups = TestGroups.REPOSITORY)
    public void startTransaction() {
        entityManager = entityManagerFactory.createEntityManager();
        if (shallRunInTransaction()) {
            entityManager.getTransaction().begin();
        }
    }

    protected EntityManager getEntityManager() {
        if (entityManager == null) {
            throw new IllegalStateException("No EntityManager. Check your test's lifecycle!");
        }

        return entityManager;
    }

    /**
     * Override to change default behaviour, which is that the test runs in a transaction.
     */
    protected boolean shallRunInTransaction() {
        return true;
    }

    /**
     * Override to change default behaviour, which is that the test rolls back its transaction.
     */
    protected boolean shallRollBackTransaction() {
        return true;
    }

    @AfterMethod(alwaysRun = true)
    protected void rollbackTransaction() {
        if (entityManager == null) {
            return;
        }

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            if (transaction.isActive()) {
                if (shallRollBackTransaction()) {
                    transaction.rollback();
                } else {
                    transaction.commit();
                }
            }
        } catch (PersistenceException e) {
            LOGGER.error("Failed to " + (shallRollBackTransaction() ? "roll back" : "commit") + " transaction", e);
        }
        entityManager.close();
        entityManager = null;
    }

    @AfterSuite(alwaysRun = true)
    public void closeEntityManagerFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    protected void setupTransactionSynchronizationRegistry() throws NamingException {
        TransactionSynchronizationRegistry registry = mock(TransactionSynchronizationRegistry.class);
        when(TestInitialContextFactory.getContextMock().lookup("java:comp/TransactionSynchronizationRegistry")).thenReturn(registry);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestInitialContextFactory.class.getName());
    }
}
