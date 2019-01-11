package no.valg.eva.admin.felles.valggeografi.model;

import lombok.Getter;
import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;

@Getter
public class Bydel extends Valggeografi<BydelSti> {

    private boolean kommuneBydel;
    private String countyName;
    private String municipalityName;

    public Bydel(BydelSti sti, String navn, boolean kommuneBydel) {
        super(sti, navn);
        this.kommuneBydel = kommuneBydel;
    }

    public Bydel(BydelSti sti, String navn, boolean kommuneBydel, String countyName, String municipalityName) {
        super(sti, navn);
        this.kommuneBydel = kommuneBydel;
        this.countyName = countyName;
        this.municipalityName = municipalityName;
    }

    @Override
    public ValggeografiNivaa nivaa() {
        return ValggeografiNivaa.BYDEL;
    }

}
