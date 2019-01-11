package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.common.voting.LockType;

interface VotingMenuFactory {

    default String resolveIconCss(LockType lockType) {
        switch (lockType) {
            case LOCKED:
                return "eva-icon-lock";
            case UNLOCKED:
                return "eva-icon-unlocked";
            default:
                return "";
        }
    }
}
