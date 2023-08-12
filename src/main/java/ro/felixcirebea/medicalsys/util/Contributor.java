package ro.felixcirebea.medicalsys.util;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Contributor implements InfoContributor {

    private Integer failedDeleteOperations = 0;
    private Integer numberOfDataNotFoundExceptions = 0;
    private Integer numberOfDataMismatchExceptions = 0;
    private Integer numberOfConstraintViolationExceptions = 0;
    private Integer numberOfConcurrencyExceptions = 0;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("operations-monitor",
                Map.of("failed-delete-operations", String.valueOf(failedDeleteOperations),
                        "data-not-found-exceptions", String.valueOf(numberOfDataNotFoundExceptions),
                        "data-mismatch-exceptions", String.valueOf(numberOfDataMismatchExceptions),
                        "constraint-violation-exceptions", String.valueOf(numberOfConstraintViolationExceptions),
                        "concurrency-exceptions", String.valueOf(numberOfConcurrencyExceptions))
        );
    }

    public void incrementFailedDeleteOperations() {
        this.failedDeleteOperations++;
    }
    public void incrementNumberOfDataNotFoundExceptions() {
        this.numberOfDataNotFoundExceptions++;
    }
    public void incrementNumberOfDataMismatchExceptions() {
        this.numberOfDataMismatchExceptions++;
    }
    public void incrementNumberOfConstraintViolationExceptions() {
        this.numberOfConstraintViolationExceptions++;
    }
    public void incrementNumberOfConcurrencyExceptions() {
        this.numberOfConcurrencyExceptions++;
    }
}
