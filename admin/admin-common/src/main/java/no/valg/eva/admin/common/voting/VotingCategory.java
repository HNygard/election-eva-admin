package no.valg.eva.admin.common.voting;

import no.valg.eva.admin.common.counting.model.CountCategory;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Comparator.comparing;

/**
 * Categories for voting.
 */
public enum VotingCategory {
    /**
     * Valgtingsstemmer
     */
    VO,
    /**
     * Fremmedstemmer
     */
    VF,
    /**
     * Stemmer i særskilt omslag
     */
    VS,
    /**
     * Stemmer i beredskapskonvolutt
     */
    VB,
    /**
     * Forhåndsstemmer innenriks
     */
    FI,
    /**
     * Forhåndsstemmer utenriks
     */
    FU,
    /**
     * Forhåndsstemmer til andre kommuner
     */
    FA,
    /**
     * Brevstemmer utenriks
     */
    FB,
    /**
     * Forhåndsstemmer i beredskapskonvolutt
     */
    FE;

    /**
     * Convenience method wrapping standard valueOf(String) method.
     *
     * @return CountCategory matching specified id
     */
    public static VotingCategory fromId(String id) {
        return VotingCategory.valueOf(id);
    }

    /**
     * @return an array of voting categories corresponding to the count category
     */
    public static VotingCategory[] from(CountCategory countCategory) {
        switch (countCategory) {
            case VO:
                return new VotingCategory[]{VO};
            case VF:
            case BF:
                return new VotingCategory[]{VF};
            case VS:
                return new VotingCategory[]{VS};
            case VB:
                return new VotingCategory[]{VB};
            case FO:
            case FS:
                return new VotingCategory[]{FI, FU, FB, FE};
            default:
                throw new IllegalArgumentException(format("Illegal count category: <%s>", countCategory));
        }
    }

    /**
     * @return text id for this category used in GUI
     * @deprecated Use {@link #getName(VotingPhase)}
     */
    @Deprecated
    public String getName() {
        return "@voting_category[" + name() + "].name";
    }

    /**
     * @return text id for this category used in GUI
     */
    public String getName(VotingPhase votingPhase) {
        return "@voting_category[" + votingPhase.name() + "_" + name() + "].name";
    }

    /**
     * Convenience method for getting the category's id wrapping standard name() method.
     *
     * @return the category's id mapped in the database
     */
    public String getId() {
        return name();
    }

    /**
     * @return an array of voting categories corresponding to the voting registration phase and election
     */
    public static VotingCategory[] from(VotingPhase votingPhase, boolean xim, boolean hasAccessToBoroughs) {
        verifyState(xim, hasAccessToBoroughs);

        switch (votingPhase) {
            case EARLY:
                return new VotingCategory[]{FI};
            case ADVANCE:
                return new VotingCategory[]{FI, FU, FB, FE};
            case ELECTION_DAY:
                if (hasAccessToBoroughs) {
                    return new VotingCategory[]{VS, VB};
                } else {
                    return new VotingCategory[]{VS, xim ? VB : VF};
                }
            case LATE:
                return new VotingCategory[]{FI};
            default:
                throw new IllegalArgumentException(format("Illegal voting registration phase: <%s>", votingPhase));
        }
    }

    private static void verifyState(boolean xim, boolean boroughLevelContest) {
        if (!xim && boroughLevelContest) {
            throw new IllegalStateException("Støtter ikke bydelsvalg med papirmantall!");
        }
    }

    public static boolean isElectionDayVotingCategory(VotingCategory votingCategory) {
        switch (votingCategory) {
            case VS:
            case VB:
            case VO:
            case VF:
                return true;
            case FI:
            case FB:
            case FU:
            case FE:
            case FA:
                return false;
            default:
                throw new IllegalArgumentException("VotingCategory " + votingCategory + " er ukjent!");
        }
    }

    public static boolean isAdvanceVotingCategory(VotingCategory votingCategory) {
        return !isElectionDayVotingCategory(votingCategory);
    }

    public static List<VotingCategory> votingCategoryList() {
        return EnumSet.allOf(VotingCategory.class).stream()
                .filter(votingCategory -> FA != votingCategory)
                .sorted(comparing(VotingCategory::getId))
                .collect(Collectors.toList());
    }
}
