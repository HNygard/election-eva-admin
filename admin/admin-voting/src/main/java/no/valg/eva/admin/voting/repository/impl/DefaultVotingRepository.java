package no.valg.eva.admin.voting.repository.impl;

import lombok.NoArgsConstructor;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.model.views.ForeignEarlyVoting;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRepository;
import no.valg.eva.admin.voting.repository.VotingStatisticsSql;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static no.evote.constants.EvoteConstants.DEAD_VOTER;
import static no.evote.constants.EvoteConstants.MULTIPLE_VOTES;
import static no.evote.constants.EvoteConstants.NOT_IN_ELECTORAL_ROLL;
import static no.evote.constants.EvoteConstants.WARNING_UNCHECKED;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.util.DateUtil.toDate;

/**
 * Default implementation of VotingRepository
 * "..interceptor class neither annotated @Interceptor nor registered through a portable extension
 */
@NoArgsConstructor
public class DefaultVotingRepository extends BaseRepository implements VotingRepository {
    protected static final String ELECTION_GROUP_PK = "electionGroupPk";
    protected static final String VOTER_PK = "voterPk";
    private static final String ELECTIONGROUPPK = "election_group_pk";
    private static final String MUNICIPALITYPK = "municipality_pk";
    private static final String MUNICIPALITY_ID = "municipalityId";
    private static final String MUNICIPALITY_PK = "municipalityPk";
    private static final String VOTINGCATEGORY_ID = "votingcategoryId";
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String LATE_VALIDATION = "lateValidation";
    private static final String PHASE = "phase";

    @Override
    public long countUnapprovedAdvanceVotings(AreaPath areaPath) {
        return getEm()
                .createNamedQuery("Voting.countUnapprovedAdvanceVotings", Long.class)
                .setParameter("electionEvenId", areaPath.getElectionEventId())
                .setParameter(MUNICIPALITY_ID, areaPath.getMunicipalityId())
                .getSingleResult()
                .longValue();
    }

    @Override
    public Voting create(final UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_GROUP) final Voting voting) {
        VotingCategory votingCategory = voting.getVotingCategory();
        if ((VO.getId().equals(votingCategory.getId()) || VB.getId().equals(votingCategory.getId()))
                && !voting.getMvArea().getMunicipality().isElectronicMarkoffs()) {
            throw new EvoteException("@voting.markOff.noElectronicVotes");
        }
        if (voting.getCastTimestamp() == null) {
            voting.setCastTimestamp(DateTime.now());
        }
        voting.setOperator(userData.getOperator());
        return super.createEntity(userData, voting);
    }

    @Override
    public Voting update(final UserData userData, final Voting voting) {
        return super.updateEntity(userData, voting);
    }

    @Override
    public void delete(final UserData userData, final Long pk) {
        super.deleteEntity(userData, Voting.class, pk);
    }

    @Override
    public Voting findByPk(final UserData userData, final Long pk) {
        return super.findEntityByPk(Voting.class, pk);
    }

    @Override
    public VotingCategory findVotingCategoryById(final String id) {
        return super.findEntityById(VotingCategory.class, id);
    }

    @Override
    public void deleteVotings(@SecureEntity(electionLevelDynamic = true) final MvElection mvElection, @SecureEntity(
            areaLevelDynamic = true) final MvArea mvArea, final Integer selectedVotingCategoryPk) {
        String queryString = "/* NO LOAD BALANCE */select delete_votings(?, ?, ?)";
        Query query = getEm().createNativeQuery(queryString);
        query.setParameter(1, mvElection.getPath());
        query.setParameter(2, mvArea.getPath());

        if (selectedVotingCategoryPk == null) {
            query.setParameter(3, 0);
        } else {
            query.setParameter(3, selectedVotingCategoryPk);
        }

        query.getSingleResult();
    }

    @Override
    public void deleteSeqVotingNumber(@SecureEntity(electionLevelDynamic = true) final MvElection mvElection, @SecureEntity(
            areaLevelDynamic = true) final MvArea mvArea) {
        String queryString = "/* NO LOAD BALANCE */select delete_sequential_voting_number(?, ?, ?)";
        Query query = getEm().createNativeQuery(queryString);
        query.setParameter(1, mvElection.getElectionGroup().getPk());
        if (mvArea.getAreaLevel() == AreaLevelEnum.COUNTRY.getLevel()) {
            query.setParameter(2, mvArea.getAreaPk(AreaLevelEnum.COUNTRY));
        } else if (mvArea.getAreaLevel() == AreaLevelEnum.COUNTY.getLevel()) {
            query.setParameter(2, mvArea.getAreaPk(AreaLevelEnum.COUNTY));
        } else if (mvArea.getAreaLevel() == AreaLevelEnum.MUNICIPALITY.getLevel()) {
            query.setParameter(2, mvArea.getAreaPk(AreaLevelEnum.MUNICIPALITY));
        }

        query.setParameter(3, mvArea.getAreaLevel());

        query.getSingleResult();
    }

    @Override
    public int slettStemmegivningerFraVelgereTilhoerendeI(ValggeografiSti valggeografiSti) {
        Query query = getEm().createNamedQuery("Voting.slettStemmegivningerFraVelgerTilhoerendeI")
                .setParameter(1, valggeografiSti.toString());
        return query.executeUpdate();
    }

    @Override
    @SuppressWarnings(WARNING_UNCHECKED)
    public List<Voting> getApprovedVotingsForVoterByElectionGroup(final UserData userData, final Long voterPk, final Long electionGroupPk) {
        Query query = getEm().createNamedQuery("Voting.findApprovedVotesForVoterByElectionGroup").setParameter(VOTER_PK, voterPk)
                .setParameter(ELECTION_GROUP_PK, electionGroupPk);
        return query.getResultList();
    }

    @Override
    public List<Object[]> getElectionDaysWithMarkoffs(final boolean isManualContest, final PollingDistrict pollingDistrict,
                                                      final VotingCategory votingCategory) {
        String sqlQuery = isManualContest ? manualContestVotingQuery() : votingQuery();
        Query query = getEm().createNativeQuery(sqlQuery);
        query.setParameter(1, pollingDistrict.getPk());
        query.setParameter(2, votingCategory.getId());
        return query.getResultList();
    }

    private String votingQuery() {
        // @formatter:off
        return "select ed.election_day_date, ed.election_day_pk, cast(count(vc) as integer) "
                + "from election_day ed "
                + "join mv_area a "
                + "    on a.polling_district_pk = ?1 "
                + "    and a.area_level = 6 "
                + "join opening_hours oh "
                + "    on oh.polling_place_pk = a.polling_place_pk "
                + "    and oh.election_day_pk = ed.election_day_pk "
                + "join mv_area ac "
                + "    on ac.polling_district_pk = a.polling_district_pk "
                + "    and ac.area_level = 5 "
                + "left join voting v "
                + "    on v.mv_area_pk = ac.mv_area_pk "
                + "    and date(v.cast_timestamp) = ed.election_day_date "
                + "left join voting_category vc "
                + "    on vc.voting_category_pk = v.voting_category_pk "
                + "    and vc.voting_category_id = ?2 "
                + "group by 1,2 "
                + "order by ed.election_day_date;";
        // @formatter:on
    }

    private String manualContestVotingQuery() {
        // @formatter:off
        return "select ed.election_day_date, ed.election_day_pk, cast(coalesce(mcv.votings, 0) as integer) "
                + "from election_day ed "
                + "join mv_area a "
                + "    on a.polling_district_pk = ?1 "
                + "    and a.area_level = 6 "
                + "join opening_hours oh "
                + "    on oh.polling_place_pk = a.polling_place_pk "
                + "    and oh.election_day_pk = ed.election_day_pk "
                + "join mv_area ac "
                + "    on ac.polling_district_pk = a.polling_district_pk "
                + "    and ac.area_level = 5 "
                + "left join manual_contest_voting mcv "
                + "    on mcv.mv_area_pk = ac.mv_area_pk "
                + "    and mcv.election_day_pk = ed.election_day_pk "
                + "left join voting_category vc "
                + "    on vc.voting_category_pk = mcv.voting_category_pk "
                + "    and vc.voting_category_id = ?2 "
                + "order by ed.election_day_date ";
        // @formatter:on
    }

    @Override
    @SuppressWarnings(WARNING_UNCHECKED)
    public List<Voting> getVotingsByElectionGroupAndVoter(final Long voterPk, final Long electionGroupPk) {
        Query query = getEm().createNamedQuery("Voting.findVotingsByElectionGroupAndVoter").setParameter(VOTER_PK, voterPk)
                .setParameter(ELECTION_GROUP_PK, electionGroupPk);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings(WARNING_UNCHECKED)
    public List<Voting> getVotingsByElectionGroupVoterAndMunicipality(final Long voterPk, final Long electionGroupPk, final Long municipalityPk) {
        return getEm().createNamedQuery("Voting.findVotingsByElectionGroupVoterAndMunicipality")
                .setParameter(VOTER_PK, voterPk)
                .setParameter(ELECTION_GROUP_PK, electionGroupPk)
                .setParameter(MUNICIPALITY_PK, municipalityPk)
                .getResultList();
    }

    @Override
    @SuppressWarnings(WARNING_UNCHECKED)
    public List<Voting> getReceivedVotingsByElectionGroupAndVoter(final Long voterPk, final Long electionGroupPk) {
        Query query = getEm().createNamedQuery("Voting.findReceivedVotingsByElectionGroupAndVoter").setParameter(VOTER_PK, voterPk)
                .setParameter(ELECTION_GROUP_PK, electionGroupPk);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings(WARNING_UNCHECKED)
    public List<Voting> getRejectedVotingsByElectionGroupAndMunicipality(final String municipalityId, final Long electionGroupPk) {
        Query query = getEm().createNamedQuery("Voting.findRejectedVotingsByElectionAndMunicipality").setParameter(MUNICIPALITY_ID, municipalityId)
                .setParameter(ELECTION_GROUP_PK, electionGroupPk);
        return query.getResultList();
    }

    @Override
    public List<Object[]> findVotingStatistics(final Long pollingPlacePk, final long municipalityPk, final Long electionGroupPk, final LocalDate startDate,
                                               final LocalDate endDate, final int votingNumberStart, final int votingNumberEnd, final boolean includeLateValidation,
                                               final String[] votingCategories,
                                               final boolean includeLateAdvanceVotings) {

        String searchString = new VotingStatisticsSql(votingCategories, startDate, endDate, includeLateValidation, includeLateAdvanceVotings).getSql();
        Query query = getEm().createNativeQuery(searchString);
        query.setParameter(VotingStatisticsSql.MUNICIPALITY_PK, municipalityPk);
        query.setParameter(VotingStatisticsSql.POLLING_PLACE_PK, pollingPlacePk);
        query.setParameter(VotingStatisticsSql.ELECTION_GROUP_PK, electionGroupPk);
        query.setParameter(VotingStatisticsSql.VOTING_NUMBER_START, votingNumberStart);
        query.setParameter(VotingStatisticsSql.VOTING_NUMBER_END, votingNumberEnd);

        if (startDate != null && endDate != null) {
            query.setParameter(VotingStatisticsSql.RECEIVED_TIMESTAMP_START, startDate.toDate());
            query.setParameter(VotingStatisticsSql.RECEIVED_TIMESTAMP_END, endDate.plusDays(1).toDate());
        }

        for (int i = 0; i < votingCategories.length; i++) {
            query.setParameter(VotingStatisticsSql.VOTING_CATEGORY_ID + i, votingCategories[i]);
        }

        return query.getResultList();
    }

    @Override
    public List<Object[]> findAdvanceVotingPickList(final Long pollingPlacePk, final long municipalityPk, final Long electionGroupPk,
                                                    final LocalDate startDate,
                                                    final LocalDate endDate, final int votingNumberStart, final int votingNumberEnd) {
        // @formatter:off
        StringBuilder searchString = new StringBuilder("SELECT v.name_line, cv.voting_number, ")
                .append("CASE WHEN (NOT v.approved) OR (voter_area.municipality_pk != :municipality_pk) THEN '")
                .append(NOT_IN_ELECTORAL_ROLL)
                .append("' WHEN v.statuskode = '5' THEN '")
                .append(DEAD_VOTER)
                .append("' ELSE '")
                .append(MULTIPLE_VOTES)
                .append("' END status, ")
                .append("v.voter_id, cv.received_timestamp, vc.voting_category_id, vc.voting_category_name, v.fictitious FROM voting cv ")
                .append("JOIN mv_area a ON (a.polling_place_pk = cv.polling_place_pk AND a.area_level = 6) ")
                .append("JOIN voting_category vc ON (vc.voting_category_pk = cv.voting_category_pk AND vc.voting_category_id IN ('FI', 'FU', 'FB', 'FE')) ")
                .append("JOIN voter v ON (v.voter_pk = cv.voter_pk) ")
                .append("LEFT JOIN mv_area voter_area ON (v.mv_area_pk = voter_area.mv_area_pk) ")
                .append("WHERE a.municipality_pk = :municipality_pk " + "AND cv.voting_rejection_pk IS NULL ")
                .append("AND cv.late_validation IS FALSE " + "AND cv.election_group_pk = :election_group_pk ")
                .append("AND (:polling_place_pk = 0 OR a.polling_place_pk = :polling_place_pk) ")
                .append("AND cv.validation_timestamp IS NULL ")
                .append("AND (cv.received_timestamp BETWEEN :received_timestamp_start AND :received_timestamp_end) ")
                .append("AND (:voting_number_start = 0 OR cv.voting_number BETWEEN :voting_number_start AND :voting_number_end) ")
                .append("AND (v.statuskode = '5' OR NOT v.approved OR (voter_area.municipality_pk != :municipality_pk) OR EXISTS ( ")
                .append("SELECT 1 FROM voting cv2 JOIN voting_category vc2 ")
                .append("ON (vc2.voting_category_pk = cv2.voting_category_pk AND vc2.voting_category_id != 'FA') ")
                .append("WHERE cv2.election_group_pk = cv.election_group_pk AND cv2.voter_pk = cv.voter_pk ")
                .append("AND cv2.voting_pk != cv.voting_pk AND cv2.voting_rejection_pk IS NULL)) " + "ORDER BY status, cv.voting_number;");
        // @formatter:on
        Query query = getEm().createNativeQuery(searchString.toString());
        query.setParameter(MUNICIPALITYPK, municipalityPk);
        query.setParameter("polling_place_pk", pollingPlacePk);
        query.setParameter(ELECTIONGROUPPK, electionGroupPk);
        query.setParameter("voting_number_start", votingNumberStart);
        query.setParameter("voting_number_end", votingNumberEnd);
        query.setParameter("received_timestamp_start", startDate.toDate());
        query.setParameter("received_timestamp_end", endDate.plusDays(1).toDate());

        return query.getResultList();
    }

    @Override
    public List<Object[]> findElectionDayVotingPickList(final long municipalityPk, final Long electionGroupPk, final int votingNumberStart,
                                                        final int votingNumberEnd, final String... votingCats) {

        StringBuilder searchString = new StringBuilder("SELECT v.name_line, cv.voting_number, ")
                .append("CASE WHEN (NOT v.approved) OR (voter_area.municipality_pk != :municipality_pk) THEN '")
                .append(NOT_IN_ELECTORAL_ROLL)
                .append("' WHEN v.statuskode = '5' THEN '")
                .append(DEAD_VOTER)
                .append("' ELSE '")
                .append(MULTIPLE_VOTES)
                .append("' END status, ")
                .append("v.voter_id, cv.received_timestamp, vc.voting_category_id, vc.voting_category_name, v.fictitious FROM voting cv ")
                .append("JOIN mv_area a ON (a.polling_place_pk = cv.polling_place_pk AND a.area_level = 6) ")
                .append("JOIN voting_category vc ON (vc.voting_category_pk = cv.voting_category_pk ")
                .append("AND vc.voting_category_id IN (")
                .append(toSqlString(votingCats))
                .append(")) ")
                .append("JOIN voter v ON (v.voter_pk = cv.voter_pk) ")
                .append("LEFT JOIN mv_area voter_area ON (v.mv_area_pk = voter_area.mv_area_pk) ")
                .append("WHERE a.municipality_pk = :municipality_pk ")
                .append("AND cv.voting_rejection_pk IS NULL ")
                .append("AND cv.election_group_pk = :election_group_pk ")
                .append("AND cv.validation_timestamp IS NULL ")
                .append("AND (:voting_number_start = 0 OR cv.voting_number BETWEEN :voting_number_start AND :voting_number_end) ")
                .append("AND (v.statuskode = '5' OR NOT v.approved OR (voter_area.municipality_pk != :municipality_pk) OR EXISTS ")
                .append("( SELECT 1 FROM voting cv2 JOIN voting_category vc2 ")
                .append("ON (vc2.voting_category_pk = cv2.voting_category_pk AND vc2.voting_category_id != 'FA') ")
                .append("WHERE cv2.election_group_pk = cv.election_group_pk " + "AND cv2.voter_pk = cv.voter_pk ")
                .append("AND cv2.voting_pk != cv.voting_pk AND cv2.voting_rejection_pk IS NULL)) ")
                .append("AND (cv.late_validation = true or vc.voting_category_id not IN ('FI', 'FU', 'FB', 'FE')) ")
                .append("ORDER BY status, cv.voting_number;");

        Query query = getEm().createNativeQuery(searchString.toString());
        query.setParameter(MUNICIPALITYPK, municipalityPk);
        query.setParameter(ELECTIONGROUPPK, electionGroupPk);
        query.setParameter("voting_number_start", votingNumberStart);
        query.setParameter("voting_number_end", votingNumberEnd);

        return query.getResultList();
    }

    private String toSqlString(final String... votingCats) {
        StringBuilder sqlStringBuilder = new StringBuilder();
        int numberOfCatsLeft = votingCats.length;
        for (String votingCat : votingCats) {
            sqlStringBuilder.append("'").append(votingCat).append("'");
            numberOfCatsLeft--;
            if (numberOfCatsLeft != 0) {
                sqlStringBuilder.append(", ");
            }
        }
        return sqlStringBuilder.toString();
    }

    @Override
    public int updateAdvanceVotingsApproved(final Long pollingPlacePk, final long municipalityPk, final Long electionGroupPk, final LocalDate startDate,
                                            final LocalDate endDate, final int votingNumberStart, final int votingNumberEnd) {

        String sql = "UPDATE voting cvu  "
                + "SET validation_timestamp = current_timestamp, approved = TRUE, mv_area_pk = v.mv_area_pk "
                + "FROM voting cv " + "JOIN mv_area a ON (a.polling_place_pk = cv.polling_place_pk AND a.area_level = 6) "
                + "JOIN voting_category vc ON (vc.voting_category_pk = cv.voting_category_pk AND vc.voting_category_id IN ('FI', 'FU', 'FB', 'FE')) "
                + "JOIN voter v on (v.voter_pk = cv.voter_pk)"
                + "WHERE a.municipality_pk = :municipality_pk AND (:polling_place_pk = 0 OR a.polling_place_pk = :polling_place_pk) "
                + "AND (cv.received_timestamp BETWEEN :received_timestamp_start AND :received_timestamp_end) "
                + "AND (:voting_number_start = 0 OR cv.voting_number between :voting_number_start AND :voting_number_end) "
                + "AND cv.late_validation IS FALSE "
                + "AND cv.election_group_pk = :election_group_pk "
                + "AND cv.validation_timestamp IS NULL "
                + "AND cvu.voting_pk = cv.voting_pk ;";

        Query query = getEm().createNativeQuery(sql);
        query.setParameter(MUNICIPALITYPK, municipalityPk);
        query.setParameter("polling_place_pk", pollingPlacePk);
        query.setParameter(ELECTIONGROUPPK, electionGroupPk);
        query.setParameter("voting_number_start", votingNumberStart);
        query.setParameter("voting_number_end", votingNumberEnd);
        query.setParameter("received_timestamp_start", startDate.toDateTimeAtStartOfDay().toDate());
        query.setParameter("received_timestamp_end", endDate.toDateTimeAtStartOfDay().plusDays(1).toDate());
        return query.executeUpdate();
    }

    @Override
    public int updateElectionDayVotingsApproved(final long municipalityPk, final Long electionGroupPk, final int votingNumberStart,
                                                final int votingNumberEnd, final String... votingCats) {

        // @formatter:off
        StringBuilder searchString = new StringBuilder("UPDATE voting cvu ")
                .append("SET validation_timestamp = current_timestamp, approved = TRUE, mv_area_pk = v.mv_area_pk ")
                .append("FROM voting cv ")
                .append("JOIN mv_area a ON (a.polling_place_pk = cv.polling_place_pk AND a.area_level = 6) ")
                .append("JOIN voting_category vc ON (vc.voting_category_pk = cv.voting_category_pk) ")
                .append("JOIN voter v ON (v.voter_pk = cv.voter_pk) ")
                .append("AND (vc.voting_category_id IN (").append(toSqlString(votingCats)).append(",'FI', 'FU', 'FB', 'FE') ) ")
                .append("WHERE a.municipality_pk = :municipality_pk ")
                .append("AND (:voting_number_start = 0 OR cv.voting_number between :voting_number_start AND :voting_number_end) ")
                .append("AND cvu.voting_pk = cv.voting_pk " + "AND cv.election_group_pk = :election_group_pk ")
                .append("AND cv.validation_timestamp IS NULL;");
        // @formatter:on
        Query query = getEm().createNativeQuery(searchString.toString());
        query.setParameter(MUNICIPALITYPK, municipalityPk);
        query.setParameter(ELECTIONGROUPPK, electionGroupPk);
        query.setParameter("voting_number_start", votingNumberStart);
        query.setParameter("voting_number_end", votingNumberEnd);
        return query.executeUpdate();
    }

    @Override
    public Voting findVotingByVotingNumber(no.valg.eva.admin.common.configuration.model.Municipality municipalityDto, VotingDto votingDto) {
        return findVotingByVotingNumber(
                municipalityDto.getPk(),
                votingDto.getElectionGroup().getPk(),
                votingDto.getVotingNumber(),
                votingDto.getVotingCategory().isEarlyVoting()
        );
    }

    @Override
    @SuppressWarnings(WARNING_UNCHECKED)
    public Voting findVotingByVotingNumber(final long municipalityPk, final Long electionGroupPk, final long votingNumber,
                                           final boolean earlyVoting) {
        String sql = "select cv.* "
                + "from voting cv " + "join mv_area a " + "on (a.polling_place_pk = cv.polling_place_pk "
                + "and a.area_level = 6) " + "join voting_category vc " + "on (vc.voting_category_pk = cv.voting_category_pk "
                + "and vc.early_voting = :early_voting) " + "join voter v " + "on (v.voter_pk = cv.voter_pk) " + "where a.municipality_pk = :municipality_pk "
                + "and cv.election_group_pk = :election_group_pk " + "and cv.voting_number = :voting_number " + "order by cv.voting_number;";

        Query query = getEm().createNativeQuery(sql, Voting.class);
        query.setParameter(MUNICIPALITYPK, municipalityPk);
        query.setParameter(ELECTIONGROUPPK, electionGroupPk);
        query.setParameter("voting_number", votingNumber);
        query.setParameter("early_voting", earlyVoting);
        List<Voting> votingList = query.getResultList();
        if (votingList.isEmpty()) {
            return null;
        } else {
            return votingList.get(0);
        }
    }

    @Override
    public boolean hasApprovedVoting(Voter voter) {
        String searchString = "from Voting v where v.voter.pk = :voter_pk and v.approved = true";
        Query query = getEm().createQuery(searchString);
        query.setParameter("voter_pk", voter.getPk());
        List<Voting> votingList = query.getResultList();
        return !votingList.isEmpty();
    }

    @Override
    @Deprecated
    public List<VotingCategory> findAdvanceVotingCategories() {
        List<VotingCategory> votingCategories = new ArrayList<>();

        VotingCategory votingCategory = findVotingCategoryById(FI.getId());
        if (votingCategory != null) {
            votingCategories.add(votingCategory);
        }
        votingCategory = findVotingCategoryById(FU.getId());
        if (votingCategory != null) {
            votingCategories.add(votingCategory);
        }
        votingCategory = findVotingCategoryById(FB.getId());
        if (votingCategory != null) {
            votingCategories.add(votingCategory);
        }
        votingCategory = findVotingCategoryById(FE.getId());
        if (votingCategory != null) {
            votingCategories.add(votingCategory);
        }
        return votingCategories;
    }

    @Override
    @SuppressWarnings(WARNING_UNCHECKED)
    public List<ForeignEarlyVoting> findForeignEarlyVotingsSentFromMunicipality(final Long electionGroupPk, final String municipalityId) {
        Query query = getEm().createNamedQuery("ForeignEarlyVoting.findForeignEarlyVotingsFromMunicipality").setParameter(ELECTION_GROUP_PK, electionGroupPk)
                .setParameter(MUNICIPALITY_ID, municipalityId);
        return query.getResultList();
    }

    @Override
    @Deprecated
    public List<VotingCategory> findAllVotingCategories() {
        return super.findAllEntities(VotingCategory.class);
    }

    @Override
    public void flyttStemmegivningerForVelgereI(MvArea fraOmraade, MvArea tilStemmekrets) {
        getEm()
                .createNamedQuery("Voting.flyttStemmegivningerForVelgereI")
                .setParameter(1, tilStemmekrets.getPk())
                .setParameter(2, fraOmraade.getAreaPath())
                .executeUpdate();
    }

    @Override
    public void flyttStemmegivningerAvgittI(MvArea fraOmraade, MvArea tilStemmested) {
        getEm()
                .createNamedQuery("Voting.flyttStemmegivningerAvgittI")
                .setParameter(1, tilStemmested.getPollingPlace().getPk())
                .setParameter(2, fraOmraade.getAreaPath())
                .executeUpdate();
    }

    @Override
    public List<Voting> findApprovedVotingsForMunicipality(Municipality municipality, ElectionGroup electionGroup) {
        return findApprovedVotingsForMunicipality(municipality, electionGroup, null, null);
    }

    @Override
    public List<Voting> findApprovedVotingsForMunicipality(Municipality municipality, ElectionGroup electionGroup,
                                                           no.valg.eva.admin.common.voting.VotingCategory votingCategory, Boolean lateValidation) {
        return getEm()
                .createNamedQuery("Voting.findApprovedVotingsByMvareaAndCategory", Voting.class)
                .setParameter(MUNICIPALITY_PK, municipality.getPk())
                .setParameter(ELECTION_GROUP_PK, electionGroup.getPk())
                .setParameter(VOTINGCATEGORY_ID, votingCategory == null ? null : votingCategory.getId())
                .setParameter(LATE_VALIDATION, lateValidation)
                .getResultList();
    }

    @Override
    public List<Voting> findVotingsToConfirm(Municipality municipality, ElectionGroup electionGroup) {
        return findVotingsToConfirm(municipality, electionGroup, null, null, null, null, null);
    }

    @Override
    public List<Voting> findVotingsToConfirm(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory,
                                             Boolean lateValidation) {
        return findVotingsToConfirm(municipality, electionGroup, votingCategory, lateValidation, null, null, null);
    }

    @Override
    public List<Voting> findVotingsToConfirm(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory,
                                             Boolean lateValidation, LocalDateTime startDate, LocalDateTime endDate, VotingPhase votingPhase) {
        return getEm()
                .createNamedQuery("Voting.findVotingsToConfirmByMvareaAndCategory", Voting.class)
                .setParameter(MUNICIPALITY_PK, municipality.getPk())
                .setParameter(ELECTION_GROUP_PK, electionGroup.getPk())
                .setParameter(VOTINGCATEGORY_ID, idForVotingCategory(votingCategory))
                .setParameter(LATE_VALIDATION, lateValidation)
                .setParameter(START_DATE, toDate(startDate), TemporalType.TIMESTAMP)
                .setParameter(END_DATE, toDate(endDate), TemporalType.TIMESTAMP)
                .setParameter(PHASE, votingPhase != null ? votingPhase.name(): null)
                .getResultList();
    }

    protected static String idForVotingCategory(no.valg.eva.admin.common.voting.VotingCategory vc) {
        return vc != null ? vc.getId() : null;
    }

    @Override
    public long countApprovedEnvelopeVotings(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory,
                                             boolean lateValidation, LocalDateTime startDate, LocalDateTime endDate) {
        return ((BigInteger) getEm()
                .createNamedQuery("Voting.countApprovedEnvelopeVotings")
                .setParameter(MUNICIPALITY_PK, municipality.getPk())
                .setParameter(ELECTION_GROUP_PK, electionGroup.getPk())
                .setParameter(VOTINGCATEGORY_ID, votingCategory.getId())
                .setParameter(LATE_VALIDATION, lateValidation)
                .setParameter(START_DATE, toDate(startDate), TemporalType.TIMESTAMP)
                .setParameter(END_DATE, toDate(endDate), TemporalType.TIMESTAMP)
                .getSingleResult()
        ).longValue();
    }

    @Override
    public long countRejectedEnvelopeVotings(Municipality municipality, ElectionGroup electionGroup, no.valg.eva.admin.common.voting.VotingCategory votingCategory,
                                             boolean lateValidation, LocalDateTime startDate, LocalDateTime endDate) {
        return ((BigInteger) getEm()
                .createNamedQuery("Voting.countRejectedEnvelopeVotings")
                .setParameter(MUNICIPALITY_PK, municipality.getPk())
                .setParameter(ELECTION_GROUP_PK, electionGroup.getPk())
                .setParameter(VOTINGCATEGORY_ID, votingCategory.getId())
                .setParameter(LATE_VALIDATION, lateValidation)
                .setParameter(START_DATE, toDate(startDate), TemporalType.TIMESTAMP)
                .setParameter(END_DATE, toDate(endDate), TemporalType.TIMESTAMP)
                .getSingleResult()
        ).longValue();
    }

    @Override
    public List<Voting> findRejectedVotingsForMunicipality(Municipality municipality, ElectionGroup electionGroup) {
        return findRejectedVotingsForMunicipality(municipality, electionGroup, null, null);
    }

    @Override
    public List<Voting> findRejectedVotingsForMunicipality(Municipality municipality, ElectionGroup electionGroup,
                                                           no.valg.eva.admin.common.voting.VotingCategory votingCategory, Boolean lateValidation) {
        return getEm()
                .createNamedQuery("Voting.findRejectedVotingsByMvareaAndCategory", Voting.class)
                .setParameter(MUNICIPALITY_PK, municipality.getPk())
                .setParameter(ELECTION_GROUP_PK, electionGroup.getPk())
                .setParameter(VOTINGCATEGORY_ID, votingCategory == null ? null : votingCategory.getId())
                .setParameter(LATE_VALIDATION, lateValidation)
                .getResultList();
    }

    @Override
    public List<Voting> findVotingsToConfirmForVoter(Voter voter) {
        return getEm()
                .createNamedQuery("Voting.findVotingsToConfirmForVoter", Voting.class)
                .setParameter(VOTER_PK, voter.getPk())
                .getResultList();
    }
}
