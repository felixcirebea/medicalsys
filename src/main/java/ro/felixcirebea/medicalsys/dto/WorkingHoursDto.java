package ro.felixcirebea.medicalsys.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class WorkingHoursDto {

    private Long id;

    @NotEmpty(message = "Name cannot be empty")
    @NotBlank(message = "Name cannot be blank")
    private String doctor;

    @NotNull(message = "Day of the week cannot be null")
    @Min(value = 1, message = "Minimum value is 1")
    @Max(value = 5, message = "Maximum value is 5")
    private Integer dayOfWeek;

    @NotNull(message = "Start hour cannot be null")
    private LocalTime startHour;

    @NotNull(message = "End hour cannot be null")
    private LocalTime endHour;

}
