package no.valg.eva.admin.frontend.common.dialog;

import static org.apache.commons.lang3.StringEscapeUtils.escapeEcmaScript;

import java.io.Serializable;
import java.util.Arrays;

import no.valg.eva.admin.frontend.util.FacesUtil;

public class Dialog implements Serializable {

	public static final String DEFAULT_ON_SHOW = "";
	public static final String DEFAULT_ON_HIDE = "";
	public static final String NO_TITLE = "";
	public static final String DEFAULT_WIDTH = "auto";
	public static final String DEFAULT_HEIGHT = "auto";
	public static final String STANDARD_WIDTH = "900";
	public static final boolean CLOSEABLE = true;
	public static final boolean NOT_CLOSEABLE = false;

	private final String id;
	private final String template;
	private final String title;
	private String onShow = DEFAULT_ON_SHOW;
	private String onHide = DEFAULT_ON_HIDE;
	private boolean closeable = CLOSEABLE;
	private String width = DEFAULT_WIDTH;
	private String height = DEFAULT_HEIGHT;

	public Dialog(String template, String title) {
		this(null, template, title);
	}

	public Dialog(String id, String template, String title) {
		if (id == null) {
			this.id = template.substring(template.lastIndexOf('/') + 1);
		} else {
			this.id = id;
		}
		this.template = template + ".xhtml";
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public String getTemplate() {
		return template;
	}

	public String getTitle() {
		return title;
	}

	public String getOnShow() {
		return onShow;
	}

	public void setOnShow(String onShow) {
		this.onShow = onShow;
	}

	public String getOnHide() {
		return onHide;
	}

	public void setOnHide(String onHide) {
		this.onHide = onHide;
	}

	public boolean isCloseable() {
		return closeable;
	}

	public void setCloseable(boolean closeable) {
		this.closeable = closeable;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getOpenJS() {
		return "PF('" + getId() + "').show()";
	}

	public void open() {
		FacesUtil.executeJS(getOpenJS());
	}

	public void setTitleAndOpen(String title) {
		FacesUtil.executeJS(getTitleJS(title));
		open();
	}

	public String getCloseJS() {
		return "PF('" + getId() + "').hide()";
	}

	public void close() {
		FacesUtil.executeJS(getCloseJS());
	}

	public void closeAndUpdate(String... updateIds) {
		close();
		FacesUtil.updateDom(Arrays.asList(updateIds));
	}

	public String getTitleJS(String title) {
		return "PF('" + getId() + "').setTitle('" + escapeEcmaScript(title) + "')";
	}

	@Override
	public String toString() {
		return "Dialog{"
				+ "id='" + id + '\''
				+ ", template='" + template + '\''
				+ ", title='" + title + '\''
				+ ", onShow='" + onShow + '\''
				+ ", onHide='" + onHide + '\''
				+ ", closeable=" + closeable
				+ ", width='" + width + '\'' + '}';
	}
}
