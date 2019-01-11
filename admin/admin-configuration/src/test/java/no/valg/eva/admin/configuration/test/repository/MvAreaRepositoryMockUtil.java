package no.valg.eva.admin.configuration.test.repository;

import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;

public final class MvAreaRepositoryMockUtil {
	private MvAreaRepositoryMockUtil() {
	}

	public static void findDigestsByPathAndLevel(MvAreaRepository repository, AreaPath areaPath, AreaLevelEnum level, List<MvAreaDigest> returnValue) {
		when(repository.findDigestsByPathAndLevel(areaPath, level)).thenReturn(returnValue);
	}

	public static void findFirstDigestByPathAndLevel(MvAreaRepository repository, AreaPath areaPath, AreaLevelEnum level, MvAreaDigest returnValue) {
		when(repository.findFirstDigestByPathAndLevel(areaPath, level)).thenReturn(returnValue);
	}

	public static void singleDigestByPath(MvAreaRepository repository, AreaPath areaPath, MvAreaDigest returnValue) {
		when(repository.findSingleDigestByPath(areaPath)).thenReturn(returnValue);
	}
}
