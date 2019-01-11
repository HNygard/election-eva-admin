package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.evote.validation.PollingStationsDivisionValidator;
import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.manntall.papir.Rodefordeler;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.PollingStationRepository;

public class PollingStationServiceBean {
	@Inject
	private PollingStationRepository pollingStationRepository;
	@Inject
	private PollingPlaceRepository pollingPlaceRepository;

	/**
	 * Calculates the number of voters and division characters on each polling station based on the number of polling stations
	 * @return list of polling station divisions containing the division characters and the number of voters
	 */
	public List<Rode> getPollingStationDivision(Integer numberOfPollingStations, PollingPlace pollingPlace) {
		throwIllegalArgumentIfMunicipalityUsesElectronicMarkoffs(pollingPlace);
		if (numberOfPollingStations == 0) {
			throw new IllegalArgumentException("Number of groups can not be null");
		} else if (numberOfPollingStations > (EvoteConstants.ALPHABET.length() / 2)) {
			throw new IllegalArgumentException("Number of groups are to high");
		}
		List<Voter> electoralRoll = pollingStationRepository.getElectoralRollForPollingPlace(pollingPlace);
		if (electoralRoll.isEmpty()) {
			return null;
		}
		return calculatePollingStationDivision(numberOfPollingStations, electoralRoll);
	}

	private List<Rode> calculatePollingStationDivision(Integer numberOfPollingStations, List<Voter> electoralRoll) {
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		return psd.calculateMostEvenDivision(numberOfPollingStations);
	}

	/**
	 * Calculates the number of voters and division characters on each polling station based on the number of polling stations
	 * @return list of polling station divisions containing the division characters and the number of voters
	 */
	public List<Rode> getPollingStationDivision(List<Rode> divisionList, PollingPlace pollingPlace) {
		throwIllegalArgumentIfMunicipalityUsesElectronicMarkoffs(pollingPlace);
		PollingStationsDivisionValidator validator = new PollingStationsDivisionValidator();
		if (!validator.isValid(divisionList)) {
			throw new IllegalArgumentException("The polling station division is not valid");
		}
		List<Voter> electoralRoll = pollingStationRepository.getElectoralRollForPollingPlace(pollingPlace);
		if (electoralRoll.isEmpty()) {
			return null;
		}
		return calculatePollingStationDivision(divisionList, electoralRoll);
	}

	private List<Rode> calculatePollingStationDivision(final List<Rode> roder, final List<Voter> manntall) {
		Rodefordeler rodefordeler = new Rodefordeler(manntall);
		rodefordeler.tellAntallVelgerePerRode(roder);
		return roder;
	}

	/**
	 * @return current division list for the polling station under parameter polling place
	 */
	public List<Rode> getDivisionListForPollingPlace(PollingPlace pollingPlace) {
		throwIllegalArgumentIfMunicipalityUsesElectronicMarkoffs(pollingPlace);
		List<Rode> divisionList = new ArrayList<>();
		for (PollingStation pollingStation : pollingStationRepository.findByPollingPlace(pollingPlace.getPk())) {
			divisionList.add(new Rode(pollingStation.getId(), pollingStation.getFirst(), pollingStation.getLast()));
		}
		if (divisionList.isEmpty()) {
			return null;
		}
		return getPollingStationDivision(divisionList, pollingPlace);
	}

	/**
	 * Removes all polling stations connected to the polling place and creates new ones representing the division in the parameter divisionList. Polling station
	 * ID is set to be a sequential number in which the station is created
	 */
	public void savePollingStationConfiguration(UserData userData, PollingPlace detachedPollingPlace, List<Rode> divisionList) {
		throwIllegalArgumentIfMunicipalityUsesElectronicMarkoffs(detachedPollingPlace);
		PollingStationsDivisionValidator validator = new PollingStationsDivisionValidator();

		Rodefordeler.sorterRoder(divisionList);
		if (!validator.isValid(divisionList)) {
			throw new IllegalArgumentException("The polling station division is not valid");
		}

		PollingPlace pollingPlace = pollingPlaceRepository.findByPk(detachedPollingPlace.getPk());

		// Remove the old polling stations
		pollingStationRepository.delete(userData, pollingStationRepository.findByPollingPlace(pollingPlace.getPk()));

		// Create new polling stations
		for (int i = 0; i < divisionList.size(); i++) {
			Rode psdd = divisionList.get(i);
			PollingStation pollingStation = new PollingStation();
			pollingStation.setFirst(psdd.getFra());
			pollingStation.setLast(psdd.getTil());
			pollingStation.setId(String.format("%02d", i + 1));
			pollingStation.setPollingPlace(pollingPlace);

			pollingStationRepository.create(userData, pollingStation);
		}
	}

	private void throwIllegalArgumentIfMunicipalityUsesElectronicMarkoffs(final PollingPlace pollingPlace) {
		// Check that the municipality owning the polling place doesn't have electronic markoffs
		if (pollingPlace.getPollingDistrict().getBorough().getMunicipality().isElectronicMarkoffs()) {
			throw new IllegalArgumentException("Polling stations can not be defined if the municipality uses electronik markoffs");
		}
	}
}
