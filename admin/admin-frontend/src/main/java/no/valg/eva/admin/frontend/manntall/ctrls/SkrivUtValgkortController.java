package no.valg.eva.admin.frontend.manntall.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

import java.io.IOException;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.SpecialPurposeReportService;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ViewScoped
public class SkrivUtValgkortController extends BaseController {

	// Injected
	private UserData userData;
	private SpecialPurposeReportService specialPurposeReportService;
	private MvAreaService mvAreaService;

	public SkrivUtValgkortController() {
		// For CDI
	}

	@Inject
	public SkrivUtValgkortController(UserData userData, SpecialPurposeReportService specialPurposeReportService, MvAreaService mvAreaService) {
		this.userData = userData;
		this.specialPurposeReportService = specialPurposeReportService;
		this.mvAreaService = mvAreaService;
	}

	public boolean isVisSkrivValgkortKnapp(Voter velger) {
		return velger != null && velger.getElectoralRollLine() != null;
	}

	/**
	 * Skriv ut valgkort rapport for funnet velger.
	 */
	public void skrivValgkort(Voter velger, MvElection valgGruppe, MvArea omrade) {
		if (velger != null) {
			execute(() -> {
				MvArea brukOmrade = omrade;
				if (velger.getMvArea() != null && velger.getMvArea().getAreaLevel() == AreaLevelEnum.POLLING_DISTRICT.getLevel()) {
					AreaPath path = velger.getMvArea().areaPath().toMunicipalityPath();
					brukOmrade = mvAreaService.findSingleByPath(path);
				}
				byte[] bytes = specialPurposeReportService.generateElectionCard(userData, velger.getPk(), brukOmrade, valgGruppe);
				try {
					FacesUtil.sendFile("electionCard.pdf", bytes);
				} catch (IOException ioe) {
					MessageUtil.buildDetailMessage(SEVERITY_ERROR, ioe.getMessage());
				}
			});
		}
	}
}
