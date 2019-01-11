package no.valg.eva.admin.frontend.common;

public class PageTitleMetaModel {

	private String label;
	private String value;
	private String valueStyleClass;
	private boolean link;

	public PageTitleMetaModel(String label, String value) {
		this(label, value, false);
	}

	public PageTitleMetaModel(String label, String value, boolean link) {
		this(label, value, link, null);
	}

	public PageTitleMetaModel(String label, String value, String valueStyleClass) {
		this(label, value, false, valueStyleClass);
	}

	public PageTitleMetaModel(String label, String value, boolean link, String valueStyleClass) {
		this.value = value;
		this.label = label;
		this.link = link;
		this.valueStyleClass = valueStyleClass;
	}

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return value;
	}

	public String getStyleClass() {
		return valueStyleClass;
	}

	public boolean isStyleClassSet() {
		return valueStyleClass != null;
	}

	public boolean isLink() {
		return link;
	}
}
