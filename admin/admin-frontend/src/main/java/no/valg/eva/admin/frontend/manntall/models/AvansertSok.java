package no.valg.eva.admin.frontend.manntall.models;

import java.io.Serializable;

import org.joda.time.LocalDate;

public class AvansertSok implements Serializable {

	private String navn;
	private LocalDate fodselsDato;
	private String adresse;
	private String kommuneId;

	public AvansertSok() {
	}

	public AvansertSok(String kommuneId) {
		this.kommuneId = kommuneId;
	}

	public String getNavn() {
		return navn;
	}

	public void setNavn(String navn) {
		this.navn = navn;
	}

	public LocalDate getFodselsDato() {
		return fodselsDato;
	}

	public void setFodselsDato(LocalDate fodselsDato) {
		this.fodselsDato = fodselsDato;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getKommuneId() {
		return kommuneId;
	}

	public void setKommuneId(String kommuneId) {
		this.kommuneId = kommuneId;
	}
}
