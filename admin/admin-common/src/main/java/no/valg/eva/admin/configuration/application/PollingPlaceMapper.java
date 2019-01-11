package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;

import java.util.List;

import static no.valg.eva.admin.configuration.application.BoroughMapper.toBorough;
import static no.valg.eva.admin.configuration.application.OpeningHoursMapper.sort;

public final class PollingPlaceMapper {

    private PollingPlaceMapper() {
    }

    public static AdvancePollingPlace toAdvancePollingPlace(PollingPlace dbPlace) {
        if (dbPlace == null) {
            return null;
        }
        if (dbPlace.isElectionDayVoting()) {
            throw new RuntimeException(dbPlace + " is not AdvancePollingPlace");
        }
        AdvancePollingPlace result = new AdvancePollingPlace(dbPlace.areaPath(), dbPlace.getAuditOplock());
        result.setBorough(toBorough(dbPlace.getPollingDistrict().getBorough()));
        result.setPk(dbPlace.getPk());
        result.setId(dbPlace.getId());
        result.setName(dbPlace.getName());
        result.setAddress(dbPlace.getAddressLine1());
        result.setPostalCode(dbPlace.getPostalCode());
        result.setPostTown(dbPlace.getPostTown());
        result.setGpsCoordinates(dbPlace.getGpsCoordinates());
        result.setAdvanceVoteInBallotBox(dbPlace.isAdvanceVoteInBallotBox());
        result.setPublicPlace(dbPlace.isPublicPlace());
        return result;
    }

    public static ElectionDayPollingPlace toElectionDayPollingPlace(PollingDistrict dbDistrict, PollingPlace dbPlace,
                                                                    List<OpeningHours> openingHours) {
        if (!isValidElectionDayDistrict(dbDistrict)) {
            throw new RuntimeException(dbDistrict + " is not regular or child and cannot be ElectionDayPollingPlace parent (" + dbDistrict.type()
                    + ")");
        }
        if (dbPlace != null && !dbPlace.isElectionDayVoting()) {
            throw new RuntimeException(dbPlace + " is not ElectionDayPollingPlace");
        }

        ElectionDayPollingPlace result;
        if (dbPlace == null) {
            result = new ElectionDayPollingPlace(dbDistrict.areaPath());
        } else {
            result = new ElectionDayPollingPlace(dbPlace.areaPath(), dbPlace.getAuditOplock());
        }

        result.setBorough(toBorough(dbDistrict.getBorough()));
        result.setParentPk(dbDistrict.getPk());
        result.setParentName(dbDistrict.getName());
        result.setId(dbDistrict.getId());
        result.setOpeningHours(sort(openingHours));
        
        if (dbPlace != null) {
            result.setPk(dbPlace.getPk());
            result.setName(dbPlace.getName());
            result.setAddress(dbPlace.getAddressLine1());
            result.setPostalCode(dbPlace.getPostalCode());
            result.setPostTown(dbPlace.getPostTown());
            result.setGpsCoordinates(dbPlace.getGpsCoordinates());
            result.setUsePollingStations(dbPlace.getUsingPollingStations());
            result.setInfoText(dbPlace.getInfoText());
        }

        return result;
    }

    private static boolean isValidElectionDayDistrict(PollingDistrict pollingDistrict) {
        return pollingDistrict.type() == PollingDistrictType.REGULAR || pollingDistrict.type() == PollingDistrictType.CHILD;
    }

    public static PollingPlace toPollingPlace(PollingPlace dbPlace, no.valg.eva.admin.common.configuration.model.local.PollingPlace place) {
        dbPlace.setId(place.getId());
        dbPlace.setName(place.getName());
        dbPlace.setAddressLine1(place.getAddress());
        dbPlace.setPostalCode(place.getPostalCode());
        dbPlace.setPostTown(place.getPostTown());
        dbPlace.setGpsCoordinates(place.getGpsCoordinates());
        dbPlace.setAddressLine2(null);
        dbPlace.setAddressLine3(null);

        if (place instanceof AdvancePollingPlace) {
            dbPlace.setElectionDayVoting(false);
            dbPlace.setUsingPollingStations(false);
            dbPlace.setAdvanceVoteInBallotBox(((AdvancePollingPlace) place).isAdvanceVoteInBallotBox());
            dbPlace.setPublicPlace(((AdvancePollingPlace) place).isPublicPlace());
        } else if (place instanceof ElectionDayPollingPlace) {
            dbPlace.setElectionDayVoting(true);
            dbPlace.setAdvanceVoteInBallotBox(false);
            dbPlace.setPublicPlace(true);
            dbPlace.setUsingPollingStations(((ElectionDayPollingPlace) place).isUsePollingStations());
            dbPlace.setInfoText(((ElectionDayPollingPlace) place).getInfoText());
        }

        return dbPlace;
    }
}
