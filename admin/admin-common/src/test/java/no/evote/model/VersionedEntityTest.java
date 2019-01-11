package no.evote.model;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.VersionedObject;

import org.testng.annotations.Test;

public class VersionedEntityTest {

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@database.error.stale_object")
	public void checkVersion_withInvalidVersion_throwsException() throws Exception {
		VersionedEntity entity = versionedEntity();
		entity.setAuditOplock(1);
		VersionedObject object = versionedObject(0);

		entity.checkVersion(object);
	}

	private VersionedEntity versionedEntity() {
		return new VersionedEntity() {
		};
	}

	private VersionedObject versionedObject(int version) {
		return new VersionedObject(version) {
		};
	}
}
