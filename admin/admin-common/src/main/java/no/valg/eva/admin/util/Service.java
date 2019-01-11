package no.valg.eva.admin.util;

/**
 * Som et komplement til Javas andre funksjonelle grensesnitt (Function, Supplier, Predicate osv.)
 * hvor det mangler grensesnitt som ikke tar parametre, og ikke har returverdi
 */
@FunctionalInterface
public interface Service {
	void execute();
}
