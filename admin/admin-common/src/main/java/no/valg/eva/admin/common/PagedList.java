package no.valg.eva.admin.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@ToString
@NoArgsConstructor(access = PRIVATE)
public class PagedList<T> implements Serializable {

    private static final long serialVersionUID = 8840769025201205688L;

    private List<T> objects;
    private int totalNumberOfObjects;
    private int offset;
    private int limit;

    public PagedList(int offset, int limit, List<T> objects, int totalNumberOfObjects) {
        this.objects = objects;
        this.offset = offset;
        this.limit = limit;
        this.totalNumberOfObjects = totalNumberOfObjects;
    }
}
