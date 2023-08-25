package ro.felixcirebea.medicalsys.helper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class ValidatorTests {

    @Test
    public void testTimeValidator_whenInputValid_thenReturnLocalTime() throws DataMismatchException {
        //Arrange
        String input = "08:00";

        //Act
        LocalTime returnValue = Validator.timeValidator(input);

        //Assert
        Assertions.assertThat(returnValue.getHour()).isEqualTo(8);
        Assertions.assertThat(returnValue.getMinute()).isEqualTo(0);
    }

    @Test
    public void testTimeValidator_whenInputInvalid_thenThrowException() {
        //Arrange
        String input = "08.00";

        //Act && assert
        Assertions.assertThatThrownBy(() -> Validator.timeValidator(input))
                .isInstanceOf(DataMismatchException.class)
                .hasMessage("The given time is not valid");
    }

    @Test
    public void testDayOfWeekValidator_whenInputValid_thenReturnDayOfWeek() throws DataMismatchException {
        //Arrange
        Integer input = 1;

        //Act
        DayOfWeek returnValue = Validator.dayOfWeekValidator(input);

        //Assert
        Assertions.assertThat(returnValue.getValue()).isEqualTo(1);
        Assertions.assertThat(returnValue).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    public void testDayOfWeekValidator_whenInputInvalid_thenThrowException() {
        //Arrange
        Integer input = 99;

        //Act && assert
        Assertions.assertThatThrownBy(() -> Validator.dayOfWeekValidator(input))
                .isInstanceOf(DataMismatchException.class)
                .hasMessage("The given date is not valid");
    }

    @Test
    public void testVacationTypeValidator_whenInputValid_thenReturnVacationType() throws DataMismatchException {
        //Arrange
        String input = "SICK_LEAVE";

        //Act
        VacationType returnValue = Validator.vacationTypeValidator(input);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(VacationType.SICK_LEAVE);
    }

    @Test
    public void testVacationTypeValidator_whenInputInvalid_thenThrowException() {
        //Arrange
        String input = "sick_leave";

        //Act && assert
        Assertions.assertThatThrownBy(() -> Validator.vacationTypeValidator(input))
                .isInstanceOf(DataMismatchException.class)
                .hasMessage("The given argument is not valid enum element");
    }

    @Test
    public void testVacationStatusValidator_whenInputValid_thenReturnVacationStatus() throws DataMismatchException {
        //Arrange
        String input = "IN_PROGRESS";

        //Act
        VacationStatus returnValue = Validator.vacationStatusValidator(input);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(VacationStatus.IN_PROGRESS);
    }

    @Test
    public void testVacationStatusValidator_whenInputInvalid_thenThrowException() {
        //Arrange
        String input = "in_progress";

        //Act && assert
        Assertions.assertThatThrownBy(() -> Validator.vacationStatusValidator(input))
                .isInstanceOf(DataMismatchException.class)
                .hasMessage("The given argument is not valid enum element");
    }

    @Test
    public void testDateValidator_whenInputValid_thenReturnLocalDate() throws DataMismatchException {
        //Arrange
        String input = "2023-01-01";

        //Act
        LocalDate returnValue = Validator.dateValidator(input);

        //Assert
        Assertions.assertThat(returnValue.getYear()).isEqualTo(2023);
        Assertions.assertThat(returnValue.getMonthValue()).isEqualTo(1);
        Assertions.assertThat(returnValue.getDayOfMonth()).isEqualTo(1);
    }

    @Test
    public void testDateValidator_whenInputInvalid_thenThrowException() {
        //Arrange
        String input = "2023/01/01";

        //Act && assert
        Assertions.assertThatThrownBy(() -> Validator.dateValidator(input))
                .isInstanceOf(DataMismatchException.class)
                .hasMessage("The given date is not valid");
    }

    @Test
    public void testIdValidator_whenInputValid_thenReturnLong() throws DataMismatchException {
        //Arrange
        String input = "10";

        //Act
        Long returnValue = Validator.idValidator(input);

        //Assert
        Assertions.assertThat(returnValue).isEqualTo(10);
    }

    @Test
    public void testIdValidator_whenInputInvalid_thenThrowException() {
        //Arrange
        String input = "test";

        //Act && assert
        Assertions.assertThatThrownBy(() -> Validator.idValidator(input))
                .isInstanceOf(DataMismatchException.class)
                .hasMessage("The given id is not a number");
    }
}
