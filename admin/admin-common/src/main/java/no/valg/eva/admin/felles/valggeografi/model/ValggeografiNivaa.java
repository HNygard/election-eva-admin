package no.valg.eva.admin.felles.valggeografi.model;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.felles.model.Nivaa;

import lombok.NonNull;

public enum ValggeografiNivaa implements Nivaa {
    VALGHENDELSE(0),
    LAND(1),
    FYLKESKOMMUNE(2),
    KOMMUNE(3),
    BYDEL(4),
    STEMMEKRETS(5),
    STEMMESTED(6),
    RODE(7);

    private static final Map<Integer, ValggeografiNivaa> areaLevelMap = new HashMap<>();

    static {
        for (ValggeografiNivaa valggeografiNivaa : EnumSet.allOf(ValggeografiNivaa.class)) {
            areaLevelMap.put(valggeografiNivaa.nivaa, valggeografiNivaa);
        }
    }

    private final int nivaa;

    ValggeografiNivaa(int nivaa) {
        this.nivaa = nivaa;
    }

    public static ValggeografiNivaa fra(AreaLevelEnum areaLevelEnum) {
        return fra(areaLevelEnum.getLevel());
    }

    public static ValggeografiNivaa fra(int areaLevel) {
        verifyValidLevel(areaLevel);
        return areaLevelMap.get(areaLevel);
    }

    private static void verifyValidLevel(int areaLevel) {
        if (areaLevel < VALGHENDELSE.nivaa || areaLevel > RODE.nivaa) {
            throw new IllegalArgumentException(format("Specified area level %d is outside valid range (%d to %d)", areaLevel, VALGHENDELSE.nivaa, RODE.nivaa));
        }
    }

    public static List<ValggeografiNivaa> listIncluding(@NonNull ValggeografiNivaa areaLevel) {
        return sublistIncluding(areaLevel);
    }

    private static List<ValggeografiNivaa> sublistIncluding(ValggeografiNivaa areaLevel) {
        List<ValggeografiNivaa> allAreaLevels = new ArrayList<>(areaLevelMap.values());

        int listToIndex = areaLevel.nivaa + 1;

        return isHighestAreaLevel(listToIndex) ? highestAreaLevelAsList()
                : sublistIncluding(allAreaLevels, listToIndex);
    }

    private static boolean isHighestAreaLevel(int toElectionGeoLevelIndex) {
        return toElectionGeoLevelIndex == VALGHENDELSE.nivaa;
    }

    private static List<ValggeografiNivaa> sublistIncluding(List<ValggeografiNivaa> allAreaLevels, int toAreaLevel) {
        return allAreaLevels.subList(VALGHENDELSE.nivaa, toAreaLevel);
    }

    private static List<ValggeografiNivaa> highestAreaLevelAsList() {
        return singletonList(ValggeografiNivaa.fra(VALGHENDELSE.nivaa));
    }

    public int nivaa() {
        return nivaa;
    }

    public String id() {
        return name();
    }

    public String visningsnavn() {
        return "@area_level[" + nivaa + "].name";
    }

    public boolean isLowerOrBelow(ValggeografiNivaa valggeografiNivaa) {
        return this.nivaa >= valggeografiNivaa.nivaa;
    }

    public AreaLevelEnum tilAreaLevelEnum() {
        return AreaLevelEnum.getLevel(nivaa);
    }
}
