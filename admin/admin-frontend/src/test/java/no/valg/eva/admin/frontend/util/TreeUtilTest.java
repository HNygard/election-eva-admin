package no.valg.eva.admin.frontend.util;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.frontend.util.TreeUtil;
import no.valg.eva.admin.rbac.domain.model.Access;

import org.primefaces.model.TreeNode;
import org.testng.annotations.Test;

public class TreeUtilTest {

	@Test
	public void issuingPointAdvanceShouldBePartOfTree() {
		List<Access> accessList = new ArrayList<>();
		accessList.add(createAccess("w.voting.issuing_point.advance"));
		accessList.add(createAccess("w.voting.receive.early_vote"));
		TreeNode root = TreeUtil.pathToTree(accessList);

		assert root.getChildren().size() == 2;
	}

	private Access createAccess(final String path) {
		Access access = new Access();
		access.setPath(path);
		return access;
	}
}
