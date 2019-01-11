package no.valg.eva.admin.backend.reporting.jasperserver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.valg.eva.admin.backend.reporting.jasperserver.api.DataType;

import org.testng.Assert;
import org.testng.annotations.Test;


public class DataTypeTest {
	private static final String XML_EXAMPLE =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<dataType>\n"
					+ "    <type>text</type>\n"
					+ "    <maxLength>10</maxLength>\n"
					+ "    <maxValue>100</maxValue>\n"
					+ "    <minValue>1</minValue>\n"
					+ "    <pattern>PATTERN</pattern>\n"
					+ "    <strictMax>false</strictMax>\n"
					+ "    <strictMin>true</strictMin>\n"
					+ "</dataType>\n";

	@Test
	public void testMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(DataType.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter writer = new StringWriter();
		DataType dataType = new DataType(DataType.Type.TEXT);
		dataType.setMaxLength(10);
		dataType.setMaxValue("100");
		dataType.setMinValue("1");
		dataType.setPattern("PATTERN");
		dataType.setStrictMax(false);
		dataType.setStrictMin(true);
		marshaller.marshal(dataType, writer);
		Assert.assertEquals(writer.toString(), XML_EXAMPLE);
	}

	@Test
	public void testUnMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(DataType.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new StringReader(XML_EXAMPLE));
		assertTrue(result instanceof DataType);
		DataType dataType = (DataType) result;
		assertEquals(dataType.getBaseType(), DataType.Type.TEXT);
		assertSame(dataType.getMaxLength(), 10);
		assertEquals(dataType.getMinValue(), "1");
		assertEquals(dataType.getMaxValue(), "100");
		assertEquals(dataType.getPattern(), "PATTERN");
	}
}

