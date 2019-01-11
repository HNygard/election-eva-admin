package no.valg.eva.admin.felles.melding;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.ERROR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class MeldingTest {

    @Test(dataProvider = "meldingTestData")
    public void testGetAlvorlighetsgrad(Melding melding) {
        Melding newMessage = getMelding(melding.getAlvorlighetsgrad(), melding.getText());
        assertEquals(newMessage.getAlvorlighetsgrad(), melding.getAlvorlighetsgrad());
    }

    @Test(dataProvider = "meldingTestData")
    public void testGetText(Melding melding) {
        Melding newMessage = getMelding(melding.getAlvorlighetsgrad(), melding.getText());
        assertEquals(newMessage.getText(), melding.getText());
    }

    @Test(dataProvider = "meldingTestData")
    public void testEquals_givenMelding_verifyEqual(Melding melding) {
        Melding newMessage = getMelding(melding.getAlvorlighetsgrad(), melding.getText());
        assertEquals(newMessage, melding);
    }

    @Test(dataProvider = "meldingTestData")
    public void testEquals_givenMeldingWithOtherAlvorlighetsgrad_verifyNotEqual(Melding melding) {
        Melding newMessage = getMelding(ERROR, melding.getText());
        assertNotEquals(newMessage, melding);
    }

    @Test(dataProvider = "meldingTestData")
    public void testEquals_givenMeldingWithOtherText_verifyNotEqual(Melding melding) {
        Melding newMessage = getMelding(melding.getAlvorlighetsgrad(), "notthesametext");
        assertNotEquals(newMessage, melding);
    }

    @Test(dataProvider = "meldingTestData")
    public void testEquals_givenMelding_verifyHashCodeEqual(Melding melding) {
        Melding newMessage = getMelding(melding.getAlvorlighetsgrad(), melding.getText());
        assertEquals(newMessage.hashCode(), melding.hashCode());
    }

    @DataProvider
    public static Object[][] meldingTestData() {
        return new Object[][]{
                {new Melding(Alvorlighetsgrad.INFO, "text")}
        };
    }

    private Melding getMelding(Alvorlighetsgrad alvorlighetsgrad, String text) {
        return new Melding(alvorlighetsgrad, text);
    }
}