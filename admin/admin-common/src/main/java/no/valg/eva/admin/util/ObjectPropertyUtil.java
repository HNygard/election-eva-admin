package no.valg.eva.admin.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Simple util that assumes default security policy and argument properties has corresponding public 'get'-method for retrieving its values (ref lombok usage) 

public class ObjectPropertyUtil {

    private ObjectPropertyUtil() {
    }

    /**
     * Resolve values from an instances properties using reflection
     *
     * @param target
     *            the instance to examine
     * @param properties
     *            the list of properties to which its values should be resolved
     * @return a map of the properties that was resolved along with its corresponding value 
     */
    public static Map<String, Object> resolvePropertyValues(Object target, String... properties) {
        final Map<String, Object> valueMap = new HashMap<>();

        if (target != null && properties != null) {
            for (String property : properties) {
                try {

                    Class objectClass = target.getClass();
                    Class fieldClass = objectClass.getDeclaredField(property).getType();

                    Object value = objectClass.getMethod(keywordPrefixForClassType(fieldClass) + 
                            property.substring(0, 1).toUpperCase() + property.substring(1)).invoke(target);
                    valueMap.put(property, value);
                    
                } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException swallow) {
                }
            }
        }
        return valueMap;
    }
    
    private static String keywordPrefixForClassType(Class clazz) {
        return clazz.isPrimitive() && clazz.getName().equals(Boolean.TYPE.getName()) ? "is" : "get";
    }

    public static Map<String, Object> resolvePropertyValues(Object target, List<String> properties) {
        final String[] props = properties != null ? properties.stream().toArray(String[]::new) : new String[0];
        return resolvePropertyValues(target, props);
    }
}
