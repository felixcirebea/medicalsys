package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Entity(name = "holidays")
@Data
public class HolidayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

}
