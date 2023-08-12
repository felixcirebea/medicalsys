package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.InvestigationDto;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.InvestigationService;
import ro.felixcirebea.medicalsys.util.Validator;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/investigations")
public class InvestigationController {

    private final InvestigationService investigationService;

    public InvestigationController(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> upsertInvestigation(@RequestBody @Valid InvestigationDto investigationDto)
            throws DataNotFoundException {
        return ResponseEntity.ok(investigationService.upsertInvestigation(investigationDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestigationDto> getInvestigationById(@PathVariable(name = "id") String investigationId)
            throws DataNotFoundException, DataMismatchException {
        return ResponseEntity.ok(investigationService.getInvestigationById(Validator.idValidator(investigationId)));
    }

    @GetMapping("/get")
    public ResponseEntity<InvestigationDto> getInvestigationByName(
            @RequestParam(name = "name") String investigationName) throws DataNotFoundException {
        return ResponseEntity.ok(investigationService.getInvestigationByName(investigationName));
    }

    @GetMapping("/by-specialty")
    public ResponseEntity<List<InvestigationDto>> getInvestigationBySpecialty(
            @RequestParam(name = "specialty") String specialtyName) throws DataNotFoundException {
        return ResponseEntity.ok(investigationService.getInvestigationBySpecialty(specialtyName));
    }

    @GetMapping("/by-duration")
    public ResponseEntity<List<InvestigationDto>> getInvestigationByDuration(
            @RequestParam(name = "duration") Integer duration) {
        return ResponseEntity.ok(investigationService.getInvestigationByDuration(duration));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InvestigationDto>> getAllInvestigations() {
        return ResponseEntity.ok(investigationService.getAllInvestigations());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteInvestigationById(@PathVariable(name = "id") String investigationId)
            throws DataMismatchException {
        return ResponseEntity.ok(investigationService.deleteInvestigationById(Validator.idValidator(investigationId)));
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<Long> deleteInvestigationByName(
            @RequestParam(name = "investigation") String investigationName) throws DataNotFoundException {
        return ResponseEntity.ok(investigationService.deleteInvestigationByName(investigationName));
    }

    @GetMapping("/pricing")
    public ResponseEntity<Map<String, Map<String, Double>>> getInvestigationWithPricing(
            @RequestParam(name = "doctor") String doctor,
            @RequestParam(name = "investigation", required = false) String investigation) throws DataNotFoundException {
        return ResponseEntity.ok(investigationService.getInvestigationWithPricing(doctor, investigation));
    }

}
