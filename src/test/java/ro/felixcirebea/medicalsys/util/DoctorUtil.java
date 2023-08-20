package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;

public class DoctorUtil {

    private static final String DOCTOR_NAME = "TestDoctor";
    private static final String SPECIALTY_NAME = "TestSpecialty";
    private static final SpecialtyEntity SPECIALTY = SpecialtyUtil.createSpecialtyEntity(1L);
    public static final double PRICE_RATE = 50D;

    public static DoctorDto createDoctorDto() {
        return DoctorDto.builder()
                .name(DOCTOR_NAME)
                .specialty(SPECIALTY_NAME)
                .priceRate(PRICE_RATE)
                .build();
    }

    public static DoctorEntity createDoctorEntity(Long id) {
        DoctorEntity doctorEntity = new DoctorEntity();
        doctorEntity.setId(id);
        doctorEntity.setName(DOCTOR_NAME);
        doctorEntity.setSpecialty(SPECIALTY);
        doctorEntity.setPriceRate(PRICE_RATE);

        return doctorEntity;
    }

}
