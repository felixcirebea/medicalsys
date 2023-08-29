package ro.felixcirebea.medicalsys.helper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ContributorTests {

    public static final int EXPECTED = 1;
    public static final int YEAR = 2023;
    public static final int MONTH = 1;
    public static final int DAY = 2;

    private Contributor contributor;

    @BeforeEach
    public void setUp() {
        contributor = new Contributor();
    }

    @Test
    public void testIncrementFailedDeleteOperations() {
        //Act
        contributor.incrementFailedDeleteOperations();

        //Assert
        Assertions.assertThat(contributor.getFailedDeleteOperations()).isEqualTo(EXPECTED);
    }

    @Test
    public void testIncrementNumberOfDataNotFoundExceptions() {
        //Act
        contributor.incrementNumberOfDataNotFoundExceptions();

        //Assert
        Assertions.assertThat(contributor.getNumberOfDataNotFoundExceptions()).isEqualTo(EXPECTED);
    }

    @Test
    public void testIncrementNumberOfDataMismatchExceptions() {
        //Act
        contributor.incrementNumberOfDataMismatchExceptions();

        //Assert
        Assertions.assertThat(contributor.getNumberOfDataMismatchExceptions()).isEqualTo(EXPECTED);
    }

    @Test
    public void testIncrementNumberOfConstraintViolationExceptions() {
        //Act
        contributor.incrementNumberOfConstraintViolationExceptions();

        //Assert
        Assertions.assertThat(contributor.getNumberOfConstraintViolationExceptions()).isEqualTo(EXPECTED);
    }

    @Test
    public void testIncrementNumberOfConcurrencyExceptions() {
        //Act
        contributor.incrementNumberOfConcurrencyExceptions();

        //Assert
        Assertions.assertThat(contributor.getNumberOfConcurrencyExceptions()).isEqualTo(EXPECTED);
    }

    @Test
    public void testIncrementCurrentDate() {
        //Act
        contributor.incrementCurrentDate();

        //Assert
        Assertions.assertThat(contributor.getCurrentDate().getYear()).isEqualTo(YEAR);
        Assertions.assertThat(contributor.getCurrentDate().getMonthValue()).isEqualTo(MONTH);
        Assertions.assertThat(contributor.getCurrentDate().getDayOfMonth()).isEqualTo(DAY);
    }

}
