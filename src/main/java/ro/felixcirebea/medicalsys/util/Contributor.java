package ro.felixcirebea.medicalsys.util;

import lombok.Getter;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
@Getter
public class Contributor implements InfoContributor {

    private LocalDate currentDate = LocalDate.of(2023, 1, 1);
    private Integer failedDeleteOperations = 0;
    private Integer numberOfDataNotFoundExceptions = 0;
    private Integer numberOfDataMismatchExceptions = 0;
    private Integer numberOfConstraintViolationExceptions = 0;
    private Integer numberOfConcurrencyExceptions = 0;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> generalInfoMap = Map.of("current-date", currentDate);
        Map<String, Object> operationsMonitorMap = Map.of(
                "failed-delete-operations",
                String.valueOf(failedDeleteOperations),
                "data-not-found-exceptions",
                String.valueOf(numberOfDataNotFoundExceptions),
                "data-mismatch-exceptions",
                String.valueOf(numberOfDataMismatchExceptions),
                "constraint-violation-exceptions",
                String.valueOf(numberOfConstraintViolationExceptions),
                "concurrency-exceptions",
                String.valueOf(numberOfConcurrencyExceptions));

        Map<String, Map<String, Object>> builderMap = Map.of(
                "general-information", generalInfoMap,
                "operations-monitor", operationsMonitorMap);

        builder.withDetails(Map.of("app-health", builderMap));
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

    public void incrementCurrentDate() {
        this.currentDate = currentDate.plusDays(1);
    }
}

