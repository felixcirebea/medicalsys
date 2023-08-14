package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity(name = "working_hours")
@Data
@SuppressWarnings("all")
public class WorkingHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @ToString.Exclude
    private DoctorEntity doctor;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime startHour;

    private LocalTime endHour;

}
