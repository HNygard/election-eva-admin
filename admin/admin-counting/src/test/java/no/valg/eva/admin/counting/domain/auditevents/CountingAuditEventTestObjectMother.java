package no.valg.eva.admin.counting.domain.auditevents;

import static java.lang.System.arraycopy;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.UNMODIFIED;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;


public final class CountingAuditEventTestObjectMother {
	private static final int BASE_ELECTION_EVENT_ID = 111111;
	private static final int BASE_COUNTRY_ID = 11;
	private static final int BASE_COUNTY_ID = 11;
	private static final int BASE_MUNICIPALITY_ID = 1111;
	private static final int BASE_BOROUGH_ID = 111111;
	private static final int BASE_POLLING_DISTRICT_ID = 1111;
	private static final int BASE_CONTEST_ID = 111111;
	private static final int BASE_ELECTION_ID = 11;
	private static final int BASE_ELECTION_GROUP_ID = 11;

	private CountingAuditEventTestObjectMother() {
	}

	public static ContestReport contestReport(ContestReportConfig config) {
		return contestReport(config, mock(ContestReport.class, RETURNS_DEEP_STUBS));
	}

	public static ContestReport contestReport(ContestReportConfig config, ContestReport contestReport) {
		reportingUnit(contestReport.getReportingUnit());
		contest(contestReport.getContest());
		return contestReport;
	}

	public static ReportingUnit reportingUnit() {
		ReportingUnit reportingUnit = mock(ReportingUnit.class, RETURNS_DEEP_STUBS);
		return reportingUnit(reportingUnit);
	}

	private static ReportingUnit reportingUnit(ReportingUnit reportingUnit) {
		when(reportingUnit.getMvArea().getAreaPath()).thenReturn(municipalityAreaPath().path());
		when(reportingUnit.getMvElection().getElectionPath()).thenReturn(electionGroupPath().path());
		when(reportingUnit.reportingUnitTypeId()).thenReturn(VALGSTYRET);
		when(reportingUnit.getNameLine()).thenReturn("Valgstyret");
		return reportingUnit;
	}

	public static Contest contest() {
		Contest contest = mock(Contest.class);
		return contest(contest);
	}

	private static Contest contest(Contest contest) {
		when(contest.electionPath()).thenReturn(contestPath());
		when(contest.getName()).thenReturn("contestName");
		return contest;
	}

	public static VoteCount voteCount(VoteCountConfig config) {
		VoteCount voteCount = mock(VoteCount.class, RETURNS_DEEP_STUBS);
		contestReport(config.getContestReportConfig(), voteCount.getContestReport());
		when(voteCount.getMvArea().getAreaPath()).thenReturn(config.getAreaPath());
		when(voteCount.getCountQualifierId()).thenReturn(config.getCountQualifierId());
		when(voteCount.getVoteCountCategoryId()).thenReturn(config.getVoteCountCategoryId());
		when(voteCount.getId()).thenReturn(config.getId());
		when(voteCount.getCountStatus()).thenReturn(config.getCountStatus());
		when(voteCount.getApprovedBallots()).thenReturn(config.getApprovedBallots());
		when(voteCount.getRejectedBallots()).thenReturn(config.getRejectedBallots());
		if (config.getTechnicalVotings() != null) {
			when(voteCount.getTechnicalVotings()).thenReturn(config.getTechnicalVotings());
		} else {
			when(voteCount.getTechnicalVotings()).thenReturn(null);
		}
		when(voteCount.isManualCount()).thenReturn(config.isManualCount());
		if (config.getModifiedBallotsProcessed() != null) {
			when(voteCount.isModifiedBallotsProcessed()).thenReturn(config.getModifiedBallotsProcessed());
		}
		if (config.getRejectedBallotsProcessed() != null) {
			when(voteCount.isRejectedBallotsProcessed()).thenReturn(config.getRejectedBallotsProcessed());
		}
		when(voteCount.getInfoText()).thenReturn(config.getInfoText());
		if (config.getForeignSpecialCovers() != null) {
			when(voteCount.getForeignSpecialCovers()).thenReturn(config.getForeignSpecialCovers());
		}
		if (config.getSpecialCovers() != null) {
			when(voteCount.getSpecialCovers()).thenReturn(config.getSpecialCovers());
		}
		if (config.getEmergencySpecialCovers() != null) {
			when(voteCount.getMvArea().getMunicipality().isElectronicMarkoffs()).thenReturn(true);
			when(voteCount.getEmergencySpecialCovers()).thenReturn(config.getEmergencySpecialCovers());
		}
		if (config.getBallotsForOtherContests() != null) {
			when(voteCount.getContestReport().getContest().isOnBoroughLevel()).thenReturn(true);
			when(voteCount.getBallotsForOtherContests()).thenReturn(config.getBallotsForOtherContests());
		}
		final BallotCountConfig[] ballotCountConfigs = config.getBallotCountConfigs();
		when(voteCount.getBallotCountSet().size()).thenReturn(ballotCountConfigs.length);
		when(voteCount.getBallotCountSet().isEmpty()).thenReturn(ballotCountConfigs.length > 0);
		when(voteCount.getBallotCountSet().iterator()).thenReturn(new Iterator<BallotCount>() {
			private int nextIndex = 0;

			@Override
			public boolean hasNext() {
				return nextIndex < ballotCountConfigs.length;
			}

			@Override
			public BallotCount next() {
				return ballotCount(ballotCountConfigs[nextIndex++]);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove not allowed");
			}
		});
		return voteCount;
	}

	public static Set<BallotCount> ballotCounts(BallotCountConfig... configs) {
		Set<BallotCount> ballotCounts = new LinkedHashSet<>();
		for (BallotCountConfig config : configs) {
			ballotCounts.add(ballotCount(config));
		}
		return ballotCounts;
	}

	public static BallotCount ballotCount(BallotCountConfig config) {
		BallotCount ballotCount = mock(BallotCount.class, RETURNS_DEEP_STUBS);
		when(ballotCount.isBlank()).thenReturn(EvoteConstants.BALLOT_BLANK.equals(config.getBallotId()));
		when(ballotCount.getBallotId()).thenReturn(config.getBallotId());
		when(ballotCount.getBallotRejectionId()).thenReturn(config.getBallotRejectionId());
		when(ballotCount.getUnmodifiedBallots()).thenReturn(config.getUnmodifiedBallots());
		if (config.getModifiedBallots() != null) {
			when(ballotCount.getModifiedBallots()).thenReturn(config.getModifiedBallots());
		}
		final CastBallotConfig[] castBallotConfigs = config.getCastBallotConfigs();
		if (castBallotConfigs != null) {
			when(ballotCount.getCastBallots().size()).thenReturn(castBallotConfigs.length);
			when(ballotCount.getCastBallots().isEmpty()).thenReturn(castBallotConfigs.length > 0);
			when(ballotCount.getCastBallots().iterator()).thenReturn(new Iterator<CastBallot>() {
				private int nextIndex = 0;

				@Override
				public boolean hasNext() {
					return nextIndex < castBallotConfigs.length;
				}

				@Override
				public CastBallot next() {
					return castBallot(castBallotConfigs[nextIndex++]);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("remove not allowed");
				}
			});
		}
		return ballotCount;
	}

	public static Set<CastBallot> castBallots(CastBallotConfig... configs) {
		Set<CastBallot> castBallots = new LinkedHashSet<>();
		for (CastBallotConfig config : configs) {
			castBallots.add(castBallot(config));
		}
		return castBallots;
	}

	public static CastBallot castBallot(CastBallotConfig config) {
		CastBallot castBallot = mock(CastBallot.class, RETURNS_DEEP_STUBS);
		when(castBallot.getId()).thenReturn(config.getCastBallotId());
		when(castBallot.getType()).thenReturn(config.getCastBallotType());
		if (!config.isBinaryDataIncluded()) {
			when(castBallot.getBinaryData()).thenReturn(null);
		}
		final CandidateVoteConfig[] candidateVoteConfigs = config.getCandidateVoteConfigs();
		if (candidateVoteConfigs != null) {
			when(castBallot.getCandidateVotes().size()).thenReturn(candidateVoteConfigs.length);
			when(castBallot.getCandidateVotes().isEmpty()).thenReturn(candidateVoteConfigs.length > 0);
			when(castBallot.getCandidateVotes().iterator()).thenReturn(new Iterator<CandidateVote>() {
				private int nextIndex = 0;

				@Override
				public boolean hasNext() {
					return nextIndex < candidateVoteConfigs.length;
				}

				@Override
				public CandidateVote next() {
					return candidateVote(candidateVoteConfigs[nextIndex++]);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("remove not allowed");
				}
			});
		}
		return castBallot;
	}

	public static Set<CandidateVote> candidateVotes(CandidateVoteConfig... configs) {
		Set<CandidateVote> candidateVotes = new LinkedHashSet<>();
		for (CandidateVoteConfig config : configs) {
			candidateVotes.add(candidateVote(config));
		}
		return candidateVotes;
	}

	public static CandidateVote candidateVote(CandidateVoteConfig config) {
		CandidateVote candidateVote = mock(CandidateVote.class, RETURNS_DEEP_STUBS);
		when(candidateVote.getCandidate().getId()).thenReturn(config.getCandidateId());
		when(candidateVote.getVoteCategory().getId()).thenReturn(config.getVoteCategoryId());
		if (config.getRenumberPosition() != null) {
			when(candidateVote.getRenumberPosition()).thenReturn(config.getRenumberPosition());
		} else {
			when(candidateVote.getRenumberPosition()).thenReturn(null);
		}
		return candidateVote;
	}

	public static AreaPath municipalityAreaPath() {
		return areaPath(1, MUNICIPALITY);
	}

	public static AreaPath pollingDistrictAreaPath() {
		return areaPath(1, POLLING_DISTRICT);
	}

	public static AreaPath areaPath(int index, AreaLevelEnum level) {
		if (index < 1) {
			throw new IllegalArgumentException("index < 1");
		}
		if (level == null) {
			throw new IllegalArgumentException("level is null");
		}
		switch (level) {
		case POLLING_DISTRICT:
			return AreaPath.from(
					electionEventId(index), countryId(index), countyId(index), municipalityId(index), boroughId(index), pollingDistrictId(index));
		case BOROUGH:
			return AreaPath.from(electionEventId(index), countryId(index), countyId(index), municipalityId(index), boroughId(index));
		case MUNICIPALITY:
			return AreaPath.from(electionEventId(index), countryId(index), countyId(index), municipalityId(index));
		case COUNTY:
			return AreaPath.from(electionEventId(index), countryId(index), countyId(index));
		case COUNTRY:
			return AreaPath.from(electionEventId(index), countryId(index));
		default:
			return AreaPath.from(electionEventId(index));
		}
	}

	private static String electionEventId(int index) {
		return String.valueOf(BASE_ELECTION_EVENT_ID * index);
	}

	private static String countryId(int index) {
		return String.valueOf(BASE_COUNTRY_ID * index);
	}

	private static String countyId(int index) {
		return String.valueOf(BASE_COUNTY_ID * index);
	}

	private static String municipalityId(int index) {
		return String.valueOf(BASE_MUNICIPALITY_ID * index);
	}

	private static String boroughId(int index) {
		return String.valueOf(BASE_BOROUGH_ID * index);
	}

	private static String pollingDistrictId(int index) {
		return String.valueOf(BASE_POLLING_DISTRICT_ID * index);
	}

	public static ElectionPath electionGroupPath() {
		return electionPath(1, ELECTION_GROUP);
	}

	public static ElectionPath contestPath() {
		return electionPath(1, CONTEST);
	}

	public static ElectionPath electionPath(int index, ElectionLevelEnum level) {
		if (index < 1) {
			throw new IllegalArgumentException("index < 1");
		}
		if (level == null) {
			throw new IllegalArgumentException("level is null");
		}

		switch (level) {
		case CONTEST:
			return ElectionPath.from(electionEventId(index), electionGroupId(index), electionId(index), contestId(index));
		case ELECTION:
			return ElectionPath.from(electionEventId(index), electionGroupId(index), electionId(index));
		case ELECTION_GROUP:
			return ElectionPath.from(electionEventId(index), electionGroupId(index));
		default:
			return ElectionPath.from(electionEventId(index));
		}
	}

	private static String electionGroupId(int index) {
		return String.valueOf(BASE_ELECTION_GROUP_ID * index);
	}

	private static String electionId(int index) {
		return String.valueOf(BASE_ELECTION_ID * index);
	}

	private static String contestId(int index) {
		return String.valueOf(BASE_CONTEST_ID * index);
	}

	public static JsonObject contestReportJsonObject(ContestReportConfig config) {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("reportingUnit", reportingUnitJsonObject())
				.add("contest", contestJsonObject());
		return jsonBuilder.asJsonObject();
	}

	public static JsonObject reportingUnitJsonObject() {
		return new JsonBuilder()
				.add("areaPath", municipalityAreaPath().path())
				.add("electionPath", electionGroupPath().path())
				.add("reportingUnitType", VALGSTYRET.name())
				.add("name", "Valgstyret")
				.asJsonObject();
	}

	public static JsonObject contestJsonObject() {
		return new JsonBuilder()
				.add("electionPath", contestPath().path())
				.add("name", "contestName")
				.asJsonObject();
	}

	public static JsonObject voteCountJsonObject(VoteCountConfig config) {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("contestReport", contestReportJsonObject(config.getContestReportConfig()))
				.add("areaPath", config.getAreaPath())
				.add("qualifier", config.getCountQualifierId())
				.add("category", config.getVoteCountCategoryId())
				.add("id", config.getId())
				.add("status", config.getCountStatus().name())
				.add("approvedBallots", config.getApprovedBallots())
				.add("rejectedBallots", config.getRejectedBallots());
		if (config.getTechnicalVotings() != null) {
			jsonBuilder.add("technicalVotings", config.getTechnicalVotings());
		}
		jsonBuilder.add("manualCount", config.isManualCount());
		if (config.getModifiedBallotsProcessed() != null) {
			jsonBuilder.add("modifiedBallotsProcessed", config.getModifiedBallotsProcessed());
		}
		if (config.getRejectedBallotsProcessed() != null) {
			jsonBuilder.add("rejectedBallotsProcessed", config.getRejectedBallotsProcessed());
		}
		jsonBuilder.add("infoText", config.getInfoText());
		if (config.getForeignSpecialCovers() != null) {
			jsonBuilder.add("foreignSpecialCovers", config.getForeignSpecialCovers());
		}
		if (config.getSpecialCovers() != null) {
			jsonBuilder.add("specialCovers", config.getSpecialCovers());
		}
		if (config.getEmergencySpecialCovers() != null) {
			jsonBuilder.add("emergencySpecialCovers", config.getEmergencySpecialCovers());
		}
		if (config.getBallotsForOtherContests() != null) {
			jsonBuilder.add("ballotsForOtherContests", config.getBallotsForOtherContests());
		}
		if (!PROTOCOL.getId().equals(config.getCountQualifierId())) {
			jsonBuilder.add("ballotCounts", ballotCountJsonArray(config.getBallotCountConfigs()));
		}
		return jsonBuilder.asJsonObject();
	}

	public static JsonArray ballotCountJsonArray(BallotCountConfig... configs) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (BallotCountConfig config : configs) {
			arrayBuilder.add(ballotCountJsonObject(config));
		}
		return arrayBuilder.build();
	}

	public static JsonObject ballotCountJsonObject(BallotCountConfig config) {
		JsonBuilder jsonBuilder = new JsonBuilder();
		if (config.getBallotId() != null) {
			jsonBuilder.add("ballotId", config.getBallotId());
		}
		if (config.getBallotRejectionId() != null) {
			jsonBuilder.add("ballotRejectionId", config.getBallotRejectionId());
		}
		jsonBuilder.add("unmodifiedBallots", config.getUnmodifiedBallots());
		if (config.getModifiedBallots() != null) {
			jsonBuilder.add("modifiedBallots", config.getModifiedBallots());
		}
		if (config.getCastBallotConfigs() != null) {
			jsonBuilder.add("castBallots", castBallotJsonArray(config.getCastBallotConfigs()));
		}
		return jsonBuilder.asJsonObject();
	}

	public static JsonArray castBallotJsonArray(CastBallotConfig... configs) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (CastBallotConfig config : configs) {
			arrayBuilder.add(castBallotJsonObject(config));
		}
		return arrayBuilder.build();
	}

	public static JsonObject castBallotJsonObject(CastBallotConfig config) {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("id", config.getCastBallotId())
				.add("type", config.getCastBallotType().name())
				.add("binaryDataIncluded", config.isBinaryDataIncluded());
		if (config.getCandidateVoteConfigs() != null) {
			jsonBuilder.add("candidateVotes", candidateVoteJsonArray(config.getCandidateVoteConfigs()));
		}
		return jsonBuilder.asJsonObject();
	}

	public static JsonArray candidateVoteJsonArray(CandidateVoteConfig... configs) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (CandidateVoteConfig config : configs) {
			arrayBuilder.add(candidateVoteJsonObject(config));
		}
		return arrayBuilder.build();
	}

	public static JsonObject candidateVoteJsonObject(CandidateVoteConfig config) {
		JsonBuilder jsonBuilder = new JsonBuilder()
				.add("candidateId", config.getCandidateId())
				.add("voteCategoryId", config.getVoteCategoryId());
		if (config.getRenumberPosition() != null) {
			jsonBuilder.add("renumberPosition", config.getRenumberPosition());
		}
		return jsonBuilder.asJsonObject();
	}

	public static BallotCountConfig[] ballotCountConfigs() {
		return new BallotCountConfig[] {
				new BallotCountConfig(1),
				new BallotCountConfig(2, 3),
				new BallotCountConfig("BALLOT_REJECTION_ID", 4)
		};
	}

	public static CastBallotConfig[] castBallotConfigs() {
		return new CastBallotConfig[] {
				new CastBallotConfig(UNMODIFIED),
				new CastBallotConfig(UNMODIFIED),
				new CastBallotConfig(UNMODIFIED)
		};
	}

	public static CandidateVoteConfig[] candidateVoteConfigs() {
		return new CandidateVoteConfig[] {
				new CandidateVoteConfig(CandidateVoteConfig.candidateId(1)),
				new CandidateVoteConfig(CandidateVoteConfig.candidateId(2)),
				new CandidateVoteConfig(CandidateVoteConfig.candidateId(3))
		};
	}

	public static class ContestReportConfig {
	}

	public static class VoteCountConfig {
		private static final String VOTE_COUNT_ID = "VC_ID";
		private static final int APPROVED_BALLOTS = 2;
		private static final int REJECTED_BALLOTS = 3;
		private static final String INFO_TEXT = "INFO_TEXT";
		private static final int SPECIAL_COVERS = 6;
		
		private final ContestReportConfig contestReportConfig;
		private final String areaPath;
		private final String countQualifierId;
		private final String voteCountCategoryId;
		private final String id;
		private final CountStatus countStatus;
		private final int approvedBallots;
		private final int rejectedBallots;
		private final Integer technicalVotings;
		private final boolean manualCount;
		private final Boolean modifiedBallotsProcessed;
		private final Boolean rejectedBallotsProcessed;
		private final String infoText;
		private final Integer foreignSpecialCovers;
		private final Integer specialCovers;
		private final Integer emergencySpecialCovers;
		private final Integer ballotsForOtherContests;
		private final BallotCountConfig[] ballotCountConfigs;
		
		public VoteCountConfig(
				ContestReportConfig contestReportConfig, String areaPath, String countQualifierId, String voteCountCategoryId, CountStatus countStatus,
				Integer technicalVotings, boolean manualCount, Integer foreignSpecialCovers, Integer specialCovers, Integer emergencySpecialCovers,
				Integer ballotsForOtherContests, BallotCountConfig[] ballotCountConfigs) {
			this.contestReportConfig = contestReportConfig;
			this.areaPath = areaPath;
			this.countQualifierId = countQualifierId;
			this.voteCountCategoryId = voteCountCategoryId;
			this.id = VOTE_COUNT_ID;
			this.countStatus = countStatus;
			this.approvedBallots = APPROVED_BALLOTS;
			this.rejectedBallots = REJECTED_BALLOTS;
			this.technicalVotings = technicalVotings;
			this.manualCount = manualCount;
			this.modifiedBallotsProcessed = null;
			this.rejectedBallotsProcessed = null;
			this.infoText = INFO_TEXT;
			this.foreignSpecialCovers = foreignSpecialCovers;
			this.specialCovers = specialCovers;
			this.emergencySpecialCovers = emergencySpecialCovers;
			this.ballotsForOtherContests = ballotsForOtherContests;
			this.ballotCountConfigs = copyBallotCountConfigs(ballotCountConfigs);
		}

		public VoteCountConfig(
				ContestReportConfig contestReportConfig, String areaPath, String voteCountCategoryId, CountStatus countStatus, boolean manualCount,
				boolean modifiedBallotsProcessed, boolean rejectedBallotsProcessed, BallotCountConfig[] ballotCountConfigs) {
			this.contestReportConfig = contestReportConfig;
			this.areaPath = areaPath;
			this.countQualifierId = FINAL.getId();
			this.voteCountCategoryId = voteCountCategoryId;
			this.id = VOTE_COUNT_ID;
			this.countStatus = countStatus;
			this.approvedBallots = APPROVED_BALLOTS;
			this.rejectedBallots = REJECTED_BALLOTS;
			this.technicalVotings = null;
			this.manualCount = manualCount;
			this.modifiedBallotsProcessed = modifiedBallotsProcessed;
			this.rejectedBallotsProcessed = rejectedBallotsProcessed;
			this.infoText = INFO_TEXT;
			this.foreignSpecialCovers = null;
			this.specialCovers = null;
			this.emergencySpecialCovers = null;
			this.ballotsForOtherContests = null;
			this.ballotCountConfigs = copyBallotCountConfigs(ballotCountConfigs);
		}

		public static VoteCountConfig protocolVoteCountConfig(Integer foreignSpecialCovers, Integer emergencySpecialCovers, Integer ballotsForOtherContests) {
			return new VoteCountConfig(
					new ContestReportConfig(), pollingDistrictAreaPath().path(), PROTOCOL.getId(), VO.getId(), SAVED, null,
					true, foreignSpecialCovers, SPECIAL_COVERS, emergencySpecialCovers, ballotsForOtherContests,
					new BallotCountConfig[] { new BallotCountConfig(1) });
		}

		public static VoteCountConfig preliminaryVoteCountConfig(CountCategory category) {
			ContestReportConfig contestReportConfig = new ContestReportConfig();
			return new VoteCountConfig(
					contestReportConfig, pollingDistrictAreaPath().path(), PRELIMINARY.getId(), category.getId(), SAVED,
					null, true, null, null, null, null,
					new BallotCountConfig[] {
							new BallotCountConfig(1),
							new BallotCountConfig(1, 2, 3, false),
							new BallotCountConfig(2, 4, 5, false) });
		}

		public static VoteCountConfig preliminaryVoteCountConfig(int technicalVotings) {
			return new VoteCountConfig(
					new ContestReportConfig(), pollingDistrictAreaPath().path(), PRELIMINARY.getId(), FO.getId(), SAVED,
					technicalVotings, true, null, null, null, null,
					new BallotCountConfig[] {
							new BallotCountConfig(1),
							new BallotCountConfig(1, 2, 3, false),
							new BallotCountConfig(2, 4, 5, false) });
		}

		public static VoteCountConfig finalVoteCountConfig(CountStatus status, boolean modifiedBallotsProcessed, boolean rejectedBallotsProcessed) {
			return new VoteCountConfig(
					new ContestReportConfig(), pollingDistrictAreaPath().path(), FO.getId(), status, true, modifiedBallotsProcessed, rejectedBallotsProcessed,
					new BallotCountConfig[] {
							new BallotCountConfig(1),
							new BallotCountConfig(1, 2, 3, true),
							new BallotCountConfig(2, 4, 5, true),
							new BallotCountConfig("BALLOT_REJECTION_ID_1", 4),
							new BallotCountConfig("BALLOT_REJECTION_ID_2", 5)});
		}

		private BallotCountConfig[] copyBallotCountConfigs(BallotCountConfig[] ballotCountConfigs) {
			if (ballotCountConfigs != null) {
				BallotCountConfig[] newBallotCountConfigs = new BallotCountConfig[ballotCountConfigs.length];
				arraycopy(ballotCountConfigs, 0, newBallotCountConfigs, 0, ballotCountConfigs.length);
				return newBallotCountConfigs;
			}
			return null;
		}

		public ContestReportConfig getContestReportConfig() {
			return contestReportConfig;
		}

		public String getAreaPath() {
			return areaPath;
		}

		public String getCountQualifierId() {
			return countQualifierId;
		}

		public String getVoteCountCategoryId() {
			return voteCountCategoryId;
		}

		public String getId() {
			return id;
		}

		public CountStatus getCountStatus() {
			return countStatus;
		}

		public int getApprovedBallots() {
			return approvedBallots;
		}

		public int getRejectedBallots() {
			return rejectedBallots;
		}

		public Integer getTechnicalVotings() {
			return technicalVotings;
		}

		public boolean isManualCount() {
			return manualCount;
		}

		public Boolean getModifiedBallotsProcessed() {
			return modifiedBallotsProcessed;
		}

		public Boolean getRejectedBallotsProcessed() {
			return rejectedBallotsProcessed;
		}

		public String getInfoText() {
			return infoText;
		}

		public Integer getForeignSpecialCovers() {
			return foreignSpecialCovers;
		}

		public Integer getSpecialCovers() {
			return specialCovers;
		}

		public Integer getEmergencySpecialCovers() {
			return emergencySpecialCovers;
		}

		public Integer getBallotsForOtherContests() {
			return ballotsForOtherContests;
		}

		public BallotCountConfig[] getBallotCountConfigs() {
			return ballotCountConfigs;
		}
	}

	public static class BallotCountConfig {
		private final String ballotId;
		private final String ballotRejectionId;
		private final int unmodifiedBallots;
		private final Integer modifiedBallots;
		private final CastBallotConfig[] castBallotConfigs;

		public BallotCountConfig(int unmodifiedBallots) {
			this.ballotId = EvoteConstants.BALLOT_BLANK;
			this.ballotRejectionId = null;
			this.unmodifiedBallots = unmodifiedBallots;
			this.modifiedBallots = null;
			this.castBallotConfigs = null;
		}

		public BallotCountConfig(int unmodifiedBallots, int modifiedBallots) {
			this.ballotId = "BALLOT_ID";
			this.ballotRejectionId = null;
			this.unmodifiedBallots = unmodifiedBallots;
			this.modifiedBallots = modifiedBallots;
			this.castBallotConfigs = new CastBallotConfig[0];
		}

		public BallotCountConfig(String ballotRejectionId, int unmodifiedBallots) {
			this.ballotId = null;
			this.ballotRejectionId = ballotRejectionId;
			this.unmodifiedBallots = unmodifiedBallots;
			this.modifiedBallots = null;
			this.castBallotConfigs = new CastBallotConfig[0];
		}

		public BallotCountConfig(int index, int unmodifiedBallots, int modifiedBallots, boolean includeCastBallots) {
			this.ballotId = "BALLOT_ID_" + index;
			this.ballotRejectionId = null;
			this.unmodifiedBallots = unmodifiedBallots;
			this.modifiedBallots = modifiedBallots;
			if (includeCastBallots) {
				this.castBallotConfigs = new CastBallotConfig[0];
			} else {
				this.castBallotConfigs = null;
			}
		}

		public String getBallotId() {
			return ballotId;
		}

		public String getBallotRejectionId() {
			return ballotRejectionId;
		}

		public int getUnmodifiedBallots() {
			return unmodifiedBallots;
		}

		public Integer getModifiedBallots() {
			return modifiedBallots;
		}

		public CastBallotConfig[] getCastBallotConfigs() {
			return castBallotConfigs;
		}
	}

	public static class CastBallotConfig {
		private static final String CAST_BALLOT_ID = "CAST_BALLOT_ID";

		private final String castBallotId;
		private final CastBallot.Type castBallotType;
		private final boolean binaryDataIncluded;
		private final CandidateVoteConfig[] candidateVoteConfigs;

		public CastBallotConfig(boolean binaryDataIncluded) {
			this.castBallotId = CAST_BALLOT_ID;
			this.castBallotType = CastBallot.Type.REJECTED;
			this.binaryDataIncluded = binaryDataIncluded;
			this.candidateVoteConfigs = null;
		}

		public CastBallotConfig(CastBallot.Type castBallotType) {
			this.castBallotId = CAST_BALLOT_ID;
			this.castBallotType = castBallotType;
			this.binaryDataIncluded = false;
			this.candidateVoteConfigs = null;
		}

		public CastBallotConfig(CastBallot.Type castBallotType, CandidateVoteConfig[] candidateVoteConfigs) {
			this.castBallotId = CAST_BALLOT_ID;
			this.castBallotType = castBallotType;
			this.binaryDataIncluded = false;
			this.candidateVoteConfigs = copyCandidateVoteConfigs(candidateVoteConfigs);
		}

		private CandidateVoteConfig[] copyCandidateVoteConfigs(CandidateVoteConfig[] candidateVoteConfigs) {
			if (candidateVoteConfigs == null) {
				return null;
			}
			CandidateVoteConfig[] newCandidateVoteConfigs = new CandidateVoteConfig[candidateVoteConfigs.length];
			arraycopy(candidateVoteConfigs, 0, newCandidateVoteConfigs, 0, candidateVoteConfigs.length);
			return newCandidateVoteConfigs;
		}

		public String getCastBallotId() {
			return castBallotId;
		}

		public CastBallot.Type getCastBallotType() {
			return castBallotType;
		}

		public boolean isBinaryDataIncluded() {
			return binaryDataIncluded;
		}

		public CandidateVoteConfig[] getCandidateVoteConfigs() {
			return candidateVoteConfigs;
		}
	}

	public static class CandidateVoteConfig {
		public static final String CANDIDATE_ID = "CANDIDATE_ID";
		public static final String VOTE_CATEGORY_ID = "VOTE_CATEGORY_ID";
		public static final Integer RENUMBER_POSITION = 1;

		private final String candidateId;
		private final String voteCategoryId;
		private final Integer renumberPosition;

		public CandidateVoteConfig() {
			this(CANDIDATE_ID, VOTE_CATEGORY_ID, null);
		}

		public CandidateVoteConfig(Integer renumberPosition) {
			this(CANDIDATE_ID, VOTE_CATEGORY_ID, renumberPosition);
		}

		public CandidateVoteConfig(String candidateId) {
			this(candidateId, VOTE_CATEGORY_ID, null);
		}

		public CandidateVoteConfig(String candidateId, String voteCategoryId, Integer renumberPosition) {
			this.candidateId = candidateId;
			this.voteCategoryId = voteCategoryId;
			this.renumberPosition = renumberPosition;
		}

		public static String candidateId(int index) {
			return CANDIDATE_ID + "_" + index;
		}

		public String getCandidateId() {
			return candidateId;
		}

		public String getVoteCategoryId() {
			return voteCategoryId;
		}

		public Integer getRenumberPosition() {
			return renumberPosition;
		}
	}
}

