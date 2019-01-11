package no.valg.eva.admin.util;

import org.jdom2.Document;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class XMLUtilTest {
	@Test
	public void documentToString_newlineShouldBeLfOnly() {
		Document document = XMLUtil.stringToDocument("<?xml version=\"1.0\"?>\n<dummyXml/>");
		String documentAsString = XMLUtil.documentToString(document);

		assertThat(documentAsString.indexOf('\r')).isLessThanOrEqualTo(-1);
	}
}
