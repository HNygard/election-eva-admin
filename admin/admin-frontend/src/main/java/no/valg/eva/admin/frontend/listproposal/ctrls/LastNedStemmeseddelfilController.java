package no.valg.eva.admin.frontend.listproposal.ctrls;

import static no.valg.eva.admin.frontend.util.FacesUtil.sendFile;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.SpecialPurposeReportService;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.listproposal.models.LastNedStemmeseddelfilPanel;

@Named
@ViewScoped
public class LastNedStemmeseddelfilController extends BaseController {

	static final String EXCEL_2007 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	static final String FILENAME_PATTERN = "@rapport.meta.Report_27.filename";

	// Injected
	private UserData userData;
	private MessageProvider messageProvider;
	private ValghierarkiService valghierarkiService;
	private ValggeografiService valggeografiService;
	private MvAreaService mvAreaService;
	private SpecialPurposeReportService specialPurposeReportService;

	private LastNedStemmeseddelfilPanel panel;

	public LastNedStemmeseddelfilController() {
		// For CDI
	}

	@Inject
	public LastNedStemmeseddelfilController(UserData userData, MessageProvider messageProvider, ValghierarkiService valghierarkiService,
			ValggeografiService valggeografiService, MvAreaService mvAreaService, SpecialPurposeReportService specialPurposeReportService) {
		this.userData = userData;
		this.messageProvider = messageProvider;
		this.valghierarkiService = valghierarkiService;
		this.valggeografiService = valggeografiService;
		this.mvAreaService = mvAreaService;
		this.specialPurposeReportService = specialPurposeReportService;
	}

	@PostConstruct
	public void init() {
		panel = new LastNedStemmeseddelfilPanel(this);
	}

	public void lastNed() {
		execute(() -> {
			String valgNavn = panel.getValg().navn();
			String valgdistrictId = panel.getValgdistrikt().id();
			String valgdistrictNavn = panel.getValgdistrikt().navn();
			String filename = messageProvider.get(FILENAME_PATTERN, valgNavn, valgdistrictId, valgdistrictNavn);
			doSendFile(filename, specialPurposeReportService.generateBallots(userData, panel.getValgdistrikt().sti()), EXCEL_2007);
		});
	}

	private void doSendFile(String filenamePattern, byte[] content, String format) {
		try {
			sendFile(filenamePattern, content, format);
		} catch (IOException e) {
			throw new EvoteException("Sending report file failed", e);
		}
	}

	public LastNedStemmeseddelfilPanel getPanel() {
		return panel;
	}

	public UserData getUserData() {
		return userData;
	}

	public ValghierarkiService getValghierarkiService() {
		return valghierarkiService;
	}

	public ValggeografiService getValggeografiService() {
		return valggeografiService;
	}

	public MvAreaService getMvAreaService() {
		return mvAreaService;
	}
}
