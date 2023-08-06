package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.exception.DataMismatchException;

public class Validator {

    public static Long idValidator(String inputId) throws DataMismatchException {
        try {
            return Long.valueOf(inputId);
        } catch (NumberFormatException e) {
            throw new DataMismatchException("The given id is not a number");
        }
    }

}
