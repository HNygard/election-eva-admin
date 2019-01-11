package no.valg.eva.admin.common.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Jamon
@Interceptor
public class JamonInterceptor implements Serializable {

	@AroundInvoke
	protected Object invokeUnderTrace(final InvocationContext ctx) throws Exception {
		String name = createInvocationTraceName(ctx.getMethod());
		Monitor monitor = MonitorFactory.start(name);
		try {
			return ctx.proceed();
		} finally {
			monitor.stop();
		}
	}

	private String createInvocationTraceName(final Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getDeclaringClass().getName());
		sb.append('.').append(method.getName());
		return sb.toString();
	}

}
