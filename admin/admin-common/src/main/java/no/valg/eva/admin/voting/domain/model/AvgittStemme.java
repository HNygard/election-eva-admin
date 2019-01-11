package no.valg.eva.admin.voting.domain.model;

import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.util.DateUtil;
import org.joda.time.DateTime;

import java.io.Serializable;

public class AvgittStemme implements Serializable {

	private DateTime stemmegivningsTidspunkt;
	private VotingCategory votingCategory;

	public AvgittStemme(VotingCategory votingCategory, DateTime stemmegivningsTidspunkt) {
		this.votingCategory = votingCategory;
		this.stemmegivningsTidspunkt = stemmegivningsTidspunkt;
	}

	public DateTime getStemmegivningsTidspunkt() {
		return stemmegivningsTidspunkt;
	}

	public VotingCategory getVotingCategory() {
		return votingCategory;
	}

	public java.time.LocalDateTime getVotingTimeStamp() {
		return DateUtil.convertToLocalDateTime(stemmegivningsTidspunkt);
	}
}
