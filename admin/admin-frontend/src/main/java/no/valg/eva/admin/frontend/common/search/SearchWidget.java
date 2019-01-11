package no.valg.eva.admin.frontend.common.search;

import lombok.NoArgsConstructor;
import org.primefaces.component.inputtext.InputText;

import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.event.AjaxBehaviorEvent;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static no.valg.eva.admin.util.ObjectPropertyUtil.resolvePropertyValues;

@FacesComponent("searchWidget")
@NoArgsConstructor
public class SearchWidget extends UINamingContainer {
    
    protected static final String ATTRIBUTE_HANDLER = "handler";
    protected static final String ATTRIBUTE_ENTITIES = "entities";

    private SearchHandler getHandler() {
        return (SearchHandler) getAttributes().get(ATTRIBUTE_HANDLER);
    }

    private List<Searchable> getEntities() {
        return (List<Searchable>) getAttributes().get(ATTRIBUTE_ENTITIES);
    }
    
    public void onSearchQueryChanged(AjaxBehaviorEvent evt) {
        final String query = getQuery(evt);
        
        if (query.isEmpty()) {
            clearSearch();
        }
        else if (canSearch()){
            getHandler().onSearchCallback(
                    filterEntitiesByQuery(query)
            );
        }
    }
    
    private String getQuery(AjaxBehaviorEvent evt) {
        final String q = (String) ((InputText) evt.getComponent()).getValue();
        return q != null ? q.trim().toLowerCase() : "";
    }
    
    public void clearSearch() {
        if (getHandler() != null) {
            getHandler().onSearchCallback(getEntities());
        }
    }

    private boolean canSearch() {
        return getEntities() != null && getEntities().size() > 0 && getHandler() != null;
    }
    
    private List<Searchable> filterEntitiesByQuery(String query) {
        return getEntities().stream()
                .filter(record -> resolvePropertyValues(record, record.getSearchableProperties()).values().stream().anyMatch(value -> {
                    if (value != null) {
                        if (value instanceof String) {
                            String theValue = (String) value;
                            return theValue.toLowerCase().startsWith(query);
                        }
                        else if (canBeInteger(query) && value instanceof Integer) {
                            return ((Integer) value) == parseInt(query);
                        }
                    }
                    return false;
                }))
                .collect(Collectors.toList());
    }

    private static boolean canBeInteger(String query) {
        try {
            parseInt(query);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
