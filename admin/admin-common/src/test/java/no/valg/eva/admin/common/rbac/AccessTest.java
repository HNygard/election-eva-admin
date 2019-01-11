package no.valg.eva.admin.common.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class AccessTest {

	@Test
	public void getName_withPath_returnsCorrectName() {
		Access access = new Access("path");

		assertThat(access.getName()).isEqualTo("@access.path");
	}

	@Test
	public void getParent_withSinglePath_returnsNull() {
		Access access = new Access("path");

		assertThat(access.getParent()).isNull();
	}

	@Test
	public void getParent_withMultiplePath_returnsParent() {
		Access access = new Access("parent.path");

		assertThat(access.getParent()).isNotNull();
		assertThat(access.getPath()).isEqualTo("parent.path");
		assertThat(access.getParent().getPath()).isEqualTo("parent");
	}

	@Test
	public void getParent_withPathAndParent_returnsParent() {
		Access access = new Access(new Access("parent"), "path");

		assertThat(access.getParent()).isNotNull();
		assertThat(access.getPath()).isEqualTo("parent.path");
		assertThat(access.getParent().getPath()).isEqualTo("parent");
	}

	@Test
	public void equals_withEqualPaths_returnsTrue() {
		Access access1 = new Access("parent.path");
		Access access2 = new Access(new Access("parent"), "path");

		assertThat(access1.equals(access2)).isTrue();
	}

	@Test
	public void equals_withUnequalPaths_returnsFalse() {
		Access access1 = new Access("parent.path");
		Access access2 = new Access(new Access("parent.path"), "path");

		assertThat(access1.equals(access2)).isFalse();
	}

	@Test
	public void equals_withString_returnsFalse() {
		Access access1 = new Access("parent.path");

		assertThat("parent.path".equals(access1)).isFalse();
	}

	@Test
	public void hashCode_withEqualPaths_returnsTrue() {
		Access access1 = new Access("parent.path");
		Access access2 = new Access(new Access("parent"), "path");

		assertThat(access1.hashCode()).isEqualTo(access2.hashCode());
	}

	@Test
	public void toString_returnsPath() {
		Access access = new Access("parent.path");

		assertThat(access.toString()).isEqualTo(access.getPath());
	}

}
