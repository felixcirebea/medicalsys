package ro.felixcirebea.medicalsys.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    private Double basePrice;

}
