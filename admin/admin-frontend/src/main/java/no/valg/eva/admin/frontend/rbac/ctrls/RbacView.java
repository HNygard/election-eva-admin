package no.valg.eva.admin.frontend.rbac.ctrls;


public enum RbacView {

	LIST("operatorList.xhtml"),
	SEARCH("operatorSearch.xhtml"),
	EXISTING("existingOperator.xhtml"),
	ELECTORAL_ROLL_LIST("electoralRollList.xhtml"),
	NEW("operatorEdit.xhtml"),
	NEW_FROM_ELECTORAL_ROLL("operatorEdit.xhtml"),
	NEW_FROM_EXISTING_VOTER("operatorEdit.xhtml"),
	EDIT("operatorEdit.xhtml"),
	CREATED("operatorCreated.xhtml");

	private String template;

	private RbacView(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public boolean isNewMode() {
		return this == NEW || this == NEW_FROM_ELECTORAL_ROLL || this == NEW_FROM_EXISTING_VOTER;
	}
}
