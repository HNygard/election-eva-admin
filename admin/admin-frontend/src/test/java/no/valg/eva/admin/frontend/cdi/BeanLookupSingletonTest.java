package no.valg.eva.admin.frontend.cdi;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.frontend.configuration.ctrls.local.LocalConfigurationController;
import org.testng.annotations.Test;

public class BeanLookupSingletonTest {

    @Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Error looking up bean.*")
    public void testLookup() {
        BeanLookupSingleton beanLookupSingleton = new BeanLookupSingleton();
        beanLookupSingleton.lookup(LocalConfigurationController.class);
    }
}