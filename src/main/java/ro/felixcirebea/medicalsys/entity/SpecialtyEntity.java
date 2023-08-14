package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "specialties")
@Data
public class SpecialtyEntity extends BaseEntity {

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "specialty")
    private List<InvestigationEntity> investigations;

    @OneToMany(mappedBy = "specialty")
    private List<DoctorEntity> doctors;

}
