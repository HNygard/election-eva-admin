package no.valg.eva.admin.util;

import java.util.function.Supplier;

import org.apache.log4j.Logger;

public class TidtakingUtil {

	private TidtakingUtil() {
		// Klassen skal ikke kunne instansieres
	}
	
	public static void taTiden(Logger log, String kontekst, Service service) {
		taTiden(log, kontekst, false, service);
	}

	public static void taTiden(Logger log, String kontekst, boolean kortLogging, Service service) {
		long starttid = startTidtaking(log, kontekst, kortLogging);
		service.execute();
		avsluttTidtaking(log, kontekst, kortLogging, starttid, null);
	}

	private static long startTidtaking(Logger log, String kontekst, boolean kortLogging) {
		if (!kortLogging) {
			log.info("Starter: " + kontekst);
		}
		return System.currentTimeMillis();
	}

	private static void avsluttTidtaking(Logger log, String kontekst, boolean kortLogging, long starttid, Object returverdi) {
		if (kortLogging) {
			log.info(kontekst + " (Tid brukt: " + (System.currentTimeMillis() - starttid) + " millisekunder"
				+ (returverdi != null ? ". Returverdi/antall: " + returverdi : "" )
				+ ")");
		} else {
			log.info("Ferdig: " + kontekst + ". Tid brukt er " + (System.currentTimeMillis() - starttid) + " millisekunder"
				+ (returverdi != null ? "Returverdi/antall: " + returverdi : "" ));
		}
	}

	public static <T> T taTiden(Logger log, String kontekst, Supplier<T> supplier) {
		return taTiden(log, kontekst, false, supplier);
	}

	public static <T> T taTiden(Logger log, String kontekst, boolean kortLogging, Supplier<T> supplier) {
		long time = startTidtaking(log, kontekst, kortLogging);
		T returverdi = supplier.get();
		avsluttTidtaking(log, kontekst, kortLogging, time, returverdi);
		return returverdi;
	}

}
