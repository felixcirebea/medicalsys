package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.VacationService;
import ro.felixcirebea.medicalsys.util.Validator;

import java.util.List;

@RestController
@RequestMapping("/vacations")
public class VacationController {

    private final VacationService vacationService;

    public VacationController(VacationService vacationService) {
        this.vacationService = vacationService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> insertVacation(@RequestBody @Valid VacationDto vacationDto)
            throws DataMismatchException, DataNotFoundException {
        return ResponseEntity.ok(vacationService.upsertVacation(vacationDto));
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
            @RequestParam(name = "type") String type) throws DataMismatchException, DataNotFoundException {
        VacationType vacationType = Validator.enumValidator(type);
        return ResponseEntity.ok(vacationService.getVacationByDoctorAndType(doctorName, vacationType));
    }

}
