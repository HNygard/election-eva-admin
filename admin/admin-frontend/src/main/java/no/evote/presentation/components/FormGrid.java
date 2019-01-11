package no.evote.presentation.components;

import javax.faces.component.UIPanel;

public class FormGrid extends UIPanel {
	public static final String COMPONENT_TYPE = "no.evote.presentation.components.Panel";
	public static final String RENDERER_TYPE = "no.evote.presentation.components.DivForm";

	public FormGrid() {
		setRendererType(RENDERER_TYPE);
	}
}
