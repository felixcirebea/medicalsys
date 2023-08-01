package ro.felixcirebea.medicalsys.util;

public class Validator {

    public static void idValidator(String inputId) {
        Long id;
        try {
            id = Long.valueOf(inputId);
        } catch (NumberFormatException e) {
            throw new RuntimeException("The given id is not a number");
        }
    }

}
