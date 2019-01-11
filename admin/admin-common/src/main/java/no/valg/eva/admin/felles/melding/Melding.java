package no.valg.eva.admin.felles.melding;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Getter
public class Melding implements Serializable {

    private static final long serialVersionUID = -4806607916394580551L;

    private Alvorlighetsgrad alvorlighetsgrad;

    private String text;

    public Melding(Alvorlighetsgrad alvorlighetsgrad, String text) {
        this.alvorlighetsgrad = alvorlighetsgrad;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Melding melding1 = (Melding) o;

        if (alvorlighetsgrad != melding1.alvorlighetsgrad) return false;
        return text.equals(melding1.text);
    }

    @Override
    public int hashCode() {
        int result = alvorlighetsgrad.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
}
