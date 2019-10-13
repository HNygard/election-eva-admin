package no.valg.eva.admin.voting.repository;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingRejection;
import no.valg.eva.admin.common.voting.model.ProcessingType;
import no.valg.eva.admin.common.voting.model.SuggestedProcessingDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.voting.domain.model.Voting;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.valg.eva.admin.common.voting.model.ProcessingType.SUGGESTED_APPROVED;
import static no.valg.eva.admin.util.DateUtil.toDate;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Log4j
@NoArgsConstructor
@Default
@ApplicationScoped
public class PagingVotingRepository extends BaseRepository {

    private static final String ALIAS_SUGGESTED_VOTING_REJECTION_TABLE = "svr";
    private static final String ALIAS_SUGGESTED_PROCESSING_TEXT_VALUES = "sp";
    private static final String ALIAS_VOTING_REJECTION_TEXT_VALUES = "vrej";
    private static final String ALIAS_VOTING_REJECTION_TABLE = "vr";
    private static final String ALIAS_VOTING_TABLE = "v";
    private static final String ELECTION_GROUP_PK = "electionGroupPk";
    private static final String MUNICIPALITY_PK = "municipalityPk";
    private static final String NULLS_LAST = "NULLS LAST";
    private static final String AREA_LEVEL = "areaLevel";
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";
    
    private static final int AREA_LEVEL_POLLING_DISTRICT = 6;
    private static final int PAGING_MAX_LIMIT = 500;

    @SuppressWarnings("unchecked")
    public List<Voting> findVotings(Municipality municipality, ElectionGroup electionGroup, VotingFilters votingFilters, VotingSorting votingSorting, Map<VotingCategory, String> votingCategoryMap, Map<VotingRejection, String> votingRejectionMap,
                                    int offset, int limit) {

        validateOffsetAndLimit(offset, limit);
        String validatedVotingsCondition = validatedVotingsCondition(votingFilters.isValidatedVotings());
        String statusCondition = votingStatusCondition(votingFilters.getVotingConfirmationStatus());
        String votingCategoryCondition = votingCategoryCondition(votingFilters.getVotingCategories());
        String rejectionJoin = rejectionJoin();
        String suggestedProcessingJoin = suggestedProcessingJoin();
        String rejectionCondition = rejectionCondition(votingFilters);
        String suggestedProcessingCondition = suggestedProcessingCondition(votingFilters);
        String dateSpanCondition = dateSpanCondition(votingFilters);
        String searchCondition = searchCondition(votingFilters);
        String votingPhaseCondition = votingPhaseCondition(votingFilters);
        String processingTypeCondition = processingTypeCondition(votingFilters);
        String orderByCondition = orderByFieldCondition(votingSorting, votingFilters.isValidatedVotings());

        String query = format("SELECT * from voting %s ", ALIAS_VOTING_TABLE) +
                appendJoin(format("mv_area mva on (%s.polling_place_pk = mva.polling_place_pk and mva.area_level = :areaLevel)", ALIAS_VOTING_TABLE)) +
                appendJoin(format("voting_category vc on %s.voting_category_pk = vc.voting_category_pk", ALIAS_VOTING_TABLE)) +
                appendJoin("voter vo on v.voter_pk = vo.voter_pk") +
                appendJoin(format("operator op on %s.operator_pk = op.operator_pk", ALIAS_VOTING_TABLE)) +
                appendLeftJoin(rejectionJoin) +
                appendLeftJoin(suggestedProcessingJoin) +
                appendVotingCategoryValues(votingCategoryMap) +
                appendVotingRejectionValues(votingRejectionMap) +
                appendVotingSuggestedProcessingValues(votingRejectionMap) +
                "WHERE v.election_group_pk = :electionGroupPk " +
                "AND v.voting_number is not null " +
                appendConditionFilter("mva.municipality_pk = :municipalityPk") +
                appendConditionFilter(statusCondition) +
                appendConditionFilter(votingCategoryCondition) +
                appendConditionFilter(validatedVotingsCondition) +
                appendConditionFilter(rejectionCondition) +
                appendConditionFilter(suggestedProcessingCondition) +
                appendConditionFilter(dateSpanCondition) +
                appendConditionFilter(searchCondition) +
                appendConditionFilter(votingPhaseCondition) +
                appendConditionFilter(processingTypeCondition) +
                "ORDER BY " + orderByCondition + " " +
                "OFFSET :offset LIMIT :limit";

        log.debug("paged Votings sql: " + query);

        Query nativeQuery = getEm().createNativeQuery(query, Voting.class);
        setDateSpanParameters(nativeQuery, votingFilters);

        return nativeQuery
                .setParameter(ELECTION_GROUP_PK, electionGroup.getPk())
                .setParameter(MUNICIPALITY_PK, municipality.getPk())
                .setParameter(AREA_LEVEL, AREA_LEVEL_POLLING_DISTRICT)
                .setParameter(OFFSET, offset)
                .setParameter(LIMIT, limit)
                .getResultList();
    }

    private String processingTypeCondition(VotingFilters votingFilters) {
        if (votingFilters.getProcessingType() != null) {
            return "v.suggested_voting_rejection_pk " + (SUGGESTED_APPROVED.getId().equals(votingFilters.getProcessingType()) ? "IS NULL" : "IS NOT NULL");
        }

        return "";
    }

    private String appendVotingRejectionValues(Map<VotingRejection, String> votingRejectionMap) {
        String votingRejectionValues = votingRejectionMap.keySet().stream()
                .map(votingRejection -> format("('%s', '%s')", votingRejection.getId(), votingRejectionMap.get(votingRejection)))
                .collect(Collectors.joining(", "));
        return format(" LEFT JOIN (VALUES  %s) AS %s (id, text) on %s.voting_rejection_id=%s.id ", votingRejectionValues, ALIAS_VOTING_REJECTION_TEXT_VALUES, ALIAS_VOTING_REJECTION_TABLE, ALIAS_VOTING_REJECTION_TEXT_VALUES);
    }

    private String appendVotingSuggestedProcessingValues(Map<VotingRejection, String> votingRejectionMap) {
        String votingRejectionValues = votingRejectionMap.keySet().stream()
                .map(votingRejection -> format("('%s', '%s')", votingRejection.getId(), votingRejectionMap.get(votingRejection)))
                .collect(Collectors.joining(", "));
        return format(" LEFT JOIN (VALUES %s) AS %s(id, text) on %s.voting_rejection_id=%s.id ", votingRejectionValues, ALIAS_SUGGESTED_PROCESSING_TEXT_VALUES, ALIAS_SUGGESTED_VOTING_REJECTION_TABLE, ALIAS_SUGGESTED_PROCESSING_TEXT_VALUES);
    }

    private String appendVotingCategoryValues(Map<VotingCategory, String> votingCategoryMap) {
        String votingCategoryNameSqlValues = votingCategoryMap.keySet().stream()
                .map(votingCategoryId -> "('" + votingCategoryId + "', '" + votingCategoryMap.get(votingCategoryId) + "')")
                .collect(Collectors.joining(", "));
        return " LEFT JOIN (VALUES " + votingCategoryNameSqlValues + ") as vcats(id, text) on vc.voting_category_id=vcats.id ";
    }

    private String votingPhaseCondition(VotingFilters votingFilters) {
        if (votingFilters.getVotingPhase() != null) {
            return format("v.phase='%s'", votingFilters.getVotingPhase().name());
        }

        return "";
    }

    private String appendLeftJoin(String join) {
        if (isBlank(join)) {
            return "";
        }
        return format("LEFT JOIN %s ", join);
    }

    private String appendJoin(String join) {
        if (isBlank(join)) {
            return "";
        }
        return format("JOIN %s ", join);
    }

    private String appendConditionFilter(String query) {
        return isBlank(query) ? " " : format("AND %s ", query);
    }

    private String searchCondition(VotingFilters votingFilters) {
        String searchCondition = votingFilters.getSearchQuery();

        if (isBlank(searchCondition)) {
            return "";
        }

        Integer number = integerIfPossible(searchCondition);

        if (number != null) {
            return "v.voting_number=" + number + "";
        }

        return "(vo.first_name ilike '" + searchCondition + "%' " +
                "OR vo.last_name ilike '" + searchCondition + "%')";
    }

    private Integer integerIfPossible(String string) {
        Integer number = null;
        try {
            number = Integer.parseInt(string);
        } catch (NumberFormatException ignored) {
            //Ignored since we just want to return an integer IF the string is in fact an integer.
        }
        return number;
    }

    private void validateOffsetAndLimit(int offset, int limit) {
        if (offset < 0) {
            String message = format("Offset cannot be a negative number: [%s]", offset);
            log.info(message);
            throw new EvoteException(message);
        }

        if (limit > PAGING_MAX_LIMIT) {
            String message = format("Limit should not be bigger than max limit: %s, provided limit: %s", PAGING_MAX_LIMIT, limit);
            log.info(message);
            throw new EvoteException(message);
        }
    }

    private String orderByFieldCondition(VotingSorting votingSorting, boolean fetchingValidatedVotings) {
        String actualSortOrder = sortOrder(votingSorting);
        String orderByCondition = "vcats.id " + actualSortOrder + ", v.voting_number " + actualSortOrder + format(" %s", NULLS_LAST);
        if (!isBlank(votingSorting.getSortField())) {
            if (votingSorting.isSortByNameLine()) {
                orderByCondition = "vo.name_line " + actualSortOrder + " " + NULLS_LAST;
            } else if (votingSorting.isSortByPersonId()) {
                orderByCondition = "vo.voter_id " + actualSortOrder + " " + NULLS_LAST;
            } else if (votingSorting.isSortBySuggestedProcessing()) {
                orderByCondition = "sp.text " + actualSortOrder;
            } else if (votingSorting.isSortByVotingCategory()) {
                orderByCondition = "vcats.text " + actualSortOrder + " " + NULLS_LAST;
            } else if (votingSorting.isSortByVotingStatus()) {
                orderByCondition = (fetchingValidatedVotings ? "v.voting_rejection_pk " : "v.suggested_voting_rejection_pk ") + actualSortOrder;
            } else if (votingSorting.isSortByRejectionReason()) {
                orderByCondition = format("%s.text %s %s", ALIAS_VOTING_REJECTION_TEXT_VALUES, actualSortOrder, NULLS_LAST);
            }
        }
        return orderByCondition;
    }

    private String sortOrder(VotingSorting votingSorting) {
        return (!isBlank(votingSorting.getSortOrder()) && votingSorting.getSortOrder().toUpperCase().contains("ASC") ? "ASC" : "DESC") + " ";
    }

    public int countVotings(Municipality municipality, ElectionGroup electionGroup,
                            VotingFilters votingFilters) {
        String validatedCondition = validatedVotingsCondition(votingFilters.isValidatedVotings());
        String statusCondition = votingStatusCondition(votingFilters.getVotingConfirmationStatus());
        String votingCategoryCondition = votingCategoryCondition(votingFilters.getVotingCategories());
        String rejectionJoin = rejectionJoin();
        String suggestedProcessingJoin = suggestedProcessingJoin();
        String rejectionCondition = rejectionCondition(votingFilters);
        String suggestedProcessing = suggestedProcessingCondition(votingFilters);
        String dateSpanCondition = dateSpanCondition(votingFilters);
        String votingPhaseCondition = votingPhaseCondition(votingFilters);

        String query = "SELECT count(voting_pk) from voting v " +
                appendJoin("mv_area mva on (v.polling_place_pk = mva.polling_place_pk and mva.area_level = :areaLevel)") +
                appendJoin("voting_category vc on v.voting_category_pk = vc.voting_category_pk") +
                appendLeftJoin(rejectionJoin) +
                appendLeftJoin(suggestedProcessingJoin) +
                "WHERE v.election_group_pk = :electionGroupPk " +
                "AND v.voting_number is not null " +
                appendConditionFilter("mva.municipality_pk = :municipalityPk") +
                appendConditionFilter(statusCondition) +
                appendConditionFilter(votingCategoryCondition) +
                appendConditionFilter(validatedCondition) +
                appendConditionFilter(rejectionCondition) +
                appendConditionFilter(suggestedProcessing) +
                appendConditionFilter(dateSpanCondition) +
                appendConditionFilter(votingPhaseCondition);

        log.debug("countVotings sql: " + query);

        Query nativeQuery = getEm().createNativeQuery(query);
        setDateSpanParameters(nativeQuery, votingFilters);

        return ((BigInteger) nativeQuery
                .setParameter(ELECTION_GROUP_PK, electionGroup.getPk())
                .setParameter(MUNICIPALITY_PK, municipality.getPk())
                .setParameter(AREA_LEVEL, AREA_LEVEL_POLLING_DISTRICT)
                .getSingleResult())
                .intValue();
    }

    private String validatedVotingsCondition(boolean validated) {
        String validatedCondition = "";
        if (validated) {
            validatedCondition = "v.validation_timestamp is not null";
        } else {
            validatedCondition = "v.validation_timestamp is null";
        }
        return validatedCondition;
    }

    private String votingStatusCondition(VotingConfirmationStatus votingConfirmationStatus) {

        if (votingConfirmationStatus == null) {
            return "";
        }

        String statusCondition;
        switch (votingConfirmationStatus) {
            case APPROVED:
                statusCondition = "v.approved = TRUE";
                break;
            case TO_BE_CONFIRMED:
                statusCondition = "v.approved = FALSE AND v.voting_rejection_pk IS NULL";
                break;
            case REJECTED:
                statusCondition = "v.approved = FALSE AND v.voting_rejection_pk IS NOT NULL";
                break;
            default:
                statusCondition = "";
        }
        return statusCondition;
    }

    private String votingCategoryCondition(List<no.valg.eva.admin.common.voting.VotingCategory> votingCategories) {
        if (votingCategories != null && !votingCategories.isEmpty()) {
            return format("vc.voting_category_id IN (%s)", votingCategories.stream()
                    .map(votingCategory -> format("'%s'", votingCategory.getId()))
                    .collect(Collectors.joining(",")));
        }
        return "";
    }

    private String rejectionJoin() {
        return format("voting_rejection %s ON %s.voting_rejection_pk=v.voting_rejection_pk", ALIAS_VOTING_REJECTION_TABLE, ALIAS_VOTING_REJECTION_TABLE);
    }

    private String suggestedProcessingJoin() {
        return format("voting_rejection %s ON %s.voting_rejection_pk=v.suggested_voting_rejection_pk", ALIAS_SUGGESTED_VOTING_REJECTION_TABLE, ALIAS_SUGGESTED_VOTING_REJECTION_TABLE);
    }

    private String rejectionCondition(VotingFilters votingFilters) {
        if (!votingFilters.getVotingRejections().isEmpty()) {
            String votingRejectionIdsCommaSeparated = votingFilters.getVotingRejections().stream()
                    .map(rejection -> format("'%s'", rejection.getId()))
                    .collect(Collectors.joining(","));
            return format("%s.voting_rejection_id IN (%s)", ALIAS_VOTING_REJECTION_TABLE, votingRejectionIdsCommaSeparated);
        }
        return "";
    }

    private String suggestedProcessingCondition(VotingFilters votingFilters) {
        if (!votingFilters.getSuggestedProcessingList().isEmpty()) {
            String commaSeparatedSuggestedRejections = votingFilters.getSuggestedProcessingList().stream()
                    .filter(suggestedProcessing -> ProcessingType.SUGGESTED_REJECTED == suggestedProcessing.getProcessingType())
                    .map(suggestedRejection -> format("'%s'", suggestedRejection.getId()))
                    .collect(Collectors.joining(","));

            String suggestedProcessingCondition = commaSeparatedSuggestedRejections.length() > 0 ? format("%s.voting_rejection_id IN (%s)", ALIAS_SUGGESTED_VOTING_REJECTION_TABLE, commaSeparatedSuggestedRejections) : "";

            Optional<SuggestedProcessingDto> suggestedApprovedOptional = votingFilters.getSuggestedProcessingList().stream()
                    .filter(suggestedProcessing -> SUGGESTED_APPROVED == suggestedProcessing.getProcessingType())
                    .findFirst();

            if (suggestedApprovedOptional.isPresent()) {
                String suggestedVotingRejectionIsNullCondition = "v.suggested_voting_rejection_pk is null";
                if (suggestedProcessingCondition.length() > 0) {
                    suggestedProcessingCondition = format("(%s OR %s)", suggestedVotingRejectionIsNullCondition, suggestedProcessingCondition);
                } else {
                    suggestedProcessingCondition = suggestedVotingRejectionIsNullCondition;
                }
            }

            return suggestedProcessingCondition;
        }

        return "";
    }

    private String dateSpanCondition(VotingFilters votingFilters) {
        if (votingFilters.getFromDate() == null || votingFilters.getToDateIncluding() == null) {
            return "";
        }
        return "(v.cast_timestamp BETWEEN coalesce(:fromDate, v.cast_timestamp) AND coalesce(:toDate, v.cast_timestamp))";
    }

    private void setDateSpanParameters(Query nativeQuery, VotingFilters votingFilters) {
        if (votingFilters.getFromDate() != null && votingFilters.getToDateIncluding() != null) {
            nativeQuery.setParameter("fromDate", toDate(votingFilters.getFromDate()), TemporalType.TIMESTAMP);
            nativeQuery.setParameter("toDate", toDate(votingFilters.getToDateIncluding()), TemporalType.TIMESTAMP);
        }
    }
}
