package no.valg.eva.admin.common.counting.model;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Context for the current count.
 */
public class CountContext implements Serializable {
	private final ElectionPath contestPath;
	private final CountCategory category;

	/**
	 * Instantiates a count context with contest path and count category.
	 * 
	 * @param contestPath
	 *            election path to contest for this count
	 * @param category
	 *            count category for this count
	 * @throws java.lang.NullPointerException
	 *             when contest path or count category is null
	 * @throws java.lang.IllegalArgumentException
	 *             when illegal use of contest path or count category
	 */
	public CountContext(ElectionPath contestPath, CountCategory category) {
		this.contestPath = contestPath;
		this.category = category;
		validate();
	}

	/**
	 * Validates count context and throws exception when invalid.
	 * 
	 * @throws java.lang.NullPointerException
	 *             when contest path or count category is null
	 * @throws java.lang.IllegalArgumentException
	 *             when illegal use of contest path or count category
	 */
	public final void validate() {
		validateContestPath();
		validateCategory();
	}

	private void validateContestPath() {
		if (contestPath == null) {
			throw new NullPointerException("expected non-null contest path");
		}
		contestPath.assertContestLevel();
	}

	private void validateCategory() {
		if (category == null) {
			throw new NullPointerException("expected non-null category");
		}
	}

	/**
	 * @return path to the contest for the current count
	 */
	public ElectionPath getContestPath() {
		return contestPath;
	}

	public ValgdistriktSti valgdistriktSti() {
		return contestPath.tilValghierarkiSti().tilValgdistriktSti();
	}

	/**
	 * @return category for the current count
	 */
	public CountCategory getCategory() {
		return category;
	}

	public String getCategoryMessageProperty() {
		return category.messageProperty();
	}

	public boolean isEarlyVoting() {
		return category.isEarlyVoting();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		CountContext rhs = (CountContext) obj;
		return new EqualsBuilder()
				.append(this.contestPath, rhs.contestPath)
				.append(this.category, rhs.category)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(contestPath)
				.append(category)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("contestPath", contestPath)
				.append("category", category)
				.toString();
	}
}
