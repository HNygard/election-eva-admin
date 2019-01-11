package no.valg.eva.admin.configuration.domain.model.manntall.papir;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Voter;

public class SideOgLinjefordelerForRoder {

	private List<Voter> manntallForKrets = new ArrayList<>();
	private int maxAntallVelgerePerSide = 1;

	private Integer nesteSide;
	private Integer nesteLinje;

	public SideOgLinjefordelerForRoder(final List<Voter> manntallForKrets, int maxAntallVelgerePerSide) {
		this.manntallForKrets = manntallForKrets;
		this.maxAntallVelgerePerSide = maxAntallVelgerePerSide;
	}

	/**
	 * Tilordner side og linje for papirmanntallet for kretser/stemmesteder med roder.
	 * Nummereringen starter på 1 for hver side.
	 * Ny side lages enten når a) max antall velgere per side nås, eller b) ny rode starter
	 */
	public void tilordneSideOgLinjeTilVelgere() {
		sorterEtterRode(manntallForKrets);

		if (manntallForKrets.isEmpty()) {
			return;
		}

		String gjeldendeRodeId = manntallForKrets.get(0).getPollingStation().getId();
		int gjeldendeSidenummer = 1;
		int gjeldendeLinjenummer = 1;
		int antallVelgerePaaDenneSiden = 0;

		for (Voter velger : manntallForKrets) {
			if (!velger.getPollingStation().getId().equals(gjeldendeRodeId)) {
				gjeldendeSidenummer++;
				antallVelgerePaaDenneSiden = 0;
				gjeldendeLinjenummer = 1;
				gjeldendeRodeId = velger.getPollingStation().getId();
			}

			if (antallVelgerePaaDenneSiden == maxAntallVelgerePerSide) {
				gjeldendeSidenummer++;
				antallVelgerePaaDenneSiden = 0;
				gjeldendeLinjenummer = 1;
			}

			velger.setElectoralRollPage(gjeldendeSidenummer);
			velger.setElectoralRollLine(gjeldendeLinjenummer);

			antallVelgerePaaDenneSiden++;
			gjeldendeLinjenummer++;

		}
		nesteSide = gjeldendeSidenummer + 1; // Nye endringer skal skje på neste side
		nesteLinje = 0;
	}

	private void sorterEtterRode(List<Voter> manntallForKrets) {
		manntallForKrets.sort((v1, v2) -> {
			if (v1.getPollingStation() != null && v2.getPollingStation() != null && !v1.getPollingStation().getId().equals(v2.getPollingStation().getId())) {
				return v1.getPollingStation().getFirst().compareTo(v2.getPollingStation().getFirst());
			} else {
				return 0;
			}
		});
	}

	/**
	 * Neste side som skal brukes for nye manntallsinnslag
	 */
	public Integer getNesteSide() {
		return nesteSide;
	}

	/**
	 * Neste linjenummer som skal brukes for nye manntallsinnslag.
	 * Er alltid 0 etter fordeling, siden nye innslag skal på ny side.
	 */
	public Integer getNesteLinje() {
		return nesteLinje;
	}

}
