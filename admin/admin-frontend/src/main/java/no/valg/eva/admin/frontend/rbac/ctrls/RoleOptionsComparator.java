package no.valg.eva.admin.frontend.rbac.ctrls;

import java.io.Serializable;
import java.util.Comparator;

import javax.faces.model.SelectItem;

public class RoleOptionsComparator implements Comparator<SelectItem>, Serializable {

	@Override
	public int compare(SelectItem item1, SelectItem item2) {
		return item1.getLabel().compareTo(item2.getLabel());
	}

}
