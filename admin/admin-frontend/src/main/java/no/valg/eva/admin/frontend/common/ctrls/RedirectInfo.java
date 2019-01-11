package no.valg.eva.admin.frontend.common.ctrls;

import java.io.Serializable;

public class RedirectInfo implements Serializable {

	public static final String REDIRECT_INFO_SESSION_KEY = RedirectInfo.class.getName();

	private Object data;
	private String url;
	private String title;

	public RedirectInfo(Object data, String url, String title) {
		this.data = data;
		this.url = url;
		this.title = title;
	}

	public Object getData() {
		return data;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}
}
