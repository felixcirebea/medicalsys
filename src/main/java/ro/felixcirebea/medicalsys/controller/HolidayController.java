package ro.felixcirebea.medicalsys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.felixcirebea.medicalsys.dto.HolidayDto;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.service.HolidayService;
import ro.felixcirebea.medicalsys.util.Validator;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Long> upsertHoliday(
            @RequestBody @Valid HolidayDto holidayDto)
            throws DataNotFoundException {
        return ResponseEntity.ok(holidayService.upsertHoliday(holidayDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HolidayDto> getHolidayById(
            @PathVariable(name = "id") String holidayId)
            throws DataMismatchException, DataNotFoundException {
        Long idValue = Validator.idValidator(holidayId);
        return ResponseEntity.ok(holidayService.getHolidayById(idValue));
    }

    @GetMapping("/by-description")
    public ResponseEntity<HolidayDto> getHolidayByDescription(
            @RequestParam(name = "description") String holidayDescription)
            throws DataNotFoundException {
        return ResponseEntity.ok(holidayService.getHolidayByDescription(holidayDescription));
    }

    @GetMapping("/all")
    public ResponseEntity<List<HolidayDto>> getAllHolidays() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    @GetMapping("/is-holiday")
    public ResponseEntity<Boolean> isHoliday(
            @RequestParam(name = "date") String inputDate)
            throws DataMismatchException {
        LocalDate idValue = Validator.dateValidator(inputDate);
        return ResponseEntity.ok(holidayService.isDateHoliday(idValue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteHolidayById(
            @PathVariable(name = "id") String inputId)
            throws DataMismatchException {
        Long idValue = Validator.idValidator(inputId);
        return ResponseEntity.ok(holidayService.deleteHolidayById(idValue));
    }

    @DeleteMapping("/by-description")
    public ResponseEntity<Long> deleteHolidayByDescription(
            @RequestParam(name = "description") String description)
            throws DataNotFoundException {
        return ResponseEntity.ok(holidayService.deleteHolidayByDescription(description));
    }
}
