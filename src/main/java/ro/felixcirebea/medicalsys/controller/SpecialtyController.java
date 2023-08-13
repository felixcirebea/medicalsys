package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.SpecialtyService;
import ro.felixcirebea.medicalsys.util.Validator;

import java.util.List;

@RestController
@RequestMapping("/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> upsertSpecialty(
            @RequestBody @Valid SpecialtyDto specialtyDto)
            throws DataNotFoundException {
        return ResponseEntity.ok(specialtyService.upsertSpecialty(specialtyDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getSpecialtyById(
            @PathVariable(name = "id") String specialtyId)
            throws DataNotFoundException, DataMismatchException {
        Long idValue = Validator.idValidator(specialtyId);
        return ResponseEntity.ok(specialtyService.getSpecialtyById(idValue));
    }

    @GetMapping("/get")
    public ResponseEntity<String> getSpecialtyByName(
            @RequestParam(name = "name") String specialtyName)
            throws DataNotFoundException {
        return ResponseEntity.ok(specialtyService.getSpecialtyByName(specialtyName));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SpecialtyDto>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteById(
            @PathVariable(name = "id") String specialtyId)
            throws DataMismatchException {
        Long idValue = Validator.idValidator(specialtyId);
        return ResponseEntity.ok(specialtyService.deleteSpecialtyById(idValue));
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<Long> deleteSpecialtyByName(
            @RequestParam(name = "name") String specialtyName)
            throws DataNotFoundException {
        return ResponseEntity.ok(specialtyService.deleteSpecialtyByName(specialtyName));
    }
}
