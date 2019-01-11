package no.valg.eva.admin.common.configuration.model.local;

import lombok.Getter;
import lombok.Setter;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.configuration.model.election.LocaleId;

public class MunicipalityConfigStatus extends VersionedObject {
	@Getter private final AreaPath municipalityPath;
	@Getter private final String municipalityName;

	@Getter @Setter private LocaleId localeId;
	@Getter @Setter private boolean useElectronicMarkoffs;

	// Status fields
	@Getter @Setter private boolean advancePollingPlaces;
	@Getter @Setter private boolean electionPollingPlaces;
	@Getter @Setter private boolean electionCard;
	@Getter @Setter private boolean pollingDistricts;
	@Getter @Setter private boolean techPollingDistricts;
	@Getter @Setter private boolean reportingUnitStemmestyre;
	@Getter @Setter private boolean reportingUnitValgstyre;
	@Getter @Setter private boolean pollingStations;
	@Getter @Setter private boolean language;
	@Getter @Setter private boolean countCategories;
	@Getter @Setter private boolean listProposals;
	@Getter @Setter private boolean electronicMarkoffs;
	@Getter @Setter private boolean scanning;

	public MunicipalityConfigStatus(AreaPath municipalityPath, String municipalityName) {
		this(municipalityPath, municipalityName, 0);
	}

	public MunicipalityConfigStatus(AreaPath municipalityPath, String municipalityName, int version) {
		super(version);
		this.municipalityPath = municipalityPath;
		this.municipalityName = municipalityName;
	}
}
