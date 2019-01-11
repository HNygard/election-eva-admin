package no.valg.eva.admin.common.counting.constants;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ResponsibilityId {
    LEDER("LEDR"),
    MEDLEM("MEDL"),
    NESTLEDER("NEST"),
    SEKRETAER("SEKR"),
    VARAMEDLEM("VARA");

    private final String id;

    private static final Map<String, ResponsibilityId> RESPONSIBILITY_ID_MAP = new HashMap<>();

    static {
        for (ResponsibilityId responsibilityId : EnumSet.allOf(ResponsibilityId.class)) {
            RESPONSIBILITY_ID_MAP.put(responsibilityId.getId(), responsibilityId);
        }
    }

    ResponsibilityId(final String id) {
        this.id = id;
    }

    public static ResponsibilityId fromId(String id) {
        if (RESPONSIBILITY_ID_MAP.get(id) == null) {
            throw new IllegalStateException("No enum value for id: " + id);
        }

        return RESPONSIBILITY_ID_MAP.get(id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return "@responsibility[" + getId() + "].name";
    }

    public static boolean isUniqueResponsibility(ResponsibilityId responsibilityId) {
        return LEDER.equals(responsibilityId)
                || NESTLEDER.equals(responsibilityId)
                || SEKRETAER.equals(responsibilityId);
    }
    
    public static List<ResponsibilityId> list() {
        return new ArrayList<>(RESPONSIBILITY_ID_MAP.values());
    }
}
