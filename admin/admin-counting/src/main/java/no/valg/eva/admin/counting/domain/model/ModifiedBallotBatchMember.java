package no.valg.eva.admin.counting.domain.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.common.persistence.EntityWriteListener;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 */
@Entity
@EntityListeners({EntityWriteListener.class})
@Table(name = "cast_vote_batch_member")
@AttributeOverride(name = "pk", column = @Column(name = "cast_vote_batch_member_pk"))
@NamedQueries({
		@NamedQuery(name = "ModifiedBallotBatchMember.findHighestSerialNumberForBallotCount",
				query = "select max(serialNumber) from ModifiedBallotBatchMember where castBallot.ballotCount.pk = :ballotCountPk")
})
public class ModifiedBallotBatchMember extends VersionedEntity {
	private ModifiedBallotBatch modifiedBallotBatch;
	private CastBallot castBallot;
	private int serialNumber;
	private boolean done;

	public ModifiedBallotBatchMember(CastBallot castBallot) {
		this.castBallot = castBallot;
	}

	public ModifiedBallotBatchMember() {
		super();
	}

	public ModifiedBallotBatchMember(CastBallot castBallot, boolean done, int serialNumber) {
		this.castBallot = castBallot;
		this.done = done;
		this.serialNumber = serialNumber;
	}

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "cast_vote_pk")
	public CastBallot getCastBallot() {
		return castBallot;
	}

	public void setCastBallot(CastBallot castBallot) {
		this.castBallot = castBallot;
	}

	@Column(name = "serial_number")
	public int getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	@Column(name = "done")
	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cast_vote_batch_pk", nullable = false)
	public ModifiedBallotBatch getModifiedBallotBatch() {
		return modifiedBallotBatch;
	}

	public void setModifiedBallotBatch(ModifiedBallotBatch modifiedBallotBatch) {
		this.modifiedBallotBatch = modifiedBallotBatch;
	}

	public String partyName() {
		CastBallot theCastBallot = getCastBallot();
		return theCastBallot != null ? theCastBallot.partyName() : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModifiedBallotBatchMember)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		ModifiedBallotBatchMember that = (ModifiedBallotBatchMember) o;

		if (done != that.done) {
			return false;
		}
		if (serialNumber != that.serialNumber) {
			return false;
		}
		if (castBallot != null ? !castBallot.equals(that.castBallot) : that.castBallot != null) {
			return false;
		}
		return !(modifiedBallotBatch != null ? !modifiedBallotBatch.equals(that.modifiedBallotBatch) : that.modifiedBallotBatch != null);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (modifiedBallotBatch != null ? modifiedBallotBatch.hashCode() : 0);
		result = 31 * result + (castBallot != null ? castBallot.hashCode() : 0);
		result = 31 * result + serialNumber;
		result = 31 * result + (done ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("castBallot", castBallot)
				.append("serialNumber", serialNumber)
				.append("done", done)
				.toString();
	}
}
