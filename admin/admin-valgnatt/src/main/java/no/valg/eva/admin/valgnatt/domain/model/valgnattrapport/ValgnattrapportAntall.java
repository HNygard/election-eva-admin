package no.valg.eva.admin.valgnatt.domain.model.valgnattrapport;

public class ValgnattrapportAntall {
	private final int antallRapporter;
	private final int antallRapporterbare;
	private final int antallIkkeFerdig;

	public ValgnattrapportAntall(int antallRapporter, int antallRapporterbare, int antallIkkeFerdig) {
		this.antallRapporter = antallRapporter;
		this.antallRapporterbare = antallRapporterbare;
		this.antallIkkeFerdig = antallIkkeFerdig;
	}
	
	public boolean ingenRapporter() {
		return antallRapporter == 0;
	}

	public int getAntallRapporterbare() {
		return antallRapporterbare;
	}

	public boolean isAlleFerdig() {
		return antallRapporter != 0 && antallIkkeFerdig == 0;
	}
}
