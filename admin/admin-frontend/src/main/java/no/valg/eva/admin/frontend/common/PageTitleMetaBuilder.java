package no.valg.eva.admin.frontend.common;

import static java.util.Collections.emptyList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.valggeografi.model.Bydel;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@ApplicationScoped
public class PageTitleMetaBuilder implements Serializable {

    @Inject
    private MessageProvider messageProvider;

    public List<PageTitleMetaModel> area(MvArea area) {
        if (area == null) {
            return Collections.emptyList();
        }

        List<PageTitleMetaModel> models = new ArrayList<>();
        addAreaPageTitleMetaModelIfApplicable(models, area, COUNTY);
        addAreaPageTitleMetaModelIfApplicable(models, area, MUNICIPALITY);
        addAreaPageTitleMetaModelIfApplicable(models, area, BOROUGH);
        addAreaPageTitleMetaModelIfApplicable(models, area, POLLING_DISTRICT);
        addAreaPageTitleMetaModelIfApplicable(models, area, POLLING_PLACE);

        return models;
    }

    private void addAreaPageTitleMetaModelIfApplicable(List<PageTitleMetaModel> models, MvArea mvArea, AreaLevelEnum areaLevelEnum) {
        if (areaLevelEnum.getLevel() == BOROUGH.getLevel()) {
            if (mvArea.hasMunicipalityPathId(AreaPath.OSLO_MUNICIPALITY_ID)) {
                models.add(areaLevelModel(areaLevelEnum, mvArea.getAreaName()));
            }
        } else if (mvArea.getActualAreaLevel().equalOrlowerThan(areaLevelEnum) && mvArea.getAreaName(areaLevelEnum) != null) {
            models.add(areaLevelModel(areaLevelEnum, mvArea.getAreaName(areaLevelEnum)));
        }
    }

    public List<PageTitleMetaModel> settlementTitle(MvElection election, MvArea area) {
        List<PageTitleMetaModel> models = new ArrayList<>();
        if (election != null) {
            String electionName = election.getElectionName();
            if (election.getActualElectionLevel().isEqualToOrLowerThan(ELECTION) && electionName != null) {
                models.add(electionLevelModel(ELECTION, electionName));
            }
        }
        List<PageTitleMetaModel> areas = area(area);
        if (!areas.isEmpty()) {
            PageTitleMetaModel areaModel = areas.get(areas.size() - 1);
            models.add(electionLevelModel(CONTEST, areaModel.getValue()));
        }
        return models;
    }

    public List<PageTitleMetaModel> countCategory(CountCategory countCategory) {
        List<PageTitleMetaModel> models = new ArrayList<>();
        if (countCategory != null) {
            models.add(model("@count.ballot.approve.rejected.category", messageProvider.get(countCategory.messageProperty())));
        }
        return models;
    }

    public List<PageTitleMetaModel> fra(Valg valg) {
        if (valg == null) {
            return emptyList();
        }
        List<PageTitleMetaModel> models = new ArrayList<>();
        models.add(electionLevelModel(ELECTION_GROUP, valg.valggruppeNavn()));
        models.add(electionLevelModel(ELECTION, valg.navn()));

        return models;
    }

    public List<PageTitleMetaModel> fra(Stemmekrets stemmekrets) {
        if (stemmekrets == null) {
            return emptyList();
        }
        List<PageTitleMetaModel> models = new ArrayList<>();
        models.add(model(COUNTY.messageProperty(), stemmekrets.fylkeskommuneNavn()));
        models.add(model(MUNICIPALITY.messageProperty(), stemmekrets.kommuneNavn()));
        models.add(model(BOROUGH.messageProperty(), stemmekrets.bydelNavn()));
        models.add(model(POLLING_DISTRICT.messageProperty(), stemmekrets.navn()));

        return models;
    }

    public List<PageTitleMetaModel> fra(Bydel bydel) {
        if (bydel == null) {
            return emptyList();
        }
        List<PageTitleMetaModel> models = new ArrayList<>();
        models.add(areaLevelModel(COUNTY, bydel.getCountyName()));
        models.add(areaLevelModel(MUNICIPALITY, bydel.getMunicipalityName()));
        models.add(areaLevelModel(BOROUGH, bydel.navn()));

        return models;
    }

    private PageTitleMetaModel areaLevelModel(AreaLevelEnum areaLevel, String textValue) {
        return model(areaLevel.messageProperty(), textValue);
    }

    private PageTitleMetaModel electionLevelModel(ElectionLevelEnum electionLevelEnum, String textValue) {
        return model(electionLevelEnum.messageProperty(), textValue);
    }

    private PageTitleMetaModel model(String messageProviderTextPropertyKey, String textValue) {
        return new PageTitleMetaModel(messageProvider.get(messageProviderTextPropertyKey), textValue);
    }

    public List<PageTitleMetaModel> election(MvElection mvElection) {
        if (mvElection == null) {
            return Collections.emptyList();
        }

        List<PageTitleMetaModel> models = new ArrayList<>();
        addElectionPageTitleMetaModelIfApplicable(models, mvElection, ELECTION_GROUP);
        addElectionPageTitleMetaModelIfApplicable(models, mvElection, ELECTION);
        addElectionPageTitleMetaModelIfApplicable(models, mvElection, CONTEST);

        return models;
    }

    private void addElectionPageTitleMetaModelIfApplicable(List<PageTitleMetaModel> models, MvElection mvElection, ElectionLevelEnum electionLevelEnum) {
        if (mvElection.getActualElectionLevel().isEqualToOrLowerThan(electionLevelEnum) && mvElection.electionLevelName(electionLevelEnum) != null) {
            models.add(electionLevelModel(electionLevelEnum, mvElection.electionLevelName(electionLevelEnum)));
        }
    }
}
