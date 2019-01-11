package no.valg.eva.admin.common.configuration.model.election;

import java.io.Serializable;

/**
 * Contains info on whether to use personal votes, renumberings, write ins, etc.
 */
public class ModifiedBallotConfiguration implements Serializable {

	private final boolean renumber;
	private final boolean renumberLimit;
	private final boolean writein;
	private final boolean strikeout;
	private final boolean personal;
	private final Integer maxWriteIn;
	private final Integer maxRenumber;

	public ModifiedBallotConfiguration(boolean renumber, boolean renumberLimit, boolean writein, boolean strikeout, boolean personal, Integer maxWriteIn,
			Integer maxRenumber) {
		this.renumber = renumber;
		this.renumberLimit = renumberLimit;
		this.writein = writein;
		this.strikeout = strikeout;
		this.personal = personal;
		this.maxWriteIn = maxWriteIn;
		this.maxRenumber = maxRenumber;
	}

	public Integer getMaxWriteIn() {
		return maxWriteIn;
	}

    public boolean isRenumber() {
        return renumber;
    }

    public boolean isWritein() {
        return writein;
    }

    public boolean isStrikeout() {
        return strikeout;
    }

    public boolean isPersonal() {
        return personal;
    }

	public Integer getMaxRenumber() {
		return maxRenumber;
	}

	public boolean isRenumberLimit() {
		return renumberLimit;
	}
}
