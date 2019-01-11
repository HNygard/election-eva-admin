package no.evote.service;

import java.io.Serializable;
import java.util.List;

import no.evote.model.views.Eligibility;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Voter;

public interface EligibilityService extends Serializable {

	List<Eligibility> findTheoreticalEligibilityForVoterInGroup(UserData userData, final Voter voter, final Long electionGroupPk);
}
