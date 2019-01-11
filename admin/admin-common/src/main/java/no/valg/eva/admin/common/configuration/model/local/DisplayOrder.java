package no.valg.eva.admin.common.configuration.model.local;

import no.valg.eva.admin.common.VersionedObject;

public class DisplayOrder extends VersionedObject {
	private Long pk;
	private int displayOrder;

	public DisplayOrder(Long pk, int displayOrder, int version) {
		super(version);
		this.pk = pk;
		this.displayOrder = displayOrder;
	}

	public Long getPk() {
		return pk;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}
}
