package no.valg.eva.admin.counting.domain.service.votecount;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.AbstractCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.event.TellingEndrerStatus;

import org.apache.log4j.Logger;

public class VoteCountStatusendringTrigger {

	private static final Logger LOGGER = Logger.getLogger(VoteCountStatusendringTrigger.class);
	private Event<TellingEndrerStatus> tellingEndrerStatusEvent;

	@Inject
	public VoteCountStatusendringTrigger(Event<TellingEndrerStatus> tellingEndrerStatusEvent) {
		this.tellingEndrerStatusEvent = tellingEndrerStatusEvent;
	}

	public <T extends AbstractCount> void fireEventForStatusendring(
			T count, MvArea countingArea, MvElection contestMvElection, CountStatus oldVoteCountCountStatus, ReportingUnitTypeId reportingUnitTypeId) {
		LOGGER.debug(count.getAreaPath() + " Qualifier:" + count.getQualifier() + " Status:" + oldVoteCountCountStatus + "->" + count.getStatus() + " Category:"
				+ count.getCategory() + " ReportingUnitTypeId: " + reportingUnitTypeId);

		if (skalTriggeEventForStatusendring(count, oldVoteCountCountStatus, contestMvElection)) {
			tellingEndrerStatusEvent.fire(
					new TellingEndrerStatus(
							countingArea.areaPath(),
							count.getQualifier(),
							contestMvElection.electionPath(),
							count.getCategory(),
							reportingUnitTypeId));
			LOGGER.debug(count.getAreaPath() + " Fired event: TellingEndrerStatus");
		}
	}

	private <T extends AbstractCount> boolean skalTriggeEventForStatusendring(T count, CountStatus oldVoteCountCountStatus, MvElection contestMvElection) {
		return !contestMvElection.getContest().isOnBoroughLevel() && isValidQualifier(count) &&
				isValidStatus(count, oldVoteCountCountStatus) && isValidCategory(count, contestMvElection);
	}

	private <T extends AbstractCount> boolean isValidQualifier(T count) {
		CountQualifier qualifier = count.getQualifier();
		return qualifier == CountQualifier.PRELIMINARY || qualifier == CountQualifier.FINAL;
	}

	private <T extends AbstractCount> boolean isValidStatus(T count, CountStatus oldStatus) {
		CountStatus newStatus = count.getStatus();
		return isValidStatusTransition(oldStatus, newStatus)
				&& (newStatus == CountStatus.APPROVED || newStatus == CountStatus.TO_SETTLEMENT || newStatus == CountStatus.REVOKED);
	}

	/**
	 * Ved endelig telling skal det rapporteres uansett stemmekategori, ellers kun ved ordinaære.
	 */
	private <T extends AbstractCount> boolean isValidCategory(T count, MvElection contestMvElection) {
		CountQualifier qualifier = count.getQualifier();
		CountCategory category = count.getCategory();
		return qualifier == CountQualifier.FINAL || forhaandsstemmerEllerValgtingOrdinaer(category) || fremmedstemmeriMultiomraade(category, contestMvElection);
	}

	/**
	 * Det er kun når tellingen settes fra "ikke godkjent" til "godkjent" eller "klar til valgoppgjør" at det skal rapporteres. Dvs godkjent og klar til
	 * valgoppgjør betraktes som en status og overganger mellom disse behandles ikke.
	 */
	private boolean isValidStatusTransition(CountStatus oldStatus, CountStatus newStatus) {
		return !((oldStatus == CountStatus.APPROVED && newStatus == CountStatus.TO_SETTLEMENT)
				|| (oldStatus == CountStatus.APPROVED && newStatus == CountStatus.APPROVED));
	}

	private boolean forhaandsstemmerEllerValgtingOrdinaer(CountCategory category) {
		return category == CountCategory.VO || category == CountCategory.FO;
	}

	private boolean fremmedstemmeriMultiomraade(CountCategory category, MvElection contestMvElection) {
		return !contestMvElection.getSingleArea() && category == CountCategory.VF;
	}
}
