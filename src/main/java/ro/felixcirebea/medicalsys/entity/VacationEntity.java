package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;
import ro.felixcirebea.medicalsys.enums.VacationType;

import java.time.LocalDate;

@Entity(name = "vacations")
@Data
public class VacationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private VacationType type;

}
