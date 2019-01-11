package no.valg.eva.admin.configuration.repository;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.List;

import no.evote.exception.EvoteException;
import no.evote.util.EvoteProperties;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.manntall.ManntallsimportMapping;
import no.valg.eva.admin.configuration.domain.model.manntall.OmraadeMapping;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class ManntallsimportMappingRepository {

	private static final Logger LOG = Logger.getLogger(ManntallsimportMappingRepository.class);
	public static final String STANDARD_MANNTALLSIMPORT_MAPPING_FILNAVN = "eva-manntallsimportMapping.json";

	private String filnavn;

	public ManntallsimportMappingRepository() {
		filnavn = getMappingfilnavn();
	}

	private String getMappingfilnavn() {
		return EvoteProperties.getProperty(EvoteProperties.MANNTALLSIMPORT_FIL_KRETSMAPPING, STANDARD_MANNTALLSIMPORT_MAPPING_FILNAVN);
	}

	public ManntallsimportMappingRepository(String mantallsimportMappingFilnavn) {
		filnavn = mantallsimportMappingFilnavn;
	}

	public List<OmraadeMapping> finnForValghendelse(ElectionEvent valghendelse) {
		ManntallsimportMapping manntallsimportMapping = getManntallsimportMapping();
		LOG.debug("Fant " + manntallsimportMapping.getMappedeOmraader().size() + " mappingregler totalt for alle valghendelser");
		return filtrer(manntallsimportMapping, valghendelse);
	}

	private ManntallsimportMapping getManntallsimportMapping() {
		File mappingfil = new File(filnavn);
		if (mappingfil.exists()) {
			return lesMappingFraFil(mappingfil);
		} else if (this.getClass().getClassLoader().getResource(filnavn) != null) {
			return lesMappingFraClasspath();
		} else {
			return ingenMapping();
		}
	}

	private ManntallsimportMapping lesMappingFraFil(File mappingfil) {
		try {
			return new Gson().fromJson(new FileReader(mappingfil), ManntallsimportMapping.class);
		} catch (FileNotFoundException e) {
			throw new EvoteException("Mappingfil ble ikke funnet - rett etter at vi sjekket om den eksisterte. Dette skal ikke kunne skje", e);
		}
	}

	private ManntallsimportMapping lesMappingFraClasspath() {
		InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filnavn));
		return new Gson().fromJson(inputStreamReader, ManntallsimportMapping.class);
	}

	private ManntallsimportMapping ingenMapping() {
		LOG.debug("Ingen mapping-fil ved navn '" + filnavn + "' ble funnet.");
		return new ManntallsimportMapping();
	}

	private List<OmraadeMapping> filtrer(ManntallsimportMapping manntallsimportMapping, ElectionEvent valghendelse) {
		return manntallsimportMapping.getMappedeOmraader().stream()
				.filter(omraadeMapping -> new AreaPath(omraadeMapping.getFraOmraade()).getElectionEventId().equals(valghendelse.getId()))
				.collect(toList());
	}

}
