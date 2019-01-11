package no.valg.eva.admin.frontend.common;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Button {
	private final ButtonState state;
	private final String name;

	public Button(ButtonState state, String name) {
		this.state = state;
		this.name = name;
	}

	public Button(ButtonState state) {
		this.state = state;
		this.name = null;
	}

	public static Button enabled(boolean enabled) {
		return enabled(enabled, null);
	}

	public static Button enabled(boolean enabled, String name) {
		if (enabled) {
			return new Button(ButtonState.ENABLED, name);
        }
		return new Button(ButtonState.DISABLED, name);
	}

	public static Button renderedAndEnabled() {
		return enabled(true);
	}

	public static Button notRendered() {
		return new Button(ButtonState.NOT_RENDERED);
	}

	public boolean isDisabled() {
		return state.isDisabled();
	}

	public boolean isRendered() {
		return state.isRendered();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Button)) {
			return false;
		}
		Button button = (Button) o;
		return new EqualsBuilder()
				.append(state, button.state)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(state)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("state", state)
				.toString();
	}

	public String getName() {
		return name;
	}
}
