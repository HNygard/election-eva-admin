package no.valg.eva.admin.common;

import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains a 4 digit municipality id (kommunenr)
 */
public class MunicipalityId {

    private static final String MUNICIPALITY_ID_REGEX = "^(\\d{4})$";
    private static final Pattern MUNICIPALITY_ID_PATTERN = Pattern.compile(MUNICIPALITY_ID_REGEX);

    private final String id;

    public MunicipalityId(String id) {
        Matcher matcher = MUNICIPALITY_ID_PATTERN.matcher(id);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(format("illegal id <%s>, must match <%s>", id, MUNICIPALITY_ID_PATTERN));
        }
        this.id = id;
    }

    public String countyId() {
        return id.substring(0, 2);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MunicipalityId that = (MunicipalityId) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
