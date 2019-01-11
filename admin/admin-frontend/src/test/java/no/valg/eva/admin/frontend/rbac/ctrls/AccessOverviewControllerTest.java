package no.valg.eva.admin.frontend.rbac.ctrls;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.faces.application.FacesMessage;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.common.rbac.service.AccessService;

import org.testng.annotations.Test;

public class AccessOverviewControllerTest extends BaseFrontendTest {

	@Test
	public void init_withAccesses_getTreeNodeNotNull() throws Exception {
		AccessOverviewController ctrl = initCtrl();

		assertThat(ctrl.getTreeRoot()).isNotNull();
		assertThat(ctrl.getTreeRoot().getChildren()).hasSize(2);
		assertThat(ctrl.getTreeRoot().getChildren().get(0).getChildren()).hasSize(1);
		assertThat(ctrl.getTreeRoot().getChildren().get(1).getChildren()).hasSize(0);
	}

	@Test
	public void viewOperators_withNoSelectedNode_returnsErrorMessage() throws Exception {
		AccessOverviewController ctrl = initCtrl();

		String result = ctrl.viewOperators();

		assertThat(result).isNull();
		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@rbac.inspect.noSelectionError");
	}

	@Test
	public void viewOperators_withSelectedNode_returnsRedirectString() throws Exception {
		AccessOverviewController ctrl = initCtrl();
		ctrl.setSelectedNode(ctrl.getTreeRoot().getChildren().get(0));

		String result = ctrl.viewOperators();

		assertThat(result).isEqualTo("accessOverviewOperators.xhtml?access=konfig&faces-redirect=true");
	}

	private AccessOverviewController initCtrl() throws Exception {
		AccessOverviewController ctrl = initializeMocks(AccessOverviewController.class);
		stub_accessService_findAll();
		ctrl.init();
		return ctrl;
	}

	private void stub_accessService_findAll() {
		when(getInjectMock(AccessService.class).findAll(getUserDataMock())).thenReturn(getAccessList());
	}

	private List<Access> getAccessList() {
		return asList(
				access(Konfigurasjon),
				access(Konfigurasjon_Grunnlagsdata),
				access(Beskyttet_Slett_Opptelling));
	}

	private Access access(Accesses access) {
		return new Access(access.paths()[0]);
	}

}
