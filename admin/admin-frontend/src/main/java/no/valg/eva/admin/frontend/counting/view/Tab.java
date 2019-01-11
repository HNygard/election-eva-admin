package no.valg.eva.admin.frontend.counting.view;

import no.valg.eva.admin.frontend.counting.ctrls.CountController;

public class Tab {

	private String id;
	private String title;
	private String template;
	private CountController controller;
	private boolean current;

	public Tab(String id, String template, CountController controller, boolean current) {
		this(id, null, template, controller, current);
	}

	public Tab(String id, String title, String template, CountController controller, boolean current) {
		this.id = id;
		this.title = title;
		this.template = template;
		this.controller = controller;
		this.current = current;
	}

	public String getTitle() {
		if (title == null) {
			if (controller.isApproved()) {
				return "@count.tab.type[" + id + "].approved";
			}
			return "@count.tab.type[" + id + "]";
		}
		return title;
	}

	public String getTemplate() {
		return template;
	}

	public CountController getController() {
		return controller;
	}

	public boolean isCurrent() {
		return current;
	}

	public String getId() {
		return id;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}
}
