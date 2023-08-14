package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import ro.felixcirebea.medicalsys.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "appointments")
@Data
@SuppressWarnings("all")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String clientName;

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

    private Double price;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.NEW;

}
