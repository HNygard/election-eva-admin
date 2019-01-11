package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ResponsibilityConflict implements Serializable {
    
    private ResponsibilityConflictType type;
    private List<String> messageArguments = new ArrayList<>();

    public ResponsibilityConflict(ResponsibilityConflictType type, String... args) {
        this.type = type;
        messageArguments.addAll(Arrays.asList(args));
    }
    
}
