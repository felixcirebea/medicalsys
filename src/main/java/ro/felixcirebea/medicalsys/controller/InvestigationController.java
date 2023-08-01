package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.felixcirebea.medicalsys.Dto.InvestigationDto;
import ro.felixcirebea.medicalsys.service.InvestigationService;

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

}
