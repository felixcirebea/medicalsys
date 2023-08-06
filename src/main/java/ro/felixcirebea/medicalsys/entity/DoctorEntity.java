package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity(name = "doctors")
@Data
public class DoctorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "specialty_id")
    private SpecialtyEntity specialty;

    private Double priceRate;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<WorkingHoursEntity> workingHours;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<VacationEntity> vacation;

}
