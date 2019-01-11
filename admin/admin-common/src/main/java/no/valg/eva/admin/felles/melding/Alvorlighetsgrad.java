package no.valg.eva.admin.felles.melding;

public enum Alvorlighetsgrad {
	ERROR("error"), WARN("warn"), INFO("info");

	private String alvorlighetsgrad;

	Alvorlighetsgrad(String alvorlighetsgrad) {
		this.alvorlighetsgrad = alvorlighetsgrad;
	}

	public String getVerdi() {
		return alvorlighetsgrad;
	}
}
