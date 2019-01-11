package no.evote.dto;

import com.google.common.base.Objects;
import org.joda.time.DateTime;

import java.io.Serializable;

public class BatchInfoDto implements Serializable {

    private static final long serialVersionUID = 7005006449587651309L;

    private Long pk;
    private DateTime timestamp;
    private Integer status;
    private String infoText;

    public BatchInfoDto(final Long pk, final DateTime timestamp, final Integer batchStatus, final String infoText) {
        this.pk = pk;
        this.timestamp = timestamp;
        this.status = batchStatus;
        this.infoText = infoText;
    }

    public Long getPk() {
        return pk;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setPk(final Long pk) {
        this.pk = pk;
    }

    public void setTimestamp(final DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(final Integer batchStatusId) {
        this.status = batchStatusId;
    }

    public void setInfoText(final String infoText) {
        this.infoText = infoText;
    }

    public String getInfoText() {
        return infoText;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        BatchInfoDto that = (BatchInfoDto) o;
        return this.pk.equals(that.getPk())
                && this.timestamp.equals(that.getTimestamp())
                && this.status.equals(that.getStatus())
                && this.infoText.equals(that.getInfoText());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pk, timestamp, status, infoText);
    }

    @Override
    public String toString() {
        return String.format("<%d, %s, %d, %s>", this.pk, this.timestamp, this.getStatus(), this.getInfoText());
    }
}
