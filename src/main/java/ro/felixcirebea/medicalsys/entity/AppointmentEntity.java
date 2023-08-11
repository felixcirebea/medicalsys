package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "appointments")
@Data
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @ToString.Exclude
    private DoctorEntity doctor;

    @ManyToOne
    @JoinColumn(name = "investigation_id")
    private InvestigationEntity investigation;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

}
