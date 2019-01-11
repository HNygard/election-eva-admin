package no.valg.eva.admin.frontend.cdi;

import lombok.NoArgsConstructor;

import javax.ejb.Singleton;

@Singleton
@NoArgsConstructor //CDI
public class BeanLookupSingleton {

    public <T> T lookup(Class<T> cls) {
        return BeanManager.lookup(cls);
    }
}
