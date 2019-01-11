package no.valg.eva.admin.frontend.common;

import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import java.util.List;

public interface RoleConflictHandler {

    MessageProvider getMessageProvider();

    List<ResponsibilityConflict> getResponsibilityConflicts();

    void onAcceptRoleConflict();

    String getLocalizedRoleConflictMessage();

    String getLocalizedRoleConflictExplanation();

    default String toLocalizedRoleConflict(ResponsibilityConflict conflict) {
        final String msgKey = "@role.conflict." + conflict.getType().name().toLowerCase();
        final String[] args = conflict.getMessageArguments().toArray(new String[0]);
        return getMessageProvider().getWithTranslatedParams(msgKey, args);
    }
}
