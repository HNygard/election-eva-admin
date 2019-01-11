package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.util.DateUtil;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class VotingOverviewUrlFactory {

    static final String VOTING_PHASE_REQUEST_PARAMETER = "phase";
    static final String FROM_DATE_REQUEST_PARAMETER = "fromDate";
    static final String TO_DATE_REQUEST_PARAMETER = "toDate";
    static final String VOTING_CATEGORY_REQUEST_PARAMETER = "votingCategory";
    static final String VOTING_STATUS_REQUEST_PARAMETER = "status";
    static final String VALIDATED_REQUEST_PARAMETER = "validated";

    static String requestUrl(VotingCategory votingCategory, VotingPhase votingPhase, LocalDate fromDate, LocalDate toDate, String votingConfirmationOverviewUrl,
                             VotingConfirmationStatus votingConfirmationStatus) {
        return votingConfirmationOverviewUrl +
                votingCategoryRequestParameter(votingCategory) +
                votingPhaseRequestParameter(votingPhase) +
                votingStatusRequestParameter(votingConfirmationStatus) +
                fromDateRequestParameter(fromDate) +
                toDateIncludingRequestParameter(toDate) +
                validatedRequestParameter(votingConfirmationStatus);
    }

    private static String validatedRequestParameter(VotingConfirmationStatus votingConfirmationStatus) {
        return formatUrlRequestParameter(VALIDATED_REQUEST_PARAMETER, votingConfirmationStatus.isValidatedStatus() + "");
    }

    private static String fromDateRequestParameter(LocalDate fromDate) {
        return formatUrlRequestParameter(FROM_DATE_REQUEST_PARAMETER, fromDateToLocalDateShortId(fromDate));
    }

    private static String fromDateToLocalDateShortId(LocalDate fromDate) {
        return toLocalDateShortId(fromDate);
    }

    private static String toDateIncludingRequestParameter(LocalDate toDate) {
        return formatUrlRequestParameter(TO_DATE_REQUEST_PARAMETER, toDateToLocalDateShortId(toDate));
    }

    private static String toDateToLocalDateShortId(LocalDate toDate) {
        return toLocalDateShortId(toDate);
    }

    private static String toLocalDateShortId(LocalDate localDate) {
        return localDate != null ?
                DateUtil.formatShortIdDate(localDate) : "";
    }

    private static String votingStatusRequestParameter(VotingConfirmationStatus votingStatus) {
        return formatUrlRequestParameter(VOTING_STATUS_REQUEST_PARAMETER, votingStatus.name());
    }

    private static String votingCategoryRequestParameter(VotingCategory votingCategory) {
        return formatUrlRequestParameter(VOTING_CATEGORY_REQUEST_PARAMETER, votingCategory.getId());
    }

    private static String votingPhaseRequestParameter(VotingPhase votingPhase) {
        return formatUrlRequestParameter(VOTING_PHASE_REQUEST_PARAMETER, votingPhase.name());
    }

    private static String formatUrlRequestParameter(String requestParameter, String value) {
        return "".concat(String.format("&%s=%s", requestParameter, value));
    }
}
