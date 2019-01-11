package no.valg.eva.admin.configuration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.model.election.GenericElectionType;
import no.valg.eva.admin.common.counting.model.configuration.ElectionRef;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class ElectionMapperTest {

	public static final ElectionPath PARENT_ELECTION_PATH = ElectionPath.from("150001.01");
	public static final GenericElectionType GENERIC_ELECTION_TYPE = GenericElectionType.F;
	public static final BigDecimal BASELINE_VOTE_FACTOR = BigDecimal.ONE;
	public static final BigDecimal CANDIDATE_RANK_VOTE_SHARE_THRESHOLD = BigDecimal.valueOf(2);
	public static final String ELECTION_GROUP_NAME = "electionGroupName";
	public static final String ID = "03";
	public static final LocalDate END_DATE_OF_BIRTH = new LocalDate(1998, 1, 1);
	public static final int LENGTH_25 = 25;
	public static final int AREA_LEVEL = 1;
	public static final int LEVELING_SEATS = 1;
	public static final String ELECTION_NAME = "electionName";
	public static final BigDecimal LEVELING_SEATS_VOTE_SHARE_THRESHOLD = BigDecimal.valueOf(3);
	public static final BigDecimal SETTLEMENT_FIRST_DIVISOR = BigDecimal.valueOf(4);
	private static final Long PK = 1L;

	@Test
	public void toCommonObject_returnsCommonElectionObject() {
		ElectionMapper electionMapper = new ElectionMapper(null, null);

		Election e = electionMapper.toCommonObject(createElectionEntity());

		assertThat(e.getGenericElectionType()).isEqualTo(GENERIC_ELECTION_TYPE);
		assertThat(e.isWritein()).isTrue();
		assertThat(e.getValgtype()).isEqualTo(Valgtype.BYDELSVALG);
		assertThat(e.isStrikeout()).isTrue();
		assertThat(e.getAreaLevel()).isEqualTo(AREA_LEVEL);
		assertThat(e.getBaselineVoteFactor()).isEqualTo(BASELINE_VOTE_FACTOR);
		assertThat(e.getCandidateRankVoteShareThreshold()).isEqualTo(CANDIDATE_RANK_VOTE_SHARE_THRESHOLD);
		assertThat(e.isCandidatesInContestArea()).isTrue();
		assertThat(e.getElectionGroupName()).isEqualTo(ELECTION_GROUP_NAME);
		assertThat(e.getParentElectionPath()).isEqualTo(PARENT_ELECTION_PATH);
		assertThat(e.getId()).isEqualTo(ID);
		assertThat(e.getEndDateOfBirth()).isEqualTo(END_DATE_OF_BIRTH);
		assertThat(e.getLevelingSeats()).isEqualTo(LEVELING_SEATS);
		assertThat(e.getLevelingSeatsVoteShareThreshold()).isEqualTo(LEVELING_SEATS_VOTE_SHARE_THRESHOLD);
		assertThat(e.getName()).isEqualTo(ELECTION_NAME);
		assertThat(e.isPenultimateRecount()).isTrue();
		assertThat(e.isPersonal()).isTrue();
		assertThat(e.isRenumber()).isTrue();
		assertThat(e.isRenumberLimit()).isTrue();
		assertThat(e.getSettlementFirstDivisor()).isEqualTo(SETTLEMENT_FIRST_DIVISOR);
		assertThat(e.isSingleArea()).isTrue();
		assertThat(e.getElectionRef().getPk()).isEqualTo(PK);
		assertThat(e.getMaxCandidateNameLength()).isEqualTo(LENGTH_25);
		assertThat(e.getMaxCandidateResidenceProfessionLength()).isEqualTo(LENGTH_25);
	}

	@Test
	public void toEntity_returnsEntity() throws Exception {

		MvElectionRepository mvElectionRepository = mock(MvElectionRepository.class);
		MvElection mvElection = createMvElection();
		when(mvElectionRepository.finnEnkeltMedSti(PARENT_ELECTION_PATH.tilValghierarkiSti())).thenReturn(mvElection);
		ElectionRepository electionRepository = mock(ElectionRepository.class);
		when(electionRepository.findElectionTypeById(GENERIC_ELECTION_TYPE.name())).thenReturn(createElectionType());
		ElectionMapper electionMapper = new ElectionMapper(mvElectionRepository, electionRepository);

		no.valg.eva.admin.configuration.domain.model.Election e = electionMapper.toEntity(createElectionCommonObject());

		assertThat(e.getPk()).isEqualTo(PK);
		assertThat(e.getElectionType().getId()).isEqualTo(GENERIC_ELECTION_TYPE.name());
		assertThat(e.isWritein()).isTrue();
		assertThat(e.getValgtype()).isEqualTo(Valgtype.BYDELSVALG);
		assertThat(e.isStrikeout()).isTrue();
		assertThat(e.getAreaLevel()).isEqualTo(AREA_LEVEL);
		assertThat(e.getBaselineVoteFactor()).isEqualTo(BASELINE_VOTE_FACTOR);
		assertThat(e.getCandidateRankVoteShareThreshold()).isEqualTo(CANDIDATE_RANK_VOTE_SHARE_THRESHOLD);
		assertThat(e.isCandidatesInContestArea()).isTrue();
		assertThat(e.getElectionGroup().getName()).isEqualTo(ELECTION_GROUP_NAME);
		assertThat(e.getElectionGroup().electionPath()).isEqualTo(PARENT_ELECTION_PATH);
		assertThat(e.getId()).isEqualTo(ID);
		assertThat(e.getEndDateOfBirth()).isEqualTo(END_DATE_OF_BIRTH);
		assertThat(e.getLevelingSeats()).isEqualTo(LEVELING_SEATS);
		assertThat(e.getLevelingSeatsVoteShareThreshold()).isEqualTo(LEVELING_SEATS_VOTE_SHARE_THRESHOLD);
		assertThat(e.getName()).isEqualTo(ELECTION_NAME);
		assertThat(e.isPenultimateRecount()).isTrue();
		assertThat(e.isPersonal()).isTrue();
		assertThat(e.isRenumber()).isTrue();
		assertThat(e.isRenumberLimit()).isTrue();
		assertThat(e.getSettlementFirstDivisor()).isEqualTo(SETTLEMENT_FIRST_DIVISOR);
		assertThat(e.isSingleArea()).isTrue();
		assertThat(e.getMaxCandidateNameLength()).isEqualTo(LENGTH_25);
		assertThat(e.getMaxCandidateResidenceProfessionLength()).isEqualTo(LENGTH_25);
	}

	private ElectionType createElectionType() {
		ElectionType t = new ElectionType();
		t.setId(GENERIC_ELECTION_TYPE.name());
		return t;
	}

	private MvElection createMvElection() {
		MvElection e = mock(MvElection.class);
		ElectionGroup eg = mock(ElectionGroup.class);
		when(eg.electionPath()).thenReturn(PARENT_ELECTION_PATH);
		when(eg.getName()).thenReturn(ELECTION_GROUP_NAME);
		when(e.getElectionGroup()).thenReturn(eg);
		return e;
	}

	private Election createElectionCommonObject() {
		Election e = new Election(PARENT_ELECTION_PATH);
		e.setElectionRef(new ElectionRef(PK));
		e.setGenericElectionType(GENERIC_ELECTION_TYPE);
		e.setWritein(true);
		e.setValgtype(Valgtype.BYDELSVALG);
		e.setStrikeout(true);
		e.setAreaLevel(AREA_LEVEL);
		e.setBaselineVoteFactor(BASELINE_VOTE_FACTOR);
		e.setCandidateRankVoteShareThreshold(CANDIDATE_RANK_VOTE_SHARE_THRESHOLD);
		e.setCandidatesInContestArea(true);
		e.setElectionGroupName(ELECTION_GROUP_NAME);
		e.setId(ID);
		e.setEndDateOfBirth(END_DATE_OF_BIRTH);
		e.setLevelingSeats(LEVELING_SEATS);
		e.setLevelingSeatsVoteShareThreshold(LEVELING_SEATS_VOTE_SHARE_THRESHOLD);
		e.setName(ELECTION_NAME);
		e.setPenultimateRecount(true);
		e.setPersonal(true);
		e.setRenumber(true);
		e.setRenumberLimit(true);
		e.setSettlementFirstDivisor(SETTLEMENT_FIRST_DIVISOR);
		e.setSingleArea(true);
		e.setMaxCandidateNameLength(LENGTH_25);
		e.setMaxCandidateResidenceProfessionLength(LENGTH_25);
		return e;
	}

	private no.valg.eva.admin.configuration.domain.model.Election createElectionEntity() {
		no.valg.eva.admin.configuration.domain.model.Election e = new no.valg.eva.admin.configuration.domain.model.Election();
		e.setPk(PK);
		e.setElectionType(createElectionType());
		e.setWritein(true);
		e.setValgtype(Valgtype.BYDELSVALG);
		e.setStrikeout(true);
		e.setAreaLevel(AREA_LEVEL);
		e.setBaselineVoteFactor(BASELINE_VOTE_FACTOR);
		e.setCandidateRankVoteShareThreshold(CANDIDATE_RANK_VOTE_SHARE_THRESHOLD);
		e.setCandidatesInContestArea(true);
		e.setElectionGroup(createElectionGroupMock());
		e.setId(ID);
		e.setEndDateOfBirth(END_DATE_OF_BIRTH);
		e.setLevelingSeats(LEVELING_SEATS);
		e.setLevelingSeatsVoteShareThreshold(LEVELING_SEATS_VOTE_SHARE_THRESHOLD);
		e.setName(ELECTION_NAME);
		e.setPenultimateRecount(true);
		e.setPersonal(true);
		e.setRenumber(true);
		e.setRenumberLimit(true);
		e.setSettlementFirstDivisor(SETTLEMENT_FIRST_DIVISOR);
		e.setSingleArea(true);
		e.setMaxCandidateNameLength(LENGTH_25);
		e.setMaxCandidateResidenceProfessionLength(LENGTH_25);
		return e;
	}

	private ElectionGroup createElectionGroupMock() {
		ElectionGroup eg = mock(ElectionGroup.class);
		when(eg.electionPath()).thenReturn(PARENT_ELECTION_PATH);
		when(eg.getName()).thenReturn(ELECTION_GROUP_NAME);
		return eg;
	}
}
