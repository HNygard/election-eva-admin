package no.valg.eva.admin.frontend.electoralroll.ctrls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.configuration.domain.model.Aarsakskode;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ApplicationScoped
public class AarsakskodeController extends BaseController {

	private VoterService voterService;

	private Map<String, String> aarsakskodeMap;

	public AarsakskodeController() {
	}

	@Inject
	public AarsakskodeController(VoterService voterService) {
		this.voterService = voterService;
	}

	@PostConstruct
	public void init() {
		List<Aarsakskode> aarsakskodeList = voterService.findAllAarsakskoder();
		aarsakskodeMap = new HashMap<>();
		for (Aarsakskode aKode : aarsakskodeList) {
			aarsakskodeMap.put(aKode.getId(), aKode.getName());
		}
	}

	public Map<String, String> getAarsakskodeMap() {
		return aarsakskodeMap;
	}
}
