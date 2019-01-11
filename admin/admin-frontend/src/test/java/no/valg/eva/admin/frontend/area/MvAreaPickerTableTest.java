package no.valg.eva.admin.frontend.area;

import java.util.ArrayList;
import java.util.Arrays;

import no.evote.presentation.filter.MvAreaFilter;
import no.valg.eva.admin.configuration.domain.model.MvArea;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class MvAreaPickerTableTest {

	public static final int ANY_LEVEL = 1;

	@Mock
	private MvAreaFilter mvAreaFilter;

	private MvAreaPickerTable mvAreaPickerTable;

	@BeforeSuite
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void setUp() {
		mvAreaPickerTable = new MvAreaPickerTable(Arrays.asList(mvAreaFilter));
		mvAreaPickerTable.setLevel(ANY_LEVEL);
		Mockito.reset(mvAreaFilter);
	}

	@Test
	public void noFilteringIsDoneIfMvAreasIsNull() {
		mvAreaPickerTable.setMvAreas(null);
		Mockito.verify(mvAreaFilter, Mockito.never()).filter(Matchers.anyList(), Matchers.eq(ANY_LEVEL));
	}

	@Test
	public void mvAreasPropertyIsNullIfMvAreasIsSetToNull() {
		mvAreaPickerTable.setMvAreas(new ArrayList<MvArea>());
		Assert.assertNotNull(mvAreaPickerTable.getMvAreas());
		mvAreaPickerTable.setMvAreas(null);
		Assert.assertNull(mvAreaPickerTable.getMvAreas());
	}
}
