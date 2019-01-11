package no.valg.eva.admin.frontend.common;

import java.io.Serializable;

public interface Component<T> extends Serializable {

    void initComponent(T context);
}
