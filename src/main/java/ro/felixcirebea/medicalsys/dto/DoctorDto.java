package ro.felixcirebea.medicalsys.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorDto {

    private Long id;
    @NotEmpty(message = "Name cannot be empty")
    private String name;
    @NotEmpty(message = "Specialty cannot be empty")
    private String specialty;
    @DecimalMin(value = "0.0", inclusive = false)
    private Double priceRate;

}
