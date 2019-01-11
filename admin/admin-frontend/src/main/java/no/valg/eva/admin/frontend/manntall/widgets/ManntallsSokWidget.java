package no.valg.eva.admin.frontend.manntall.widgets;

import lombok.NoArgsConstructor;
import no.evote.security.UserData;
import no.evote.service.configuration.VoterService;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.configuration.service.ManntallsnummerService;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.manntall.models.AvansertSok;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.AVANSERT;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.FODSELSNUMMER;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.LOPENUMMER;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.MANNTALLSNUMMER;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Named
@ViewScoped
@NoArgsConstructor        // For CDI
public class ManntallsSokWidget extends BaseController {

    private static final int MAX_SEARCH_RESULT = 50;
    private static final String FORHANDSSTEMMETYPE_PREFIX = "FB/FU/FI/FE -";
    private static final String VALGGTINGSTEMMETYPER_PREFIX = "VB/VS/VF -";
    @Inject
    @EjbProxy
    protected VotingService votingService;
    // Injected
    private UserDataController userDataController;
    private ManntallsnummerService manntallsnummerService;
    private VoterService voterService;
    private ManntallsSokListener listener;
    private int activeTabIndex;
    private String stemmekategori;
    private String lopenummer;
    private String manntallsnummer;
    private String fodselsnummer;
    private AvansertSok avansertSok;
    private List<Voter> resultatSok = new ArrayList<>();
    private Voter velger;
    private Manntallsnummer manntallsnummerObject;

    @Inject
    public ManntallsSokWidget(UserDataController userDataController, ManntallsnummerService manntallsnummerService, VoterService voterService) {
        this.userDataController = userDataController;
        this.manntallsnummerService = manntallsnummerService;
        this.voterService = voterService;
    }

    public void addListener(ManntallsSokListener listener) {
        this.listener = listener;
        reset();
    }

    public void reset() {
        getResultatSok().clear();
        velgerValgt(null);
        setLopenummer(null);
        setManntallsnummer(null);
        setFodselsnummer(null);
        if (listener == null) {
            setAvansertSok(new AvansertSok());
        } else {
            setAvansertSok(new AvansertSok(listener.getKommuneSti().kommuneId()));
        }

    }

    public void sokLopenummer() {
        if (listener != null) {
            listener.manntallsSokInit();
        }
        prepareSearch();
        execute(() -> {
            long lopenummerLong = Long.parseLong(lopenummer);
            boolean isTidligStemme = FO.getId().equals(getStemmekategori());
            Voting stemmegivning = votingService.findVotingByVotingNumber(
                    getUserData(), listener.getValggruppeSti(), listener.getKommuneSti(), lopenummerLong, isTidligStemme);
            if (stemmegivning == null) {
                manntallsSokTomtResultat(LOPENUMMER);
            } else {
                velgerValgt(stemmegivning.getVoter());
            }
        });
    }

    public void sokManntallsnummer() {
        if (listener != null) {
            listener.manntallsSokInit();
        }
        prepareSearch();
        execute(() -> {
            if (!manntallsnummerService.erValgaarssifferGyldig(getUserData(), new Manntallsnummer(manntallsnummer))) {
                buildDetailMessage("@voting.validation.electionCardNotValid", SEVERITY_ERROR);
                return;
            }

            resultatSok = voterService.findByManntallsnummer(getUserData(), new Manntallsnummer(manntallsnummer));

            if (resultatSok.isEmpty()) {
                manntallsSokTomtResultat(MANNTALLSNUMMER);
            } else {
                velgerValgt(resultatSok.get(0));
            }
        });
    }

    public void sokFodselsnummer() {
        if (listener != null) {
            listener.manntallsSokInit();
        }
        prepareSearch();
        execute(() -> {
            resultatSok = voterService.findByElectionEventAndId(getUserData(), getFodselsnummer(), userDataController.getElectionEvent().getPk());

            if (resultatSok.isEmpty()) {
                manntallsSokTomtResultat(FODSELSNUMMER);
            } else {
                velgerValgt(resultatSok.get(0));
            }
        });
    }

    public void sokAvansert() {
        if (listener != null) {
            listener.manntallsSokInit();
        }
        prepareSearch();
        execute(() -> {
            Voter aVoter = new Voter();
            aVoter.setElectionEvent(userDataController.getElectionEvent());
            aVoter.setNameLine(getAvansertSok().getNavn());
            aVoter.setAddressLine1(getAvansertSok().getAdresse());
            aVoter.setDateOfBirth(getAvansertSok().getFodselsDato());

            resultatSok = voterService.searchVoter(getUserData(), aVoter, null,
                    getAvansertSok().getKommuneId(), MAX_SEARCH_RESULT, false,
                    getUserData().getElectionEventPk());

            if (resultatSok.isEmpty()) {
                manntallsSokTomtResultat(AVANSERT);
            } else if (resultatSok.size() > MAX_SEARCH_RESULT) {
                resultatSok.clear();
                buildDetailMessage("@electoralRoll.specifySearch", SEVERITY_ERROR);
            } else if (resultatSok.size() == 1) {
                velgerValgt(resultatSok.get(0));
            }
        });
    }

    public void onRowSelect(SelectEvent event) {
        velgerValgt((Voter) event.getObject());
    }

    public void onTabChange(TabChangeEvent event) {
        reset();
        if (listener != null) {
            listener.manntallsSokInit();
        }
    }

    private void manntallsSokTomtResultat(ManntallsSokType manntallsSokType) {
        if (listener != null) {
            listener.manntallsSokTomtResultat();
            String message = listener.manntallsTomtResultatMelding(manntallsSokType);
            if (!isEmpty(message)) {
                buildDetailMessage(message, SEVERITY_ERROR);
            }
        }
    }

    private void prepareSearch() {
        getResultatSok().clear();
        setVelger(null);
        manntallsnummerObject = null;
    }

    private void velgerValgt(Voter velger) {
        setVelger(velger);
        if (velger != null && velger.getNumber() != null) {
            manntallsnummerObject = manntallsnummerService.beregnFulltManntallsnummer(getUserData(), velger.getNumber());
        } else {
            manntallsnummerObject = null;
        }
        if (listener != null && velger != null) {
            listener.manntallsSokVelger(velger);
        }
    }

    private UserData getUserData() {
        return userDataController.getUserData();
    }

    public Voter getVelger() {
        return velger;
    }

    public void setVelger(Voter velger) {
        this.velger = velger;
    }

    public List<Voter> getResultatSok() {
        return resultatSok;
    }

    public String getStemmekategori() {
        return stemmekategori;
    }

    public void setStemmekategori(String stemmekategori) {
        this.stemmekategori = stemmekategori;
    }

    public String getLopenummer() {
        return lopenummer;
    }

    public void setLopenummer(String lopenummer) {
        this.lopenummer = lopenummer;
    }

    public String getStemmekategoriPrefix() {
        if (FO.getId().equals(getStemmekategori())) {
            return FORHANDSSTEMMETYPE_PREFIX;
        }
        return VALGGTINGSTEMMETYPER_PREFIX;
    }

    public String getManntallsnummer() {
        return manntallsnummer;
    }

    public void setManntallsnummer(String manntallsnummer) {
        this.manntallsnummer = manntallsnummer;
    }

    public String getFodselsnummer() {
        return fodselsnummer;
    }

    public void setFodselsnummer(String fodselsnummer) {
        this.fodselsnummer = fodselsnummer;
    }

    public AvansertSok getAvansertSok() {
        return avansertSok;
    }

    public void setAvansertSok(AvansertSok avansertSok) {
        this.avansertSok = avansertSok;
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public Manntallsnummer getManntallsnummerObject() {
        return manntallsnummerObject;
    }
}
