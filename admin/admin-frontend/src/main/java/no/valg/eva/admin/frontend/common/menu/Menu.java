package no.valg.eva.admin.frontend.common.menu;

import lombok.Getter;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Menu {
	private boolean header;
	private String text;
	private String url;
	private String icon;
	private String cssClass;
	private boolean dialog;
	private boolean enabled;
	private List<Menu> children = new ArrayList<>();
	private Object source;
	@Getter
	private boolean deprecated = false;

	public Menu(String text) {
		this(text, null, false, null);
	}

	public Menu(String text, boolean header) {
		this(text, null, header, null);
	}

	public Menu(String text, boolean header, boolean deprecated) {
		this(text, null, header, null);
		this.deprecated = deprecated;
	}

	public Menu(String text, String url) {
		this(text, url, false, null);
	}

	public Menu(String text, String url, boolean deprecated) {
		this(text, url, false, null);
		this.deprecated = deprecated;
	}

	public Menu(String text, boolean header, EnumUserMenuIcons icon) {
		this(text, null, header, icon);
	}

	private Menu(String text, String url, boolean header, EnumUserMenuIcons icon) {
		this.text = text;
		this.url = url;
		this.header = header;
		this.icon = icon == null ? null : icon.getValue();
		this.enabled = true;
		if (url != null && url.contains("/")) {
			cssClass = url.substring(url.lastIndexOf('/') + 1);
			String queryString = url.contains("?") ? cssClass.substring(cssClass.indexOf('?') + 1) : null;
			cssClass = cssClass.substring(0, cssClass.indexOf('.'));
			if (queryString != null) {
				StringTokenizer tokens = new StringTokenizer(queryString, "=");
				while (tokens.hasMoreTokens()) {
					cssClass += "_" + tokens.nextToken();
				}
			}
		}
	}

    public void addChild(Menu item) {
		children.add(item);
	}

	public List<Menu> getChildren() {
		return children;
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

	public boolean hasHeader() {
		return header;
	}

	public String getText() {
		return text;
	}

	public String getUrl() {
		return url;
	}

	public String getIcon() {
		return icon;
	}

	public boolean isDialog() {
		return dialog;
	}

	public void setDialog(boolean dialog) {
		this.dialog = dialog;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Menu setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public Object getSource() {
		return source;
	}

	public Menu setSource(Object source) {
		this.source = source;
		return this;
	}

	public boolean isCentralConfirmDialog() {
		return "centralConfirmDialog".equals(url);
	}

	public boolean isReportLink() {
		return source instanceof ValghendelsesRapport;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public String toString() {
		return "Menu{"
				+ "header=" + header
				+ ", text='" + text + '\''
				+ ", url='" + url + '\''
				+ ", icon='" + icon + '\''
				+ ", dialog=" + dialog
				+ ", children=" + children
				+ '}';
	}
}
