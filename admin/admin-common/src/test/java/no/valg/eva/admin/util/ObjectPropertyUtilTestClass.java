package no.valg.eva.admin.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class ObjectPropertyUtilTestClass {
    
    @Getter
    private String name;

    @Getter
    private int age;

    @Getter
    private Integer strength;

    @Getter
    private long height;
    
    @Getter
    private Long length;

    @Getter
    private boolean doesExercise;

    @Getter
    private Boolean shouldExercise;

    @Getter
    private String[] currentHobbies;

    @Getter
    private List<String> hobbiesForTheFuture;
    
    private Map<String, Integer> secretWithNumberMap;
}
