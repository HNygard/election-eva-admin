package no.evote.converters;

import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import no.valg.eva.admin.BaseFrontendTest;

public class BaseConverterTest extends BaseFrontendTest {

	protected FacesContext facesContextMock;
	protected UIComponent uiComponentMock;

	protected void setUp() throws Exception {
		facesContextMock = createMock(FacesContext.class);
		uiComponentMock = createMock(UIComponent.class);
		when(facesContextMock.getViewRoot().getLocale()).thenReturn(Locale.ENGLISH);
	}

	protected void mockUIComponentAttribute(String key, Object value) {
		when(uiComponentMock.getAttributes().get(key)).thenReturn(value);
	}

	protected void mockEvaluateExpressionGet(String expression, Class expectedType, Object value) {
		when(facesContextMock.getApplication().evaluateExpressionGet(facesContextMock, expression, expectedType)).thenReturn(value);
	}

}
