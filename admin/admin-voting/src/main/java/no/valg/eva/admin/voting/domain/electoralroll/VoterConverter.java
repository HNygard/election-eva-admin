package no.valg.eva.admin.voting.domain.electoralroll;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Foedselsnummer;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Locale;

public class VoterConverter {
	private static final Logger LOGGER = Logger.getLogger(VoterConverter.class);

	private static final String PERIOD = ".";
	private static final String ID_00 = "00";
	private static final String ID_01 = "01";
	private static final String ID_0000 = "0000";
	private static final String SPACE = " ";
	private static final String MATRIKKELADRESSE = "M";

	protected MvAreaRepository mvAreaRepository;
	
	private ElectionEvent electionEvent;
	private DateTimeFormatter skdFormatRegDato;
	private ImportElectoralRollBoroughDistribution electoralRollImportBoroughDistribution = new ImportElectoralRollBoroughDistribution();
	
	public VoterConverter(ElectionEvent electionEvent, MvAreaRepository mvAreaRepository) {
		this.electionEvent = electionEvent;
		this.mvAreaRepository = mvAreaRepository;
		Locale locale = Locale.getDefault();
		this.skdFormatRegDato = DateTimeFormat.forPattern(SkdVoterRecord.SKD_DATE_FORMAT).withLocale(locale);
	}

	public Voter fromVoterRecord(VoterRecord voterRecord) throws ParseException {
		Voter voter = new Voter();
		populateFromVoterRecord(voter, voterRecord);
		return voter;
	}

	public void populateFromVoterRecord(Voter preExistingVoter, VoterRecord voterRecord) throws ParseException {
		preExistingVoter.setImportBatchNumber(Integer.parseInt(voterRecord.kjorenr()));
		preExistingVoter.setAarsakskode(voterRecord.aarsakskode());
		preExistingVoter.setEndringstype(voterRecord.endringstypeChar());
		
		if (voterRecord.timestamp().trim().length() == 26) {
			
			Timestamp ts = voterRecord.timestampAsTimestamp();
			preExistingVoter.setDateTimeSubmitted(ts);
		}
		
		if (voterRecord.regDato().trim().length() == 8) {
			
			preExistingVoter.setRegDato(skdFormatRegDato.parseLocalDate(voterRecord.regDato()));
		}
		preExistingVoter.setId(voterRecord.foedselsnr().trim());
		if (voterRecord.statuskode().length() > 0) {
			preExistingVoter.setStatuskode(voterRecord.statuskode().charAt(0));
		}
		preExistingVoter.setLastName(voterRecord.etternavn().trim());
		preExistingVoter.setFirstName(voterRecord.fornavn().trim());
		preExistingVoter.setMiddleName(voterRecord.mellomnavn().trim());
		preExistingVoter.setNameLine(buildNameLine(preExistingVoter));
		mapAdresse(preExistingVoter, voterRecord);
		mapPostadresse(preExistingVoter, voterRecord);
		if (voterRecord.spesRegType().length() > 0) {
			preExistingVoter.setSpesRegType(voterRecord.spesRegType().charAt(0));
		}
		preExistingVoter.setApproved(true);
		preExistingVoter.setEligible(true);
		preExistingVoter.setElectionEvent(electionEvent);
		preExistingVoter.setCountryId("47"); // We must hardcode 47 for Norway. This information is not present in electoral roll files
		
		if (voterRecord.kommunenr().length() == 4) {
			
			preExistingVoter.setCountyId(voterRecord.kommunenr().substring(0, 2));
		}
		convertMunicipalityId(preExistingVoter, voterRecord);
		if (electionEvent.isVoterImportMunicipality()) {
			preExistingVoter.setBoroughId(voterRecord.kommunenr().trim() + ID_00);
			preExistingVoter.setPollingDistrictId(ID_0000);
		} else if (isVoterCitizenOfOsloWithoutBelongingToABorough(voterRecord)) {
			assignVoterToBoroughAccordingToBirthDate(voterRecord, preExistingVoter);
		} else {
			preExistingVoter.setPollingDistrictId(voterRecord.valgkrets().trim());
			
			if (voterRecord.valgkrets().length() == 4) {
				
				preExistingVoter.setBoroughId(BoroughIdResolver.boroughIdFor(voterRecord));
			}
		}

		MvArea mvArea = mvAreaRepository.findSingleByPath(buildPollingDistrictPath(electionEvent, preExistingVoter.getCountryId(), preExistingVoter.getCountyId(),
			preExistingVoter.getMunicipalityId(), preExistingVoter.getBoroughId(), preExistingVoter.getPollingDistrictId()));
		preExistingVoter.setMvArea(mvArea);
		
		preExistingVoter.setAdditionalInformation(makeAdditionalInformation(voterRecord));
	}

	private void mapAdresse(Voter preExistingVoter, VoterRecord voterRecord) {
		preExistingVoter.setPostalCode(voterRecord.postnr().trim());
		preExistingVoter.setPostTown(voterRecord.poststed().trim());

		if (MATRIKKELADRESSE.equals(voterRecord.adressetype())) {
			preExistingVoter.setAddressLine1(voterRecord.adressenavn().trim());
		} else {
			preExistingVoter.setAddressLine1(voterRecord.adresse().trim());
		}

		preExistingVoter.setAddressLine2(voterRecord.tilleggsadresse().trim());
	}

	private void mapPostadresse(Voter preExistingVoter, VoterRecord voterRecord) {
		preExistingVoter.setMailingAddressLine1(voterRecord.postadrLinje1().trim());
		preExistingVoter.setMailingAddressLine2(voterRecord.postadrLinje2().trim());
		preExistingVoter.setMailingAddressLine3(voterRecord.postadrLinje3().trim());
		preExistingVoter.setMailingCountryCode(voterRecord.postadrLandkode().trim());
		if (voterRecord.postadrLinje1().trim().length() > 0 || voterRecord.postadrLinje2().trim().length() > 0
			|| voterRecord.postadrLinje3().trim().length() > 0) {
			preExistingVoter.setMailingAddressSpecified(true);
		} else {
			preExistingVoter.setMailingAddressSpecified(false);
		}
	}

	private static void convertMunicipalityId(Voter voter, VoterRecord voterRecord) {
		voter.setMunicipalityId(voterRecord.kommunenr().trim());
	}


	private String buildNameLine(Voter voter) {
		StringBuilder nameLine = new StringBuilder(voter.getLastName());
		nameLine.append(SPACE);
		nameLine.append(voter.getFirstName());
		nameLine.append(StringUtils.isEmpty(voter.getMiddleName()) ? "" : SPACE + voter.getMiddleName());
		return nameLine.toString().trim();
	}

	private boolean isVoterCitizenOfOsloWithoutBelongingToABorough(VoterRecord skdVoterRecord) {
		return skdVoterRecord.valgkrets().equals(ID_0000) && skdVoterRecord.kommunenr().equals(AreaPath.OSLO_MUNICIPALITY_ID);
	}

	private void assignVoterToBoroughAccordingToBirthDate(VoterRecord skdVoterRecord, Voter voter) {
		voter.setBoroughId(electoralRollImportBoroughDistribution.findBoroughWithResponsibilityForVoter(skdVoterRecord.foedselsnr()));
		
		String boroughNumberPartOfId = voter.getBoroughId().trim().substring(4);
		
		String pollingDistrictOne = boroughNumberPartOfId + ID_01;
		voter.setPollingDistrictId(pollingDistrictOne);
		LOGGER.info("Fordelt Oslo-boer med fnr " + skdVoterRecord.foedselsnr() + " til bydel " + voter.getBoroughId());
	}

	private AreaPath buildPollingDistrictPath(ElectionEvent electionEvent, String countryId, String countyId, String municipalityId,
											  String boroughId, String pollingDistrictId) {
		StringBuilder sb = new StringBuilder(electionEvent.getId());
		sb.append(PERIOD);
		sb.append(countryId);
		sb.append(PERIOD);
		sb.append(countyId);
		sb.append(PERIOD);
		sb.append(municipalityId);
		sb.append(PERIOD);
		sb.append(boroughId);
		sb.append(PERIOD);
		sb.append(pollingDistrictId);
		return AreaPath.from(sb.toString());
	}

	private String makeAdditionalInformation(VoterRecord skdVoterRecord) {
		if (skdVoterRecord.eligibleInSamiElection()) {
			return "@electoralRoll.eligigbleInSamiElection";
		} else {
			return null;
		}
	}

	public LegacyPollingDistrict fromVoterRecord(VoterRecord voterRecord, Voter voter) {
		LegacyPollingDistrict legacyPollingDistrict = new LegacyPollingDistrict();
		if (!voterRecord.legacyKommunenr().equals(voterRecord.kommunenr()) || !voterRecord.legacyValgkrets().equals(voterRecord.valgkrets())) {
			legacyPollingDistrict.setVoter(voter);
			legacyPollingDistrict.setLegacyMunicipalityId(voterRecord.legacyKommunenr());
			legacyPollingDistrict.setLegacyPollingDistrictId(voterRecord.legacyValgkrets());
		}
		return legacyPollingDistrict;
	}

	public static Voter fromVoterRecordWithOnlyMunicipalityIdAndBirthdate(VoterRecord skdVoterRecord) {
		Voter voter = new Voter();
		convertMunicipalityId(voter, skdVoterRecord);
		voter.setDateOfBirth(new Foedselsnummer(skdVoterRecord.foedselsnr()).dateOfBirth());
		return voter;
	}
}
