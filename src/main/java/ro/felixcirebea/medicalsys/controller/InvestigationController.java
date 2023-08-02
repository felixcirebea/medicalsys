package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.service.InvestigationService;
import ro.felixcirebea.medicalsys.util.Validator;

import java.util.List;

@RestController
@RequestMapping("/investigations")
public class InvestigationController {

    private final InvestigationService investigationService;

    public InvestigationController(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> upsertInvestigation(@RequestBody @Valid InvestigationDto dto) {
        return ResponseEntity.ok(investigationService.upsertInvestigation(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestigationDto> getInvestigationById(@PathVariable(name = "id") String investigationId) {
        Validator.idValidator(investigationId);
        return ResponseEntity.ok(investigationService.getInvestigationById(investigationId));
    }

    @GetMapping("/get")
    public ResponseEntity<InvestigationDto> getInvestigationByName(
            @RequestParam(name = "name") String investigationName) {
        return ResponseEntity.ok(investigationService.getInvestigationByName(investigationName));
    }

    @GetMapping("/by-specialty")
    public ResponseEntity<List<InvestigationDto>> getInvestigationBySpecialty(
            @RequestParam(name = "specialty") String specialtyName) {
        return ResponseEntity.ok(investigationService.getInvestigationBySpecialty(specialtyName));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InvestigationDto>> getAllInvestigations() {
        return ResponseEntity.ok(investigationService.getAllInvestigations());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteInvestigationById(@PathVariable(name = "id") String investigationId) {
        Validator.idValidator(investigationId);
        return ResponseEntity.ok(investigationService.deleteInvestigationById(investigationId));
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<Long> deleteInvestigationByName(
            @RequestParam(name = "investigation") String investigationName) {
        return ResponseEntity.ok(investigationService.deleteInvestigationByName(investigationName));
    }

}
