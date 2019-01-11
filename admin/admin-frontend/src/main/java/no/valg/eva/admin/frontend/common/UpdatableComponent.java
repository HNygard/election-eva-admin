package no.valg.eva.admin.frontend.common;

import java.io.Serializable;

public interface UpdatableComponent<T> extends Serializable {

    void initComponent(T context, UpdatableComponentHandler handler);

    void componentDidUpdate(T context);
}
