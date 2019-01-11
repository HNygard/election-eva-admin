package no.valg.eva.admin.frontend.voting.ctrls.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.EvoteConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static no.valg.eva.admin.util.DateUtil.endOfDay;
import static no.valg.eva.admin.util.DateUtil.startOfDay;
import static no.valg.eva.admin.util.DateUtil.toDate;
import static no.valg.eva.admin.util.DateUtil.toLocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class VotingPeriodViewModel {

    private static final DateTimeFormatter shortDateFormatter = java.time.format.DateTimeFormatter.ofPattern("d. MMMM", EvoteConstants.DEFAULT_JAVA_LOCALE);
    private static final DateTimeFormatter fullDateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy", EvoteConstants.DEFAULT_JAVA_LOCALE);

    private LocalDateTime fromDate;
    private LocalDateTime toDateIncluding;

    public Date getFromDateAsLegacyDate() {
        return toDate(fromDate);
    }

    public void setFromDateAsLegacyDate(Date date) {
        final LocalDateTime localDateTime = toLocalDateTime(date);
        this.fromDate = startOfDay(localDateTime.toLocalDate());
    }

    public String getFromDateAsShortDate() {
        return shortDateFormatter.format(fromDate).toLowerCase();
    }

    public String getFromDateAsFullDate() {
        return fullDateFormatter.format(fromDate).toLowerCase();
    }

    public Date getToDateAsLegacyDate() {
        return toDate(toDateIncluding);
    }

    public void setToDateAsLegacyDate(Date date) {
        final LocalDateTime localDateTime = toLocalDateTime(date);
        this.toDateIncluding = endOfDay(localDateTime.toLocalDate());
    }

    public String getToDateAsShortDate() {
        return shortDateFormatter.format(toDateIncluding).toLowerCase();
    }

    public String getToDateAsFullDate() {
        return fullDateFormatter.format(toDateIncluding).toLowerCase();
    }
}
