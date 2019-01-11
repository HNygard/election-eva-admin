package no.valg.eva.admin.util;

public class MathUtil {

    private static final double PERCENT_FACTOR = 100;

    private MathUtil() {
    }

    public static int calculatePercentage(double part, double total) {
        return (int) Math.round((part / total) * PERCENT_FACTOR);
    }
}
