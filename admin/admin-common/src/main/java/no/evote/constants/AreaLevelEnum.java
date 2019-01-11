package no.evote.constants;

import java.util.Comparator;

public enum AreaLevelEnum {
    NONE(-1), ROOT(0), COUNTRY(1), COUNTY(2), MUNICIPALITY(3), BOROUGH(4), POLLING_DISTRICT(5), POLLING_PLACE(6), POLLING_STATION(7);

    private final int level;

    AreaLevelEnum(final int level) {
        this.level = level;
    }

    public static AreaLevelEnum getLevel(final int level) {
        for (AreaLevelEnum areaLevelEnum : AreaLevelEnum.values()) {
            if (level == areaLevelEnum.getLevel()) {
                return areaLevelEnum;
            }
        }
        return NONE;
    }

    public static Comparator<AreaLevelEnum> comparator() {
        return Comparator.comparing(AreaLevelEnum::getLevel);
    }

    public int getLevel() {
        return level;
    }

    /**
     * Confusing method name
     *
     * @deprecated use {@link #messageProperty()} ()} instead.
     */
    @Deprecated
    public String getName() {
        return messageProperty();
    }

    public String messageProperty() {
        return "@area_level[" + level + "].name";
    }

    public boolean lowerThan(AreaLevelEnum level) {
        return this.getLevel() > level.getLevel();
    }

    public boolean equalOrlowerThan(AreaLevelEnum level) {
        return this.getLevel() >= level.getLevel();
    }

    public String getLevelDescription() {
        return name().replace('_', ' ').toLowerCase();
    }
}
