package ro.felixcirebea.medicalsys.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ro.felixcirebea.medicalsys.enums.VacationType;

import java.time.LocalDate;

@Data
@Builder
public class VacationDto {

    private Long id;

    @NotEmpty(message = "Doctor cannot be empty")
    @NotBlank(message = "Doctor cannot be blank")
    private String doctor;

    @NotNull(message = "Start date cannot be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Start date cannot be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Type cannot be null")
    private VacationType type;

}
