package no.valg.eva.admin.backend.reporting.jasperserver;

import com.google.common.collect.Lists;
import no.valg.eva.admin.backend.reporting.jasperserver.api.ReportMetaData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

import static com.google.common.collect.Lists.newArrayList;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.ReportMetaData.AreaLevel.COUNTY;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.ReportMetaData.AreaLevel.MUNICIPALITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class ReportMetaDataTest {
    private static final String NAME_NO_NB = "Kretsstatistikk";
    private static final String PATTERN_NO_NB = "_${electionEventName}_${municipalityId}_nb_NO";
    private static final String REPORT_URI = "100.configuration/100.kretsstatistikk";
    private static final String REPORT_OPT_PATH_PARAM = "EE1.CO1.CNT1.MUN1.BOR1.PD1";
	private static final java.lang.String AREA_PATH_MASK = "Mask";
	private static final String XML_EXAMPLE =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<reportMetaData>\n"
					+ "    <areaLevel>county</areaLevel>\n"
					+ "    <areaLevel>municipality</areaLevel>\n"
					+ "    <areaPathMask>" + AREA_PATH_MASK + "</areaPathMask>\n"
					+ "    <async>false</async>\n"
					+ "    <filenamePattern>" + PATTERN_NO_NB + "</filenamePattern>\n"
					+ "    <fixedParameterValue>\n"
					+ "        <parameter>fixedParam</parameter>\n"
					+ "        <value>fixedValue</value>\n"
					+ "    </fixedParameterValue>\n"
					+ "    <format>pdf</format>\n"
					+ "    <format>xls</format>\n"
					+ "    <mandatoryParameter>mandParam</mandatoryParameter>\n"
					+ "    <optionalPathParameter>" + REPORT_OPT_PATH_PARAM + "</optionalPathParameter>\n"
					+ "    <reportName>" + NAME_NO_NB + "</reportName>\n"
					+ "    <reportUri>" + REPORT_URI + "</reportUri>\n"
					+ "    <unselectableParameterValue>\n"
					+ "        <parameter>param</parameter>\n"
					+ "        <userRoleMvAreaRegExp>regexp</userRoleMvAreaRegExp>\n"
					+ "        <value>unselValue</value>\n"
					+ "    </unselectableParameterValue>\n"
					+ "</reportMetaData>\n";

    private static final String IGNORED_ELEMENTS_HERE = "_IGNORED_ELEMENTS_HERE_";
	private static final String XML_EXAMPLE_HIDDEN =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<reportMetaData>\n"
					+ "    <areaLevel>county</areaLevel>\n"
					+ "    <areaLevel>municipality</areaLevel>\n"
					+ "    <areaPathMask>" + AREA_PATH_MASK + "</areaPathMask>\n"
					+ "    <async>false</async>\n"
					+ "    <filenamePattern>" + PATTERN_NO_NB + "</filenamePattern>\n"
					+ "    <fixedParameterValue>\n"
					+ "        <parameter>fixedParam</parameter>\n"
					+ "        <value>fixedValue</value>\n"
					+ "    </fixedParameterValue>\n"
					+ "    <format>pdf</format>\n"
					+ "    <format>xls</format>\n"
					+ "    <hidden>true</hidden>\n"
					+ "    <mandatoryParameter>mandParam</mandatoryParameter>\n"
					+ "    <optionalPathParameter>" + REPORT_OPT_PATH_PARAM + "</optionalPathParameter>\n"
					+ IGNORED_ELEMENTS_HERE
					+ "    <reportName>" + NAME_NO_NB + "</reportName>\n"
					+ "    <reportUri>" + REPORT_URI + "</reportUri>\n"
					+ "    <unselectableParameterValue>\n"
					+ "        <parameter>param</parameter>\n"
					+ "        <userRoleMvAreaRegExp>regexp</userRoleMvAreaRegExp>\n"
					+ "        <value>unselValue</value>\n"
					+ "    </unselectableParameterValue>\n"
					+ "</reportMetaData>\n";
    private static final String EMPTY_STRING = "";

	@Test(dataProvider = "hidden")
	public void testMarshal(boolean hidden) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(ReportMetaData.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter writer = new StringWriter();
		ReportMetaData reportMetaData = new ReportMetaData();
        reportMetaData.setFormats(Lists.newArrayList(ReportMetaData.Format.PDF, ReportMetaData.Format.XLS));
		reportMetaData.setReportName(NAME_NO_NB);
		reportMetaData.setFilenamePattern(PATTERN_NO_NB);
		reportMetaData.setReportUri(REPORT_URI);
		reportMetaData.setAreaLevels(newArrayList(COUNTY, MUNICIPALITY));
		reportMetaData.setAreaPathMask(AREA_PATH_MASK);
		if (hidden) {
            reportMetaData.setHidden(true);
		}
		reportMetaData.setMandatoryParameters(newArrayList("mandParam"));
		reportMetaData.setOptionalPathParameter(REPORT_OPT_PATH_PARAM);
		reportMetaData.setUnselectableParameterValues(newArrayList(new ReportMetaData.UnselectableParameterValue("param", "unselValue", "regexp")));
		reportMetaData.setFixedParameterValues(newArrayList(new ReportMetaData.FixedParameterValue("fixedParam", "fixedValue")));
		marshaller.marshal(reportMetaData, writer);
		assertThat(writer.toString()).isEqualTo(
				hidden ? XML_EXAMPLE_HIDDEN.replace(IGNORED_ELEMENTS_HERE, EMPTY_STRING) : XML_EXAMPLE.replace(IGNORED_ELEMENTS_HERE, EMPTY_STRING));
	}

	@DataProvider(name = "hidden")
	public static Object[][] testUnMarshal() {
		return new Object[][] {
				{ false },
				{ true }
		};
	}

	@Test(dataProvider = "hidden")
	public void testUnMarshal(boolean hidden) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(ReportMetaData.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new StringReader(hidden ? XML_EXAMPLE_HIDDEN : XML_EXAMPLE));
		assertTrue(result instanceof ReportMetaData);
		ReportMetaData reportMetaData = (ReportMetaData) result;
		assertFalse(reportMetaData.getAsync());
		assertEquals(reportMetaData.getReportUri(), REPORT_URI);
		assertEquals(reportMetaData.getFilenamePattern(), PATTERN_NO_NB);
		assertEquals(reportMetaData.getReportName(), NAME_NO_NB);
		assertSame(reportMetaData.getFormats().size(), 2);
		assertSame(reportMetaData.getAreaLevels().get(0).ordinal(), COUNTY.ordinal());
		assertSame(reportMetaData.getAreaLevels().get(1).ordinal(), MUNICIPALITY.ordinal());
		assertThat(reportMetaData.getAreaPathMask()).isEqualTo(AREA_PATH_MASK);
		assertThat(reportMetaData.isHidden()).isEqualTo(hidden);
		assertSame(reportMetaData.getMandatoryParameters().size(), 1);
		assertThat(reportMetaData.getMandatoryParameters().get(0)).isEqualTo("mandParam");
		assertThat(reportMetaData.getUnselectableParameterValues().get(0).getValue()).isEqualTo("unselValue");
		assertThat(reportMetaData.getUnselectableParameterValues().get(0).getUserRoleMvAreaRegExp()).isEqualTo("regexp");
		assertThat(reportMetaData.getFixedParameterValues().get(0).getParameter()).isEqualTo("fixedParam");
		assertThat(reportMetaData.getFixedParameterValues().get(0).getValue()).isEqualTo("fixedValue");
        assertThat(reportMetaData.getOptionalPathParameter()).isEqualTo(REPORT_OPT_PATH_PARAM);
	}

	@Test
	public void whenParseWithUnknownElements_unknownElementsAreIgnored() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(ReportMetaData.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new StringReader(XML_EXAMPLE
				.replace(IGNORED_ELEMENTS_HERE, "<unknownElement>UnknownText</unknownElement>\n")));
		assertTrue(result instanceof ReportMetaData);
	}
}
