package no.evote.security;

import no.evote.constants.ElectionLevelEnum;

/**
 * This has to be implemented by entities that are used together with the @SecureEntity annotation, if the election level is defined dynamically (i.e. it can not be
 * specified up front, but is retrieved some other way).
 */
public interface ContextSecurableDynamicElection extends ContextSecurable {

	ElectionLevelEnum getActualElectionLevel();

}
