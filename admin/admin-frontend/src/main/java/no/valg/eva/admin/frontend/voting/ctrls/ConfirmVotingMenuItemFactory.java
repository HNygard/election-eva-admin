package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.ConfirmationCategoryStatus;
import org.joda.time.LocalDate;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static no.valg.eva.admin.common.voting.Tense.PRESENT;
import static no.valg.eva.admin.frontend.common.CssClass.UI_STATE_EXPIRED;
import static no.valg.eva.admin.frontend.common.CssClass.UI_STATE_NEEDS_VERIFICATION;
import static no.valg.eva.admin.frontend.common.CssClass.UI_STATE_NOT_STARTED;
import static no.valg.eva.admin.util.DateUtil.endOfDay;
import static no.valg.eva.admin.util.DateUtil.startOfDay;

@ViewScoped
@Named
@NoArgsConstructor
public class ConfirmVotingMenuItemFactory implements VotingMenuFactory, Serializable {

    private static final long serialVersionUID = -1349450773774460032L;

    @Inject
    private MessageProvider messageProvider;

    List<ConfirmVotingMenuItem> buildMenuItems(List<ConfirmationCategoryStatus> confirmationCategoryStatuses) {
        PrimitiveIterator.OfInt menuItemIndexIterator = IntStream.range(0, confirmationCategoryStatuses.size()).iterator();

        return confirmationCategoryStatuses.stream()
                .map(confirmationCategoryStatus -> ConfirmVotingMenuItem.builder()
                        .id(menuItemIndexIterator.nextInt())
                        .votingCategory(confirmationCategoryStatus.getVotingCategory())
                        .votingPhase(confirmationCategoryStatus.getVotingPhase())
                        .backgroundCss(resolveMenuItemBackgroundCss(confirmationCategoryStatus))
                        .menuLabel(toMessage(
                                confirmationCategoryStatus.getMessageProperty()))
                        .dataAftId(resolveDataAftId(confirmationCategoryStatus.getMessageProperty()))
                        .categoryOpen(resolveCategoryOpen(confirmationCategoryStatus.getTense(), confirmationCategoryStatus))
                        .categoryClosedMessage(resolveNotOpenForConfirmationMessage(confirmationCategoryStatus.getTense(),
                                confirmationCategoryStatus.getStartingDate()))
                        .startDate(startOfDay(confirmationCategoryStatus.getStartingDate()))
                        .endDateIncluding(endOfDay(confirmationCategoryStatus.getEndingDate().minusDays(1)))
                        .build())
                .collect(Collectors.toList());
    }

    private String resolveMenuItemBackgroundCss(ConfirmationCategoryStatus confrmationCategoryStatuses) {

        if (confrmationCategoryStatuses.isNeedsVerification()) {
            return UI_STATE_NEEDS_VERIFICATION.getId();
        }

        switch (confrmationCategoryStatuses.getTense()) {
            case FUTURE:
                return UI_STATE_NOT_STARTED.getId();
            case PAST:
                return UI_STATE_EXPIRED.getId();
            case PRESENT:
            default:
                return "";
        }
    }

    private String getVotingCategoryStartDate(LocalDate categoryStartDate) {
        return DateUtil.getFormattedShortDate(categoryStartDate);
    }

    private boolean resolveCategoryOpen(Tense currentTense, ConfirmationCategoryStatus confirmationCategoryStatus) {
        return PRESENT == currentTense || confirmationCategoryStatus.isNeedsVerification();
    }

    private String toMessage(String messageProperty) {
        return messageProvider.get(messageProperty);
    }

    private String resolveDataAftId(String messageProperty) {
        Pattern p = Pattern.compile("\\[(.*?)]");
        Matcher m = p.matcher(messageProperty);

        if (m.find()) {
            return m.group(1);
        }

        return "";
    }

    private String resolveNotOpenForConfirmationMessage(Tense tense, LocalDate categoryStartDate) {
        switch (tense) {
            case PAST:
                return messageProvider.get("@voting.confirmation.expired.message");
            case FUTURE:
                return messageProvider.get("@voting.confirmation.not.started.message", getVotingCategoryStartDate(categoryStartDate
                ));
            default:
                return "";
        }
    }
}
