package no.valg.eva.admin.frontend.picker.cfg;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public abstract class ContextPickerCfg {

	protected AreaCfg areaCfg;
	protected ElectionCfg electionCfg;
	private List<String> keepRequestParams;

	public ContextPickerCfg() {
		areaCfg = new AreaCfg();
		electionCfg = new ElectionCfg();
		keepRequestParams = new ArrayList<>();
	}

	public abstract String getPageTitle();

	public abstract String getUri();

	public AreaCfg getAreaCfg() {
		return areaCfg;
	}

	public void setAreaCfg(AreaCfg areaCfg) {
		this.areaCfg = areaCfg;
	}

	public void setElectionCfg(ElectionCfg electionCfg) {
		this.electionCfg = electionCfg;
	}

	public ElectionCfg getElectionCfg() {
		return electionCfg;
	}

	public boolean isKeepRequestParams() {
		return !keepRequestParams.isEmpty();
	}

	public void setKeepRequestParams(String parameterName) {
		keepRequestParams.add(parameterName);
	}

	public List<String> getKeepRequestParams() {
		return keepRequestParams;
	}
}
