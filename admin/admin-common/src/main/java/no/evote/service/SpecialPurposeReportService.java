package no.evote.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;

import org.joda.time.LocalDate;

/**
 * Generates various reports that are not included in the reporting module.
 */
public interface SpecialPurposeReportService extends Serializable {

	byte[] generateElectionCard(UserData userData, Long vpk, MvArea mvArea, MvElection mvElection);

	byte[] generateScanningBoxLabel(UserData userData, ElectionPath electionPath, CountCategory countCategory, AreaPath areaPath, Integer numberOfStickers);

	byte[] generateEmptyElectionCard(UserData userData, Voter voter, ValggruppeSti valggruppeSti, KommuneSti kommuneSti, String pollingDistrictId,
			String pollingPlaceName);

	/**
	 * Generates a report over changes in electoral roll for a municipality
	 */
	byte[] generateElectoralRollHistoryForMunicipality(UserData userData, Municipality municipality, char endringstype,
			LocalDate startDate, LocalDate endDate, Long electionEventPk,
			String selectedSearchMode, Boolean searchOnlyApproved);

	byte[] generateBallots(UserData userData, ValgdistriktSti valgdistriktSti);
}
