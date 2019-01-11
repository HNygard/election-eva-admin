package no.valg.eva.admin.common.counting.model;

import java.io.Serializable;

/**
 */
public class BallotCountRef implements Serializable {
	private long pk;

	public BallotCountRef(long pk) {
		this.pk = pk;
	}

	public BallotCountRef(String ballotCountPk) {
		pk = Long.valueOf(ballotCountPk);
	}

	public long getPk() {
		return pk;
	}
}
