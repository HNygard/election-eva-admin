package no.valg.eva.admin.frontend.opptelling;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class BehandleManueltForkastedeController extends ForkastedeOpptellingerController {
	@Override
	protected String url() {
		return "/secure/counting/approveManualRejectedCount.xhtml?category=%s&contestPath=%s&areaPath=%s&fraMeny=true";
	}
}
