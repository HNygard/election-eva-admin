package no.valg.eva.admin.common.configuration.model.central;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.evote.dto.ConfigurationDto;
import no.evote.dto.ReportingUnitTypeDto;
import no.valg.eva.admin.common.configuration.model.election.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;

public class CentralConfigurationSummary implements Serializable {
	private boolean hasAnyMunicipalitiesWithNonElectronicMarkOffs;
	private boolean voterNumbersHaveBeenGenerated;
	private boolean reportingUnitsConfigured = false;
	private List<Country> countriesWithoutCounties;
	private List<County> countiesWithoutMunicipalities;
	private List<Municipality> municipalitiesWithoutBoroughs;
	private List<ConfigurationDto> boroughsWithoutPollingDistricts;
	private List<ConfigurationDto> pollingDistrictsWithoutVoters;
	private List<String> votersWithoutPollingDistricts;
	private List<ElectionGroup> groupsWithoutElections;
	private List<Election> electionsWithoutContests;
	private List<Municipality> municipalitiesWithoutEncompassingPollingDistricts;
	private List<Municipality> municipalitiesWithoutEncompassingBoroughs;
	private List<ConfigurationDto> municipalitiesWithPollingPlacesWithoutPollingStations = new ArrayList<>();
	private List<County> countiesUnderConfiguration = new ArrayList<>();
	private List<County> countiesApprovedConfiguration = new ArrayList<>();
	private List<Municipality> municipalitiesUnderConfiguration = new ArrayList<>();
	private List<Municipality> municipalitiesApprovedConfiguration = new ArrayList<>();
	private List<Contest> contestsUnderConfiguration = new ArrayList<>();
	private List<Contest> contestsFinishedConfiguration = new ArrayList<>();
	private List<Contest> contestsApprovedConfiguration = new ArrayList<>();
	private List<ElectionEvent> electionEventWithoutGroups = new ArrayList<>();
	private List<ReportingUnitTypeDto> reportingUnitTypeDtoList;

	public boolean canBeMovedToLocalConfiguration() {
		
		return isEmpty(countriesWithoutCounties) && isEmpty(countiesWithoutMunicipalities)
				&& isEmpty(boroughsWithoutPollingDistricts) && isMunicipalityConfigurationOk() && isEmpty(pollingDistrictsWithoutVoters)
				&& isEmpty(votersWithoutPollingDistricts) && isEmpty(electionEventWithoutGroups) && isEmpty(groupsWithoutElections)
				&& isEmpty(electionsWithoutContests) && reportingUnitsConfigured;
	}

	public boolean isMunicipalityConfigurationOk() {
		return isEmpty(municipalitiesWithoutBoroughs) && isEmpty(municipalitiesWithoutEncompassingPollingDistricts)
				&& isEmpty(municipalitiesWithoutEncompassingBoroughs);
	}

	public boolean canBeApproved() {
		return isEmpty(contestsUnderConfiguration) && isEmpty(countiesUnderConfiguration) && isEmpty(municipalitiesUnderConfiguration)
				&& isEmpty(contestsFinishedConfiguration);
	}

	public boolean allCountiesHaveApprovedConfiguration() {
		return isEmpty(countiesUnderConfiguration) && !isEmpty(countiesApprovedConfiguration);
	}

	public boolean allMunicipalitiesHaveApprovedConfiguration() {
		return isEmpty(municipalitiesUnderConfiguration) && !isEmpty(municipalitiesApprovedConfiguration);
	}

	public boolean allContestsHaveApprovedConfiguration() {
		return isEmpty(contestsFinishedConfiguration) && isEmpty(contestsUnderConfiguration) && !isEmpty(contestsApprovedConfiguration);
	}

	private boolean isEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}

	public boolean isHasAnyMunicipalitiesWithNonElectronicMarkOffs() {
		return hasAnyMunicipalitiesWithNonElectronicMarkOffs;
	}

	public void setHasAnyMunicipalitiesWithNonElectronicMarkOffs(boolean hasAnyMunicipalitiesWithNonElectronicMarkOffs) {
		this.hasAnyMunicipalitiesWithNonElectronicMarkOffs = hasAnyMunicipalitiesWithNonElectronicMarkOffs;
	}

	public boolean isVoterNumbersHaveBeenGenerated() {
		return voterNumbersHaveBeenGenerated;
	}

	public void setVoterNumbersHaveBeenGenerated(boolean voterNumbersHaveBeenGenerated) {
		this.voterNumbersHaveBeenGenerated = voterNumbersHaveBeenGenerated;
	}

	public List<Country> getCountriesWithoutCounties() {
		return countriesWithoutCounties;
	}

	public void setCountriesWithoutCounties(List<Country> countriesWithoutCounties) {
		this.countriesWithoutCounties = countriesWithoutCounties;
	}

	public List<County> getCountiesWithoutMunicipalities() {
		return countiesWithoutMunicipalities;
	}

	public void setCountiesWithoutMunicipalities(List<County> countiesWithoutMunicipalities) {
		this.countiesWithoutMunicipalities = countiesWithoutMunicipalities;
	}

	public List<Municipality> getMunicipalitiesWithoutBoroughs() {
		return municipalitiesWithoutBoroughs;
	}

	public void setMunicipalitiesWithoutBoroughs(List<Municipality> municipalitiesWithoutBoroughs) {
		this.municipalitiesWithoutBoroughs = municipalitiesWithoutBoroughs;
	}

	public List<ConfigurationDto> getBoroughsWithoutPollingDistricts() {
		return boroughsWithoutPollingDistricts;
	}

	public void setBoroughsWithoutPollingDistricts(List<ConfigurationDto> boroughsWithoutPollingDistricts) {
		this.boroughsWithoutPollingDistricts = boroughsWithoutPollingDistricts;
	}

	public List<ConfigurationDto> getPollingDistrictsWithoutVoters() {
		return pollingDistrictsWithoutVoters;
	}

	public void setPollingDistrictsWithoutVoters(List<ConfigurationDto> pollingDistrictsWithoutVoters) {
		this.pollingDistrictsWithoutVoters = pollingDistrictsWithoutVoters;
	}

	public List<String> getVotersWithoutPollingDistricts() {
		return votersWithoutPollingDistricts;
	}

	public void setVotersWithoutPollingDistricts(List<String> votersWithoutPollingDistricts) {
		this.votersWithoutPollingDistricts = votersWithoutPollingDistricts;
	}

	public List<Election> getElectionsWithoutContests() {
		return electionsWithoutContests;
	}

	public void setElectionsWithoutContests(List<Election> electionsWithoutContests) {
		this.electionsWithoutContests = electionsWithoutContests;
	}

	public List<Municipality> getMunicipalitiesWithoutEncompassingPollingDistricts() {
		return municipalitiesWithoutEncompassingPollingDistricts;
	}

	public void setMunicipalitiesWithoutEncompassingPollingDistricts(List<Municipality> municipalitiesWithoutEncompassingPollingDistricts) {
		this.municipalitiesWithoutEncompassingPollingDistricts = municipalitiesWithoutEncompassingPollingDistricts;
	}

	public List<Municipality> getMunicipalitiesWithoutEncompassingBoroughs() {
		return municipalitiesWithoutEncompassingBoroughs;
	}

	public void setMunicipalitiesWithoutEncompassingBoroughs(List<Municipality> municipalitiesWithoutEncompassingBoroughs) {
		this.municipalitiesWithoutEncompassingBoroughs = municipalitiesWithoutEncompassingBoroughs;
	}

	public List<ElectionGroup> getGroupsWithoutElections() {
		return groupsWithoutElections;
	}

	public void setGroupsWithoutElections(List<ElectionGroup> groupsWithoutElections) {
		this.groupsWithoutElections = groupsWithoutElections;
	}

	public List<ConfigurationDto> getMunicipalitiesWithPollingPlacesWithoutPollingStations() {
		return municipalitiesWithPollingPlacesWithoutPollingStations;
	}

	public void setMunicipalitiesWithPollingPlacesWithoutPollingStations(List<ConfigurationDto> municipalitiesWithPollingPlacesWithoutPollingStations) {
		this.municipalitiesWithPollingPlacesWithoutPollingStations = municipalitiesWithPollingPlacesWithoutPollingStations;
	}

	public List<County> getCountiesUnderConfiguration() {
		return countiesUnderConfiguration;
	}

	public void setCountiesUnderConfiguration(List<County> countiesUnderConfiguration) {
		this.countiesUnderConfiguration = countiesUnderConfiguration;
	}

	public List<County> getCountiesApprovedConfiguration() {
		return countiesApprovedConfiguration;
	}

	public void setCountiesApprovedConfiguration(List<County> countiesApprovedConfiguration) {
		this.countiesApprovedConfiguration = countiesApprovedConfiguration;
	}

	public List<Municipality> getMunicipalitiesUnderConfiguration() {
		return municipalitiesUnderConfiguration;
	}

	public void setMunicipalitiesUnderConfiguration(List<Municipality> municipalitiesUnderConfiguration) {
		this.municipalitiesUnderConfiguration = municipalitiesUnderConfiguration;
	}

	public List<Municipality> getMunicipalitiesApprovedConfiguration() {
		return municipalitiesApprovedConfiguration;
	}

	public void setMunicipalitiesApprovedConfiguration(List<Municipality> municipalitiesApprovedConfiguration) {
		this.municipalitiesApprovedConfiguration = municipalitiesApprovedConfiguration;
	}

	public List<Contest> getContestsUnderConfiguration() {
		return contestsUnderConfiguration;
	}

	public void setContestsUnderConfiguration(List<Contest> contestsUnderConfiguration) {
		this.contestsUnderConfiguration = contestsUnderConfiguration;
	}

	public List<Contest> getContestsFinishedConfiguration() {
		return contestsFinishedConfiguration;
	}

	public void setContestsFinishedConfiguration(List<Contest> contestsFinishedConfiguration) {
		this.contestsFinishedConfiguration = contestsFinishedConfiguration;
	}

	public List<Contest> getContestsApprovedConfiguration() {
		return contestsApprovedConfiguration;
	}

	public void setContestsApprovedConfiguration(List<Contest> contestsApprovedConfiguration) {
		this.contestsApprovedConfiguration = contestsApprovedConfiguration;
	}

	public List<ElectionEvent> getElectionEventWithoutGroups() {
		return electionEventWithoutGroups;
	}

	public void setElectionEventWithoutGroups(List<ElectionEvent> electionEventWithoutGroups) {
		this.electionEventWithoutGroups = electionEventWithoutGroups;
	}

	public boolean isReportingUnitsConfigured() {
		return reportingUnitsConfigured;
	}

	public void setReportingUnitsConfigured(boolean reportingUnitsConfigured) {
		this.reportingUnitsConfigured = reportingUnitsConfigured;
	}

	public List<ReportingUnitTypeDto> getReportingUnitTypeDtoList() {
		return reportingUnitTypeDtoList;
	}

	public void setReportingUnitTypeDtoList(List<ReportingUnitTypeDto> reportingUnitTypeDtoList) {
		this.reportingUnitTypeDtoList = reportingUnitTypeDtoList;
	}
}
