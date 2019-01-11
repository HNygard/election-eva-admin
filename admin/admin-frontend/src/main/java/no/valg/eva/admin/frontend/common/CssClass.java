package no.valg.eva.admin.frontend.common;

import lombok.Getter;

public enum CssClass {

    UI_STATE_NEEDS_VERIFICATION("ui-state-needs-verification"),
    UI_STATE_NOT_STARTED("ui-state-not-started"),
    UI_STATE_EXPIRED("ui-state-expired");

    @Getter
    private String id;

    private CssClass(String id) {
        this.id = id;
    }
}
