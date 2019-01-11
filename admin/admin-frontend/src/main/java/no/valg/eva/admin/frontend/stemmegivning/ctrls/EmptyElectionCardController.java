package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste.LAG_NYTT_VALGKORT;

import java.io.IOException;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.evote.security.UserData;
import no.evote.service.SpecialPurposeReportService;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

import org.joda.time.LocalDate;

@Named
@ViewScoped
public class EmptyElectionCardController extends KontekstAvhengigController {
	// Injected
	private SpecialPurposeReportService specialPurposeReportService;

	private ValggruppeSti valggruppeSti;
	private KommuneSti kommuneSti;
	private String name;
	private String address;
	private String zip;
	private String town;
	private LocalDate dateOfBirth;
	private String pollingDistrict;
	private String pollingPlaceName;

	@SuppressWarnings("unused")
	public EmptyElectionCardController() {
		// For CDI
	}

	@Inject
	public EmptyElectionCardController(SpecialPurposeReportService specialPurposeReportService) {
		this.specialPurposeReportService = specialPurposeReportService;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE));
		setup.leggTil(geografi(KOMMUNE).medTjeneste(LAG_NYTT_VALGKORT));
		return setup;
	}

	@Override
	public void initialized(Kontekst data) {
		this.valggruppeSti = data.valggruppeSti();
		this.kommuneSti = data.kommuneSti();
	}

	public void makeElectionCard() {
		Voter voter = new Voter();
		voter.setNameLine(name);
		voter.setAddressLine1(address);
		voter.setPostalCode(zip);
		voter.setPostTown(town);
		voter.setDateOfBirth(dateOfBirth);

		execute(() -> {
			try {
				byte[] bytes = specialPurposeReportService.generateEmptyElectionCard(
						getUserData(),
						voter,
						valggruppeSti,
						kommuneSti,
						pollingDistrict,
						pollingPlaceName);
				FacesUtil.sendFile("electionCard.pdf", bytes);
			} catch (IOException e) {
				throw new EvoteException("Error generating electionCard.pdf " + e, e);
			}
		});

	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(final String zip) {
		this.zip = zip;
	}

	public String getTown() {
		return town;
	}

	public void setTown(final String town) {
		this.town = town;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(final LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getPollingDistrict() {
		return pollingDistrict;
	}

	public void setPollingDistrict(final String pollingDistrict) {
		this.pollingDistrict = pollingDistrict;
	}

	public String getPollingPlaceName() {
		return pollingPlaceName;
	}

	public void setPollingPlaceName(final String pollingPlaceName) {
		this.pollingPlaceName = pollingPlaceName;
	}

	@Override
	protected Kontekst sjekkKontekstValggeografiSti(Kontekst kontekst, UserData userData) {
		ValggeografiSti kontekstValggeografiSti = kontekst.getValggeografiSti();
		if (kontekstValggeografiSti != null) {
			ValghendelseSti valghendelseSti = userData.operatorValggeografiSti().valghendelseSti();
			if (!kontekstValggeografiSti.likEllerUnder(valghendelseSti)) {
				return null;
			}
		}
		return kontekst;
	}
}
