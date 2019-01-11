package no.valg.eva.admin.voting.domain.electoralroll;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.AreaPath.OSLO_MUNICIPALITY_ID;
import static no.valg.eva.admin.common.AreaPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VoterConverterTest extends MockUtilsTestCase {

	private static final AreaPath AREA_PATH_GRUNERLOKKA_POLLING_DISTRICT = from("201503.47.03.0301.030102.0201");
	private static final AreaPath AREA_PATH_SAGENE_POLLING_DISTRICT = from("201503.47.03.0301.030103.0301");
	private static final AreaPath AREA_PATH_STOVNER_POLLING_DISTRICT = from("201503.47.03.0301.030111.1101");
	private static final String MV_ELECTION_EVENT_ID = "201503";
	private static final String POLLING_DISTRICT_0000 = "0000";
	private static final String POLLING_DISTRICT_NR_ONE_STOVNER = "1101";

	@Test
	public void fromVoterRecord_forAValidRecord_convertsItIntoAVoter() throws Exception {
		VoterRecord voterRecord = getVoterRecord();
		ElectionEvent electionEventMock = getElectionEventMock();
		Voter expectedVoter = getVoter();
		VoterConverter voterConverter = getVoterConverter(electionEventMock);

		Voter voter = voterConverter.fromVoterRecord(voterRecord);

		assertThat(voter.getEndringstype()).isEqualTo(expectedVoter.getEndringstype());
		assertThat(voter.getFirstName()).isEqualTo(expectedVoter.getFirstName());
		assertThat(voter.getLastName()).isEqualTo(expectedVoter.getLastName());
	}
	
	@Test
	public void populateFromVoterRecord_forAnExistingVoter_populatesTheExistingObject() throws Exception {
		VoterRecord voterRecord = getVoterRecord();
		ElectionEvent electionEventMock = getElectionEventMock();
		Voter expectedVoter = getVoter();
		VoterConverter voterConverter = getVoterConverter(electionEventMock);

		Voter preExistingVoter = getVoter();
		voterConverter.populateFromVoterRecord(preExistingVoter, voterRecord);

		assertThat(preExistingVoter.getEndringstype()).isEqualTo(expectedVoter.getEndringstype());
		assertThat(preExistingVoter.getFirstName()).isEqualTo(expectedVoter.getFirstName());
		assertThat(preExistingVoter.getLastName()).isEqualTo(expectedVoter.getLastName());
	}
	
	@Test
	public void fromVoterRecord_legacyPollingDistrictEqualsCurrentPollingDistrict_returnsEmptyLegacyPollingDistrict() {
		VoterRecord voterRecord = getVoterRecord();
		ElectionEvent electionEventMock = getElectionEventMock();
		Voter voter = getVoter();
		VoterConverter voterConverter = getVoterConverter(electionEventMock);
		
		LegacyPollingDistrict legacyPollingDistrict = voterConverter.fromVoterRecord(voterRecord, voter);
		
		assertThat(legacyPollingDistrict.getVoter()).isNull();
		assertThat(legacyPollingDistrict.getLegacyMunicipalityId()).isNullOrEmpty();
		assertThat(legacyPollingDistrict.getLegacyPollingDistrictId()).isNullOrEmpty();
	}
	
	@Test
	public void fromVoterRecord_legacyPollingDistrictDifferentFromCurrentPollingDistrict_returnsNonEmptyLegacyPollingDistrict() {
		VoterRecord voterRecord = getVoterRecord(getManntallsimportradForVelgerMedLegacyPollingDistrict());
		ElectionEvent electionEventMock = getElectionEventMock();
		Voter voter = getVoter();
		VoterConverter voterConverter = getVoterConverter(electionEventMock);
		
		LegacyPollingDistrict legacyPollingDistrict = voterConverter.fromVoterRecord(voterRecord, voter);
		
		assertThat(legacyPollingDistrict.getVoter()).isNotNull();
		assertThat(legacyPollingDistrict.getLegacyMunicipalityId()).isEqualTo("0191");
		assertThat(legacyPollingDistrict.getLegacyPollingDistrictId()).isEqualTo("0091");
	}

	@Test(dataProvider = "adressefordelingOslo")
	public void populateVoter_gittOsloborgerUtenAdresse_fordelesTilBydelPåBakgrunnAvFødselsnummer(String scenarioNavn, String fødselsnummer,
																							   String stemmekretsId, AreaPath forventetOmrådesti) throws Exception {
		VoterRecord voterRecord = getVoterRecord(fødselsnummer, stemmekretsId, OSLO_MUNICIPALITY_ID);
		ElectionEvent electionEventMock = getElectionEventMock();
		VoterConverter voterConverter = getVoterConverter(electionEventMock);

		Voter preExistingVoter = getVoter();
		voterConverter.populateFromVoterRecord(preExistingVoter, voterRecord);

		verify(voterConverter.mvAreaRepository, times(1)).findSingleByPath(forventetOmrådesti);
	}

	@DataProvider
	public Object[][] adressefordelingOslo() {
		return new Object[][]{
			{ "velgerFødt20endeUtenAdresseFordelesTilGrünerløkka", 	 "20023237025", POLLING_DISTRICT_0000,  		 AREA_PATH_GRUNERLOKKA_POLLING_DISTRICT },
			{ "velgerFødt28endeUtenAdresseFordelesTilSagene", 		 "28063512038", POLLING_DISTRICT_0000,			 AREA_PATH_SAGENE_POLLING_DISTRICT },
			{ "velgerFødt28endeMedAdresseStovnerFordelesTilStovner", "28063512038", POLLING_DISTRICT_NR_ONE_STOVNER, AREA_PATH_STOVNER_POLLING_DISTRICT }
		};
	}

	@Test(dataProvider = "adresseTestData")
	public void populateVoter_konvertererAdresseRiktig(String manntallsRad, String expectedAddressLine1,
																				   String expectedAddressLine2, String expectedAddressLine3) throws Exception {
		VoterRecord voterRecord = getVoterRecord(manntallsRad);
		ElectionEvent electionEventMock = getElectionEventMock();
		VoterConverter voterConverter = getVoterConverter(electionEventMock);

		Voter preExistingVoter = getVoter();
		voterConverter.populateFromVoterRecord(preExistingVoter, voterRecord);
		
		assertThat(preExistingVoter.getAddressLine1()).isEqualTo(expectedAddressLine1);
		assertThat(preExistingVoter.getAddressLine2()).isEqualTo(expectedAddressLine2);
		assertThat(preExistingVoter.getAddressLine3()).isEqualTo(expectedAddressLine3);
	}

	@DataProvider
	public Object[][] adresseTestData() {
		return new Object[][]{
			{ getManntallsimportradForVelgerMedOrdinærAdresse(), 	"Major Forbus gate 10", "", null},
			{ getManntallsimportradForVelgerMedOrdinærAdresseOgCO(), "Major Forbus gate 10", "c/o Mammamor", null},
			{ getManntallsimportradForVelgerMedMatrikkeladresse(), 	"Matrikkelsted", "", null }
		};
	}

	private VoterRecord getVoterRecord() {
		return getVoterRecord(getManntallsimportradForVelgerMedOrdinærAdresse());
	}

	/** DEV-NOTE: Det ville vært mer naturlig å representere importrader med bruk av feks StringUtils.padLeft/Right,
	 *            men strengen i denne (og lignende metoder) er kopiert fra et anonymisert manntall, og for å redusere
	 *            risiko for feil er strengen beholdt i sin opprinnelige form.
	 */
	private String getManntallsimportradForVelgerMedOrdinærAdresse() {
		return "   2010-12-02-17.20.54.00000002122010000000000011Bakkevik Petter                         "
			+ "Bakkevik                                          Petter                                                                                              "
			+ "01010460000009900000000001776HALDEN          Major Forbus gate        Major Forbus gate 10                    O                              "
			+ "00001                                                                                                                           I01010001";
	}

	private String getManntallsimportradForVelgerMedLegacyPollingDistrict() {
		return "   2010-12-02-17.20.54.00000002122010000000000011Bakkevik Petter                         " 
            + "Bakkevik                                          Petter                                                                                              "
			+ "01910460000009900000000001776HALDEN          Major Forbus gate        Major Forbus gate 10                    O                              "
			+ "00091                                                                                                                           I01010001";
	}

	private String getManntallsimportradForVelgerMedOrdinærAdresseOgCO() {
		return "   2010-12-02-17.20.54.00000002122010000000000011Bakkevik Petter                         "
			+ "Bakkevik                                          Petter                                                                                              "
			+ "01010460000009900000000001776HALDEN          Major Forbus gate        Major Forbus gate 10                    Oc/o Mammamor                  "
			+ "00001                                                                                                                           I01010001";
	}

	private String getManntallsimportradForVelgerMedMatrikkeladresse() {
		return "   2014-11-15-18.45.06.23307601010001xxxxxxxxxxx1Olsen Per-Hans                          "
			+ "Olsen                                             Per-Hans                                                                                            "
			+ "10370008800030000000     4480KVINESDAL       Matrikkelsted                                                    M                              "
			+ 			"00004                                                                                                                        000 01010001\n";
	}

	private VoterRecord getVoterRecord(String record) {
		String kjorenr = "12345";
		return new SkdVoterRecord(record, kjorenr);
	}

	private VoterRecord getVoterRecord(final String fodselsnr, final String pollingDistrict, final String municipalityId) {
		return new FakeVoterRecord() {
			@Override
			public Character endringstypeChar() {
				return null;
			}

			@Override
			public boolean isElectoralRollChange() {
				return false;
			}

			@Override
			public String foedselsnr() {
				return fodselsnr;
			}

			@Override
			public String valgkrets() {
				return pollingDistrict;
			}
			
			@Override
			public String legacyValgkrets() { return pollingDistrict; }

			@Override
			public String kommunenr() {
				return municipalityId;
			}
			
			@Override
			public String legacyKommunenr() { return municipalityId; }
		};
	}

	private ElectionEvent getElectionEventMock() {
		ElectionEvent electionEventMock = mock(ElectionEvent.class, RETURNS_DEEP_STUBS);
		when(electionEventMock.isVoterImportMunicipality()).thenReturn(false);
		when(electionEventMock.getId()).thenReturn(MV_ELECTION_EVENT_ID);
		return electionEventMock;
	}

	private VoterConverter getVoterConverter(ElectionEvent electionEventMock) {
		return new VoterConverter(electionEventMock, mock(MvAreaRepository.class));
	}

	protected Voter getVoter() {
		Voter voter = new Voter();
		voter.setEndringstype(' ');
		voter.setFirstName("Petter");
		voter.setLastName("Bakkevik");
		return voter;
	}
}
