package no.valg.eva.admin.frontend.counting.ctrls;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.service.AntallStemmesedlerLagtTilSideService;
import no.valg.eva.admin.frontend.counting.view.AntallStemmesedlerLagtTilSideModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

@Named
@ViewScoped
public class AntallStemmesedlerLagtTilSideController extends KontekstAvhengigController {

	@Inject
	private AntallStemmesedlerLagtTilSideService service;

	private AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide;

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		if (getUserData().isElectionEventAdminUser()) {
			setup.leggTil(geografi(KOMMUNE));
		}
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		antallStemmesedlerLagtTilSide = service.hentAntallStemmesedlerLagtTilSide(getUserData(), kontekst.kommuneSti());
	}

	public int getTotaltAntallStemmesedler() {
		if (antallStemmesedlerLagtTilSide == null) {
			return 0;
		}
		return antallStemmesedlerLagtTilSide.getTotaltAntallStemmesedlerLagtTilSideForValg();
	}

	public boolean isLagreAntallStemmesedlerDisabled() {
		return antallStemmesedlerLagtTilSide == null || !antallStemmesedlerLagtTilSide.isLagringAvAntallStemmesedlerLagtTilSideMulig();
	}

	public void lagreAntallStemmesedler() {
		service.lagreAntallStemmesedlerLagtTilSide(getUserData(), antallStemmesedlerLagtTilSide);
		MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, getMessageProvider().get("@opptelling.antallStemmesedlerLagtTilSide.erLagret"));
	}

	public AntallStemmesedlerLagtTilSideModel getAntallStemmesedlerModel() {
		return new AntallStemmesedlerLagtTilSideModel(this);
	}

	public AntallStemmesedlerLagtTilSide getAntallStemmesedlerLagtTilSide() {
		return antallStemmesedlerLagtTilSide;
	}

	public String getHeaderFooterStyle() {
		if (antallStemmesedlerLagtTilSide == null || antallStemmesedlerLagtTilSide.getAntallStemmesedlerLagtTilSideForValgList().size() > 1) {
			return "";
		}
		return "hide-table-header hide-table-footer";
	}
}
