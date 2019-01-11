package no.valg.eva.admin.frontend.common.search;

import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class SearchWidgetTestClass implements Searchable {
    
    private String name; 
    private int age;
    private String nonSearchableAddress;
    
    @Override
    public List<String> getSearchableProperties() {
        return Arrays.asList("name", "age");
    }
}
