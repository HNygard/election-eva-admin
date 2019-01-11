package no.valg.eva.admin.felles.valghierarki.model;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.felles.model.Nivaa;

public enum ValghierarkiNivaa implements Nivaa {
    VALGHENDELSE(0),
    VALGGRUPPE(1),
    VALG(2),
    VALGDISTRIKT(3);

    private final int nivaa;

    private static final Map<Integer, ValghierarkiNivaa> ELECTION_HIERARCHY_LEVEL_MAP = new HashMap<>();

    static {
        for (ValghierarkiNivaa valghierarkiNivaa : EnumSet.allOf(ValghierarkiNivaa.class)) {
            ELECTION_HIERARCHY_LEVEL_MAP.put(valghierarkiNivaa.nivaa, valghierarkiNivaa);
        }
    }

    ValghierarkiNivaa(int nivaa) {
        this.nivaa = nivaa;
    }

    public static ValghierarkiNivaa fra(ElectionLevelEnum electionLevelEnum) {
        return fra(electionLevelEnum.getLevel());
    }

    public static ValghierarkiNivaa fra(int electionHierarchyLevelId) {

        ValghierarkiNivaa electionHierarchyLevel = ELECTION_HIERARCHY_LEVEL_MAP.get(electionHierarchyLevelId);
        if (electionHierarchyLevel == null) {
            throw new IllegalArgumentException("Unknown electionHierarchyLevel: " + electionHierarchyLevelId);
        }

        return electionHierarchyLevel;
    }

    public static List<ValghierarkiNivaa> listIncluding(ValghierarkiNivaa electionHierarchyLevel) {
        if (electionHierarchyLevel == null) {
            throw new IllegalArgumentException("Argument electionHierarchyLevel is null");
        }

        int listToIndex = electionHierarchyLevel.nivaa + 1;

        return electionHierarchyLevel == VALGHENDELSE ?
                singletonList(ValghierarkiNivaa.fra(VALGHENDELSE.nivaa))
                : new ArrayList<>(ELECTION_HIERARCHY_LEVEL_MAP.values()).subList(VALGHENDELSE.nivaa, listToIndex);

    }

    public int nivaa() {
        return nivaa;
    }

    public String id() {
        return name();
    }

    public String visningsnavn() {
        return "@election_level[" + nivaa + "].name";
    }

    public ElectionLevelEnum tilElectionLevelEnum() {
        return ElectionLevelEnum.getLevel(nivaa);
    }
}
