package no.valg.eva.admin.frontend.configuration.ctrls;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_EML_Behandle;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import no.evote.dto.BatchInfoDto;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;

/**
 * Controller for exporting EML.
 */
@Named("emlExportController")
@ViewScoped
public class EMLExportController extends BaseExportController {

	private Map<Long, Boolean> validEML = new HashMap<>();
	private List<BatchInfoDto> batchInfoDtos;

	@PostConstruct
	public void init() {
		if (!canGenerateEML()) {
			MessageUtil.buildDetailMessage("@election.election_event.eml_disabled", FacesMessage.SEVERITY_WARN);
		}
		fetchBatchInfos();
	}

	public boolean canGenerateEML() {
		int statusId = getElectionEvent().getElectionEventStatus().getId();
		return (statusId == ElectionEventStatusEnum.APPROVED_CONFIGURATION.id());
	}

	public boolean isKonfigurasjonEmlBehandle() {
		return getUserDataController().getUserAccess().hasAccess(Konfigurasjon_EML_Behandle);
	}

	public void generateEML() {
		execute(() -> {
			getExportService().generateEML(getUserData(), getElectionEvent().getPk());
			fetchBatchInfos();
		});
	}

	public List<BatchInfoDto> getGeneratedEMLBatches() {
		return batchInfoDtos;
	}

	public boolean hasBeenValidated(BatchInfoDto batchInfo) {
		return batchInfo != null && validEML.containsKey(batchInfo.getPk());
	}

	public boolean isValid(BatchInfoDto batchInfo) {
		return validEML.get(batchInfo.getPk());
	}

	public void validateBatch(BatchInfoDto batchInfo) throws IOException, ParserConfigurationException {
		validEML.put(batchInfo.getPk(), getExportService().validateGeneratedEML(getUserData(), batchInfo.getPk()));
	}

	public void download(BatchInfoDto batchInfo) throws IOException {
		String electionEventId = getUserDataController().getElectionEvent().getId();
		byte[] bytes = getExportService().getGeneratedEML(getUserData(), batchInfo.getPk());
		ExternalContext context = getFacesContext().getExternalContext();
		context.responseReset();
		HttpServletResponse response = (HttpServletResponse) context.getResponse();
		response.setContentType("application/force-download");
		response.addHeader("Content-Disposition", "attachment; filename=\"EML_" + electionEventId + ".zip\"");

		ServletOutputStream out = response.getOutputStream();
		response.setContentLength(bytes.length);
		out.write(bytes);
		out.close();

		getFacesContext().responseComplete();
	}

	private void fetchBatchInfos() {
		batchInfoDtos = getExportService().getGeneratedEMLBatches(getUserData(), getElectionEvent().getId());
	}
}
