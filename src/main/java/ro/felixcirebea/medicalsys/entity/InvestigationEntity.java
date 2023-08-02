package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "investigations")
@Data
public class InvestigationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String name;
    @ManyToOne
    @JoinColumn(name = "specialty_id")
    private SpecialtyEntity specialty;
    private Double basePrice;

}
