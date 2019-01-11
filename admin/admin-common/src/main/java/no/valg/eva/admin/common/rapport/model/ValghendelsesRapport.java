package no.valg.eva.admin.common.rapport.model;

import java.io.Serializable;

import no.valg.eva.admin.common.rbac.Access;

public class ValghendelsesRapport implements Serializable {
	private String rapportId;
	private ReportCategory kategori;
	private Access access;
	private boolean synlig;
	private boolean tilgjengelig;

	public ValghendelsesRapport(String rapportId, ReportCategory kategori, Access access) {
		this.rapportId = rapportId;
		this.kategori = kategori;
		this.access = access;
	}

	public String getRapportId() {
		return rapportId;
	}

	public String getNameKey() {
		return "@rapport.meta." + rapportId + ".name";
	}

	public ReportCategory getKategori() {
		return kategori;
	}

	public Access getAccess() {
		return access;
	}

	public boolean isSynlig() {
		return synlig;
	}

	public void setSynlig(boolean synlig) {
		this.synlig = synlig;
	}

	public boolean isTilgjengelig() {
		return tilgjengelig;
	}

	public void setTilgjengelig(boolean tilgjengelig) {
		this.tilgjengelig = tilgjengelig;
	}
}
