package no.valg.eva.admin.frontend.common.search;

import no.valg.eva.admin.test.MockUtilsTestCase;
import org.primefaces.component.inputtext.InputText;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.component.UIComponent;
import javax.faces.event.AjaxBehaviorEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class SearchWidgetTest extends MockUtilsTestCase {

    private SearchWidget widget;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        this.widget = initializeMocks(SearchWidget.class);
    }

    @Test(dataProvider = "dataProviderDefault")
    public void searchWidgetTest(String query, List<Searchable> searchables, SearchHandler handler) {
        givenEntities(searchables);
        withSearchHandler(handler);
        thenSearchingWithQueryShouldAssertHandler(query);
    }
    
    private void givenEntities(List<Searchable> entities) {
        widget.getAttributes().put(SearchWidget.ATTRIBUTE_ENTITIES, entities);
    }
    
    private void withSearchHandler(SearchHandler handler) {
        widget.getAttributes().put(SearchWidget.ATTRIBUTE_HANDLER, handler);
    }
    
    private void thenSearchingWithQueryShouldAssertHandler(String query) {
        AjaxBehaviorEvent searchEventMock = createMock(AjaxBehaviorEvent.class);
        UIComponent inputText = createMock(InputText.class);
        when(((InputText) inputText).getValue()).thenReturn(query);
        when(searchEventMock.getComponent()).thenReturn(inputText);
        widget.onSearchQueryChanged(searchEventMock);
    }

    
    @DataProvider
    private Object[][] dataProviderDefault() {

        List<Searchable> allEntities = asList(
                SearchWidgetTestClass.builder()
                        .name("Per")
                        .age(32)
                        .nonSearchableAddress("Utti skogen og lengre til")
                        .build(),
                SearchWidgetTestClass.builder()
                        .name("Pål")
                        .age(33)
                        .build(),
                SearchWidgetTestClass.builder()
                        .name("Espen Askeladd")
                        .age(28)
                        .nonSearchableAddress("Der grasrota gror")
                        .build()
        );
        
        return new Object[][] {
                {"", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities, allEntities);
                }},
                {"pål", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities.size(), 1);
                    assertEquals(filteredEntities.get(0), allEntities.get(1));
                }},
                {"Pål", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities.size(), 1);
                    assertEquals(filteredEntities.get(0), allEntities.get(1));
                }},
                {"PÅL", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities.size(), 1);
                    assertEquals(filteredEntities.get(0), allEntities.get(1));
                }},
                {"pÅl", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities.size(), 1);
                    assertEquals(filteredEntities.get(0), allEntities.get(1));
                }},
                {"espen as", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities.size(), 1);
                    assertEquals(filteredEntities.get(0), allEntities.get(2));
                }},
                {"askeladd", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities.size(), 0);
                }},
                {"p", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities.size(), 2);
                    assertEquals(filteredEntities.get(0), allEntities.get(0));
                    assertEquals(filteredEntities.get(1), allEntities.get(1));
                }},
                {"33", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    assertEquals(filteredEntities.size(), 1);
                    assertEquals(filteredEntities.get(0), allEntities.get(1));
                }},
                {"per", emptyList(), (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    throw new IllegalArgumentException("This should not happen as entity list is empty!");
                }},
                {"Der grasrota", allEntities, (SearchHandler<SearchWidgetTestClass>) filteredEntities -> {
                    // Should not search on non-searchable class property
                    assertEquals(filteredEntities.size(), 0);
                }},
        };
    }
}