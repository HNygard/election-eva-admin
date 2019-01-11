package no.evote.presentation.components;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * Render a combination of text and form fields as a set of divs and spans.
 */
public class DivFormRenderer extends Renderer {
	private static final String CLASS_ATTR = "class";
	private static final String DIV_ELEM = "div";
	private static final String SPAN_ELEM = "span";

	private static final String ROW_CLASS = "fwFormRow";

	private static final String[] DEFAULT_COLUMN_CLASSES = new String[] { "fwFormLabel", "fwFormField" };

	@Override
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException {

		ResponseWriter writer = context.getResponseWriter();
		writer.startElement(DIV_ELEM, component);
		writer.writeAttribute(CLASS_ATTR, "fwForm", null);

		writer.flush();
	}

	@Override
	public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.endElement(DIV_ELEM);
		writer.flush();
	}

	@Override
	public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException {

		int columns = 2;
		String[] columnClasses = DEFAULT_COLUMN_CLASSES;

		Map<String, Object> attributes = component.getAttributes();
		if (attributes.containsKey("columns")) {
			columns = Integer.parseInt((String) attributes.get("columns"));
		}

		if (attributes.containsKey("columnClasses")) {
			columnClasses = ((String) attributes.get("columnClasses")).split("\\s");
		}

		ResponseWriter writer = context.getResponseWriter();
		List<UIComponent> children = component.getChildren();

		int currentColumn = 0;
		for (UIComponent child : children) {
			if (currentColumn == 0) {
				startRow(writer, component);
			}

			if (child.isRendered()) {
				startColumn(writer, component, currentColumn, columnClasses);
				child.encodeAll(context);
				closeColumn(writer);
			}
			currentColumn++;

			if (currentColumn == columns) {
				closeRow(writer, component);
				currentColumn = 0;
			}
		}

		if (currentColumn != 0) {
			// Close the last row
			closeRow(writer, component);
		}
		writer.startElement(DIV_ELEM, component);
		writer.writeAttribute(CLASS_ATTR, "clear", null);
		writer.endElement(DIV_ELEM);

		writer.flush();
	}

	private void closeColumn(final ResponseWriter writer) throws IOException {
		writer.endElement(SPAN_ELEM);
	}

	private void startColumn(final ResponseWriter writer, final UIComponent component, final int currentColumn, final String[] columnClasses)
			throws IOException {
		writer.startElement(SPAN_ELEM, component);
		if (currentColumn < columnClasses.length) {
			writer.writeAttribute(CLASS_ATTR, columnClasses[currentColumn], "");
		}
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

	private void startRow(final ResponseWriter writer, final UIComponent component) throws IOException {
		writer.startElement(DIV_ELEM, component);
		writer.writeAttribute(CLASS_ATTR, ROW_CLASS, "");
	}

	private void closeRow(final ResponseWriter writer, final UIComponent component) throws IOException {
		writer.startElement(DIV_ELEM, component);
		writer.writeAttribute("class", "clear", null);
		writer.endElement(DIV_ELEM);

		writer.endElement(DIV_ELEM);
	}
}
