package no.evote.service.configuration;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.local.ReportCountCategories;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ElectionVoteCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.util.List;

import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;

public class ReportCountCategoryServiceBean {

    private static final Logger LOGGER = Logger.getLogger(ReportCountCategoryServiceBean.class);

    @Inject
    private ReportCountCategoryRepository reportCountCategoryRepository;
    @Inject
    private ElectionVoteCountCategoryRepository electionVoteCountCategoryRepository;
    @Inject
    private ContestAreaRepository contestAreaRepository;
    @Inject
    private MvAreaRepository mvAreaRepository;
    @Inject
    private MunicipalityRepository municipalityRepository;

    public List<ReportCountCategory> findReportCountCategoryElementByArea(Municipality municipality, ElectionGroup electionGroup) {

        Municipality domainMunicipality = municipalityRepository.findByPk(municipality.getPk());
        List<ReportCountCategory> reportCountCategoryListFromDb = reportCountCategoryRepository.findReportCountCategories(domainMunicipality, electionGroup);
        List<ElectionVoteCountCategory> centralCats = electionVoteCountCategoryRepository.findElectionVoteCountCategories(electionGroup, BF);

        ReportCountCategories.Criteria criteria = new ReportCountCategories.Criteria(domainMunicipality.isElectronicMarkoffs(),
                isSamiParent(electionGroup, mvAreaRepository.findSingleByPath(domainMunicipality.areaPath())));

        ReportCountCategories reportCountCategories;
        try {
            reportCountCategories = new ReportCountCategories(reportCountCategoryListFromDb, centralCats);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Exception creating new ReportCountCategories: " + e.getMessage(), e);
            throw new EvoteException("@report_count_category.config.error");
        }

        return addMunicipalityToCategories(domainMunicipality, reportCountCategories.filter(criteria).list());
    }

    public boolean isValgtingOrdinaereAndSentraltSamlet(Municipality municipality, ElectionGroup electionGroup) {
        List<ReportCountCategory> categories = findReportCountCategoryElementByArea(municipality, electionGroup);
        for (ReportCountCategory category : categories) {
            if (category.getCountCategory() == CountCategory.VO && category.getCountingMode() == CountingMode.CENTRAL) {
                return true;

            }
        }
        return false;
    }

    public void updateCategories(UserData userData, Municipality municipality, ElectionGroup electionGroup, List<ReportCountCategory> categories) {
        validate(municipality, categories);

        List<ReportCountCategory> oldList = reportCountCategoryRepository.findReportCountCategories(municipality, electionGroup);
        for (ReportCountCategory category : categories) {
            oldList.remove(category);
            if (category.getPk() == null) {
                reportCountCategoryRepository.create(userData, category);
            } else {
                reportCountCategoryRepository.update(userData, category);
            }
        }
        // Delete if any old left
        for (ReportCountCategory category : oldList) {
            reportCountCategoryRepository.delete(userData, category.getPk());
        }
    }

    private void validate(Municipality municipality, List<ReportCountCategory> categories) {
        if (!municipality.isElectronicMarkoffs()) {
            for (ReportCountCategory category : categories) {
                String voteCountCategoryId = category.getVoteCountCategory().getId();
                if (voteCountCategoryId.equals(VB.getId())) {
                    throw new EvoteException("@report_count_category.no_electronic_markoffs.electronic");
                }
            }
        }
    }

    private boolean isSamiParent(ElectionGroup electionGroup, MvArea mvArea) {
        return contestAreaRepository.existsContestAreaParentForElectionGroupAndMunicipality(electionGroup.getPk(), mvArea.getPk());
    }

    private List<ReportCountCategory> addMunicipalityToCategories(Municipality municipality, List<ReportCountCategory> list) {
        for (ReportCountCategory cat : list) {
            cat.setMunicipality(municipality);
        }
        return list;
    }
}
