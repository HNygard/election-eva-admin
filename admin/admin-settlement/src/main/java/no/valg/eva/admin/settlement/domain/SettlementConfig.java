package no.valg.eva.admin.settlement.domain;

import static java.lang.String.format;

import no.valg.eva.admin.configuration.domain.model.Election;

public enum SettlementConfig {
	RENUMBER(true, false, false, false), RENUMBER_AND_STRIKEOUT(true, true, false, false), PERSONAL(false, false, true, false), PERSONAL_AND_WRITE_IN(false,
			false, true, true);

	private final boolean renumber;
	private final boolean strikeout;
	private final boolean personal;
	private final boolean writeIn;

	SettlementConfig(boolean renumber, boolean strikeout, boolean personal, boolean writeIn) {
		this.renumber = renumber;
		this.strikeout = strikeout;
		this.personal = personal;
		this.writeIn = writeIn;
	}

	public static SettlementConfig from(Election election) {
		boolean renumber = election.isRenumber();
		boolean strikeout = election.isStrikeout();
		boolean personal = election.isPersonal();
		boolean writeIn = election.isWritein();
		if (renumber && strikeout && !personal && !writeIn) {
			return RENUMBER_AND_STRIKEOUT;
		}
		if (renumber && !strikeout && !personal && !writeIn) {
			return RENUMBER;
		}
		if (!renumber && !strikeout && personal && writeIn) {
			return PERSONAL_AND_WRITE_IN;
		}
		if (!renumber && !strikeout && personal) {
			return PERSONAL;
		}
		throw new IllegalArgumentException(
				format("Unknown settlement config (renumber=%s, strikeout=%s, personal=%s, writeIn=%s)",
						renumber, strikeout, personal, writeIn));
	}

	public boolean isRenumber() {
		return renumber;
	}

	public boolean isStrikeout() {
		return strikeout;
	}

	public boolean isPersonal() {
		return personal;
	}

	public boolean isWriteIn() {
		return writeIn;
	}
}
