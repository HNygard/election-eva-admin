package no.valg.eva.admin.counting.port.adapter.service.valgnatt.ssb2013;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import no.valg.eva.admin.counting.port.adapter.service.valgnatt.ValgnattRequest;

import org.apache.log4j.Logger;

public final class SsbXmlGenerator {
	private static final Logger LOG = Logger.getLogger(SsbXmlGenerator.class);

	private SsbXmlGenerator() {
		// Intentionally empty
	}

	public static String generateSsbXML(final ValgnattRequest ssbRequestData) {
		StringWriter stringWriterXML4DRExIm = new StringWriter();
		try {
			JAXBContext context = JAXBContext.newInstance(ValgnattRequest.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.w3.org/2001/XMLSchema-instance");
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.w3.org/2001/XMLSchema");
			marshaller.marshal(ssbRequestData, stringWriterXML4DRExIm);
		} catch (JAXBException e) {
			LOG.error(e.getMessage(), e);
		}
		return stringWriterXML4DRExIm.toString();
	}

}
