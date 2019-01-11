package no.valg.eva.admin.frontend.common;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.ERROR;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.INFO;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.WARN;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.felles.melding.Melding;

import org.testng.annotations.Test;


public class MeldingerWidgetTest {

	@Test
	public void add_medMeldinger_sortererListeEtterAlvorlighetsgrad() throws Exception {
		MeldingerWidget widget = new MeldingerWidget();
		widget.add(new Melding(INFO, ""));
		widget.add(new Melding(WARN, ""));
		widget.add(new Melding(ERROR, ""));

		assertThat(widget).hasSize(3);
		assertThat(widget.get(0).getAlvorlighetsgrad()).isSameAs(ERROR);
		assertThat(widget.get(1).getAlvorlighetsgrad()).isSameAs(WARN);
		assertThat(widget.get(2).getAlvorlighetsgrad()).isSameAs(INFO);
	}

	@Test
	public void addAll_medMeldinger_sortererListeEtterAlvorlighetsgrad() throws Exception {
		List<Melding> meldinger = asList(
				new Melding(INFO, ""),
				new Melding(WARN, ""),
				new Melding(ERROR, ""));

		MeldingerWidget widget = new MeldingerWidget(meldinger);

		assertThat(widget).hasSize(3);
		assertThat(widget.get(0).getAlvorlighetsgrad()).isSameAs(ERROR);
		assertThat(widget.get(1).getAlvorlighetsgrad()).isSameAs(WARN);
		assertThat(widget.get(2).getAlvorlighetsgrad()).isSameAs(INFO);
	}

	@Test
	public void getErrors_med1Av3_returerer1Melding() throws Exception {
		List<Melding> meldinger = asList(
				new Melding(INFO, ""),
				new Melding(WARN, ""),
				new Melding(ERROR, ""));

		MeldingerWidget widget = new MeldingerWidget(meldinger).getErrors();

		assertThat(widget).hasSize(1);
		assertThat(widget.get(0).getAlvorlighetsgrad()).isSameAs(ERROR);
	}

	@Test
	public void getWarnings_med1Av3_returerer1Melding() throws Exception {
		List<Melding> meldinger = asList(
				new Melding(INFO, ""),
				new Melding(WARN, ""),
				new Melding(ERROR, ""));

		MeldingerWidget widget = new MeldingerWidget(meldinger).getWarnings();

		assertThat(widget).hasSize(1);
		assertThat(widget.get(0).getAlvorlighetsgrad()).isSameAs(WARN);
	}

	@Test
	public void getInfos_med1Av3_returerer1Melding() throws Exception {
		List<Melding> meldinger = asList(
				new Melding(INFO, ""),
				new Melding(WARN, ""),
				new Melding(ERROR, ""));

		MeldingerWidget widget = new MeldingerWidget(meldinger).getInfos();

		assertThat(widget).hasSize(1);
		assertThat(widget.get(0).getAlvorlighetsgrad()).isSameAs(INFO);
	}

}

