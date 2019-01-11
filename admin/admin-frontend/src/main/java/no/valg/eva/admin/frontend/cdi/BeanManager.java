package no.valg.eva.admin.frontend.cdi;

import no.evote.exception.EvoteException;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public final class BeanManager {

    private BeanManager() {
    }

    public static <T> T lookup(Class<T> cls) {
        try {
            javax.enterprise.inject.spi.BeanManager beanManager = (javax.enterprise.inject.spi.BeanManager) new InitialContext()
                    .lookup("java:comp/BeanManager");
            Bean<?> bean = beanManager.getBeans(cls).iterator().next();
            CreationalContext ctx = beanManager.createCreationalContext(bean);
            return (T) beanManager.getReference(bean, cls, ctx);
        } catch (NamingException ne) {
            throw new EvoteException("Error looking up bean: " + ne, ne);
        }
    }

}
