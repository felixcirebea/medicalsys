package ro.felixcirebea.medicalsys.entity;

import jakarta.persistence.*;
import lombok.Data;

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

}
