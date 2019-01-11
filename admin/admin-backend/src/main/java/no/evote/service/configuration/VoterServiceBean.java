package no.evote.service.configuration;

import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.DELETE_VOTERS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.model.Batch;
import no.evote.security.UserData;
import no.evote.service.BatchServiceBean;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.BoroughRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class VoterServiceBean {
	private static final String ID_UNKNOWN = "???";
	private static final Logger LOGGER = Logger.getLogger(VoterServiceBean.class);

	@Inject
	private BatchServiceBean batchService;
	@Inject
	private BakgrunnsjobbDomainService bakgrunnsjobbDomainService;
	@Inject
	private BoroughRepository boroughRepository;
	@Inject
	private CountryServiceBean countryService;
	@Inject
	private CountyServiceBean countyService;
	@Inject
	private MunicipalityRepository municipalityRepository;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private PollingPlaceRepository pollingPlaceRepository;

	public Voter updateWithManualData(UserData userData, Voter updatedVoter) {
		Voter oldVoter = voterRepository.findByPk(updatedVoter.getPk());
		if (oldVoter == null) {
			throw new EvoteException("Voters can only be created with SKD data");
		}
		updatedVoter.setEndringstype(updatedVoter.getCorrectEndringsType(oldVoter));
		return voterRepository.update(userData, updatedVoter);
	}

	public void deleteVoters(UserData userData, MvElection mvElection, MvArea mvArea, boolean slettHistorikk) {
		LOGGER.info("delete voters - start");
		Batch batch = batchService.createBatchForDeleteVoters(userData, DELETE_VOTERS, mvArea.toString());
		LOGGER.info("delete voters - created batch with pk " + batch.getPk());
		try {
			voterRepository.deleteVoters(mvElection.getPath(), mvArea.getPath());
			if (slettHistorikk) {
				voterRepository.deleteAuditVoters(mvElection.getPath());
			}
			bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(userData, batch, BATCH_STATUS_COMPLETED_ID);
			LOGGER.info("delete voters - end");
		} catch (Exception e) {
			bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(userData, batch, BATCH_STATUS_FAILED_ID);
			LOGGER.error("delete voters - error: ", e);
		}
	}

	public void prepareNewInitialLoad(UserData userData, MvElection mvElection, MvArea mvArea) {
		deleteVoters(userData, mvElection, mvArea, true);
	}

	/**
	 * Creates a voter with a fictitious SSN and
	 * 
	 * @return voter with a fictitious SSN
	 */
	public Voter createFictitiousVoter(UserData userData, AreaPath municipalityPath) {
		municipalityPath.assertMunicipalityLevel();

		MvArea municipalityArea = mvAreaRepository.findSingleByPath(municipalityPath);
		PollingDistrict municipalityDistrict = municipalityArea.getMunicipality().getMunicipalityPollingDistrict();
		PollingPlace firstPollingPlace = pollingPlaceRepository.findFirstPollingPlace(municipalityDistrict.getPk());
		if (firstPollingPlace == null) {
			throw new EvoteException("@voting.noPollingPlaceInMunicipalityDistrict");
		}
		MvArea mvArea = mvAreaRepository.findSingleByPath(municipalityDistrict.areaPath());

		DateTimeFormatter dateFormat = DateTimeFormat.forPattern("ddMMyyyy").withLocale(userData.getJavaLocale());

		LocalDate startDate = dateFormat.parseLocalDate("01011950");
		LocalDate endDate = dateFormat.parseLocalDate("01011990");
		String fictitiousSSN = findFreeFictitiousSSN(startDate, endDate, mvArea);

		Voter fictitiousVoter = buildFictitiousVoter(fictitiousSSN, mvArea);

		return voterRepository.create(userData, fictitiousVoter);
	}

	private Voter buildFictitiousVoter(String fictitiousSSN, MvArea mvArea) {
		Voter fictitiousVoter = new Voter();
		fictitiousVoter.setMvArea(mvArea);
		fictitiousVoter.setElectionEvent(mvArea.getElectionEvent());
		fictitiousVoter.setId(fictitiousSSN);
		fictitiousVoter.setCountryId(mvArea.getCountryId());
		fictitiousVoter.setCountyId(mvArea.getCountyId());
		fictitiousVoter.setMunicipalityId(mvArea.getMunicipalityId());
		fictitiousVoter.setBoroughId(mvArea.getBoroughId());
		fictitiousVoter.setPollingDistrictId(mvArea.getPollingDistrictId());
		fictitiousVoter.setEligible(true);
		fictitiousVoter.setFirstName("");
		fictitiousVoter.setLastName("");
		fictitiousVoter.setNameLine("");
		fictitiousVoter.setApproved(false);
		fictitiousVoter.setDateTimeSubmitted(DateTime.now().toDate());
		fictitiousVoter.setVotingCardReturned(false);
		fictitiousVoter.setTemporaryCredentialsCount(0);
		fictitiousVoter.setFictitious(true);

		return fictitiousVoter;
	}

	private String findFreeFictitiousSSN(LocalDate lowerLimit, LocalDate upperLimit, MvArea mvArea) {
		Long electionEventPk = mvArea.getElectionEvent().getPk();

		for (LocalDate localDate = lowerLimit; localDate.isBefore(upperLimit); localDate = localDate.plusDays(1)) {
			
			for (int i = 0; i <= 9; i++) {
				
				String proposedSSN = buildFictitiousSSN(localDate, i);
				if (!existsVoterWithSSN(proposedSSN, electionEventPk)) {
					return proposedSSN;
				}
			}
		}

		throw new EvoteException("It is not possible to generate more fictitious SSNs");

	}

	private boolean existsVoterWithSSN(String proposedSSN, Long electionEventPk) {
		return voterRepository.existsVoterWithSSN(proposedSSN, electionEventPk);
	}

	private String buildFictitiousSSN(final LocalDate date, final int fictiveNumber) {
		DateTimeFormatter skdFormatRegDato = DateTimeFormat.forPattern("ddMMyy").withLocale(Locale.forLanguageTag(EvoteConstants.DEFAULT_LOCALE));
		StringBuilder ssn = new StringBuilder();
		ssn.append(skdFormatRegDato.print(date));
		ssn.append("00");
		ssn.append(String.valueOf(fictiveNumber));
		ssn.append("00");

		return ssn.toString();
	}

	public List<String> getVotersWithoutPollingDistricts(final Long electionEventPk) {
		List<String[]> ids = voterRepository.getVotersWithoutPollingDistricts(electionEventPk);

		// Although the polling district ID identifies the missing polling district, we need to return more information (area path and names of the entities in
		// the area path) so that it is easier to find in the area hierarchy
		return generateAreaPathInformation(electionEventPk, ids);
	}

	/**
	 * Generate area path information for polling districts that area missing.
	 */
	private List<String> generateAreaPathInformation(final Long electionEventPk, final List<String[]> ids) {
		List<String> result = new ArrayList<>(ids.size());
		final String idDelim = ".";
		for (String[] tmp : ids) {
			StringBuilder builder = new StringBuilder();
			
			String municipalityId = tmp[2];
			String boroughId = tmp[3];
			String pollingDistrictId = tmp[4];
			

			builder.append(StringUtils.join(new String[] { municipalityId, boroughId, pollingDistrictId }, idDelim));
			builder.append(" ");

			Country country = countryService.findCountryById(electionEventPk, tmp[0]);
			if (country != null) {
				County county = countyService.findCountyById(country.getPk(), tmp[1]);
				if (county != null) {
					Municipality municipality = municipalityRepository.findMunicipalityById(county.getPk(), municipalityId);
					if (municipality != null) {
						builder.append(municipality.getName());
						Borough borough = boroughRepository.findBoroughById(municipality.getPk(), boroughId);
						builder.append(", ");
						if (borough != null) {
							builder.append(borough.getName());
						} else {
							builder.append(ID_UNKNOWN);
						}
					} else {
						builder.append(ID_UNKNOWN);
					}
				} else {
					builder.append(ID_UNKNOWN);
				}
			} else {
				builder.append(ID_UNKNOWN);
			}

			result.add(builder.toString());
		}

		Collections.sort(result);
		return result;
	}
}
