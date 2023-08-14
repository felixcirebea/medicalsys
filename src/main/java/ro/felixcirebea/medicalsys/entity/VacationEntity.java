package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.enums.VacationType;

import java.time.LocalDate;

@Entity(name = "vacations")
@Data
@SuppressWarnings("all")
public class VacationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @ToString.Exclude
    private DoctorEntity doctor;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private VacationType type;

    @Enumerated(EnumType.STRING)
    private VacationStatus status = VacationStatus.PLANNED;

}
