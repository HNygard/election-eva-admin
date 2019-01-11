package no.valg.eva.admin.frontend.counting.view.ballotcount;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class HeaderRowTest {

	@Test
	public void construct_checkState() throws Exception {
		HeaderRow row = new HeaderRow("@some.title");

		assertThat(row.getTitle()).isEqualTo("@some.title");
		assertThat(row.getStyleClass()).isEqualTo("bold");
		assertThat(row.isModifiedCountInput()).isFalse();
		assertThat(row.getModifiedCount()).isZero();
		assertThat(row.isUnmodifiedCountInput()).isFalse();
		assertThat(row.getUnmodifiedCount()).isZero();
		assertThat(row.getId()).isNull();
		assertThat(row.getProtocolCount()).isNull();
		assertThat(row.getDiff()).isNull();
		assertThat(row.getCount()).isZero();
		assertThat(row.isCountInput()).isFalse();
		assertThat(row.getRowStyleClass()).isEqualTo("row_header");
	}
}
