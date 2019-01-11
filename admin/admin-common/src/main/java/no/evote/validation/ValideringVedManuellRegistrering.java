package no.evote.validation;

/**
 * Hensikten med denne klassen er å kunne markere valideringsregler for utføring manuelt, dvs. ikke automatisk feks ved insert i databasen.
 * Grensesnittet har ingen annen betydning enn at siden det er annerledes enn Default (som er standardverdi),
 * så kjøres ikke valideringsreglene automatisk ved insert i databasen. 
 * See feks VoterTest for eksempler på bruk av manuell validering
 */
public interface ValideringVedManuellRegistrering {

}
