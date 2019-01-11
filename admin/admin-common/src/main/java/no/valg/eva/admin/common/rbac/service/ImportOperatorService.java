package no.valg.eva.admin.common.rbac.service;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.common.rbac.PollingPlaceResponsibleOperator;
import no.valg.eva.admin.common.rbac.VoteReceiver;

public interface ImportOperatorService {

	/**
	 * Itererer gjennom listen med AdvancedVotingOperator og oppretter nye brukere/operator om de ikke eksistrer, samt oppretter
	 * "forhåndsstemmemottakere"-OperatorRole/rollen på brukeren (om denne ikke finnes) på angitt forhåndsstemmested.
	 * @param userData
	 *            data om innlogget bruker
	 * @param earlyVotingOperatorList
	 *            liste med forhåndsstemmemottakere
	 */
	void importEarlyVoteReceiverOperator(UserData userData, List<ImportOperatorRoleInfo> earlyVotingOperatorList);

	/**
	 * Itererer gjennom listene med VotingOperator og PollingPlaceResponsibleOperator og oppretter nye brukere/operator om de ikke eksistrer, samt oppretter
	 * "ansvarlig valglokale"-OperatorRole/rollen på brukeren (om denne ikke finnes) på angitt stemmekrets.
	 *
	 *
	 * @param userData
	 *            data om innlogget bruker
	 */
	void importVotingAndPollingPlaceResponsibleOperators(UserData userData, List<VoteReceiver> votingOperatorList,
			List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleOperatorList);

}
