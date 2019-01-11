package no.valg.eva.admin.frontend.common;

import no.valg.eva.admin.felles.melding.Alvorlighetsgrad;
import no.valg.eva.admin.felles.melding.Melding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.ERROR;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.INFO;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.WARN;

public class MeldingerWidget extends ArrayList<Melding> {

    private static final long serialVersionUID = 7955973023474178661L;

    public MeldingerWidget filter(Alvorlighetsgrad alvorlighetsgrad) {
        return this.stream().filter(melding -> melding.getAlvorlighetsgrad() == alvorlighetsgrad).collect(Collectors.toCollection(MeldingerWidget::new));

    }

    public MeldingerWidget getErrors() {
        return filter(ERROR);
    }

    public MeldingerWidget getWarnings() {
        return filter(WARN);
    }

    public MeldingerWidget getInfos() {
        return filter(INFO);
    }

    public MeldingerWidget() {
    }

    MeldingerWidget(List<Melding> meldinger) {
        init(meldinger);
    }

    private void init(List<Melding> meldinger) {
        addAll(meldinger);
    }

    @Override
    public boolean add(Melding melding) {
        boolean result = super.add(melding);
        sorter();
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends Melding> c) {
        boolean result = super.addAll(c);
        sorter();
        return result;
    }

    private void sorter() {
        sort(comparing(Melding::getAlvorlighetsgrad));
    }
}
