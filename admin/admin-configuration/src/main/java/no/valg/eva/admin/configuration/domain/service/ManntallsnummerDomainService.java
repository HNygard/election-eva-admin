package no.valg.eva.admin.configuration.domain.service;

import javax.inject.Inject;

import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

import org.joda.time.LocalDate;

public class ManntallsnummerDomainService {
	private static final int TITALLSYSTEMET_BASE = 10;

	private ElectionEventRepository electionEventRepository;

	@Inject
	public ManntallsnummerDomainService(ElectionEventRepository electionEventRepository) {
		this.electionEventRepository = electionEventRepository;
	}
	
	public Manntallsnummer beregnFulltManntallsnummer(Long kortManntallsNummer, ElectionEvent electionEvent) {
		int valgaarssiffer = valgaarssifferForValghendelse(electionEvent);
		return new Manntallsnummer(kortManntallsNummer, valgaarssiffer);
	}
	
	public boolean erValgaarssifferGyldig(Manntallsnummer manntallsnummer, ElectionEvent electionEvent) {
		int valgaarssifferIValghendelse = valgaarssifferForValghendelse(electionEvent);
		int valgaarssifferIManntallsnummer = manntallsnummer.getValgaarssiffer();
		return valgaarssifferIManntallsnummer == valgaarssifferIValghendelse;
	}

	public int valgaarssifferForValghendelse(ElectionEvent electionEvent) {
		LocalDate sisteValgdag = electionEventRepository.findLatestElectionDay(electionEvent);
		int valgaar = sisteValgdag.getYear();
		return minsteTverrsum(valgaar);
	}

	int minsteTverrsum(int valgaar) {
		int tverrsum = valgaar;
		while (tverrsum > TITALLSYSTEMET_BASE - 1) {
			int sisteSiffer = tverrsum % TITALLSYSTEMET_BASE;
			tverrsum = (tverrsum / TITALLSYSTEMET_BASE) + sisteSiffer; 
		}
		return tverrsum;
	}

}
