package no.valg.eva.admin.backend.reporting.jasperserver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperExecutionRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/**
 * Tests JAXB marshalling and unmarshalling of {@link no.valg.eva.admin.backend.reporting.jasperserver.api.JasperExecutionRequest}
 */
public class JasperExecutionRequestTest {
	private static final String XML_RESULT =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<reportExecutionRequest>\n"
					+ "    <async>false</async>\n"
					+ "    <outputFormat>pdf</outputFormat>\n"
					+ "    <parameters>\n"
					+ "        <reportParameter name=\"nameOfParameter\">\n"
					+ "            <value>value1</value>\n"
					+ "            <value>value2</value>\n"
					+ "        </reportParameter>\n"
					+ "    </parameters>\n"
					+ "    <reportUnitUri>reports/Myreport</reportUnitUri>\n"
					+ "</reportExecutionRequest>\n";

	private static final String XML_EXAMPLE =
			"<reportExecutionRequest>\n"
					+ "    <reportUnitUri>/supermart/details/CustomerDetailReport</reportUnitUri>\n"
					+ "    <async>true</async>\n"
					+ "    <freshData>false</freshData>\n"
					+ "    <saveDataSnapshot>false</saveDataSnapshot>\n"
					+ "    <outputFormat>html</outputFormat>\n"
					+ "    <interactive>true</interactive>\n"
					+ "    <ignorePagination>false</ignorePagination>\n"
					+ "    <pages>1-5</pages>\n"
					+ "    <parameters>\n"
					+ "        <reportParameter name=\"someParameterName\">\n"
					+ "            <value>value 1</value>\n"
					+ "            <value>value 2</value>\n"
					+ "        </reportParameter>\n"
					+ "        <reportParameter name=\"someAnotherParameterName\">\n"
					+ "            <value>another value</value>\n"
					+ "        </reportParameter>\n"
					+ "    </parameters>\n"
					+ "</reportExecutionRequest>\n";

	@Test
	public void testMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(JasperExecutionRequest.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		JasperExecutionRequest jasperExecutionRequest = new JasperExecutionRequest();
		jasperExecutionRequest.setReportUnitUri("reports/Myreport");
		jasperExecutionRequest.setOutputFormat("pdf");
		JasperExecutionRequest.ReportParameter reportParameter = new JasperExecutionRequest.ReportParameter("nameOfParameter", Lists.newArrayList("value1",
				"value2"));
		jasperExecutionRequest.setParameters(Lists.newArrayList(reportParameter));
		StringWriter writer = new StringWriter();
		marshaller.marshal(jasperExecutionRequest, writer);
		Assert.assertEquals(writer.toString(), XML_RESULT);
	}

	@Test
	public void testUnMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(JasperExecutionRequest.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new StringReader(XML_EXAMPLE));
		assertTrue(result instanceof JasperExecutionRequest);
		JasperExecutionRequest jasperExecutionRequest = (JasperExecutionRequest) result;
		assertEquals(jasperExecutionRequest.getParameters().size(), 2);
	}
}
