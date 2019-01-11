package no.valg.eva.admin.common.configuration.model.local;

import lombok.Getter;
import lombok.Setter;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.configuration.model.election.LocaleId;

public class CountyConfigStatus extends VersionedObject {
	@Getter private final AreaPath countyPath;
	@Getter private final String countyName;
	@Getter @Setter private LocaleId localeId;

	// Status fields
	@Getter @Setter private boolean language;
	@Getter @Setter private boolean reportingUnitFylkesvalgstyre;
	@Getter @Setter private boolean listProposals;
	@Getter @Setter private boolean scanning;

	public CountyConfigStatus(AreaPath countyPath, String countyName) {
		this(countyPath, countyName, 0);
	}

	public CountyConfigStatus(AreaPath countyPath, String countyName, int version) {
		super(version);
		this.countyPath = countyPath;
		this.countyName = countyName;
	}
}
