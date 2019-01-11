package no.valg.eva.admin.common;

import java.util.Random;

public class Randomizer {
	public static final Randomizer INSTANCE = new Randomizer();

	Randomizer() {
		// intentionally empty
	}

	public boolean trueOrFalse() {
		return new Random().nextInt(2) == 1;
	}

	public int nextInt() {
		return new Random().nextInt();
	}
}
