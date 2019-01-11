package no.valg.eva.admin.common.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;

import java.io.Serializable;
import java.util.List;

import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;

@Builder
@AllArgsConstructor
@Data
public class UserMenuMetadata implements Serializable {
    private boolean accessToBoroughs;
    private boolean hasElectionsWithTypeProportionalRepresentation;
    private List<CountCategory> validVoteCountCategories;
    private boolean electronicMarkOffsConfigured;
    private boolean scanningEnabled;
    private ElectionPath electionPath;
    private AreaPath areaPath;

    public boolean hasMinimumMunicipalityAndElectionGroup() {
        return hasMinAreaLevel(MUNICIPALITY) && hasMinElectionLevel(ELECTION_GROUP);
    }

    public boolean hasMinAreaLevel(AreaLevelEnum level) {
        return getAreaPath().getLevel().getLevel() <= level.getLevel();
    }

    private boolean hasMinElectionLevel(ElectionLevelEnum level) {
        return getElectionPath().getLevel().getLevel() <= level.getLevel();
    }

    public boolean isValidCountCategoryId(CountCategory countCategory) {
        return getValidVoteCountCategories().contains(countCategory);
    }

    public boolean hasAccessToBoroughs() {
        return accessToBoroughs;
    }

    public boolean hasElectionsWithTypeProportionalRepresentation() {
        return hasElectionsWithTypeProportionalRepresentation;
    }
}
