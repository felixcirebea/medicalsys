package ro.felixcirebea.medicalsys.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvestigationDto {

    private Long id;

    @NotEmpty(message = "Name cannot be empty")
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotEmpty(message = "Specialty cannot be empty")
    @NotBlank(message = "Specialty cannot be blank")
    private String specialty;

    @Min(value = 150, message = "The minimum price is 150")
    private Double basePrice;

    @NotNull(message = "Duration cannot be empty")
    @Min(value = 30, message = "The minimum duration is 30 minutes")
    private Integer duration;

}
