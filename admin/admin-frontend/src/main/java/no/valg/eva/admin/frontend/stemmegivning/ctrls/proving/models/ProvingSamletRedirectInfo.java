package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models;

import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;

public class ProvingSamletRedirectInfo extends RedirectInfo {

	private ProvingSamletForm form;

	public ProvingSamletRedirectInfo(Object data, String url, String title, ProvingSamletForm form) {
		super(data, url, title);
		this.form = form;
	}

	public ProvingSamletForm getForm() {
		return form;
	}
}
