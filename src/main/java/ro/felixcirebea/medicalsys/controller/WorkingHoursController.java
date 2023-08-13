package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.WorkingHoursDto;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.WorkingHoursService;

import java.util.List;

@RestController
@RequestMapping("/working-hours")
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    public WorkingHoursController(WorkingHoursService workingHoursService) {
        this.workingHoursService = workingHoursService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> upsertWorkingHours(
            @RequestBody @Valid WorkingHoursDto workingHoursDto)
            throws DataNotFoundException {
        return ResponseEntity.ok(workingHoursService.upsertWorkingHours(workingHoursDto));
    }

    @GetMapping("/by-doctor-and-day")
    public ResponseEntity<List<WorkingHoursDto>> getWorkingHoursByDoctorAndDay(
            @RequestParam(name = "doctor", required = false) String doctorName,
            @RequestParam(name = "day", required = false) Integer dayOfWeek)
            throws DataNotFoundException, DataMismatchException {
        return ResponseEntity.ok(workingHoursService.getWorkingHoursByDoctorAndDay(doctorName, dayOfWeek));
    }

    @DeleteMapping("/by-doctor-and-day")
    public ResponseEntity<Long> deleteWorkingHoursByDoctorAndDay(
            @RequestParam(name = "doctor") String doctorName,
            @RequestParam(name = "day", required = false) Integer dayOfWeek)
            throws DataNotFoundException, DataMismatchException {
        return ResponseEntity.ok(workingHoursService.deleteWorkingHoursByDoctorAndDay(doctorName, dayOfWeek));
    }
}
