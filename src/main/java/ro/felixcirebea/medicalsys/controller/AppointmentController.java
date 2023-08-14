package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.AppointmentDto;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.AppointmentService;
import ro.felixcirebea.medicalsys.util.Validator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/available-hours")
    public ResponseEntity<List<LocalTime>> getAvailableHours(
            @RequestParam(name = "doctor") String doctorName,
            @RequestParam(name = "investigation") String investigation,
            @RequestParam(name = "date") LocalDate desiredDate)
            throws DataNotFoundException, ConcurrencyException {
        return ResponseEntity.ok(
                appointmentService.getAvailableHours(doctorName, investigation, desiredDate));
    }

    @PostMapping("/book")
    public ResponseEntity<Long> bookAppointment(
            @RequestBody @Valid AppointmentDto appointmentDto)
            throws DataNotFoundException, ConcurrencyException {
        return ResponseEntity.ok(appointmentService.bookAppointment(appointmentDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getAppointmentById(
            @PathVariable(name = "id") String appointmentId)
            throws DataMismatchException, DataNotFoundException {
        Long appointmentIdValue = Validator.idValidator(appointmentId);
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentIdValue));
    }

    @PostMapping("/cancel-book")
    public ResponseEntity<String> deleteAppointmentByIdAndClientName(
            @RequestParam(name = "id") Long id,
            @RequestParam(name = "clientName") String clientName)
            throws DataNotFoundException {
        return ResponseEntity.ok(
                appointmentService.cancelAppointmentByIdAndName(id, clientName));
    }


}
