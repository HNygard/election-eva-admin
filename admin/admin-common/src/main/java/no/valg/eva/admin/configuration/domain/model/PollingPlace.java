package no.valg.eva.admin.configuration.domain.model;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.model.VersionedEntity;
import no.evote.persistence.AntiSamyEntityListener;
import no.evote.security.ContextSecurable;
import no.evote.validation.AntiSamy;
import no.evote.validation.Gps;
import no.evote.validation.ID;
import no.evote.validation.Letters;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "polling_place", uniqueConstraints = {@UniqueConstraint(columnNames = {"polling_district_pk", "polling_place_id"}),
        @UniqueConstraint(columnNames = {"polling_district_pk", "election_day_voting"})})
@AttributeOverride(name = "pk", column = @Column(name = "polling_place_pk"))
@AntiSamy
@EntityListeners({AntiSamyEntityListener.class})
@NamedQueries({
        @NamedQuery(name = "PollingPlace.findById", query = "SELECT pp FROM PollingPlace pp WHERE pp.pollingDistrict.pk = :pollingDistrictPk AND pp.id = :id"),
        @NamedQuery(name = "PollingPlace.findFirstPollingPlace", query = "SELECT pp FROM PollingPlace pp WHERE pp.pollingDistrict.pk = :pollingDistrictPk"),
        @NamedQuery(name = "PollingPlace.findFirstByPollingDistrictPkAndAdvanceVoteInBallotBox",
                query = "SELECT pp FROM PollingPlace pp WHERE pp.pollingDistrict.pk = :pollingDistrictPk AND pp.advanceVoteInBallotBox = :advanceVoteInBallotBox"),
        @NamedQuery(name = "PollingPlace.findByElectionDayVoting", query = "SELECT pp FROM PollingPlace pp WHERE pp.pollingDistrict.pk = :pollingDistrictPk "
                + "AND pp.electionDayVoting=true"),
        @NamedQuery(name = "PollingPlace.findPollingPlacesWithOpeningHours",
                query = "SELECT DISTINCT pp FROM MvArea mva "
                        + " JOIN mva.pollingPlace pp "
                        + " LEFT JOIN FETCH pp.openingHours "
                        + " WHERE mva.electionEvent.pk = :electionEventPk "
                        + "   AND mva.areaLevel = 6 "
                        + "   AND pp.electionDayVoting=true"),
        @NamedQuery(name = "PollingPlace.findPollingPlaceWithOpeningHours",
                query = "SELECT pp FROM PollingPlace pp "
                        + " LEFT JOIN FETCH pp.openingHours "
                        + " WHERE pp.pk= :pollingPlacePk "),

        @NamedQuery(
                name = "PollingPlace.getElectoralRollForPollingPlace",
                query = "SELECT v FROM Voter v WHERE v.mvArea.pollingDistrict.pk = :pollingDistrictPk")})
@NamedQuery(name = "PollingPlace.findByPollingDistrict", query = "SELECT pp FROM PollingPlace pp WHERE pp.pollingDistrict.pk = :pollingDistrictPk")
public class PollingPlace extends VersionedEntity implements java.io.Serializable, ContextSecurable {

    private static final int MAX_NUMBER_OF_OPENING_HOURS = 4;
    private PollingDistrict pollingDistrict;
    private String id;
    private boolean electionDayVoting;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String postalCode;
    private String postTown;
    private String infoText;
    private Boolean usingPollingStations = false;
    private boolean advanceVoteInBallotBox;
    private String gpsCoordinates;
    private boolean publicPlace;
    private Set<OpeningHours> openingHours = new HashSet<>();

    public PollingPlace() {
    }

    public PollingPlace(final String id, final String name, final String postalCode, final PollingDistrict pollingDistrict) {
        this.id = id;
        this.name = name;
        this.postalCode = postalCode;
        this.pollingDistrict = pollingDistrict;
    }

    @Transient
    public static Comparator<PollingPlace> sortById() {
        return comparing(PollingPlace::getId);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "polling_district_pk", nullable = false)
    @NotNull
    public PollingDistrict getPollingDistrict() {
        return pollingDistrict;
    }

    public void setPollingDistrict(final PollingDistrict pollingDistrict) {
        this.pollingDistrict = pollingDistrict;
    }

    @Column(name = "polling_place_id", nullable = false, length = 4)
    @ID(size = 4)
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Column(name = "election_day_voting", nullable = false)
    public boolean isElectionDayVoting() {
        return electionDayVoting;
    }

    public void setElectionDayVoting(final boolean electionDayVoting) {
        this.electionDayVoting = electionDayVoting;
    }

    @Column(name = "polling_place_name", nullable = false, length = 50)
    @LettersOrDigits(extraChars = " .,-'/")
    @StringNotNullEmptyOrBlanks
    @Size(max = 50)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Column(name = "address_line1", length = 50)
    @LettersOrDigits
    @Size(max = 50)
    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(final String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    @Column(name = "address_line2", length = 50)
    @LettersOrDigits
    @Size(max = 50)
    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(final String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    @Column(name = "address_line3", length = 50)
    @LettersOrDigits
    @Size(max = 50)
    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(final String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    @Column(name = "postal_code", length = 4)
    @no.evote.validation.PostalCode
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    @Letters
    @Column(name = "post_town", length = 50)
    @Size(max = 50)
    public String getPostTown() {
        return postTown;
    }

    public void setPostTown(final String postTown) {
        this.postTown = postTown;
    }

    @Column(name = "info_text", length = 150)
    @Size(max = 150)
    @Pattern(regexp = ElectionCardConfig.REGEX_INFO_TEXT, message = "@config.local.election_card.infoText_invalid")
    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(final String infoText) {
        this.infoText = infoText;
    }

    @Override
    public Long getAreaPk(final AreaLevelEnum level) {
        switch (level) {
            case POLLING_PLACE:
                return this.getPk();
            case POLLING_DISTRICT:
                return pollingDistrict.getPk();
            default:
                return null;
        }
    }

    @Override
    public Long getElectionPk(final ElectionLevelEnum level) {
        return null;
    }

    @Column(name = "using_polling_stations", nullable = false)
    public Boolean getUsingPollingStations() {
        return usingPollingStations;
    }

    public void setUsingPollingStations(final Boolean usingPollingStations) {
        this.usingPollingStations = usingPollingStations;
    }

    @Column(name = "advance_vote_in_ballot_box", nullable = false)
    public boolean isAdvanceVoteInBallotBox() {
        return advanceVoteInBallotBox;
    }

    public void setAdvanceVoteInBallotBox(final boolean advanceVoteInBallotBox) {
        this.advanceVoteInBallotBox = advanceVoteInBallotBox;
    }

    @Column(name = "public_place", nullable = false)
    public boolean isPublicPlace() {
        return publicPlace;
    }

    public void setPublicPlace(final boolean publicPlace) {
        this.publicPlace = publicPlace;
    }

    @Size(max = 50)
    @Gps
    @Column(name = "gps_coordinates")
    public String getGpsCoordinates() {
        return gpsCoordinates;
    }

    public void setGpsCoordinates(final String gpsCoordinates) {
        this.gpsCoordinates = gpsCoordinates;
    }

    @OneToMany(mappedBy = "pollingPlace", fetch = FetchType.LAZY, cascade = ALL, orphanRemoval = true)
    public Set<OpeningHours> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(Set<OpeningHours> openingHours) {
        this.openingHours = openingHours;
    }

    public AreaPath areaPath() {
        return getPollingDistrict().areaPath().add(getId());
    }

    @Transient
    public boolean isEditable() {
        return !AreaPath.CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID.equals(getId());
    }

    public void removeAllOpeningHours() {
        getOpeningHours().forEach(currentOpeningHours -> currentOpeningHours.setPollingPlace(null));
        getOpeningHours().clear();
    }

    @Transient
    public void addOpeningHours(List<OpeningHours> openingHours) {
        for (no.valg.eva.admin.configuration.domain.model.OpeningHours currentOpeningHour : openingHours) {
            addOpeningHour(currentOpeningHour);
        }
    }

    @Transient
    public void addOpeningHour(OpeningHours openingHours) {
        if (getOpeningHours() != null && getOpeningHours().size() >= MAX_NUMBER_OF_OPENING_HOURS) {
            throw new EvoteException("Pollingplace [" + this + "] already has max number of opening hours[" + getOpeningHours().size() + "] - not possible to add more!");
        }

        openingHours.setPollingPlace(this);
        getOpeningHours().add(openingHours);
    }

    @Transient
    public List<OpeningHours> openingHoursAsList() {
        return new ArrayList<>(openingHours);
    }
}
