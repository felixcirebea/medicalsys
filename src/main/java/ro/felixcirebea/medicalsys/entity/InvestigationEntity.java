package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "investigations")
@Data
@SuppressWarnings("all")
public class InvestigationEntity extends BaseEntity {

    @Column(unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "specialty_id")
    @ToString.Exclude
    private SpecialtyEntity specialty;

    private Double basePrice;

    private Integer duration;

}
