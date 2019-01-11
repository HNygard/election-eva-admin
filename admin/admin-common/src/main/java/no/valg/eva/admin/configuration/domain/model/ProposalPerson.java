package no.valg.eva.admin.configuration.domain.model;

import java.util.Iterator;
import java.util.List;

import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.util.StringUtil;
import no.valg.eva.admin.common.UserMessage;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

public abstract class ProposalPerson extends VersionedEntity implements ContextSecurable {
	public abstract String getId();

	public abstract void setId(String string);

	public abstract boolean isIdSet();

	/**
	 * @return true if invalid else false
	 */
	public abstract boolean isInvalid();

	public abstract LocalDate getDateOfBirth();

	public abstract Ballot getBallot();

	public abstract int getDisplayOrder();

	public abstract String getPostalCode();

	public abstract void setPostTown(String postTown);

	public abstract void setDisplayOrder(int i);

	public abstract void setNameLine(String nameline);

	public abstract String getFirstName();

	public abstract String getMiddleName();

	public abstract String getLastName();

	public abstract void addValidationMessage(UserMessage userMessage);

	public abstract List<UserMessage> getValidationMessageList();

	public abstract void clearValidationMessages();

	public abstract void setApproved(boolean approved);

	public String getValidationMessage() {
		StringBuilder validationMsg = new StringBuilder("");
		Iterator<UserMessage> it = getValidationMessageList().iterator();
		while (it.hasNext()) {
			validationMsg.append(it.next().getMessage());
			if (it.hasNext()) {
				validationMsg.append(", ");
			}
		}
		return StringUtil.capitalize(validationMsg.toString());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		appendName(builder, getFirstName());
		appendName(builder, getMiddleName());
		appendName(builder, getLastName());
		return builder.toString();
	}

	private void appendName(final StringBuilder builder, final String name) {
		if (!StringUtils.isEmpty(name)) {
			appendSpaceIfNotEmpty(builder);
			builder.append(name);
		}
	}

	private void appendSpaceIfNotEmpty(final StringBuilder builder) {
		if (builder.length() != 0) {
			builder.append(" ");
		}
	}

	public void setNameLine() {
		setNameLine(toString());
	}

	public boolean isCreated() {
		return getPk() != null;
	}

	public boolean hasSearchInformation() {
		return !StringUtils.isEmpty(getFirstName()) || !StringUtils.isEmpty(getLastName()) || getDateOfBirth() != null;
	}

}
