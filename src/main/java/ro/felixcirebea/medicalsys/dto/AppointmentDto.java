package ro.felixcirebea.medicalsys.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentDto {

    private Long id;

    @NotEmpty(message = "Client name cannot be empty")
    @NotBlank(message = "Client name cannot be blank")
    private String clientName;

    @NotEmpty(message = "Doctor cannot be empty")
    @NotBlank(message = "Doctor cannot be blank")
    private String doctor;

    @NotEmpty(message = "Investigation cannot be empty")
    @NotBlank(message = "Investigation cannot be blank")
    private String investigation;

    @NotNull(message = "Date cannot be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Start hour cannot be null")
    private LocalTime startHour;

    private Double price;

}
