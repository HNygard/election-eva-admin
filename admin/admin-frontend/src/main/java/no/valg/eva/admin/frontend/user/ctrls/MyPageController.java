package no.valg.eva.admin.frontend.user.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.UserMenuMetadata;
import no.valg.eva.admin.common.rbac.service.UserDataService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.reports.ctrls.ReportLinksController;
import no.valg.eva.admin.frontend.user.AdminMenuBuilder;
import no.valg.eva.admin.frontend.user.UserMenuBuilder;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@ViewScoped
public class MyPageController extends BaseController {

    // Injected
    private UserDataController userDataController;
    private UserDataService userDataService;
    private MessageProvider messageProvider;
    private ReportLinksController reportLinksController;

    private AdminMenuBuilder adminMenuBuilder;
    private UserMenuBuilder userMenuBuilder;

    public MyPageController() {
        // CDI
    }

    @Inject
    public MyPageController(UserDataController userDataController, UserDataService userDataService, MessageProvider messageProvider,
                            ReportLinksController reportLinksController) {
        this.userDataController = userDataController;
        this.userDataService = userDataService;
        this.messageProvider = messageProvider;
        this.reportLinksController = reportLinksController;
    }

    @PostConstruct
    public void init() {
        UserMenuMetadata userMenuMetaData = userDataService.findUserMenuMetadata(userDataController.getUserData());
        adminMenuBuilder = new AdminMenuBuilder(this);

        List<ValghendelsesRapport> reports = reportLinksController.getReports();
        userMenuBuilder = new UserMenuBuilder(userMenuMetaData, getPageAccess(), getUserData(),
                messageProvider, menusEnabled(), reports);
    }

    public boolean isRenderAccordion() {
        return !adminMenuBuilder.getMenus().isEmpty();
    }

    public String getAccordionName() {
        return messageProvider.getByElectionEvent(userDataController.getRole().getName(),
                userDataController.getUserData().getElectionEventPk());
    }

    public AdminMenuBuilder getAdminMenuBuilder() {
        return adminMenuBuilder;
    }

    public UserMenuBuilder getUserMenuBuilder() {
        return userMenuBuilder;
    }

    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    public UserDataController getUserDataController() {
        return userDataController;
    }

    public UserData getUserData() {
        return userDataController.getUserData();
    }

    public ReportLinksController getReportLinksController() {
        return reportLinksController;
    }

    private boolean menusEnabled() {
        return userDataController.isOverrideAccess()
                || userDataController.getElectionEvent().isDemoElection()
                || userDataController.getElectionEvent().getElectionEventStatus().getId() == ElectionEventStatusEnum.APPROVED_CONFIGURATION.id();
    }
}
