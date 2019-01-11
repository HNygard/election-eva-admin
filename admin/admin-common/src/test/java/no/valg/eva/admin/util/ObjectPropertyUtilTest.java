package no.valg.eva.admin.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ObjectPropertyUtilTest {
    
    private final String[] allProperties = new String[]{"name", "age", "strength", "height", "length", "doesExercise", "shouldExercise", "currentHobbies", "hobbiesForTheFuture"};
    
    private ObjectPropertyUtilTestClass instance;
    private Map<String, Object> valueMap;
    

    @BeforeMethod
    public void setUp() {
        instance = null; valueMap = null;
    }

    @Test
    public void resolveAllKnownPropertiesWithoutValuesGivesCorrespondingMap() {
        givenEmptyInstance();
        whenPropertiesRetrieved(allProperties);
        thenAllPropertiesShouldBeInMap();
    }
    
    private void givenEmptyInstance() {
        this.instance = new ObjectPropertyUtilTestClass();
    }
    
    private void whenPropertiesRetrieved(String... props) {
        this.valueMap = ObjectPropertyUtil.resolvePropertyValues(instance, props);
    }
    
    private void thenAllPropertiesShouldBeInMap() {
        assertEquals(valueMap.size(), allProperties.length);
        for (String prop : allProperties) {
            assertTrue(valueMap.keySet().contains(prop));
        }
    }

    @Test
    public void resolveTwoKnownPropertiesWithValuesGivesCorrespondingMapWithValues() {
        givenInstanceWithValues();
        whenPropertiesRetrieved("name", "currentHobbies");
        thenTwoPropertiesShouldBeInMap();
    }
    
    private void givenInstanceWithValues() {
        this.instance = new ObjectPropertyUtilTestClass("Per R. Johnson", 32, 105, 198, 891L, false, true, new String[]{"car searching", "scuba jumping", "reading"}, asList("lying", "dying", "heavy liftin'"), new HashMap<>());
    }
    
    private void thenTwoPropertiesShouldBeInMap() {
        assertEquals(valueMap.size(), 2);
        assertEquals(valueMap.get("name"), "Per R. Johnson");
        
        String[] expectedCurrentHobbies = new String[]{"car searching", "scuba jumping", "reading"};
        String[] currentHobbies = (String[]) valueMap.get("currentHobbies");
        assertEquals(currentHobbies.length, expectedCurrentHobbies.length);
        for (int i = 0; i < expectedCurrentHobbies.length; i++) {
            assertEquals(currentHobbies[i], expectedCurrentHobbies[i]);
        }
    }

    @Test
    public void resolveTwoKnownAndOneUnknownGivesMapWithTwoValues() {
        givenInstanceWithValues();
        whenPropertiesRetrieved("name", "currentHobbies", "unknownPropertyInClass");
        thenTwoPropertiesShouldBeInMap();
    }
    
    @Test
    public void resolveThreeKnownWhereOneHasNoGetterMethodGivesMapWithTwoValues() {
        givenInstanceWithValues();
        whenPropertiesRetrieved("name", "currentHobbies", "secretWithNumberMap");
        thenTwoPropertiesShouldBeInMap();
    }
}