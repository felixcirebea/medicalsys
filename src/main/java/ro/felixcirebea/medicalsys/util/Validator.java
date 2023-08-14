package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class Validator {

    public static final String INVALID_TIME_MSG = "The given time is not valid";
    public static final String INVALID_DATE_MSG = "The given date is not valid";
    public static final String INVALID_ENUM_MSG = "The given argument is not valid enum element";
    public static final String INVALID_ID_MSG = "The given id is not a number";

    public static LocalTime timeValidator(String inputTime)
            throws DataMismatchException {
        try {
            return LocalTime.parse(inputTime);
        } catch (DateTimeParseException e) {
            throw new DataMismatchException(INVALID_TIME_MSG);
        }
    }

    public static DayOfWeek dayOfWeekValidator(Integer dayOfWeek)
            throws DataMismatchException {
        try {
            return DayOfWeek.of(dayOfWeek);
        } catch (DateTimeException e) {
            throw new DataMismatchException(INVALID_DATE_MSG);
        }
    }

    public static VacationType vacationTypeValidator(String inputType)
            throws DataMismatchException {
        try {
            return VacationType.valueOf(inputType);
        } catch (IllegalArgumentException e) {
            throw new DataMismatchException(INVALID_ENUM_MSG);
        }
    }

    public static VacationStatus vacationStatusValidator(String inputType)
            throws DataMismatchException {
        try {
            return VacationStatus.valueOf(inputType);
        } catch (IllegalArgumentException e) {
            throw new DataMismatchException(INVALID_ENUM_MSG);
        }
    }

    public static LocalDate dateValidator(String inputDate)
            throws DataMismatchException {
        try {
            return LocalDate.parse(inputDate);
        } catch (DateTimeParseException e) {
            throw new DataMismatchException(INVALID_DATE_MSG);
        }
    }

    public static Long idValidator(String inputId)
            throws DataMismatchException {
        try {
            return Long.valueOf(inputId);
        } catch (NumberFormatException | NullPointerException e) {
            throw new DataMismatchException(INVALID_ID_MSG);
        }
    }

}
