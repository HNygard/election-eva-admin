package no.valg.eva.admin.frontend.voting.confirmation;


import com.google.gson.Gson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.frontend.util.CookieStore;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

@Named
@ViewScoped
@NoArgsConstructor
public class VotingConfirmationOptionalTableColumnManager implements Serializable {

    static final String COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN = "votingtablecolumns";

    private Gson gson;

    @Getter
    private List<VotingConfirmationOptionalTableColumn> optionalTableColumns;

    @Getter
    @Setter
    private List<VotingConfirmationOptionalTableColumn> selectedOptionalTableColumns;

    @PostConstruct
    public void postConstruct() {
        this.gson = new Gson();
        this.optionalTableColumns = asList(VotingConfirmationOptionalTableColumn.values());
        this.selectedOptionalTableColumns = getSelectedFromCookieOrDefaultColumns();
    }

    List<VotingConfirmationOptionalTableColumn> getSelectedFromCookieOrDefaultColumns() {
        final Cookie cookie = CookieStore.getCookie(COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN);
        return cookie != null ? loadColumnsFromCookieValue(cookie.getValue()) : getDefaultColumns();
    }

    private List<VotingConfirmationOptionalTableColumn> loadColumnsFromCookieValue(String serializedValue) {
        try {
            final String[] ids = gson.fromJson(serializedValue, String[].class);
            return stream(ids)
                    .map(VotingConfirmationOptionalTableColumn::valueOf)
                    .collect(Collectors.toList());
        } catch (RuntimeException re) {
            return getDefaultColumns();
        }
    }

    private List<VotingConfirmationOptionalTableColumn> getDefaultColumns() {
        return asList(
                VotingConfirmationOptionalTableColumn.VOTING_NUMBER
        );
    }

    void persistSelectedColumns(List<VotingConfirmationOptionalTableColumn> columns) {
        final String columnsSerialized = gson.toJson(
                columns.stream()
                        .map(VotingConfirmationOptionalTableColumn::getId)
                        .collect(Collectors.toList())
        );

        CookieStore.save(COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN, columnsSerialized);
    }

    public void onSelectedOptionalTableColumnsChanged() {
        persistSelectedColumns(getSelectedOptionalTableColumns());
    }

    public boolean shouldRenderOptionalColumn(String columnId) {
        return getSelectedOptionalTableColumns().stream()
                .anyMatch(selectedColumn -> selectedColumn.getId().equals(columnId));
    }
}
