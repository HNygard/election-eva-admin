package no.evote.service.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;

@Default
@ApplicationScoped
public class MvAreaServiceBean {
	private static final String PERIOD = ".";

	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private PollingDistrictRepository pollingDistrictRepository;
	@Inject
	private MunicipalityRepository municipalityRepository;

	public MvAreaServiceBean() {

	}

	public MvArea findByPollingDistrict(Long pollingDistrictPk) {
		PollingDistrict pollingDistrict = pollingDistrictRepository.findByPk(pollingDistrictPk);
		Borough borough = pollingDistrict.getBorough();
		Municipality municipality = borough.getMunicipality();
		County county = municipality.getCounty();
		Country country = county.getCountry();
		ElectionEvent electionEvent = country.getElectionEvent();
		String path = electionEvent.getId() + PERIOD + country.getId() + PERIOD + county.getId()
				+ PERIOD + municipality.getId() + PERIOD + borough.getId() + PERIOD + pollingDistrict.getId();
		return mvAreaRepository.findSingleByPath(AreaPath.from(path));
	}

	public MvArea findByMunicipality(Long municipalityPk) {
		Municipality municipality = municipalityRepository.findByPk(municipalityPk);
		County county = municipality.getCounty();
		Country country = county.getCountry();
		ElectionEvent electionEvent = country.getElectionEvent();
		String path = electionEvent.getId() + PERIOD + country.getId() + PERIOD + county.getId() + PERIOD + municipality.getId();
		return mvAreaRepository.findSingleByPath(AreaPath.from(path));
	}

	public MvArea findSingleByPath(AreaPath path) {
		return mvAreaRepository.findSingleByPath(path);
	}
}
