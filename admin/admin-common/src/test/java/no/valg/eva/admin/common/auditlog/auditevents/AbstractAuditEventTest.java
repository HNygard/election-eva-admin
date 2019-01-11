package no.valg.eva.admin.common.auditlog.auditevents;

import static no.valg.eva.admin.test.ObjectAssert.assertThat;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import no.valg.eva.admin.common.auditlog.AuditEventType;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.test.ObjectAssert;

import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractAuditEventTest extends MockUtilsTestCase {
	@Test
	public void thatAuditEventSuppliesObjectClassesStaticMethod() throws IllegalAccessException {
		Method objectClassesMethod = null;
		Class<? extends AuditEvent> eventClass = null;
		try {
			eventClass = getAuditEventClass();
			objectClassesMethod = eventClass.getDeclaredMethod("objectClasses", AuditEventType.class);
		} catch (NoSuchMethodException e) {
			Assert.fail(eventClass.getSimpleName() + " does not implement public static Class[] objectClasses(AuditEventType auditEventType)");
		}
		ObjectAssert.assertThat(objectClassesMethod.getReturnType()).isEqualTo(Class[].class);
		Assert.assertTrue(Modifier.isStatic(objectClassesMethod.getModifiers()));
		Assert.assertTrue(Modifier.isPublic(objectClassesMethod.getModifiers()));
		int numberOfSupportedEventTypes = 0;
		for (AuditEventType type : AuditEventTypes.values()) {
			try {
				Object objectClasses = objectClassesMethod.invoke(null, type);
				assertNotNull(objectClasses, eventClass.getSimpleName() + ".objectClasses(...) should not return null for event type " + type.name()
						+ " but rather throw UnsupportedOperationException");
				numberOfSupportedEventTypes++;
			} catch (InvocationTargetException e) {
				assertThat(e.getTargetException() instanceof UnsupportedOperationException);
			}
		}
		assertTrue(numberOfSupportedEventTypes > 0, eventClass.getSimpleName() + " did not support any event types");
	}

	/**
	 * @return the audit event class under test
	 */
	protected abstract Class<? extends AuditEvent> getAuditEventClass();

}
