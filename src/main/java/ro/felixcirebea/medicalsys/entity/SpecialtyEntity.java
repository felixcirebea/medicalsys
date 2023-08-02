package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity(name = "specialties")
@Data
public class SpecialtyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String name;
    @OneToMany(mappedBy = "specialty", cascade = CascadeType.ALL)
    private List<InvestigationEntity> investigations;
    @OneToMany(mappedBy = "specialty", cascade = CascadeType.ALL)
    private List<DoctorEntity> doctors;
}
