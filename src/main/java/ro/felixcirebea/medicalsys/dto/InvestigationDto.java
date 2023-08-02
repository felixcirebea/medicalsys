package ro.felixcirebea.medicalsys.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvestigationDto {

    private Long id;
    @NotEmpty(message = "Name cannot be empty")
    private String name;
    @NotEmpty(message = "Specialty cannot be empty")
    private String specialty;
    private Double basePrice;

}
