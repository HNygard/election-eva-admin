package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@Builder
public class RegisterVotingInEnvelopeViewModel implements Serializable {

    @Getter
    @Setter
    private String electoralRollNumber;

    @Getter
    @Setter
    private String label;

    @Getter
    @Setter
    private String view;
}
