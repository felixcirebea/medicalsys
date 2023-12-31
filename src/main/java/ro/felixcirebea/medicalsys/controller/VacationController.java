package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.VacationService;
import ro.felixcirebea.medicalsys.helper.Validator;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vacations")
public class VacationController {

    private final VacationService vacationService;

    public VacationController(VacationService vacationService) {
        this.vacationService = vacationService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> insertVacation(
            @RequestBody @Valid VacationDto vacationDto)
            throws DataNotFoundException, ConcurrencyException {
        return ResponseEntity.ok(vacationService.insertVacation(vacationDto));
    }

    @PostMapping("/update-status")
    public ResponseEntity<Long> cancelVacation(
            @RequestParam(name = "doctor") String doctorName,
            @RequestParam(name = "start-date") String startDate)
            throws DataMismatchException, DataNotFoundException, ConcurrencyException {
        return ResponseEntity.ok(vacationService.cancelVacation(doctorName, startDate));
    }

    @GetMapping("/by-doctor-and-dates")
    public ResponseEntity<List<VacationDto>> getVacationByDoctorAndDates(
            @RequestParam(name = "doctor") String doctorName,
            @RequestParam(name = "start-date", required = false) String startDate,
            @RequestParam(name = "end-date", required = false) String endDate)
            throws DataNotFoundException, DataMismatchException {
        return ResponseEntity.ok(vacationService.getVacationByDoctorAndDates(doctorName, startDate, endDate));
    }

    @GetMapping("/by-doctor-and-type")
    public ResponseEntity<List<VacationDto>> getVacationByDoctorAndType(
            @RequestParam(name = "doctor", required = false) String doctorName,
            @RequestParam(name = "type") String type)
            throws DataMismatchException, DataNotFoundException {
        VacationType vacationType = Validator.vacationTypeValidator(type);
        return ResponseEntity.ok(vacationService.getVacationByDoctorAndType(doctorName, vacationType));
    }

    @GetMapping("/is-vacation")
    public ResponseEntity<Boolean> isVacation(
            @RequestParam(name = "doctor") String doctorName,
            @RequestParam(name = "date") String date)
            throws DataMismatchException, DataNotFoundException {
        LocalDate dateValue = Validator.dateValidator(date);
        return ResponseEntity.ok(vacationService.isDateVacation(doctorName, dateValue));
    }

    @GetMapping("/by-doctor-and-status")
    public ResponseEntity<List<VacationDto>> getVacationByDoctorAndStatus(
            @RequestParam(name = "doctor") String doctorName,
            @RequestParam(name = "status") String status)
            throws DataMismatchException, DataNotFoundException {
        VacationStatus statusValue = Validator.vacationStatusValidator(status);
        return ResponseEntity.ok(vacationService.getVacationByStatus(doctorName, statusValue));
    }
}
