package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;

public class SpecialtyUtil {

    public static final String SPECIALTY_NAME = "TestSpecialty";

    public static SpecialtyDto createSpecialtyDto() {
        return SpecialtyDto.builder()
                .name(SPECIALTY_NAME)
                .build();
    }

    public static SpecialtyEntity createSpecialtyEntity(Long id) {
        SpecialtyEntity specialtyEntity = new SpecialtyEntity();
        specialtyEntity.setId(id);
        specialtyEntity.setName(SPECIALTY_NAME);
        return specialtyEntity;
    }

}
