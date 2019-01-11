package no.valg.eva.admin.common.counting.service.valgnatt;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;

/**
 * Defines API for aggregating and publishing results to Valgnatt / EVA Resultat
 */
public interface ValgnattReportService extends Serializable {

	/**
	 * Finds all polling districts with voters, maps to counting (and reporting) configuration and exports the result to Valgnatt.
	 * @param userData for access check
	 * @param contestElectionPath used to identify the Contest and Election instances
	 */
	void exportGeographyAndVoters(UserData userData, ElectionPath contestElectionPath);

	/**
	 * Finds Valgnattskjema list from existing or newly created SsbReport instanses
	 * @param userData for access check
	 * @param electionPath identifies MvElection for Election
	 * @return list of Valgnattskjema
	 */
	List<Valgnattrapportering> rapporteringerForGrunnlagsdata(UserData userData, ElectionPath electionPath);

	/**
	 * @param userData for access check
	 * @param contestPath angir contest/valgdistrikt
	 * @return
	 */
	boolean kanFylketRapportere(UserData userData, ElectionPath contestPath);

	/**
	 * Finds and exports candidates for each contest in election.
	 * @param userData for access check
	 * @param electionPath used to identify the Election instances
	 */
	void exportPartiesAndCandidates(UserData userData, ElectionPath electionPath);

	/**
	 * Finner antall stemmeskjema eller oppgjørsskjema rapporter som kan rapporteres.
	 * @param userData for access check
	 * @param contestElectionPath angir contest
	 * @param reportingAreaPath angir kommune
	 * @return antall stemmeskjemaer eller oppgjørsskjemaer (alltid ett) som kan rapporteres
	 */
	long antallRapporterbare(UserData userData, ElectionPath contestElectionPath, AreaPath reportingAreaPath);

	/**
	 * @param userData for access check
	 * @param contestElectionPath angir contest
	 * @param reportingAreaPath angir kommune
	 * @return true hvis alt er rapportert, ellers false
	 */
	boolean altErRapportert(UserData userData, ElectionPath contestElectionPath, AreaPath reportingAreaPath);

		/**
		 * Finner eller lager nye Valgnattrapport instanser for å følge opp stemmeskjema rapportering.
		 * @param userData for tilgangssjekk
		 * @param electionPath angir contest/valgdistrikt
		 * @param areaPath angir kommune
		 * @return liste av Valgnattrapportering, en for hver rapportering som skal foretas, på kommune eller kretser.
		 */
	List<Valgnattrapportering> rapporteringerForStemmeskjema(UserData userData, ElectionPath electionPath, AreaPath areaPath);

	/**
	 * Lager stemmeskjema og rapporterer til Valgnatt.
	 * @param userData for tilgangssjekk
	 * @param contestPath angir MvElection for Contest
	 * @param areaPath angir MvArea det skal rapporteres for
	 * @param valgnattrapportering for å håndtere status for rapporteringen
	 */
	void rapporterStemmeskjema(UserData userData, ElectionPath contestPath, AreaPath areaPath, Valgnattrapportering valgnattrapportering);

	/**
	 * Lager oppgjørsskjemaer og rapporterer til Valgnatt.
	 * @param userData for tilgangssjekk
	 * @param contestPath angir MvElection for Contest
	 * @param areaPath angir MvArea det skal rapporteres for
	 * @param valgnattrapportering for å håndtere status for rapporteringen
	 */
	void rapporterOppgjørsskjema(UserData userData, ElectionPath contestPath, AreaPath areaPath, Valgnattrapportering valgnattrapportering);

	/**
	 * Finner eller lager ny Valgnattrapport instans for å følge opp oppgjørsskjema rapportering.
	 * @param userData for tilgangssjekk
	 * @param electionPath angir contest/valgdistrikt
	 * @param areaPath
     * @return Valgnattrapportering som representerer et oppgjørsskjema
	 */
	Valgnattrapportering rapporteringerForOppgjorsskjema(UserData userData, ElectionPath electionPath, AreaPath areaPath);
}
