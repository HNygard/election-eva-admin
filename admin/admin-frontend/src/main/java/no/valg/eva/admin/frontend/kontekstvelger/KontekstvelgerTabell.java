package no.valg.eva.admin.frontend.kontekstvelger;

import java.util.List;

import no.valg.eva.admin.frontend.kontekstvelger.panel.KontekstvelgerPanel;

public abstract class KontekstvelgerTabell<P extends KontekstvelgerPanel, R extends KontekstvelgerRad, I> {
	private final P panel;
	private List<R> rader;
	private boolean visAntallRader;
	private R valgtRad;

	protected KontekstvelgerTabell(P panel, boolean visAntallRader) {
		this.panel = panel;
		this.visAntallRader = visAntallRader;
	}

	protected P getPanel() {
		return panel;
	}

	public List<R> getRader() {
		return rader;
	}

	protected void setRader(List<R> rader) {
		this.rader = rader;
		if (rader.size() == 1) {
			setValgtRad(rader.get(0));
		} else {
			setValgtRad(null);
		}
	}

	public R getValgtRad() {
		return valgtRad;
	}

	public void setValgtRad(R valgtRad) {
		this.valgtRad = valgtRad;
		valgtRadSatt();
	}

	public int getAntallRader() {
		return rader.size();
	}

	public boolean isVisAntallRader() {
		return visAntallRader;
	}

	public void setVisAntallRader(boolean visAntallRader) {
		this.visAntallRader = visAntallRader;
	}

	protected abstract void valgtRadSatt();

	public boolean isRadValgt() {
		return valgtRad != null;
	}

	public boolean isVisTabell() {
		return isVisKnapp() || rader.size() != 1;
	}

	public boolean isKnappDeaktivert() {
		return valgtRad == null;
	}

	public abstract void oppdater();

	public abstract I getId();

	public abstract String getNavn();

	public abstract boolean isVisKnapp();
}
