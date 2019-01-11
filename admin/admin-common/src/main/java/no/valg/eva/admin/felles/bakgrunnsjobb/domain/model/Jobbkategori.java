package no.valg.eva.admin.felles.bakgrunnsjobb.domain.model;

public enum Jobbkategori {
	COUNT_UPLOAD("e.count.upload"), CONFIGURATION_DOWNLOAD("e.configuration.download"), DELETE_VOTERS("e.delete.voters"),
	ELECTORAL_ROLL("e.batch.electoral_roll"), ELECTORAL_ROLL_DOWNLOAD("e.electoral_roll.download"),
	VOTER_NUMBER("e.batch.voter_number"), VALGKORTUNDERLAG("e.batch.valgkortunderlag");

	private final String oldAccessPath;

	Jobbkategori(String oldAccessPath) {
		this.oldAccessPath = oldAccessPath;
	}

	public static Jobbkategori fromAccessPath(String accessPath) {
		if (COUNT_UPLOAD.toAccessPath().equals(accessPath)) {
			return COUNT_UPLOAD;
		} else if (CONFIGURATION_DOWNLOAD.toAccessPath().equals(accessPath)) {
			return CONFIGURATION_DOWNLOAD;
		} else if (DELETE_VOTERS.toAccessPath().equals(accessPath)) {
			return DELETE_VOTERS;
		} else if (ELECTORAL_ROLL.toAccessPath().equals(accessPath)) {
			return ELECTORAL_ROLL;
		} else if (ELECTORAL_ROLL_DOWNLOAD.toAccessPath().equals(accessPath)) {
			return ELECTORAL_ROLL_DOWNLOAD;
		} else if (VOTER_NUMBER.toAccessPath().equals(accessPath)) {
			return VOTER_NUMBER;
		} else if (VALGKORTUNDERLAG.toAccessPath().equals(accessPath)) {
			return VALGKORTUNDERLAG;
		}
		throw new IllegalArgumentException("Unsupported accessPath '" + accessPath + "'");
	}

	public String toAccessPath() {
		return oldAccessPath;
	}
}
