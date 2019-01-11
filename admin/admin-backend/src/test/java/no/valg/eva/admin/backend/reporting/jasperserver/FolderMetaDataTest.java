package no.valg.eva.admin.backend.reporting.jasperserver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.valg.eva.admin.backend.reporting.jasperserver.api.FolderMetaData;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FolderMetaDataTest {
	public static final String REPORT_ACCESS = "report.access";
	private static final String XML_EXAMPLE =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<folderMetaData>\n"
					+ "    <access>" + REPORT_ACCESS + "</access>\n"
					+ "</folderMetaData>\n";

	@Test
	public void testMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(FolderMetaData.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter writer = new StringWriter();
		FolderMetaData folderMetaData = new FolderMetaData();
		folderMetaData.setAccess(REPORT_ACCESS);
		marshaller.marshal(folderMetaData, writer);
		Assert.assertEquals(writer.toString(), XML_EXAMPLE);
	}

	@Test
	public void testUnMarshal() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(FolderMetaData.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new StringReader(XML_EXAMPLE));
		assertTrue(result instanceof FolderMetaData);
		FolderMetaData folderMetaData = (FolderMetaData) result;
		assertEquals(folderMetaData.getAccess(), REPORT_ACCESS);
	}
}
