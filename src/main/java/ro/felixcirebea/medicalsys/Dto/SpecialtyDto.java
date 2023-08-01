package ro.felixcirebea.medicalsys.Dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpecialtyDto {

    private Long id;
    @NotEmpty(message = "Name cannot be empty")
    private String name;

}
