package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.util.Contributor;

@ControllerAdvice
@Slf4j
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    public static final String UNIQUE_CONSTRAINT_VIOLATION = "Unique constraint violation";
    public static final String DATA_NOT_FOUND_EXCEPTION = "DataNotFoundException";
    public static final String DATA_MISMATCH_EXCEPTION = "DataMismatchException";
    public static final String CONCURRENCY_EXCEPTION = "ConcurrencyException";
    public static final String UNKNOWN_VALIDATION_ERROR = "Unknown validation error.";
    public static final String VALIDATION_FAILED = "Validation failed: ";
    private final Contributor infoContributor;

    public ControllerAdvisor(Contributor infoContributor) {
        this.infoContributor = infoContributor;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            Exception ex,
            WebRequest webRequest) {

        log.error(ex.getMessage());
        infoContributor.incrementNumberOfConstraintViolationExceptions();

        return handleExceptionInternal(
                ex, UNIQUE_CONSTRAINT_VIOLATION,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }

    @ExceptionHandler({
            DataNotFoundException.class,
            DataMismatchException.class,
            ConcurrencyException.class
    })
    public ResponseEntity<Object> handleDataExceptions(
            Exception ex,
            WebRequest webRequest) {

        log.error(ex.getMessage());
        switch (ex.getClass().getSimpleName()) {
            case DATA_NOT_FOUND_EXCEPTION -> infoContributor.incrementNumberOfDataNotFoundExceptions();
            case DATA_MISMATCH_EXCEPTION -> infoContributor.incrementNumberOfDataMismatchExceptions();
            case CONCURRENCY_EXCEPTION -> infoContributor.incrementNumberOfConcurrencyExceptions();
        }

        return handleExceptionInternal(
                ex, ex.getMessage(), new HttpHeaders(),
                HttpStatus.BAD_REQUEST, webRequest);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {

        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder errorMessage = new StringBuilder(VALIDATION_FAILED);

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(err ->
                    errorMessage.append(err.getDefaultMessage()).append("; "));
        } else {
            errorMessage.append(UNKNOWN_VALIDATION_ERROR);
        }
        log.error(errorMessage + ex.getMessage());

        return handleExceptionInternal(
                ex, errorMessage.toString(),
                headers, status, request);
    }
}
