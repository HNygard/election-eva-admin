package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.model.SpesRegType;
import no.evote.model.Statuskode;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.configuration.domain.model.Aarsakskode;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;

public interface VoterService extends Serializable {
	List<Voter> findByElectionEventAndId(UserData userData, String id, Long electionEventPk);

	List<Voter> searchVoter(UserData userData, Voter voter, String countyId, String municipalityId, Integer maxResultsize, boolean approved,
			Long electionEventPk);

	List<Voter> findByManntallsnummer(UserData userData, Manntallsnummer manntallsnummer);

	Voter create(UserData userData, Voter voter);

	List<Aarsakskode> findAllAarsakskoder();

	List<SpesRegType> findAllSpesRegTypes(UserData userData);

	List<Statuskode> findAllStatuskoder(UserData userData);

	void deleteVoters(UserData userData, MvElection mvElection, MvArea mvArea);

	void deleteVotersWithoutMvArea(UserData userData, Long electionEventPk);

	void prepareNewInitialLoad(UserData userData, MvElection mvElection, MvArea mvArea);

	Voter createFictitiousVoter(UserData userData, AreaPath municipalityPath);

	Voter updateWithManualData(UserData userData, Voter voter);
}
