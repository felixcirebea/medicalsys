package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "doctors")
@Data
@SuppressWarnings("all")
public class DoctorEntity extends BaseEntity {

    @Column(unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "specialty_id")
    @ToString.Exclude
    private SpecialtyEntity specialty;

    private Double priceRate;

    @OneToMany(mappedBy = "doctor")
    private List<WorkingHoursEntity> workingHours;

    @OneToMany(mappedBy = "doctor")
    private List<VacationEntity> vacation;

    @OneToMany(mappedBy = "doctor")
    private List<AppointmentEntity> appointments;

}
