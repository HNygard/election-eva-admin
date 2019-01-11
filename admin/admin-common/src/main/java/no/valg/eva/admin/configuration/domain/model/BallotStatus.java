package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.constants.EvoteConstants;
import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

/**
 * Status for ballot / candidate list
 */
@Entity
@Table(name = "ballot_status", uniqueConstraints = @UniqueConstraint(columnNames = "ballot_status_id"))
@AttributeOverride(name = "pk", column = @Column(name = "ballot_status_pk"))
public class BallotStatus extends VersionedEntity implements java.io.Serializable {

	private int id;
	private String name;

	public enum BallotStatusValue {
		UNDERCONSTRUCTION(EvoteConstants.BALLOT_STATUS_UNDERCONSTRUCTION), WITHDRAWN(EvoteConstants.BALLOT_STATUS_WITHDRAWN), PENDING(
				EvoteConstants.BALLOT_STATUS_PENDING), APPROVED(EvoteConstants.BALLOT_STATUS_APPROVED), REJECTED(EvoteConstants.BALLOT_STATUS_REJECTED);

		private final int id;

		private BallotStatusValue(final int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public static BallotStatusValue get(final Integer id) {
			for (BallotStatusValue ballotStatus : BallotStatusValue.values()) {
				if (ballotStatus.id == id) {
					return ballotStatus;
				}
			}
			return null;
		}
	}

	@Transient
	public boolean isListLocked() {
		switch (getBallotStatusValue()) {
		case WITHDRAWN:
			return true;
		case APPROVED:
			return true;
		case REJECTED:
			return true;
		default:
			return false;
		}
	}

	@Transient
	public BallotStatusValue getBallotStatusValue() {
		return BallotStatusValue.get(id);
	}

	@Column(name = "ballot_status_id", nullable = false, length = 4)
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Column(name = "ballot_status_name", nullable = false)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPk() == null) ? 0 : getPk().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BallotStatus other = (BallotStatus) obj;
		if (getPk() == null) {
			if (other.getPk() != null) {
				return false;
			}
		} else if (!getPk().equals(other.getPk())) {
			return false;
		}
		return true;
	}

}
