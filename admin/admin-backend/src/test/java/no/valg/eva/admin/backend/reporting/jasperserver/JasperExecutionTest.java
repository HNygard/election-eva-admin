package no.valg.eva.admin.backend.reporting.jasperserver;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperExecution;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/**
 * Tests marshalling and unmarshalling of {@link no.valg.eva.admin.backend.reporting.jasperserver.api.JasperExecution}
 */
public class JasperExecutionTest {
	private static final String REQUEST_ID = "677629003_1355225037212_1";
	private static final String XML_EXPECTED =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<reportExecution>\n"
					+ "    <currentPage>1</currentPage>\n"
					+ "    <exports>\n"
					+ "        <export>\n"
					+ "            <id>pdf</id>\n"
					+ "            <status>queued</status>\n"
					+ "        </export>\n"
					+ "    </exports>\n"
					+ "    <reportURI>reports/Myreport</reportURI>\n"
					+ "    <requestId>" + REQUEST_ID + "</requestId>\n"
					+ "    <status>execution</status>\n"
					+ "</reportExecution>\n";

	@Test
	public void testMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(JasperExecution.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter writer = new StringWriter();
		JasperExecution jasperExecution = new JasperExecution(1, "reports/Myreport", REQUEST_ID, "execution", Lists.newArrayList(new JasperExecution.Export(
				"pdf", "queued")));
		marshaller.marshal(jasperExecution, writer);
		Assert.assertEquals(writer.toString(), XML_EXPECTED);
	}

	@Test
	public void testUnMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(JasperExecution.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new StringReader(XML_EXPECTED));
		Assert.assertTrue(result instanceof JasperExecution);
		Assert.assertEquals(((JasperExecution) result).getExports().size(), 1);
		Assert.assertEquals(((JasperExecution) result).getExports().get(0).getId(), "pdf");
	}
}
