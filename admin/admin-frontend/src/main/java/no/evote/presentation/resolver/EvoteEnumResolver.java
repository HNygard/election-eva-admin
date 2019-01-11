package no.evote.presentation.resolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import no.evote.presentation.cache.EnumCache;

public class EvoteEnumResolver extends ELResolver {

	@Override
	public Object getValue(final ELContext context, final Object base, final Object property) {
		Object result = null;
		if (base == null) {
			String prop = property != null ? property.toString() : "";
			result = EnumCache.instance().getClassForKey(prop);
		} else if (base instanceof Class) {
			result = EnumCache.instance().getValueForKey(((Class<?>) base).getSimpleName() + "." + property);
		}
		if (result != null) {
			context.setPropertyResolved(true);
		}
		return result;
	}

	@Override
	public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
		return null;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context, final Object base) {
		return null;
	}

	@Override
	public Class<?> getType(final ELContext context, final Object base, final Object property) {
		return null;
	}

	@Override
	public boolean isReadOnly(final ELContext context, final Object base, final Object property) {
		return false;
	}

	@Override
	public void setValue(final ELContext context, final Object base, final Object property, final Object arg3) {
		// empty
	}
}
