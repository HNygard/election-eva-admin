package no.valg.eva.admin.backend.reporting.jasperserver;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import no.valg.eva.admin.backend.reporting.jasperserver.api.ReportExecutionStatus;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ReportExecutionStatusTest {
	private static final String ERR_CODE = "errCode";
	private static final String STATUS_PLACEHOLDER = "_status_";
	private static final String MESSAGE_1 = "message1";
	private static final String PARAM_1 = "param1";
	private static final String PARAM_2 = "param2";
	private static final String XML_EXAMPLE = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<status>\n"
			+ "    <errorDescriptor>\n"
			+ "        <errorCode>errCode</errorCode>\n"
			+ "        <message>message1</message>\n"
			+ "        <parameters>\n"
			+ "            <parameter>param1</parameter>\n"
			+ "            <parameter>param2</parameter>\n"
			+ "        </parameters>\n"
			+ "    </errorDescriptor>\n"
			+ "    <value>_status_</value>\n"
			+ "</status>\n";

	@DataProvider(name = "statuses")
	public static Object[][] testUnMarshal() {
		return new Object[][] {
				{ ReportExecutionStatus.Status.FAILED },
				{ ReportExecutionStatus.Status.EXECUTION },
				{ ReportExecutionStatus.Status.READY }
		};
	}

	@Test(dataProvider = "statuses")
	public void testMarshal(ReportExecutionStatus.Status status) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(ReportExecutionStatus.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter writer = new StringWriter();
		ReportExecutionStatus reportExecutionStatus = new ReportExecutionStatus();
		reportExecutionStatus.setValue(status);
		reportExecutionStatus.setErrorDescriptor(new ReportExecutionStatus.ErrorDescriptor(ERR_CODE, MESSAGE_1, newArrayList(PARAM_1, PARAM_2)));
		marshaller.marshal(reportExecutionStatus, writer);
		assertThat(writer.toString()).isEqualTo(XML_EXAMPLE.replace(STATUS_PLACEHOLDER, status.name().toLowerCase()));

	}

	@Test(dataProvider = "statuses")
	public void testUnmarshal(ReportExecutionStatus.Status status) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(ReportExecutionStatus.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new StringReader(XML_EXAMPLE.replace(STATUS_PLACEHOLDER, status.name().toLowerCase())));
		assertTrue(result instanceof ReportExecutionStatus);
		ReportExecutionStatus reportExecutionStatus = (ReportExecutionStatus) result;
		assertThat(reportExecutionStatus.getValue()).isEqualTo(status);
		assertThat(reportExecutionStatus.getErrorDescriptor().getErrorCode()).isEqualTo(ERR_CODE);
		assertThat(reportExecutionStatus.getErrorDescriptor().getParameters().get(0)).isEqualTo(PARAM_1);
		assertThat(reportExecutionStatus.getErrorDescriptor().getParameters().get(1)).isEqualTo(PARAM_2);
		assertThat(reportExecutionStatus.getErrorDescriptor().getMessage()).isEqualTo(MESSAGE_1);
	}
}
