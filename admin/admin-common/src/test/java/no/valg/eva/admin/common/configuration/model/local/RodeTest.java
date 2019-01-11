package no.valg.eva.admin.common.configuration.model.local;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class RodeTest {

	@Test
	public void constructor_lagrerIdFraOgTilSomStoreBokstaver() {
		Rode rode = new Rode("a-b", "a", "b");
		
		assertThat(rode.getId()).isEqualTo("A-B");
		assertThat(rode.getFra()).isEqualTo("A");
		assertThat(rode.getTil()).isEqualTo("B");
	}
	
	@Test
	public void constructor_autogenerererIdHvisIkkeOPpgitt() {
		Rode rode = new Rode("A", "B");
		assertThat(rode.getId()).isEqualTo("A - B");
	}
	
	@Test
	public void constructor_egengenerertIdHarStoreBokstaver() {
		Rode rode = new Rode("a", "b");
		assertThat(rode.getId()).isEqualTo("A - B");
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*ID.*")
	public void constructor_idMaaHaEnVerdi() {
		new Rode(null,"a", "b");
	}
	
	@Test
	public void leggTilVelgere_returnererNyttObjektMedEkstraVelgere() {
		assertThat(new Rode("A", "B", 0).leggTilVelgere(2).getAntallVelgere()).isEqualTo(2);
	}
	
}