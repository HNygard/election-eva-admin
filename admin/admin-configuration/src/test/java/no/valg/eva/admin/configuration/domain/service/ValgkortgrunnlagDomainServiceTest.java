package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.GenererValgkortgrunnlagStatus;
import no.evote.security.UserData;
import no.evote.util.EvotePropertiesTestUtil;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_COMPLETED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_STARTED_ID;
import static no.evote.constants.GenererValgkortgrunnlagStatus.MANNTALLSNUMRE_MANGLER;
import static no.evote.constants.GenererValgkortgrunnlagStatus.OK;
import static no.evote.constants.GenererValgkortgrunnlagStatus.TOMT_MANNTALL;
import static no.evote.util.EvoteProperties.NO_VALG_EVA_ADMIN_VALGKORTGRUNNLAG_FOLDER;
import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_AVGANG;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.VALGKORTUNDERLAG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValgkortgrunnlagDomainServiceTest extends MockUtilsTestCase {

	@Test(dataProvider = "forutsetninger")
	public void sjekkForutsetningerForGenerering(boolean tomtManntall, boolean manntallGenerert, GenererValgkortgrunnlagStatus forventetResultat) throws Exception {
		ValgkortgrunnlagDomainService valgkortgrunnlagDomainService = initializeMocks(ValgkortgrunnlagDomainService.class);
		ElectionEvent electionEvent = new ElectionEvent();

        when(getInjectMock(VoterRepository.class).areVotersInElectionEvent(any())).thenReturn(!tomtManntall);
		when(getInjectMock(BakgrunnsjobbDomainService.class).erManntallsnummergenereringFullfortUtenFeil(electionEvent)).thenReturn(manntallGenerert);

		GenererValgkortgrunnlagStatus genererValgkortgrunnlagStatus = valgkortgrunnlagDomainService.sjekkForutsetningerForGenerering(electionEvent);

		assertThat(genererValgkortgrunnlagStatus).isEqualTo(forventetResultat);
	}

	@DataProvider
	public Object[][] forutsetninger() {
		return new Object[][] {
			{ true,  false, TOMT_MANNTALL },
			{ false, false, MANNTALLSNUMRE_MANGLER },
			{ false, true,  OK },
		};
	}

	@Test(dataProvider = "jobbstatuser")
	public void genererValgkortgrunnlag_jobbstatus(boolean skalFeileUansett, boolean harVelgereUtenManntallsnummer, boolean tillatVelgereIkkeTilknyttetValgdistrikt, int statusVedJobbensSlutt) throws Exception {
		ValgkortgrunnlagDomainService valgkortgrunnlagDomainService = initializeMocks(ValgkortgrunnlagDomainService.class);
		BakgrunnsjobbDomainService bakgrunnsjobbDomainService = getInjectMock(BakgrunnsjobbDomainService.class);
		UserData userData = userData();
		EvotePropertiesTestUtil.setProperty(NO_VALG_EVA_ADMIN_VALGKORTGRUNNLAG_FOLDER, "./");

		if (skalFeileUansett) {
			when(getInjectMock(VoterRepository.class).findVotersForValgkortgrunnlag(any())).thenThrow(new RuntimeException("Feil under generering"));
		} else {
			if (harVelgereUtenManntallsnummer) {
				Voter velger = velger("nn-NO");
				velger.setNumber(null);
				when(getInjectMock(VoterRepository.class).findVotersForValgkortgrunnlag(any())).thenReturn(singletonList(velger));
			} else {
				when(getInjectMock(VoterRepository.class).findVotersForValgkortgrunnlag(any())).thenReturn(emptyList());
			}
		}
		
		when(getInjectMock(ReportingUnitRepository.class).findAlleValgstyrerIValghendelse(any())).thenReturn(emptyList());
		when(getInjectMock(ManntallsnummerDomainService.class).valgaarssifferForValghendelse(any())).thenReturn(1);
		when(getInjectMock(PollingPlaceRepository.class).findPollingPlacesWithOpeningHours(any())).thenReturn(emptyList());
		when(getInjectMock(ContestRepository.class).antallMultiomraadedistrikter(any())).thenReturn(0); // ikke sametingsvalg
		when(getInjectMock(ContestAreaRepository.class).finnForValghendelseMedValgdistrikt(any())).thenReturn(emptyList());
		when(getInjectMock(ReportingUnitRepository.class).finnOpptellingsvalgstyrer(any())).thenReturn(emptyList());
		
		Path generertFil = valgkortgrunnlagDomainService.genererValgkortgrunnlag(userData, tillatVelgereIkkeTilknyttetValgdistrikt);

		verify(bakgrunnsjobbDomainService).lagBakgrunnsjobb(eq(userData), eq(VALGKORTUNDERLAG), eq(BATCH_STATUS_STARTED_ID), any(), any());
		verify(bakgrunnsjobbDomainService).oppdaterBakgrunnsjobb(eq(userData), any(), eq(statusVedJobbensSlutt));
		if (statusVedJobbensSlutt == BATCH_STATUS_COMPLETED_ID) {
			Files.delete(generertFil);
		}
	}
	
	@DataProvider
	public Object[][] jobbstatuser() {
		return new Object[][] {
			{ false, false, false, BATCH_STATUS_COMPLETED_ID },
			{ true, false,  false, BATCH_STATUS_FAILED_ID },
			{ false, true,  false, BATCH_STATUS_FAILED_ID },
			{ false, true,  true,  BATCH_STATUS_COMPLETED_ID }
		};
	}

	private UserData userData() {
        return mock(UserData.class);
	}

	@Test(dataProvider = "valgkortgrunnlag")
	public void genererValgkortgrunnlag(int antallMultiomraadevalgdistrikter, boolean childArea, String maalform, boolean brukPostadresse,
										String forventetValgstyreEksport, boolean godtaVelgereUtenManntallsnummer, boolean isElectronicMarkoffs,
										boolean isPollingStations) throws Exception {
		ValgkortgrunnlagDomainService valgkortgrunnlagDomainService = initializeMocks(ValgkortgrunnlagDomainService.class);
		ElectionEvent electionEvent = new ElectionEvent();
		Path fil = Files.createTempFile("valgkortgrunnlag_eksport", "test");

		String expectedPollingStation = isPollingStations ? "A-D;" : "-;";
		String expectedPageAndLine = isElectronicMarkoffs ? "-;-;" : "8;9;";
		String forventetBostedsadresse = "velgerAdrLinje1;velgerAdrLinje2;velgerAdrLinje3;0103;Oslo Ring1;";
		String forventetPostadresse = "velgerPostAdrLinje1;velgerPostAdrLinje2;velgerPostAdrLinje3;;;";
		String forventetAdresse = brukPostadresse ? forventetPostadresse : forventetBostedsadresse;
		
		String forventetValgkorttekstVanlig = "Du kan avlegge stemmer:|- I urne|- I det runde arkivet;";
		String forventetValgkorttekstMinus30 = "nn-NO".equals(maalform) ?
			"I din kommune er det berre høve til å førehandsrøyste.|Du kan førehandsrøyste i alle kommunar.;"
			: "I din kommune er det kun anledning til å forhåndsstemme.|Du kan forhåndsstemme i alle landets kommuner.;";
		String forventetValgkorttekst = childArea? forventetValgkorttekstMinus30 : forventetValgkorttekstVanlig;
			
		String forventetValgkorteksport = forventetValgstyreEksport
			+ "0201;"
			+ expectedPollingStation
			+ expectedPageAndLine
			+ "1956;0301;0000001234;Fornavn Etternavn;000000123414;" 
			+ forventetAdresse
			+ forventetValgkorttekst
			+ "11.09.2017 kl. 09:30 - 11:50|11.09.2017 kl. 12:00 - 20:45;"
			+ "stemmestedsnavn;ssAdrL1;0104;Oslo indre;" + maalform;

		UserData userData = userData();
		when(userData().electionEvent()).thenReturn(electionEvent);
		when(getInjectMock(ReportingUnitRepository.class).findAlleValgstyrerIValghendelse(any())).thenReturn(valgstyrer(maalform));
		when(getInjectMock(ManntallsnummerDomainService.class).valgaarssifferForValghendelse(any())).thenReturn(1);
		when(getInjectMock(PollingPlaceRepository.class).findPollingPlacesWithOpeningHours(any())).thenReturn(stemmestedOgAapningstider());
		when(getInjectMock(VoterRepository.class).findVotersForValgkortgrunnlag(any()))
			.thenReturn(velgere(maalform, brukPostadresse, false, godtaVelgereUtenManntallsnummer, isElectronicMarkoffs, isPollingStations));
		when(getInjectMock(ContestRepository.class).antallMultiomraadedistrikter(any())).thenReturn(antallMultiomraadevalgdistrikter);
		when(getInjectMock(ContestAreaRepository.class).finnForValghendelseMedValgdistrikt(any())).thenReturn(valgdistriktsomraader(childArea, maalform));
		when(getInjectMock(ReportingUnitRepository.class).finnOpptellingsvalgstyrer(any())).thenReturn(opptellingsvalgstyrer(maalform));

		valgkortgrunnlagDomainService.genererValgkortgrunnlag(userData, fil, godtaVelgereUtenManntallsnummer);

		assertThat(innholdIFil(fil)).isEqualTo(singletonList(forventetValgkorteksport));
	}

	@DataProvider
	private Object[][] valgkortgrunnlag() {
		return new Object[][] {
			{ 0, false, "nb-NO", false, "Valgstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;", true, false, false },
			{ 0, false, "nb-NO", true, "Valgstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;", true, false, true },
			{ 0, false, "nb-NO", true, "Valgstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;", true, true, false },
			{ 0, false, "nn-NO", false, "Valstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;", true, false, true },
			{ 1, false, "nb-NO", false, "Samevalgstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;", true, false, false },
			{ 1, false, "nn-NO", false, "Samevalstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;", true, false, false },
			{ 1, true, "nb-NO", false, "Opptellingsvalgstyret i Østre valgkrets;opptellingsValgstyreAdrLinje1;9876;Kautokeino;", true, false, true },
			{ 1, true, "nn-NO", false, "Oppteljingsvalstyret i Østre valgkrets;opptellingsValgstyreAdrLinje1;9876;Kautokeino;", true, false, false },
			{ 1, true, "nb-NO", false, "Opptellingsvalgstyret i Østre valgkrets;opptellingsValgstyreAdrLinje1;9876;Kautokeino;", false, false, false }
		};
	}

	private List<ReportingUnit> valgstyrer(String maalform) {
		ReportingUnit reportingUnit = new ReportingUnit();
		reportingUnit.setNameLine("Oslo");
		reportingUnit.setAddressLine1("valgstyreAdrLinje1");
		reportingUnit.setAddressLine2("valgstyreAdrLinje2");
		reportingUnit.setAddressLine3("valgstyreAdrLinje3");
		reportingUnit.setPostalCode("0101");
		reportingUnit.setPostTown("Oslo sentrum");
		reportingUnit.setMvArea(mvArea("0301", "Oslo", maalform));
		
		return singletonList(reportingUnit);
	}
	
	private MvArea mvArea(String id, String navn, String maalform) {
		return mvArea(id, navn, maalform, false);
	}
	
	private MvArea mvArea(String id, String navn, String maalform, boolean isElectronicMarkoffs) {
		MvArea mvArea = new MvArea();

		mvArea.setMunicipality(new Municipality(id, navn, null));
		mvArea.setMunicipalityName(navn);

		if (id.length() == 2) {
			mvArea.setAreaLevel(AreaLevelEnum.COUNTY.getLevel());
			mvArea.setCountyId(id);
		} else {
			mvArea.setAreaLevel(AreaLevelEnum.MUNICIPALITY.getLevel());
			mvArea.setMunicipalityId(id);
		}
		
		mvArea.getMunicipality().setLocale(new Locale());
		mvArea.getMunicipality().getLocale().setId(maalform);
		mvArea.getMunicipality().setElectronicMarkoffs(isElectronicMarkoffs);
		mvArea.setPollingDistrict(new PollingDistrict());
		mvArea.getPollingDistrict().setPollingPlaces(new HashSet<>(stemmestedOgAapningstider()));
		
		return mvArea;
	}

	private List<PollingPlace> stemmestedOgAapningstider() {
		PollingPlace stemmested = new PollingPlace();
		stemmested.setPk(1L);
		stemmested.setElectionDayVoting(true);
		stemmested.setInfoText("Du kan avlegge stemmer:\n\r- I urne\n\r- I det runde arkivet");
		stemmested.setName("stemmestedsnavn");
		stemmested.setAddressLine1("ssAdrL1");
		stemmested.setAddressLine2("ssAdrL2");
		stemmested.setAddressLine3("ssAdrL3");
		stemmested.setPostalCode("0104");
		stemmested.setPostTown("Oslo indre");

		LocalDate valgdagDato = new LocalDate(2017, 9, 11);
		ElectionDay valgdag = new ElectionDay();
		valgdag.setDate(valgdagDato);

		Set<OpeningHours> aapningstider = new HashSet<>();
		aapningstider.add(aapningstid(valgdag, 9, 30, 11, 50, 1L));
		aapningstider.add(aapningstid(valgdag, 12, 0, 20, 45, 2L));
		
		stemmested.setOpeningHours(aapningstider);
		return singletonList(stemmested);
	}

	private OpeningHours aapningstid(ElectionDay valgdag, int startTime, int startMinutter, int sluttTime, int sluttMinutter, long pk) {
		OpeningHours aapningstid = new OpeningHours();
		aapningstid.setPk(pk);
		aapningstid.setStartTime(new LocalTime(startTime, startMinutter));
		aapningstid.setEndTime(new LocalTime(sluttTime, sluttMinutter));
		aapningstid.setElectionDay(valgdag);
		return aapningstid;
	}

	private List<Voter> velgere(String maalform, boolean brukPostadresse) {
		return velgere(maalform, brukPostadresse, false,true, false, true);
	}
	
	private List<Voter> velgere(String maalform, boolean brukPostadresse, boolean ikkeStemmestedIKrets, boolean taMedVelgerUtenManntallsnummer,
								boolean isElectronicMarkoffs, boolean isPollingStation) {
		
		Voter velger = velger(maalform, brukPostadresse, isElectronicMarkoffs, isPollingStation);
		if (ikkeStemmestedIKrets) {
			velger.getMvArea().getPollingDistrict().setPollingPlaces(emptySet());
		}
		
		Voter velgerFiktiv = velger(maalform);
		velgerFiktiv.setFictitious(true);
		
		Voter velgerIUtlandet = velger(maalform);
		velgerIUtlandet.setMailingAddressSpecified(true);
		velgerIUtlandet.setMailingCountryCode("010");
		
		Voter velgerIKretsenForHeleKommunen = velger(maalform);
		velgerIKretsenForHeleKommunen.getMvArea().setPollingDistrictId("0000");
		
		Voter velgerUtenKretstilknytning = velger(maalform);
		velgerUtenKretstilknytning.setMvArea(null);
		
		Voter velgerUtenValgdistriktstilknytning = velger(maalform);
		velgerUtenValgdistriktstilknytning.setMunicipalityId("3333");
		
		Voter velgerSomIkkeHarStemmerett = velger(maalform);
		velgerSomIkkeHarStemmerett.setEligible(false);
		
		Voter velgerSomIkkeErGodkjent = velger(maalform);
		velgerSomIkkeErGodkjent.setApproved(false);

		Voter velgerSomHarBostedsadresseOg0000SomPostnummer = velger(maalform);
		velgerSomHarBostedsadresseOg0000SomPostnummer.setPostalCode("0000");
		velgerSomHarBostedsadresseOg0000SomPostnummer.setMailingAddressSpecified(false);

		Voter velgerSomBorPaaSvalbardMedSpesRegKode3 = velger(maalform);
		velgerSomBorPaaSvalbardMedSpesRegKode3.setSpesRegType('3');
		velgerSomBorPaaSvalbardMedSpesRegKode3.setMailingAddressSpecified(true);
		velgerSomBorPaaSvalbardMedSpesRegKode3.setMailingAddressLine1("Postboks 123");
		velgerSomBorPaaSvalbardMedSpesRegKode3.setMailingAddressLine2("9171 LONGYEARBYEN");
		velgerSomBorPaaSvalbardMedSpesRegKode3.setMailingCountryCode("000");

		Voter velgerSomErAvgaattFraManntalletOgManglerManntallsnummer = velger(maalform);
		velgerSomErAvgaattFraManntalletOgManglerManntallsnummer.setNumber(null);
		velgerSomErAvgaattFraManntalletOgManglerManntallsnummer.setEndringstype(ENDRINGSTYPE_AVGANG);

        List<Voter> velgere = new ArrayList<>(asList(velger, velgerFiktiv, velgerIUtlandet, velgerIKretsenForHeleKommunen, velgerUtenKretstilknytning,
                velgerUtenValgdistriktstilknytning, velgerSomIkkeHarStemmerett, velgerSomIkkeErGodkjent, velgerSomHarBostedsadresseOg0000SomPostnummer,
                velgerSomBorPaaSvalbardMedSpesRegKode3, velgerSomErAvgaattFraManntalletOgManglerManntallsnummer));

		if (taMedVelgerUtenManntallsnummer) {
			Voter velgerSomManglerManntallsnummerMenEllersErOk = velger(maalform);
			velgerSomManglerManntallsnummerMenEllersErOk.setNumber(null);
			velgere.add(velgerSomManglerManntallsnummerMenEllersErOk);
		}

		return velgere;
	}

	private Voter velger(String maalform) {
		return velger(maalform, false, false, true);
	}


	private Voter velger(String maalform, boolean brukPostadresse, boolean isElectronicMarkoffs, boolean isPollingStation) {
		Voter velger = new Voter();

		velger.setDateOfBirth(new LocalDate(1956, 12, 1));
		velger.setMvArea(mvArea("0301", "Oslo", maalform, isElectronicMarkoffs));
		velger.setCountyId("03");
		velger.setMunicipalityId("0301");
		velger.setPollingDistrictId("0201");
		velger.setNumber(1234L);
		if (isPollingStation) {
			velger.setPollingStation(new PollingStation());
			velger.getPollingStation().setId("A-D");
		}
		velger.setElectoralRollPage(8);
		velger.setElectoralRollLine(9);
		velger.setNameLine("Etternavn Fornavn");
		velger.setFirstName("Fornavn");
		velger.setLastName("Etternavn");

		if (brukPostadresse) {
			velger.setMailingAddressSpecified(true);
			velger.setMailingAddressLine1("velgerPostAdrLinje1");
			velger.setMailingAddressLine2("velgerPostAdrLinje2");
			velger.setMailingAddressLine3("velgerPostAdrLinje3");
			velger.setPostalCode("0000"); // Er ofte satt slik på velgere med postadresse
			velger.setMailingCountryCode("000");
		} else {
			velger.setMailingAddressSpecified(false);
			velger.setAddressLine1("velgerAdrLinje1");
			velger.setAddressLine2("velgerAdrLinje2");
			velger.setAddressLine3("velgerAdrLinje3");
			velger.setPostalCode("0103");
			velger.setPostTown("Oslo Ring1");
		}

		velger.setApproved(true);
		velger.setEligible(true);
		return velger;
	}

	private List<ContestArea> valgdistriktsomraader(boolean childArea, String maalform) {
		return asList(
			valgdistriktomraade("0301", "Oslo", childArea, false, maalform),
			valgdistriktomraade("0001", "Østre valgkrets", false, true, maalform)
		);
	}

	private ContestArea valgdistriktomraade(String id, String navn, boolean childArea, boolean parentArea, String maalform) {
		ContestArea childContestArea = new ContestArea();
		childContestArea.setChildArea(childArea);
		childContestArea.setParentArea(parentArea);
		childContestArea.setMvArea(mvArea(id, navn, maalform));
		childContestArea.setContest(valgdistrikt());
		return childContestArea;
	}

	private Contest valgdistrikt() {
		Contest valgdistrikt = new Contest();
		valgdistrikt.setId("000001");
		return valgdistrikt;
	}

	private List<ReportingUnit> opptellingsvalgstyrer(String maalform) {
		ReportingUnit reportingUnit = new ReportingUnit();
		reportingUnit.setNameLine("Østre valgkrets");
		reportingUnit.setAddressLine1("opptellingsValgstyreAdrLinje1");
		reportingUnit.setAddressLine2("opptellingsValgstyreAdrLinje2");
		reportingUnit.setAddressLine3("opptellingsValgstyreAdrLinje3");
		reportingUnit.setPostalCode("9876");
		reportingUnit.setPostTown("Kautokeino");
		reportingUnit.setMvElection(mvElection());

		reportingUnit.setMvArea(mvArea("0001", "Østre valgkrets", maalform));

		return singletonList(reportingUnit);
	}

	private MvElection mvElection() {
		MvElection mvElection = new MvElection();
		mvElection.setContestId("000001");
		return mvElection;
	}

	private List<String> innholdIFil(Path fil) throws IOException {
		return Files.readAllLines(fil, StandardCharsets.UTF_8);
	}

	@Test
	public void genererValgkortgrunnlag_manglerValglokaleEllerAapningstider_girTommeVerdierForValglokale() throws Exception {
		ValgkortgrunnlagDomainService valgkortgrunnlagDomainService = initializeMocks(ValgkortgrunnlagDomainService.class);
		ElectionEvent electionEvent = new ElectionEvent();
		Path fil = Files.createTempFile("valgkortgrunnlag_eksport", "test");
		String maalform = "nb-NO";
		String forventetValgkorteksportUtenAapningstider = "Valgstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;"
			+ "0201;A-D;8;9;1956;0301;0000001234;Fornavn Etternavn;000000123414;velgerAdrLinje1;velgerAdrLinje2;velgerAdrLinje3;"
			+ "0103;Oslo Ring1;Du kan avlegge stemmer:|- I urne|- I det runde arkivet;;;;;;" + maalform;
		String forventetValgkorteksportUtenStemmested = "Valgstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;"
			+ "0201;A-D;8;9;1956;0301;0000001234;Fornavn Etternavn;000000123414;velgerAdrLinje1;velgerAdrLinje2;velgerAdrLinje3;"
			+ "0103;Oslo Ring1;;;;;;;" + maalform;

		List<Voter> velgere = velgere(maalform, false);
		UserData userData = userData();
		when(userData().electionEvent()).thenReturn(electionEvent);
		when(getInjectMock(ReportingUnitRepository.class).findAlleValgstyrerIValghendelse(any())).thenReturn(valgstyrer(maalform));
		when(getInjectMock(ManntallsnummerDomainService.class).valgaarssifferForValghendelse(any())).thenReturn(1);
		when(getInjectMock(PollingPlaceRepository.class).findPollingPlacesWithOpeningHours(any())).thenReturn(stemmestedOgAapningstider());
		when(getInjectMock(VoterRepository.class).findVotersForValgkortgrunnlag(any())).thenReturn(velgere);
		when(getInjectMock(ContestAreaRepository.class).finnForValghendelseMedValgdistrikt(any())).thenReturn(valgdistriktsomraader(false, maalform));
		when(getInjectMock(ReportingUnitRepository.class).finnOpptellingsvalgstyrer(any())).thenReturn(opptellingsvalgstyrer(maalform));

		// Variant 1: Åpningstider finnes ikke
		velgere.get(0).getMvArea().getPollingDistrict().getPollingPlaces().iterator().next().setOpeningHours(emptySet());
		valgkortgrunnlagDomainService.genererValgkortgrunnlag(userData, fil, true);
		assertThat(innholdIFil(fil)).isEqualTo(singletonList(forventetValgkorteksportUtenAapningstider));

		// Variant 2: Stemmested finnes ikke
		velgere.get(0).getMvArea().getPollingDistrict().setPollingPlaces(emptySet());
		valgkortgrunnlagDomainService.genererValgkortgrunnlag(userData, fil, true);
		assertThat(innholdIFil(fil)).isEqualTo(singletonList(forventetValgkorteksportUtenStemmested));
	}
	
	@Test
	public void genererValgkortgrunnlag_velgereTilknyttetValgdistriktPaaFylkesnivaa_blirMedIEksporten() throws Exception {
		ValgkortgrunnlagDomainService valgkortgrunnlagDomainService = initializeMocks(ValgkortgrunnlagDomainService.class);
		ElectionEvent electionEvent = new ElectionEvent();
		Path fil = Files.createTempFile("valgkortgrunnlag_eksport", "test");

		String maalform = "nb-NO";
		String forventetValgkorteksport = "Valgstyret i Oslo kommune;valgstyreAdrLinje1;0101;Oslo sentrum;"
			+ "0201;A-D;8;9;1956;0301;0000001234;Fornavn Etternavn;000000123414;velgerAdrLinje1;velgerAdrLinje2;velgerAdrLinje3;"
			+ "0103;Oslo Ring1;Du kan avlegge stemmer:|- I urne|- I det runde arkivet;11.09.2017 kl. 09:30 - 11:50|11.09.2017 kl. 12:00 - 20:45;"
			+ "stemmestedsnavn;ssAdrL1;0104;Oslo indre;" + maalform;

		UserData userData = userData();
		when(userData().electionEvent()).thenReturn(electionEvent);
		when(getInjectMock(ReportingUnitRepository.class).findAlleValgstyrerIValghendelse(any())).thenReturn(valgstyrer(maalform));
		when(getInjectMock(ManntallsnummerDomainService.class).valgaarssifferForValghendelse(any())).thenReturn(1);
		when(getInjectMock(PollingPlaceRepository.class).findPollingPlacesWithOpeningHours(any())).thenReturn(stemmestedOgAapningstider());
		when(getInjectMock(VoterRepository.class).findVotersForValgkortgrunnlag(any())).thenReturn(singletonList(velger(maalform)));
		when(getInjectMock(ContestRepository.class).antallMultiomraadedistrikter(any())).thenReturn(0);
		when(getInjectMock(ContestAreaRepository.class).finnForValghendelseMedValgdistrikt(any()))
			.thenReturn(singletonList(valgdistriktomraade("03", "Oslo", false, false, maalform)));
		when(getInjectMock(ReportingUnitRepository.class).finnOpptellingsvalgstyrer(any())).thenReturn(opptellingsvalgstyrer(maalform));

		valgkortgrunnlagDomainService.genererValgkortgrunnlag(userData, fil, false);

		assertThat(innholdIFil(fil)).isEqualTo(singletonList(forventetValgkorteksport));
	}

	@Test
	public void genererValgkortgrunnlag_minus30KommunerMedKretserUtenStemmesteder_faarOgsaaInfotekst() throws Exception {
		ValgkortgrunnlagDomainService valgkortgrunnlagDomainService = initializeMocks(ValgkortgrunnlagDomainService.class);
		ElectionEvent electionEvent = new ElectionEvent();
		Path fil = Files.createTempFile("valgkortgrunnlag_eksport", "test");
		String maalform = "nb-NO";

		String forventetValgkorteksport = "Opptellingsvalgstyret i Østre valgkrets;opptellingsValgstyreAdrLinje1;9876;Kautokeino;"
			+ "0201;A-D;8;9;1956;0301;0000001234;Fornavn Etternavn;000000123414;"
			+ "velgerAdrLinje1;velgerAdrLinje2;velgerAdrLinje3;0103;Oslo Ring1;"
			+ "I din kommune er det kun anledning til å forhåndsstemme.|Du kan forhåndsstemme i alle landets kommuner.;"
			+ ";;;;;" + maalform;

		UserData userData = userData();
		when(userData().electionEvent()).thenReturn(electionEvent);
		when(getInjectMock(ReportingUnitRepository.class).findAlleValgstyrerIValghendelse(any())).thenReturn(valgstyrer(maalform));
		when(getInjectMock(ManntallsnummerDomainService.class).valgaarssifferForValghendelse(any())).thenReturn(1);
		when(getInjectMock(PollingPlaceRepository.class).findPollingPlacesWithOpeningHours(any())).thenReturn(emptyList());
		when(getInjectMock(VoterRepository.class).findVotersForValgkortgrunnlag(any())).thenReturn(velgere(maalform, false, true, true, false, true));
		when(getInjectMock(ContestRepository.class).antallMultiomraadedistrikter(any())).thenReturn(1);
		when(getInjectMock(ContestAreaRepository.class).finnForValghendelseMedValgdistrikt(any())).thenReturn(valgdistriktsomraader(true, maalform));
		when(getInjectMock(ReportingUnitRepository.class).finnOpptellingsvalgstyrer(any())).thenReturn(opptellingsvalgstyrer(maalform));

		valgkortgrunnlagDomainService.genererValgkortgrunnlag(userData, fil, true);

		assertThat(innholdIFil(fil)).isEqualTo(singletonList(forventetValgkorteksport));
	}
	
}
