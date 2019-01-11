package no.valg.eva.admin.voting.domain.model;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class Stemmegivningsstatistikk {

	private int godkjenteFhsg;
	private int godkjenteVtsg;
	private int forkastedeFhsg;
	private int forkastedeVtsg;
	
	@SuppressWarnings("unused") // Brukes av Voting.SqlResultSetMapping
	public Stemmegivningsstatistikk(BigInteger godkjenteFhsg, BigInteger godkjenteVtsg, BigInteger forkastedeFhsg, BigInteger forkastedeVtsg) {
		this.godkjenteFhsg = godkjenteFhsg.intValue();
		this.godkjenteVtsg = godkjenteVtsg.intValue();
		this.forkastedeFhsg = forkastedeFhsg.intValue();
		this.forkastedeVtsg = forkastedeVtsg.intValue();
	}

	public Stemmegivningsstatistikk(int godkjenteFhsg, int godkjenteVtsg, int forkastedeFhsg, int forkastedeVtsg) {
		this.godkjenteFhsg = godkjenteFhsg;
		this.godkjenteVtsg = godkjenteVtsg;
		this.forkastedeFhsg = forkastedeFhsg;
		this.forkastedeVtsg = forkastedeVtsg;
	}

	public void addApprovedElectionDayVotings(int approvedElectionDayVotes) {
		godkjenteVtsg += approvedElectionDayVotes;
	}
	
	public void addRejectedElectionDayVotings(int rejectedElectionDayVotes) {
		forkastedeVtsg += rejectedElectionDayVotes;
	}

	public void addRejectedEarlyVotings(int rejectedEarlyVotes) {
		forkastedeFhsg += rejectedEarlyVotes;
	}

	public Stemmegivningsstatistikk add(Stemmegivningsstatistikk stemmegivningsstatistikk) {
		return new Stemmegivningsstatistikk(
				godkjenteFhsg + stemmegivningsstatistikk.getGodkjenteFhsg(),
				godkjenteVtsg + stemmegivningsstatistikk.getGodkjenteVtsg(),
				forkastedeFhsg + stemmegivningsstatistikk.getForkastedeFhsg(),
				forkastedeVtsg + stemmegivningsstatistikk.getForkastedeVtsg());
	}
}
