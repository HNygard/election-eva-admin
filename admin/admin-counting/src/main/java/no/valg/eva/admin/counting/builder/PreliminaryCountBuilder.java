package no.valg.eva.admin.counting.builder;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.counting.domain.model.ManualContestVoting;
import no.valg.eva.admin.counting.domain.service.votecount.PreliminaryCountDataProvider;
import no.valg.eva.admin.voting.domain.model.Voting;

import java.util.Collection;
import java.util.List;

public class PreliminaryCountBuilder extends CountBuilder<PreliminaryCount, PreliminaryCountBuilder> {

    public static PreliminaryCountBuilder create(
			CountCategory category,
			AreaPath areaPath,
			String areaName,
			String reportingUnitAreaName,
			boolean manualCount,
			boolean electronicMarkOffs,
			boolean requiredProtocolCount) {
        PreliminaryCountBuilder preliminaryCountBuilder = new PreliminaryCountBuilder(category, areaPath, areaName, reportingUnitAreaName, manualCount);
        preliminaryCountBuilder.getCount().setElectronicMarkOffs(electronicMarkOffs);
        preliminaryCountBuilder.getCount().setRequiredProtocolCount(requiredProtocolCount);

        return preliminaryCountBuilder;
    }

    private PreliminaryCountBuilder(CountCategory category, AreaPath areaPath, String areaName, String reportingUnitAreaName, boolean manualCount) {
        super(category, areaPath, areaName, reportingUnitAreaName, manualCount);
    }

	@Override
	protected PreliminaryCount initCount(
			CountCategory category,
			AreaPath areaPath,
			String areaName,
			String reportingUnitAreaName,
			boolean manualCount) {
		PreliminaryCount preliminaryCount = new PreliminaryCount(null, areaPath, category, areaName, reportingUnitAreaName, manualCount);
		preliminaryCount.setBlankBallotCount(0);
		return preliminaryCount;
	}

	@Override
	protected PreliminaryCountBuilder initSelf() {
		return this;
	}

	public PreliminaryCountBuilder applyDataProvider(PreliminaryCountDataProvider dataProvider) {
        getCount().setId(dataProvider.id());
        getCount().setVersion(dataProvider.version());
        getCount().setStatus(dataProvider.status());
        getCount().setComment(dataProvider.comment());
        getCount().setBlankBallotCount(dataProvider.blankBallotCount());
        getCount().setQuestionableBallotCount(dataProvider.questionableBallotCount());
        getCount().setBallotCounts(dataProvider.ballotCounts());
        getCount().setLateValidationCovers(dataProvider.lateValidationCovers());
        getCount().setAntallStemmesedlerLagtTilSideLagret(dataProvider.isAntallStemmesedlerLagtTilSideLagret());
        getCount().setExpectedBallotCount(dataProvider.expectedBallotCount());
        getCount().setMarkOffCount(dataProvider.markOffCount());
        getCount().setTotalBallotCountForOtherPollingDistricts(dataProvider.totalBallotCountForOtherPollingDistricts());
		return this;
	}

	public PreliminaryCountBuilder applyElectionDays(final List<ElectionDay> electionDays) {
        getCount().setDailyMarkOffCounts(new DailyMarkOffCounts());
		for (final ElectionDay electionDay : electionDays) {
            getCount().getDailyMarkOffCounts().add(new DailyMarkOffCount(electionDay.getDate()));
		}
		return this;
	}

	public PreliminaryCountBuilder applyManualContestVotings(List<ManualContestVoting> manualContestVotings) {
        getCount().setDailyMarkOffCounts(new DailyMarkOffCounts());
		for (ManualContestVoting manualContestVoting : manualContestVotings) {
			DailyMarkOffCount dailyMarkOffCount = new DailyMarkOffCount(manualContestVoting.getElectionDay().getDate());
			dailyMarkOffCount.setMarkOffCount(manualContestVoting.getVotings());
            getCount().getDailyMarkOffCounts().add(dailyMarkOffCount);
		}
		return this;
	}

	public PreliminaryCountBuilder applyVotings(Collection<Voting> votings) {
        return applyVotings(votings, getCount().getDailyMarkOffCounts());
	}

}
