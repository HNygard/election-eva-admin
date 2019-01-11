package no.valg.eva.admin.frontend.user;

import no.evote.security.UserData;
import no.valg.eva.admin.frontend.common.menu.Menu;
import no.valg.eva.admin.frontend.security.PageAccess;
import no.valg.eva.admin.frontend.user.ctrls.MyPageController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdminMenuBuilder implements Serializable {

	private List<Menu> menus;
	private PageAccess pageAccess;
	private UserData userData;

	public AdminMenuBuilder(MyPageController myPageController) {
		menus = new ArrayList<>();
		pageAccess = myPageController.getPageAccess();
		userData = myPageController.getUserData();
		createAccordionConfigMenu();
		createAccordionElectoralRollMenu();
		createAccordionRbacMenu();
		createAccordionDeleteMenu();
	}

	public List<Menu> getMenus() {
		return menus;
	}

	private void createAccordionConfigMenu() {
		Menu configMenu = new Menu("@menu.config.header", true);
		addIfAccess(configMenu, "@menu.config.edit_election_event", "/secure/election/electionEvent.xhtml");
		addIfAccess(configMenu, "@menu.config.election_event_reports", "/secure/election/electionEventReports.xhtml");
		addIfAccess(configMenu, "@menu.config.election", "/secure/config/listElections.xhtml");
		addIfAccess(configMenu, "@menu.config.area", "/secure/config/listAreas.xhtml");
		addIfAccess(configMenu, "@menu.config.partyregister", "/secure/listProposal/editPartyList.xhtml");
		addIfAccess(configMenu, "@menu.config.election_vote_count_category", "/secure/categories/configureCentralCountCategories.xhtml");
		if (pageAccess.hasAccess(userData, "/secure/config/centralConfigurationOverview.xhtml")) {
			configMenu.addChild(new Menu("@menu.config.central_overview", "centralConfirmDialog"));
		}
		addIfAccess(configMenu, "@menu.config.generate_download_EML", "/secure/config/generateEML.xhtml");
		addIfAccess(configMenu, "@menu.config.election_event_administration", "/secure/election/listElectionEvents.xhtml");
		addIfAccess(configMenu, "@menu.config.copy_roles", "/secure/rbac/copyRoles.xhtml");
		addIfAccess(configMenu, "@menu.config.certificate_management", "/secure/config/signingKeys.xhtml");
		addIfAccess(configMenu, "@menu.config.reporting_unit_type", "/secure/reportingUnit/reportingUnitType.xhtml");

		addIfAccess(configMenu, "@menu.administration.valgnatt_config", "/secure/administration/valgnattConfig.xhtml");

		addIfAccess(configMenu, "@menu.config.translation", "/secure/translation/translationsEdit.xhtml");

		addIfAccess(configMenu, "@menu.config.valgstyrer", "/secure/config/adresselisteValgstyrer.xhtml");
		addIfAccess(configMenu, "@menu.config.samevalgstyrer", "/secure/config/adresselisteSamevalgstyrer.xhtml");
		
		addIfAccess(configMenu, "@menu.config.scanningconfig", "/secure/config/exportScanningConfig.xhtml");

		leggTilHvisIkkeTom(configMenu);
	}

	private void leggTilHvisIkkeTom(Menu configMenu) {
		if (!configMenu.isEmpty()) {
			Menu root = new Menu("");
			root.addChild(configMenu);
			getMenus().add(root);
		}
	}

	private void createAccordionElectoralRollMenu() {
		Menu electoralRollMenu = new Menu("@menu.electoralRoll.header", true);
		addIfAccess(electoralRollMenu, "@menu.electoralRoll.import", "/secure/manntall/importElectoralRoll.xhtml");
		addIfAccess(electoralRollMenu, "@menu.config.area_import", "/secure/manntall/importAreaHierarchy.xhtml");
		addIfAccess(electoralRollMenu, "@menu.config.area_changes_import", "/secure/manntall/importChangesInAreaHierarchy.xhtml");
		addIfAccess(electoralRollMenu, "@menu.electoralRoll.voterNumbers", "/secure/manntall/generateVoterNumbers.xhtml");
		addIfAccess(electoralRollMenu, "@menu.electoralRoll.genererValgkortgrunnlag", "/secure/manntall/genererValgkortgrunnlag.xhtml");

		leggTilHvisIkkeTom(electoralRollMenu);
	}

	private void createAccordionRbacMenu() {
		Menu rbacMenu = new Menu("@menu.rbac", true);
		addIfAccess(rbacMenu, "@menu.rbac.roles", "/secure/rbac/adminRoles.xhtml");
		addIfAccess(rbacMenu, "@menu.rbac.import_export", "/secure/rbac/exportImportOperators.xhtml");
		addIfAccess(rbacMenu, "@menu.rbac.roles_export_import", "/secure/rbac/exportImportRoles.xhtml");
		addIfAccess(rbacMenu, "@menu.config.operator.import_buypass_number", "/secure/rbac/importBuypassKeySerialnumber.xhtml");
		addIfAccess(rbacMenu, "@menu.accessOverview", "/secure/rbac/accessOverview.xhtml");

		leggTilHvisIkkeTom(rbacMenu);
	}

	private void createAccordionDeleteMenu() {

		Menu deleteMenu = new Menu("@menu.delete", true);
		addIfAccess(deleteMenu, "@delete.vote_counts.header", "/secure/opptelling/slettOpptellinger.xhtml");
		addIfAccess(deleteMenu, "@delete.votings.header", "/secure/delete/deleteVotings.xhtml");
		addIfAccess(deleteMenu, "@delete.settlement.header", "/secure/delete/deleteSettlement.xhtml");
		addIfAccess(deleteMenu, "@delete.levelingSeatSettlement.header", "/secure/delete/deleteLevelingSeatSettlement.xhtml");
		addIfAccess(deleteMenu, "@delete.votersWithoutMvArea.header", "/secure/delete/deleteVotersWithoutMvArea.xhtml");
		addIfAccess(deleteMenu, "@delete.voters.header", "/secure/delete/deleteVoters.xhtml");
		addIfAccess(deleteMenu, "@delete.listDeleteVoters.header", "/secure/delete/deleteVotersBatches.xhtml");
		addIfAccess(deleteMenu, "@delete.prepareInitialLoad.header", "/secure/delete/prepareNewInitialLoad.xhtml");

		leggTilHvisIkkeTom(deleteMenu);
	}

	private void addIfAccess(Menu menu, String i18n, String url) {
		addIfAccess(menu, i18n, url, true);
	}

	private void addIfAccess(Menu menu, String i18n, String url, boolean enabled) {
		if (pageAccess.hasAccess(userData, url)) {
			menu.addChild(new Menu(i18n, url).setEnabled(enabled));
		}
	}

}
