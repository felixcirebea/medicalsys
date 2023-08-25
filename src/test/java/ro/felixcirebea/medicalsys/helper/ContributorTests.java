package ro.felixcirebea.medicalsys.helper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ContributorTests {

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
        Assertions.assertThat(contributor.getFailedDeleteOperations()).isEqualTo(1);
    }

    @Test
    public void testIncrementNumberOfDataNotFoundExceptions() {
        //Act
        contributor.incrementNumberOfDataNotFoundExceptions();

        //Assert
        Assertions.assertThat(contributor.getNumberOfDataNotFoundExceptions()).isEqualTo(1);
    }

    @Test
    public void testIncrementNumberOfDataMismatchExceptions() {
        //Act
        contributor.incrementNumberOfDataMismatchExceptions();

        //Assert
        Assertions.assertThat(contributor.getNumberOfDataMismatchExceptions()).isEqualTo(1);
    }

    @Test
    public void testIncrementNumberOfConstraintViolationExceptions() {
        //Act
        contributor.incrementNumberOfConstraintViolationExceptions();

        //Assert
        Assertions.assertThat(contributor.getNumberOfConstraintViolationExceptions()).isEqualTo(1);
    }

    @Test
    public void testIncrementNumberOfConcurrencyExceptions() {
        //Act
        contributor.incrementNumberOfConcurrencyExceptions();

        //Assert
        Assertions.assertThat(contributor.getNumberOfConcurrencyExceptions()).isEqualTo(1);
    }

    @Test
    public void testIncrementCurrentDate() {
        //Act
        contributor.incrementCurrentDate();

        //Assert
        Assertions.assertThat(contributor.getCurrentDate().getYear()).isEqualTo(2023);
        Assertions.assertThat(contributor.getCurrentDate().getMonthValue()).isEqualTo(1);
        Assertions.assertThat(contributor.getCurrentDate().getDayOfMonth()).isEqualTo(2);
    }

}
