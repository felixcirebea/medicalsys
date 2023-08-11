package ro.felixcirebea.medicalsys.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.AppointmentService;

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

    @GetMapping("available-hours")
    public ResponseEntity<List<LocalTime>> getAvailableHours(
            @RequestParam(name = "doctor") String doctorName,
            @RequestParam(name = "investigation") String investigation,
            @RequestParam(name = "date") LocalDate desiredDate) throws DataNotFoundException {
        return ResponseEntity.ok(appointmentService.getAvailableHours(doctorName, investigation, desiredDate));
    }

}
