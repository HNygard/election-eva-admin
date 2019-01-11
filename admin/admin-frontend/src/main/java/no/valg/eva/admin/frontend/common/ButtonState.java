package no.valg.eva.admin.frontend.common;

public enum ButtonState {
	/** button is rendered, but disabled */
	DISABLED(true, false),
	/** button is rendered and enabled */
	ENABLED(true, true),
	/** button is not rendered */
	NOT_RENDERED(false, false);
	
	private final boolean rendered;
	private final boolean enabled;

	private ButtonState(boolean rendered, boolean enabled) {
		this.rendered = rendered;
		this.enabled = enabled;
	}
	
	public boolean isRendered() {
		return rendered;
	}
	
	public boolean isDisabled() {
		return !enabled;
	}
}
