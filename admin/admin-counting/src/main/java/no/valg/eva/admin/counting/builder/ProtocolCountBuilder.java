package no.valg.eva.admin.counting.builder;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.voting.domain.model.Voting;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.util.LangUtil.zeroIfNull;

public class ProtocolCountBuilder extends CountBuilder<ProtocolCount, ProtocolCountBuilder> {
    private boolean foreignSpecialCoversEnabled;

    private ProtocolCountBuilder(AreaPath areaPath, String areaName, String reportingUnitAreaName, boolean manualCount) {
        super(VO, areaPath, areaName, reportingUnitAreaName, manualCount);
    }

    public static ProtocolCountBuilder create(AreaPath areaPath, String areaName, String reportingUnitAreaName, boolean electronicMarkOffs,
                                              boolean foreignSpecialCoversEnabled, boolean includeBallotCountForOtherContest, boolean manualCount) {

        ProtocolCountBuilder protocolCountBuilder = new ProtocolCountBuilder(areaPath, areaName, reportingUnitAreaName, manualCount);


        protocolCountBuilder.getCount().setElectronicMarkOffs(electronicMarkOffs);
        if (electronicMarkOffs) {
            protocolCountBuilder.getCount().setEmergencySpecialCovers(0);
        }
        protocolCountBuilder.foreignSpecialCoversEnabled = foreignSpecialCoversEnabled;
        if (foreignSpecialCoversEnabled) {
            protocolCountBuilder.getCount().setForeignSpecialCovers(0);
        }
        protocolCountBuilder.getCount().setQuestionableBallotCount(0);
        protocolCountBuilder.getCount().setSpecialCovers(0);
        if (includeBallotCountForOtherContest) {
            protocolCountBuilder.getCount().setBallotCountForOtherContests(0);
        } else {
            protocolCountBuilder.getCount().setBallotCountForOtherContests(null);
        }

        return protocolCountBuilder;
    }

    @Override
    protected ProtocolCount initCount(CountCategory category, AreaPath areaPath, String areaName, String reportingUnitAreaName, boolean manualCount) {
        return new ProtocolCount(null, areaPath, areaName, reportingUnitAreaName, manualCount);
    }

    @Override
    protected ProtocolCountBuilder initSelf() {
        return this;
    }

    public ProtocolCountBuilder applyProtocolVoteCount(VoteCount protocolVoteCount) {
        if (protocolVoteCount != null) {
            getCount().setStatus(CountStatus.fromId(protocolVoteCount.getVoteCountStatus().getId()));
            getCount().setId(protocolVoteCount.getId());
            getCount().setVersion(protocolVoteCount.getAuditOplock());

            CountCategory countCategoryFromProtocolVoteCount = CountCategory.fromId(protocolVoteCount.getVoteCountCategory().getId());
            sanityCheck(VO, countCategoryFromProtocolVoteCount,
                    "expected count category to be <%s> but was <%s> on protocol vote count entity");

            if (this.foreignSpecialCoversEnabled) {
                getCount().setForeignSpecialCovers(protocolVoteCount.getForeignSpecialCovers());
            }
            getCount().setSpecialCovers(zeroIfNull(protocolVoteCount.getSpecialCovers()));
            if (getCount().isElectronicMarkOffs()) {
                getCount().setEmergencySpecialCovers(protocolVoteCount.getEmergencySpecialCovers());
            }
            getCount().setComment(protocolVoteCount.getInfoText());

            setBlankOrdinaryAndQuestionable(protocolVoteCount);
        }
        return this;
    }

    private void setBlankOrdinaryAndQuestionable(final VoteCount protocolVoteCount) {
        // at møtebøkene for stemmestyret og valgstyret skal bli riktige
        if (protocolVoteCount.hasBlankBallotCount()) {
            getCount().setBlankBallotCount(protocolVoteCount.getBlankBallotCount());
        } else {
            getCount().setBlankBallotCount(null);
        }
        getCount().setOrdinaryBallotCount(protocolVoteCount.getApprovedBallots());
        getCount().setQuestionableBallotCount(protocolVoteCount.getRejectedBallots());
        Integer ballotsForOtherContests = protocolVoteCount.getBallotsForOtherContests();
        if (ballotsForOtherContests != null) {
            getCount().setBallotCountForOtherContests(ballotsForOtherContests);
        }
    }

    public ProtocolCountBuilder applyElectionDays(final List<ElectionDay> electionDays) {
        getCount().setDailyMarkOffCounts(new DailyMarkOffCounts());
        for (final ElectionDay electionDay : electionDays) {
            getCount().getDailyMarkOffCounts().add(new DailyMarkOffCount(electionDay.getDate()));
        }
        return this;
    }

    public ProtocolCountBuilder applyElectionDaysForOtherContests(List<ElectionDay> electionDays) {
        getCount().setDailyMarkOffCountsForOtherContests(new DailyMarkOffCounts());
        for (ElectionDay electionDay : electionDays) {
            getCount().getDailyMarkOffCountsForOtherContests().add(new DailyMarkOffCount(electionDay.getDate()));
        }
        return this;
    }

    public ProtocolCountBuilder applyVotings(Collection<Voting> votings) {
        return applyVotings(votings, getCount().getDailyMarkOffCounts());
    }

    public ProtocolCountBuilder applyVotingsForAnotherContest(Collection<Voting> votings) {
        return applyVotings(votings, getCount().getDailyMarkOffCountsForOtherContests());
    }

    public ProtocolCountBuilder applyManualContestVotings(final List<ManualContestVoting> manualContestVotings) {
        getCount().setDailyMarkOffCounts(new DailyMarkOffCounts());
        for (ManualContestVoting manualContestVoting : manualContestVotings) {
            DailyMarkOffCount dailyMarkOffCount = new DailyMarkOffCount(manualContestVoting.getElectionDay().getDate());
            dailyMarkOffCount.setMarkOffCount(manualContestVoting.getVotings());
            getCount().getDailyMarkOffCounts().add(dailyMarkOffCount);
        }
        return this;
    }

    /**
     * @throws java.lang.IllegalArgumentException when expected value is not equal to actual value
     */
    private void sanityCheck(final Object expected, final Object actual, final String msgFormat) {
        if (!expected.equals(actual)) {
            String msg = format(msgFormat, expected, actual);
            throw new IllegalArgumentException(msg);
        }
    }
}
