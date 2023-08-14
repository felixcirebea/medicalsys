package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "holidays")
@Data
public class HolidayEntity extends BaseEntity {

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

}
