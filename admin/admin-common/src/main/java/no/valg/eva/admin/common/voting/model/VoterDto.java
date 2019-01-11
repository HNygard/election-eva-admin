package no.valg.eva.admin.common.voting.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.valg.eva.admin.configuration.domain.model.MvArea;

import java.io.Serializable;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class VoterDto implements Serializable {

    private static final long serialVersionUID = 5595019790580592238L;

    @EqualsAndHashCode.Include
    private String id;
    private Long number;
    private String nameLine;
    private String firstName;
    private String middleName;
    private String lastName;
    private MvArea mvArea;
    private boolean approved = false;
    private boolean fictitious = false;
    private boolean eligible;
}
