package no.valg.eva.admin.frontend.picker.cfg;

import no.evote.constants.ElectionLevelEnum;

@Deprecated
public class ElectionCfg {

	private ElectionLevelEnum level;

	public ElectionCfg() {
		// convenience
	}

	public ElectionCfg(ElectionLevelEnum level) {
		this.level = level;
	}

	public ElectionCfg setLevel(ElectionLevelEnum level) {
		this.level = level;
		return this;
	}

	public ElectionLevelEnum getElectionLevel() {
		return level;
	}

	public Integer getLevel() {
		return level == null ? null : level.getLevel();
	}
}
