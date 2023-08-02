package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.service.DoctorService;
import ro.felixcirebea.medicalsys.util.Validator;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> upsertDoctor(@RequestBody @Valid DoctorDto dto) {
        return ResponseEntity.ok(doctorService.upsertDoctor(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable(name = "id") String doctorId) {
        Validator.idValidator(doctorId);
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @GetMapping("/by-name")
    public ResponseEntity<DoctorDto> getDoctorByName(@RequestParam(name = "name") String doctorName) {
        return ResponseEntity.ok(doctorService.getDoctorByName(doctorName));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteDoctorById(@PathVariable(name = "id") String doctorId) {
        Validator.idValidator(doctorId);
        return ResponseEntity.ok(doctorService.deleteDoctorById(doctorId));
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<Long> deleteDoctorByName(@RequestParam(name = "name") String doctorName) {
        return ResponseEntity.ok(doctorService.deleteDoctorByName(doctorName));
    }
}