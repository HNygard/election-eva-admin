package no.valg.eva.admin.common;

/**
 * Enumeration of the business processes in the election domain
 */
public enum Process {
	NONE,
	AUTHENTICATION,
	CENTRAL_CONFIGURATION,
	LOCAL_CONFIGURATION,
	ELECTORAL_ROLL,
	AUTHORIZATION,
	VOTING,
	COUNTING,
	SETTLEMENT,
	FORECASTING
}
