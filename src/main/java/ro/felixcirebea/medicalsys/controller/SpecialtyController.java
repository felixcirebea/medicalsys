package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.Dto.SpecialtyDto;
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
    public ResponseEntity<Long> upsertSpecialty(@RequestBody @Valid SpecialtyDto dto) {
        return ResponseEntity.ok(specialtyService.upsertSpecialty(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getSpecialty(@PathVariable(name = "id") String specialtyId) {
        Validator.idValidator(specialtyId);
        return ResponseEntity.ok(specialtyService.getSpecialtyById(specialtyId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SpecialtyDto>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable(name = "id") String specialtyId) {
        Validator.idValidator(specialtyId);
        return ResponseEntity.ok(specialtyService.deleteSpecialtyById(specialtyId));
    }

}
