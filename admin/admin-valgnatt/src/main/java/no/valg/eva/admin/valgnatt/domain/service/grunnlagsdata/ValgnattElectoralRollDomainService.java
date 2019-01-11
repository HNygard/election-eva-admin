package no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;
import no.valg.eva.admin.configuration.domain.model.valgnatt.IntegerWrapper;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ReportConfiguration;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.valgnatt.ValgnattElectoralRollRepository;
import no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.ElectoralRollCountReport;

import org.apache.log4j.Logger;

/**
 * Finds electoral roll per polling district and maps this to the geography of the report configuration (if reporting is done on municipality, voters from all
 * polling districts are aggregated on municipality, if parent polling district (tellekrets) voters are aggregated on parent.)
 */
public class ValgnattElectoralRollDomainService {

	private static final Logger LOG = Logger.getLogger(ValgnattElectoralRollDomainService.class);

	private final ValgnattElectoralRollRepository valgnattElectoralRollRepository;
	private final PollingDistrictRepository pollingDistrictRepository;

	@Inject
	public ValgnattElectoralRollDomainService(ValgnattElectoralRollRepository valgnattElectoralRollRepository,
											  PollingDistrictRepository pollingDistrictRepository) {
		this.valgnattElectoralRollRepository = valgnattElectoralRollRepository;
		this.pollingDistrictRepository = pollingDistrictRepository;
	}

	/**
	 * @param mvElection mvElection data for contest (level 4 in Election hiearchy)
	 * @return ElectoralRoll with voters distributed on the configured counting and reporting geography
	 */
	public ElectoralRollCountReport findVotersAndReportingAreas(MvElection mvElection) {

		ElectoralRollCountReport electoralRollCountReport = new ElectoralRollCountReport(mvElection.getElectionEventId(), mvElection.getElectionName(),
				mvElection.getElection().getValgtype(), mvElection.electionYear());

		ElectionPath electionPath = mvElection.getElection().electionPath();
		Map<Long, ElectoralRollCount> electoralRollForPollingDistrictMap = findElectoralRollForPollingDistrict(electionPath);
		List<ReportConfiguration> reportConfigurations = valgnattElectoralRollRepository.valgnattReportConfiguration(mvElection);

		for (ReportConfiguration reportConfiguration : reportConfigurations) {
			if (reportConfiguration.isParent()) {
				leggTilTellekretser(electoralRollCountReport, electoralRollForPollingDistrictMap, reportConfiguration);
			} else if (reportConfiguration.isByMunicipality()) {
				leggTilSentraltSamlede(electoralRollCountReport, electoralRollForPollingDistrictMap, reportConfiguration);
			} else {
				leggTilAndreKretser(electoralRollCountReport, electoralRollForPollingDistrictMap, reportConfiguration, mvElection.getElectionEvent());
			}
		}
		return electoralRollCountReport;
	}

	private Map<Long, ElectoralRollCount> findElectoralRollForPollingDistrict(ElectionPath electionPath) {
		List<ElectoralRollCount> electoralRollCounts = valgnattElectoralRollRepository.valgnattElectoralRoll(electionPath);

		Map<Long, ElectoralRollCount> electoralRollForPollingDistrictMap = new HashMap<>();
		for (ElectoralRollCount electoralRollCount : electoralRollCounts) {
			electoralRollForPollingDistrictMap.put(electoralRollCount.getPollingDistrictPk(), electoralRollCount);
		}
		return electoralRollForPollingDistrictMap;
	}

	private void leggTilTellekretser(ElectoralRollCountReport electoralRollCountReport, Map<Long, ElectoralRollCount> electoralRollForPollingDistrictMap,
									 ReportConfiguration reportConfiguration) {
		PollingDistrict parentPollingDistrict = pollingDistrictRepository.findByPk(reportConfiguration.getPollingDistrictPk());
		electoralRollCountReport.add(createElectoralRollForPollingDistricts(
				electoralRollForPollingDistrictMap,
				reportConfiguration,
				parentPollingDistrict.getChildPollingDistricts()));
	}

	private void leggTilSentraltSamlede(ElectoralRollCountReport electoralRollCountReport, Map<Long, ElectoralRollCount> electoralRollForPollingDistrictMap,
										ReportConfiguration reportConfiguration) {
		PollingDistrict municipalityPollingDistrict = pollingDistrictRepository.findByPk(reportConfiguration.getPollingDistrictPk());
		Municipality municipality = municipalityPollingDistrict.getBorough().getMunicipality();
		electoralRollCountReport.add(createElectoralRollForPollingDistricts(
				electoralRollForPollingDistrictMap,
				reportConfiguration,
				municipality.pollingDistricts()));
	}

	private ElectoralRollCount createElectoralRollForPollingDistricts(
			Map<Long, ElectoralRollCount> electoralRollForPollingDistrictMap,
			ReportConfiguration reportConfiguration,
			Collection<PollingDistrict> childPollingDistricts) {

		ElectoralRollCount electoralRollCountForParentPollingDistrict = createElectoralRollForPollingDistrict(reportConfiguration);

		for (PollingDistrict pollingDistrict : childPollingDistricts) {
			ElectoralRollCount electoralRollCount = electoralRollForPollingDistrictMap.get(pollingDistrict.getPk());
			if (electoralRollCount == null) {
				LOG.info("Fant ikke manntall for polling district med pk " + reportConfiguration.getPollingDistrictPk()
						+ ". Dette er forventet oppførsel for tekniske kretser, tellekretser og kommunekretser uten manntall.");
				continue;
			}
			electoralRollCountForParentPollingDistrict = electoralRollCountForParentPollingDistrict.add(electoralRollCount);
		}
		return electoralRollCountForParentPollingDistrict;
	}

	private ElectoralRollCount createElectoralRollForPollingDistrict(ReportConfiguration config) {
		return ElectoralRollCount.emptyInstance(
				config.getPollingDistrictPk(),
				config.getPollingDistrictId(),
				config.getPollingDistrictName(),
				config.getMunicipalityId(),
				config.getMunicipalityName(),
				config.getCountyName(),
				config.getCountyId(),
				config.getBoroughId(),
				config.getBoroughName(),
				config.getValgdistriktId(),
				config.getValgdistriktName(),
				config.getContestPk());
	}

	private void leggTilAndreKretser(ElectoralRollCountReport electoralRollCountReport, Map<Long, ElectoralRollCount> stemmeberettigedePrStemmekrets,
									 ReportConfiguration reportConfiguration, ElectionEvent valghendelse) {
		ElectoralRollCount electoralRollCountForPollingDistrict = stemmeberettigedePrStemmekrets.get(reportConfiguration.getPollingDistrictPk());
		if (electoralRollCountForPollingDistrict == null) {
			if (reportConfiguration.isMunicipalityPollingDistrict()) {
				electoralRollCountForPollingDistrict = summerMinus30Kretser(stemmeberettigedePrStemmekrets.values(), reportConfiguration, valghendelse);
				electoralRollCountReport.add(electoralRollCountForPollingDistrict.withValgdistrikt(reportConfiguration.getValgdistriktId(), reportConfiguration
						.getValgdistriktName()));
			} else {
				LOG.warn("Fant ikke manntall for polling district med pk " + reportConfiguration.getPollingDistrictPk() + ". Sjekk at konfigurasjonen er korrekt.");
				return;
			}
		} else {
			electoralRollCountReport.add(electoralRollCountForPollingDistrict.withValgdistrikt(reportConfiguration.getValgdistriktId(), reportConfiguration
					.getValgdistriktName()));
		}
	}

	private ElectoralRollCount summerMinus30Kretser(Collection<ElectoralRollCount> sbPrStemmekrets, ReportConfiguration reportConfiguration, ElectionEvent
			valghendelse) {
		ElectoralRollCount electoralRollCountForPollingDistrict;
		electoralRollCountForPollingDistrict = createElectoralRollForPollingDistrict(reportConfiguration);
		List<Integer> pkListe = valgnattElectoralRollRepository.pollingDistrictPkListeForBarneOmråder(valghendelse, reportConfiguration.getContestPk()).stream()
				.map(IntegerWrapper::get).collect(Collectors.toList());
		int voterTotal = sbPrStemmekrets.stream()
				.filter(e -> pkListe.stream().mapToInt(Integer::intValue).anyMatch(i -> i == e.getPollingDistrictPk().intValue()))
				.mapToInt(ElectoralRollCount::getVoterTotal)
				.sum();
		electoralRollCountForPollingDistrict = electoralRollCountForPollingDistrict.setVoterTotal(voterTotal);
		return electoralRollCountForPollingDistrict;
	}
}
