package ro.felixcirebea.medicalsys.controller;

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
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.util.Contributor;

@ControllerAdvice
@Slf4j
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    private final Contributor infoContributor;

    public ControllerAdvisor(Contributor infoContributor) {
        this.infoContributor = infoContributor;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(Exception ex, WebRequest webRequest) {
        String responseBody = "Unique constraint violation";
        log.error(ex.getMessage());
        infoContributor.incrementNumberOfConstraintViolationExceptions();
        return handleExceptionInternal(ex, responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }

    @ExceptionHandler({DataNotFoundException.class, DataMismatchException.class})
    public ResponseEntity<Object> handleDataExceptions(Exception ex, WebRequest webRequest) {
        log.error(ex.getMessage());
        switch (ex.getClass().getSimpleName()) {
            case "DataNotFoundException" -> infoContributor.incrementNumberOfDataNotFoundExceptions();
            case "DataMismatchException" -> infoContributor.incrementNumberOfDataMismatchExceptions();
        }
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }

//    TODO check why UpsertInvestigation-Insert with blank duration field doesn't throw the expected exception

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(err -> errorMessage.append(err.getDefaultMessage()).append("; "));
        } else {
            errorMessage.append("Unknown validation error.");
        }
        log.error(errorMessage + ex.getMessage());
        return handleExceptionInternal(ex, errorMessage.toString(), headers, status, request);
    }
}
