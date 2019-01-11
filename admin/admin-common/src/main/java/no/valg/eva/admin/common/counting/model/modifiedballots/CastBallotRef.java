package no.valg.eva.admin.common.counting.model.modifiedballots;

import java.io.Serializable;

/**
 * Contains reference to id and pk to image data for cast ballot.
 */
public class CastBallotRef implements Serializable {

    private final Long binaryDataPk;
    private final String castBallotId;

    public CastBallotRef(Long binaryDataPk, String castBallotId) {
        this.binaryDataPk = binaryDataPk;
        this.castBallotId = castBallotId;
    }

    public Long getBinaryDataPk() {
        return binaryDataPk;
    }

    public String getCastBallotId() {
        return castBallotId;
    }
}
