package no.evote.security;


import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;

/**
 * This has to be implemented by entities that are used together with the @SecureEntity annotation.
 */
public interface ContextSecurable {
	Long getAreaPk(final AreaLevelEnum level);

	Long getElectionPk(final ElectionLevelEnum level);
}
