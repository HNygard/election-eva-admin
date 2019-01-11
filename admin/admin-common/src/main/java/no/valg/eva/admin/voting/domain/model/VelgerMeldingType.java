package no.valg.eva.admin.voting.domain.model;

public enum VelgerMeldingType {
	// @formatter:off
	FORHANDSTEMME_STENGT_PGA_AVKRYSSNINGSMANNTALL_KJORT("@voting.search.forhåndsstemmerStengtPgaAvkrysningsmanntallKjort"),
	FORHANDSTEMME_ANNEN_KOMMUNE("@voting.search.voterNotInMunicipalityAdvance"),
	INGEN_VALGKRETS_FOR_VELGER("@voting.search.noContestsForVoter"),
	STEMMERETT_KUN_VED_KOMMUNEVALG("@voting.markOff.notEligibleInAllContests"),
	STEMMERETT_VED_SAMETINGSVALG("@electoralRoll.eligigbleInSamiElection"),
	VELGER_AVGANG_I_MANNTALL("@voting.search.voterDepartedElectoralRoll"),
	VELGER_IKKE_STEMMEBERETTIGET_GRUNNET_ALDER("@voting.search.noContestsForVoter"),
	ALLEREDE_IKKE_GODKJENT_STEMME_FORKASTET("@voting.search.votingAlreadyCastedRejected"),
	ALLEREDE_IKKE_GODKJENT_STEMME("@voting.search.votingAlreadyCastedNotApproved"),
	VELGER_KAN_STEMME_I_KONVOLUTT("@voting.search.voterMustCastSpecialCover"),
	VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND_URNE("@voting.search.voterAlreadyCastedInBallotBoxCanCastAdvance"),
	VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND_IKKE_URNE("@voting.search.voterAlreadyCastedCanCastAdvance"),
	VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND("@voting.search.voterCanCastAdvance"),
	ALLEREDE_GODKJENT_STEMME("@voting.search.votingAlreadyCasted"),
	VELGER_IKKE_MANNTALLSFORT_DENNE_KOMMUNEN("@voting.search.electionDay.notInMunicipality"), // {0} er ikke manntallsført for denne kommunen.
	VELGER_IKKE_MANNTALLSFORT_DENNE_KRETSEN("@voting.search.electionDay.notInPollingDistrict"), // {0} hører ikke til denne stemmekretsen.
	VELGER_IKKE_STEMMEBERETTIGET("@voting.search.voterNotApproved"),
	VELGER_IKKE_AVLAGT_STEMME("@voting.search.voterHasNotVotedYet");
	// @formatter:on

	private String key;

	VelgerMeldingType(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
