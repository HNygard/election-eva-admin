package no.valg.eva.admin.common.configuration.model.local;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.configuration.model.Displayable;

import static no.valg.eva.admin.util.StringUtil.isSet;

public abstract class Place extends VersionedObject implements Displayable {

    private final AreaPath path;
    private Long pk;
    private String id;
    private String name;

    protected Place(AreaPath path, int version) {
        super(version);
        this.path = path;
    }

    @Override
    public String display() {
        return name;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public AreaPath getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValid() {
        return isSet(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Place place = (Place) o;

        if (id != null ? !id.equals(place.id) : place.id != null) {
            return false;
        }
        if (!path.equals(place.path)) {
            return false;
        }
        return pk != null ? pk.equals(place.pk) : place.pk == null;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + (pk != null ? pk.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
