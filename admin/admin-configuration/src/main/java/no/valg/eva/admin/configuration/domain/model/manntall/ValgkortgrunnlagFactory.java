package no.valg.eva.admin.configuration.domain.model.manntall;

import no.evote.constants.AreaLevelEnum;
import no.evote.model.BaseEntity;
import no.valg.eva.admin.backend.i18n.MessageProvider;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Voter;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class ValgkortgrunnlagFactory {

	private static final int ANTALL_RADER_PER_UTSKRIFT_TIL_LOGG = 1000;
	private static final Logger LOG = Logger.getLogger(ValgkortgrunnlagFactory.class);
	private static final String SEPARATOR_NONE = "-";

	private Map<String, ReportingUnit> valgstyrePerKommune;
	private int valgaarssifferForValghendelse;
	private Map<Long, PollingPlace> stemmestederMedAapningstiderMap;
	private boolean harMultiomraadedistrikter;
	private Map<String, ContestArea> valgdistriktsomraadePerKommune;
	private Map<String, ContestArea> valgdistriktsomraadePerFylke;
	private Map<String, ReportingUnit> opptellingsvalgstyrerPerValgdistrikt;

	private int antallRaderGenerert;
	
	public ValgkortgrunnlagFactory(List<ReportingUnit> valgstyrer, int valgaarssifferForValghendelse, List<PollingPlace> stemmestederMedAapningstider,
								   boolean harMultiomraadedistrikter, List<ContestArea> valgdistriktsomraader, List<ReportingUnit> opptellingsvalgstyrer) {
	
		this.valgstyrePerKommune = valgstyrer.stream().collect(Collectors.toMap(ru -> ru.getMvArea().getMunicipality().getId(), ru -> ru));
		this.valgaarssifferForValghendelse = valgaarssifferForValghendelse;
		this.stemmestederMedAapningstiderMap = stemmestederMedAapningstider.stream().collect(toMap(BaseEntity::getPk, pp -> pp));
		this.harMultiomraadedistrikter = harMultiomraadedistrikter;
		this.valgdistriktsomraadePerKommune = valgdistriktsomraader.stream()
			.filter(ca -> ca.getMvArea().getAreaLevel() == AreaLevelEnum.MUNICIPALITY.getLevel())
			.collect(toMap(ca -> ca.getMvArea().getMunicipalityId(), ca -> ca));
		this.valgdistriktsomraadePerFylke = valgdistriktsomraader.stream()
			.filter(ca -> ca.getMvArea().getAreaLevel() == AreaLevelEnum.COUNTY.getLevel())
			.collect(toMap(ca -> ca.getMvArea().getCountyId(), ca -> ca));
		this.opptellingsvalgstyrerPerValgdistrikt = opptellingsvalgstyrer.stream().collect(toMap(ru -> ru.getMvElection().getContestId(), ru -> ru));
		this.antallRaderGenerert = 0;
	}
	
	public ValgkortgrunnlagRad tilValgkortgrunnlagRad(Voter velger) {
		try {
			ValgkortgrunnlagRad valgkortgrunnlagRad = new ValgkortgrunnlagRad();
			konverterValgstyreinfo(velger, valgkortgrunnlagRad);
			konverterPapirmanntallsinfo(velger, valgkortgrunnlagRad);
			konverterKommuneOgKretstilhorighet(velger, valgkortgrunnlagRad);
			konverterPersoninfo(velger, valgkortgrunnlagRad);
			konverterManntallsnummerinfo(velger, valgkortgrunnlagRad);
			konverterAdresseinformasjon(velger, valgkortgrunnlagRad);
			konverterStemmestedsinfo(velger, valgkortgrunnlagRad);

			oppdaterStatistikk();
			return valgkortgrunnlagRad;
		} catch (Exception e) {
			LOG.error("Generering av valgkortgrunnlag for velger " + velger.getId() + " feilet");
			throw e;
		}
	}

	private void konverterValgstyreinfo(Voter velger, ValgkortgrunnlagRad valgkortgrunnlagRad) {
		ContestArea valgdistriktsomraade = valgdistriktsomraadePerKommune.get(velger.getMunicipalityId());
		ReportingUnit valgstyret = finnValgstyre(velger, valgdistriktsomraade);
		
		if (valgstyret == null) {
			LOG.error("Valgstyret for kommune " + velger.getMunicipalityId() + " eksisterer ikke");
		} else {
			valgkortgrunnlagRad.setValgstyretNavn(konverterValgstyretsNavn(velger, valgdistriktsomraade, valgstyret));
			valgkortgrunnlagRad.setValgstyretAdresselinje1(valgstyret.getAddressLine1());
			valgkortgrunnlagRad.setValgstyretPostnummer(valgstyret.getPostalCode());
			valgkortgrunnlagRad.setValgstyretPoststed(valgstyret.getPostTown());
		}
	}

	private ReportingUnit finnValgstyre(Voter velger, ContestArea valgdistriktsomraade) {
		if (erMinus30kommune(valgdistriktsomraade)) {
			return opptellingsvalgstyrerPerValgdistrikt.get(valgdistriktsomraade.getContest().getId());
		} else {
			return valgstyrePerKommune.get(velger.getMunicipalityId());
		}
	}

	private boolean erMinus30kommune(ContestArea valgdistriktsomraade) {
		return harMultiomraadedistrikter && valgdistriktsomraade.isChildArea();
	}

	private String konverterValgstyretsNavn(Voter velger, ContestArea valgdistriktsomraade, ReportingUnit valgstyre) {
		boolean erNynorsk = erNynorsk(velger);
		
		if (erMinus30kommune(valgdistriktsomraade)) {
			String opptellingsvalgstyret = erNynorsk ? "Oppteljingsvalstyret" : "Opptellingsvalgstyret";
			return opptellingsvalgstyret + " i " + valgstyretsNavn(valgstyre);
		} else if (erPluss30kommune(valgdistriktsomraade)) {
			String samevalgstyret = erNynorsk ? "Samevalstyret" : "Samevalgstyret";
			return samevalgstyret + " i " + valgstyretsNavn(valgstyre) + " kommune";
		} else {
			String valgstyret = erNynorsk ? "Valstyret" : "Valgstyret";
			return valgstyret + " i " + valgstyretsNavn(valgstyre) + " kommune";
		}
	}

	private boolean erNynorsk(Voter velger) {
		boolean nynorsk = false;
		if (velger.getMvArea().getMunicipality().getLocale() == null) {
			LOG.warn("Kommune " + velger.getMvArea().getMunicipality().getId() + " har ikke konfigurert målform. Benytter bokmål på valgkortet");
		} else if ("nn-NO".equals(velger.getMvArea().getMunicipality().getLocale().getId())) {
			nynorsk = true;
		}
		return nynorsk;
	}

	private String valgstyretsNavn(ReportingUnit valgstyre) {
		return valgstyre.getNameLine();
	}

	private boolean erPluss30kommune(ContestArea valgdistriktsomraade) {
		return harMultiomraadedistrikter && !valgdistriktsomraade.isChildArea();
	}

	private void konverterPapirmanntallsinfo(Voter velger, ValgkortgrunnlagRad valgkortgrunnlagRad) {
		if (velger.isInMunicipalityWithElectronicMarkoffs()) {
			valgkortgrunnlagRad.setRode(SEPARATOR_NONE);
			valgkortgrunnlagRad.setManntallSide(SEPARATOR_NONE);
			valgkortgrunnlagRad.setManntallLinje(SEPARATOR_NONE);
		} else {
			if (velger.getPollingStation() == null) {
				valgkortgrunnlagRad.setRode(SEPARATOR_NONE);
			} else {
				valgkortgrunnlagRad.setRode(velger.getPollingStation().getId());
			}
			valgkortgrunnlagRad.setManntallSide("" + velger.getElectoralRollPage());
			valgkortgrunnlagRad.setManntallLinje("" + velger.getElectoralRollLine());
		}
	}

	private void konverterKommuneOgKretstilhorighet(Voter velger, ValgkortgrunnlagRad valgkortgrunnlagRad) {
		valgkortgrunnlagRad.setValgkretsId(velger.getPollingDistrictId());
		valgkortgrunnlagRad.setKommuneId(velger.getMvArea().getMunicipality().getId());
		valgkortgrunnlagRad.setMaalform(velger.getMvArea().getMunicipality().getLocale().getId());
	}

	private void konverterPersoninfo(Voter velger, ValgkortgrunnlagRad valgkortgrunnlagRad) {
		valgkortgrunnlagRad.setFodselsaar("" + velger.getFodselsaar());
		valgkortgrunnlagRad.setNavn(velger.getFullName());
	}

	private void konverterManntallsnummerinfo(Voter velger, ValgkortgrunnlagRad valgkortgrunnlagRad) {
		valgkortgrunnlagRad.setKortManntallsnummer("" + velger.getNumber());
		valgkortgrunnlagRad.setFulltManntallsnummer(new Manntallsnummer(velger.getNumber(), valgaarssifferForValghendelse).getManntallsnummer());
	}
	
	private void konverterAdresseinformasjon(Voter velger, ValgkortgrunnlagRad valgkortgrunnlagRad) {
		if (velger.isMailingAddressSpecified()) {
			valgkortgrunnlagRad.setAdresselinje1(velger.getMailingAddressLine1());
			valgkortgrunnlagRad.setAdresselinje2(velger.getMailingAddressLine2());
			valgkortgrunnlagRad.setAdresselinje3(velger.getMailingAddressLine3());
			valgkortgrunnlagRad.setPostnummer("");
			valgkortgrunnlagRad.setPoststed("");
		} else {
			valgkortgrunnlagRad.setAdresselinje1(velger.getAddressLine1());
			valgkortgrunnlagRad.setAdresselinje2(velger.getAddressLine2());
			valgkortgrunnlagRad.setAdresselinje3(velger.getAddressLine3());
			valgkortgrunnlagRad.setPostnummer(velger.getPostalCode());
			valgkortgrunnlagRad.setPoststed(velger.getPostTown());
		}
	}

	private void konverterStemmestedsinfo(Voter velger, ValgkortgrunnlagRad valgkortgrunnlagRad) {
		Set<PollingPlace> stemmesteder = velger.getMvArea().getPollingDistrict().getPollingPlaces();
		PollingPlace stemmested = stemmesteder.stream().filter(PollingPlace::isElectionDayVoting).findFirst().orElse(null);
		if (stemmested == null) {
			LOG.warn("Stemmested for velger " + velger.getId() + " ikke funnet");
		} else {
			if (stemmested.getOpeningHours() == null || stemmested.getOpeningHours().isEmpty()) {
				LOG.warn("Åpningstider for stemmested " + stemmested.getId() + " er ikke definert. Velger " + velger.getId()
					+ " får derfor ikke informasjon om stemmested på valgkortet");
			} else {
				valgkortgrunnlagRad.setValglokaleAapningstider(konverterAapningstider(stemmested));
				valgkortgrunnlagRad.setValglokaleNavn(stemmested.getName());
				valgkortgrunnlagRad.setValglokaleAdresselinje1(stemmested.getAddressLine1());
				valgkortgrunnlagRad.setValglokalePostnummer(stemmested.getPostalCode());
				valgkortgrunnlagRad.setValglokalePoststed(stemmested.getPostTown());
			}

			if (stemmested.getInfoText() != null) {
				valgkortgrunnlagRad.setInfotekst(linjeskiftTilVertikalStrek(stemmested.getInfoText()));
			}
		}

		if (erIMinus30Kommune(velger)) {
			String infoTekst = infoTekstMinus30Kommune(velger);
			valgkortgrunnlagRad.setInfotekst(linjeskiftTilVertikalStrek(infoTekst));
		}
	}

	private String konverterAapningstider(PollingPlace stemmested) {
		PollingPlace stemmestedMedAapningstider = stemmestederMedAapningstiderMap.get(stemmested.getPk());
		if (stemmestedMedAapningstider == null) {
			LOG.warn("Stemmested " + stemmested.getId() + " har ikke åpningstider");
			return null;
		} else {
			return AapningstidFormatter.format(stemmestedMedAapningstider.getOpeningHours());
		}
	}

	private boolean erIMinus30Kommune(Voter velger) {
		ContestArea valgdistriktsomraade = valgdistriktsomraadePerKommune.get(velger.getMunicipalityId());
		return erMinus30kommune(valgdistriktsomraade);
	}

	private String infoTekstMinus30Kommune(Voter velger) {
		Locale locale = erNynorsk(velger) ? new Locale("nn", "NO") : new Locale("nb", "NO");
		return MessageProvider.get(locale, "@valgkort.sametingsvalg.minus30kommune.infotekst");
	}

	private String linjeskiftTilVertikalStrek(String tekst) {
		return tekst.replace("\n", "|").replace("\r", "");
	}

	private void oppdaterStatistikk() {
		antallRaderGenerert++; // Ikke trådsikkert
		if (antallRaderGenerert % ANTALL_RADER_PER_UTSKRIFT_TIL_LOGG == 0) {
			LOG.debug("Antall valgkortgrunnlagsrader konvertert: " + antallRaderGenerert);
		}
	}

	public boolean erTilknyttetValgdistrikt(Voter velger) {
		return valgdistriktsomraadePerKommune.get(velger.getMunicipalityId()) != null
			|| valgdistriktsomraadePerFylke.get(velger.getCountyId()) != null;
	}
}
