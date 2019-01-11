package no.valg.eva.admin.common.configuration.model.local;

import static no.valg.eva.admin.util.StringUtil.isSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.configuration.model.Displayable;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;

@Data
@EqualsAndHashCode(callSuper = false, of = {"pk"})
public class ResponsibleOfficer extends VersionedObject implements Displayable {
	private Long pk;
	private AreaPath areaPath;
	private ResponsibilityId responsibilityId;
	private int displayOrder;
	private String firstName;
	private String middleName;
	private String lastName;
	private String address;
	private String postalCode;
	private String postalTown;
	private String email;
	private String tlf;
	// Used in search only
	private String id;
	private String age;
	/* Brukes for å kunne skille mellom data som ikke trenger validering (feks ved import direkte fra manntall),
	   eller i situasjoner der validering bør skje, feks ved manuell registrering av styremedlem */
	private boolean skalValideres = false;

	public ResponsibleOfficer() {
		this(0);
	}

	public ResponsibleOfficer(int version) {
		super(version);
	}

	public DisplayOrder displayOrder() {
		return new DisplayOrder(pk, displayOrder, getVersion());
	}

	@Override
	public String display() {
		return getFullName();
	}

	public boolean isValid() {
		return responsibilityId != null && isSet(firstName, lastName);
	}

	public String getFullName() {

		StringBuilder fullName = new StringBuilder();
		fullName.append(this.firstName);
		fullName.append(" ");
		if (this.middleName != null) {
			fullName.append(this.middleName);
			fullName.append(" ");
		}
		fullName.append(this.lastName);
		return fullName.toString();
	}

	public boolean isPersisted() {
		return pk != null;
	}
}
