package no.valg.eva.admin.frontend.i18n;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static no.valg.eva.admin.frontend.i18n.ResourceBundleManager.EVA_MESSAGES_BUNDLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ResourceBundleManagerTest {
    
    private ResourceBundleManager resourceBundleManager;

    @BeforeMethod
    public void setUp() {
        resourceBundleManager = new ResourceBundleManager();
    }

    @Test
    public void testGetBundle_givenValidBundle_verifiesBundleLoaded() {
        ResourceBundle resourceBundle = resourceBundleManager.getBundle(new Locale("nb", "NO"));
        assertEquals(resourceBundle.getBaseBundleName(), EVA_MESSAGES_BUNDLE);
        assertTrue(resourceBundle.getKeys().hasMoreElements());
    }
}