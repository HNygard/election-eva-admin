package no.valg.eva.admin.frontend.voting.confirmation;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.frontend.util.CookieStore;
import no.valg.eva.admin.frontend.util.CookieStoreTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.frontend.voting.confirmation.VotingConfirmationOptionalTableColumn.DATE;
import static no.valg.eva.admin.frontend.voting.confirmation.VotingConfirmationOptionalTableColumn.TIME;
import static no.valg.eva.admin.frontend.voting.confirmation.VotingConfirmationOptionalTableColumn.VOTER_LISTED_IN;
import static no.valg.eva.admin.frontend.voting.confirmation.VotingConfirmationOptionalTableColumn.VOTING_NUMBER;
import static no.valg.eva.admin.frontend.voting.confirmation.VotingConfirmationOptionalTableColumnManager.COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class VotingConfirmationOptionalTableColumnManagerTest extends BaseFrontendTest {
    
    private VotingConfirmationOptionalTableColumnManager manager;
    
    private List<Cookie> cookies;

    @BeforeMethod
    public void setUp() {
        initializeMocks();
        CookieStoreTest cst = new CookieStoreTest();
        cst.setCookieStoreTestContext();
        cookies = cst.getInMemoryCookieStore();
        manager = new VotingConfirmationOptionalTableColumnManager();
        manager.postConstruct();
    }

    @Test
    public void noTableColumnCookieReturnsDefaultColumns() {
        assertNoTableColumnCookie();
        assertEquals(manager.getSelectedFromCookieOrDefaultColumns(), defaultColumns());
    }
    
    private void assertNoTableColumnCookie() {
        assertNull(CookieStore.getCookie(COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN));
    }
    
    private List<VotingConfirmationOptionalTableColumn> defaultColumns() {
        return asList(VOTING_NUMBER);
    }

    @Test
    public void persistAndRetrieveColumnsShouldMatch() {

        List<VotingConfirmationOptionalTableColumn> expectedColumns = asList(DATE, TIME, VOTER_LISTED_IN);
        
        manager.persistSelectedColumns(expectedColumns);
        assertTableColumnCookie();
        assertPersistedColumnsMatch(expectedColumns);
    }
    
    private void assertTableColumnCookie() {
        assertNotNull(CookieStore.getCookie(COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN));
    }
    
    private void assertPersistedColumnsMatch(List<VotingConfirmationOptionalTableColumn> expectedColumns) {
        final AtomicInteger ix = new AtomicInteger();
        manager.getSelectedFromCookieOrDefaultColumns().forEach(actual -> {
            VotingConfirmationOptionalTableColumn expected = expectedColumns.get(ix.getAndIncrement());
            assertEquals(actual.getId(), expected.getId());
        });
    }

    @Test
    public void multiplePersistShouldReplaceNotAdd() {
        List<VotingConfirmationOptionalTableColumn> expectedColumns = asList(
                DATE,
                TIME
        );
        manager.persistSelectedColumns(expectedColumns);
        assertPersistedColumnsMatch(expectedColumns);
        
        
        expectedColumns = asList(
                VOTER_LISTED_IN
        );
        manager.persistSelectedColumns(expectedColumns);
        assertPersistedColumnsMatch(expectedColumns);
        
        
        expectedColumns = asList(
                TIME,
                VOTING_NUMBER,
                VOTER_LISTED_IN
        );
        manager.persistSelectedColumns(expectedColumns);
        assertPersistedColumnsMatch(expectedColumns);
    }

    @Test
    public void invalidTableColumnReturnsDefaultColumns() {
        cookies.add(new Cookie(COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN, "[TIME,HORSES,VOTER_LISTED_IN]"));
        assertPersistedColumnsMatch(defaultColumns());
    }

    @Test
    public void invalidJsonSyntaxReturnsDefaultColumns() {
        cookies.add(new Cookie(COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN, "[TIME,HORSES,VOT"));
        assertPersistedColumnsMatch(defaultColumns());
    }

    @Test
    public void invalidTableColumnCookieCanBeOverwritten() {
        cookies.add(new Cookie(COOKIE_VOTING_CONFIRMATION_TABLE_COLUMN, "[TIME,HORSES,VOT"));
        assertPersistedColumnsMatch(defaultColumns());

        manager.persistSelectedColumns(asList(VOTING_NUMBER));
        assertPersistedColumnsMatch(asList(VOTING_NUMBER));
    }

    @Test(dataProvider = "onSelectedOptionalTableColumnsTestData")
    public void testOnSelectedOptionalTableColumnsChanged(List<VotingConfirmationOptionalTableColumn> selectedColumns) throws NoSuchFieldException, IllegalAccessException {

        VotingConfirmationOptionalTableColumnManager managerMock = mock(VotingConfirmationOptionalTableColumnManager.class);
        when(managerMock.getSelectedOptionalTableColumns()).thenReturn(selectedColumns);

        doCallRealMethod().when(managerMock).onSelectedOptionalTableColumnsChanged();

        managerMock.onSelectedOptionalTableColumnsChanged();

        verify(managerMock).persistSelectedColumns(selectedColumns);
    }

    @DataProvider
    public Object[][] onSelectedOptionalTableColumnsTestData() {
        return new Object[][]{
                {selectedColumns(VOTING_NUMBER)},
                {selectedColumns(VOTING_NUMBER, DATE)},
                {selectedColumns()}
        };
    }

    private List<VotingConfirmationOptionalTableColumn> selectedColumns(VotingConfirmationOptionalTableColumn... columns) {
        return asList(columns);
    }

    @Test(dataProvider = "shouldRenderOptionalColumnTestData")
    public void testShouldRenderOptionalColumn(List<VotingConfirmationOptionalTableColumn> selectedColumns, String columnId, boolean expectsRendered) {
        manager.setSelectedOptionalTableColumns(selectedColumns);
        assertEquals(manager.shouldRenderOptionalColumn(columnId), expectsRendered);
    }

    @DataProvider
    public Object[][] shouldRenderOptionalColumnTestData() {

        return new Object[][]{
                {selectedColumns(VOTING_NUMBER, DATE), VOTING_NUMBER.name(), true},
                {selectedColumns(), VOTING_NUMBER.name(), false},
                {selectedColumns(VOTING_NUMBER, DATE), TIME.name(), false},
                {selectedColumns(VOTING_NUMBER, DATE, TIME), TIME.name(), true},
        };
    }
}


