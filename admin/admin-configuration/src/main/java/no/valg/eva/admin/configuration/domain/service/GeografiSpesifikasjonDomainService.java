package no.valg.eva.admin.configuration.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;

public class GeografiSpesifikasjonDomainService {

	private MvAreaRepository mvAreaRepository;

	@Inject
	public GeografiSpesifikasjonDomainService(MvAreaRepository mvAreaRepository) {
		this.mvAreaRepository = mvAreaRepository;
	}

	public GeografiSpesifikasjon lagGeografiSpesifikasjonForKommuner(String valghendelseId) {
		List<MvAreaDigest> kommuner = mvAreaRepository.findDigestsByPathAndLevel(AreaPath.from(valghendelseId), AreaLevelEnum.MUNICIPALITY);
		List<String> kommuneIdListe = kommuner.stream()
			.map(digest -> AreaPath.from(digest.getAreaPath()).getMunicipalityId())
			.collect(Collectors.toList());
		return new GeografiSpesifikasjon(kommuneIdListe, new ArrayList<>());
	}

	public GeografiSpesifikasjon lagGeografiSpesifikasjonForKretser(String valghendelseId) {
		List<MvAreaDigest> kretser = mvAreaRepository.findDigestsByPathAndLevel(AreaPath.from(valghendelseId), AreaLevelEnum.POLLING_DISTRICT);
		List<String> kretsIdListe = kretser.stream()
			.map(digest -> AreaPath.from(digest.getAreaPath()).getMunicipalityId() + "." + AreaPath.from(digest.getAreaPath()).getPollingDistrictId())
			.collect(Collectors.toList());
		return new GeografiSpesifikasjon(new ArrayList<>(), kretsIdListe);
	}

	public GeografiSpesifikasjon lagGeografiSpesifikasjonForBegrensetAntallKretser(String valghendelseId, int maxAntallKretserPerKommune) {
		Map<String, Integer> antallKretserSaaLangtPerKommune = new HashMap<>();
		List<MvAreaDigest> kretser = mvAreaRepository.findDigestsByPathAndLevel(AreaPath.from(valghendelseId), AreaLevelEnum.POLLING_DISTRICT);
		List<String> kretsIdListe = kretser.stream()
			.filter(digest -> maxAntallPerKretsFilter(maxAntallKretserPerKommune, digest, antallKretserSaaLangtPerKommune))
			.map(digest -> AreaPath.from(digest.getAreaPath()).getMunicipalityId() + "." + AreaPath.from(digest.getAreaPath()).getPollingDistrictId())
			.collect(Collectors.toList());
		return new GeografiSpesifikasjon(new ArrayList<>(), kretsIdListe);
	}

	private boolean maxAntallPerKretsFilter(int maxAntallKretserPerKommune, MvAreaDigest digest, Map<String, Integer> antallKretserSaaLangtPerKommune) {
		String municipalityId = AreaPath.from(digest.getAreaPath()).getMunicipalityId();
		String pollingDistrictId = AreaPath.from(digest.getAreaPath()).getPollingDistrictId();
		return kretsSomSkalVæreMedUansett(pollingDistrictId)
			|| sjekkOgOppdaterAntallPerKrets(maxAntallKretserPerKommune, antallKretserSaaLangtPerKommune, municipalityId);
	}

	private boolean sjekkOgOppdaterAntallPerKrets(int maxAntallKretserPerKommune, Map<String, Integer> antallKretserSaaLangtPerKommune, String municipalityId) {
		if (antallKretserSaaLangtPerKommune.containsKey(municipalityId)) {
			Integer antallSaaLangt = antallKretserSaaLangtPerKommune.get(municipalityId);
			if (antallSaaLangt >= maxAntallKretserPerKommune) {
				return false;
			} else {
				antallKretserSaaLangtPerKommune.put(municipalityId, antallSaaLangt + 1);
				return true;
			}
		} else {
			antallKretserSaaLangtPerKommune.put(municipalityId, 1);
			return true;
		}
	}

	private boolean kretsSomSkalVæreMedUansett(String pollingDistrictId) {
		return "0000".equals(pollingDistrictId);
	}

	public GeografiSpesifikasjon lagGeografiSpesifikasjonForStemmesteder(String valghendelseId) {
		List<MvAreaDigest> stemmesteder = mvAreaRepository.findDigestsByPathAndLevel(AreaPath.from(valghendelseId), AreaLevelEnum.POLLING_PLACE);
		List<String> stemmestedIdListe = stemmesteder.stream()
			.map(digest -> AreaPath.from(digest.getAreaPath()).getMunicipalityId()
				+ "." + AreaPath.from(digest.getAreaPath()).getPollingDistrictId()
				+ "." + AreaPath.from(digest.getAreaPath()).getPollingPlaceId())
			.collect(Collectors.toList());
		return new GeografiSpesifikasjon(new ArrayList<>(), new ArrayList<>(), stemmestedIdListe);
	}
}
