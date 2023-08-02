package ro.felixcirebea.medicalsys.converter;

import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;

@Component
public class DoctorConverter {

    public DoctorEntity fromDtoToEntity(DoctorDto doctorDto, SpecialtyEntity specialtyEntity) {
        DoctorEntity doctorEntity = new DoctorEntity();

        doctorEntity.setName(doctorDto.getName());
        doctorEntity.setSpecialty(specialtyEntity);
        doctorEntity.setPriceRate(doctorDto.getPriceRate());

        return doctorEntity;
    }

    public DoctorDto fromEntityToDto(DoctorEntity doctorEntity) {
        return DoctorDto.builder()
                .id(doctorEntity.getId())
                .name(doctorEntity.getName())
                .specialty(doctorEntity.getSpecialty().getName())
                .priceRate(doctorEntity.getPriceRate())
                .build();
    }
}
