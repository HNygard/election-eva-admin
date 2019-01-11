package no.evote.validation;

import java.io.Serializable;

abstract class ValidatorBase implements Serializable {
    protected String extraChars;

    protected boolean isValid(final String value) {
        if (value == null || value.length() == 0) {
            return true;
        }

        for (int i = 0; i < value.length(); i++) {
            Character currentChar = value.charAt(i);

            if (Character.isLetterOrDigit(currentChar) || extraChars.indexOf(currentChar) > -1) {
                continue;
            }
            return false;
        }

        return true;
    }
}
