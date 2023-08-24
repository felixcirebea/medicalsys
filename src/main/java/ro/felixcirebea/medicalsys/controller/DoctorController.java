package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.DoctorService;
import ro.felixcirebea.medicalsys.helper.Validator;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> upsertDoctor(
            @RequestBody @Valid DoctorDto doctorDto)
            throws DataNotFoundException {
        return ResponseEntity.ok(doctorService.upsertDoctor(doctorDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctorById(
            @PathVariable(name = "id") String doctorId)
            throws DataNotFoundException, DataMismatchException {
        Long idValue = Validator.idValidator(doctorId);
        return ResponseEntity.ok(doctorService.getDoctorById(idValue));
    }

    @GetMapping("/by-name")
    public ResponseEntity<DoctorDto> getDoctorByName(
            @RequestParam(name = "name") String doctorName)
            throws DataNotFoundException {
        return ResponseEntity.ok(doctorService.getDoctorByName(doctorName));
    }

    @GetMapping("/by-specialty")
    public ResponseEntity<List<DoctorDto>> getDoctorsBySpecialty(
            @RequestParam(name = "specialty") String specialtyName)
            throws DataNotFoundException {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialty(specialtyName));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteDoctorById(
            @PathVariable(name = "id") String doctorId)
            throws DataMismatchException {
        Long idValue = Validator.idValidator(doctorId);
        return ResponseEntity.ok(doctorService.deleteDoctorById(idValue));
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<Long> deleteDoctorByName(
            @RequestParam(name = "name") String doctorName)
            throws DataNotFoundException {
        return ResponseEntity.ok(doctorService.deleteDoctorByName(doctorName));
    }
}
