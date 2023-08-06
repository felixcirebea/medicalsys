package ro.felixcirebea.medicalsys.converter;

import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;

@Component
public class VacationConverter {

    public VacationEntity fromDtoToEntity(VacationDto vacationDto, DoctorEntity doctorEntity)
            throws DataMismatchException {
        VacationEntity vacationEntity = new VacationEntity();
        vacationEntity.setDoctor(doctorEntity);
        vacationEntity.setStartDate(vacationDto.getStartDate());
        vacationEntity.setEndDate(vacationDto.getEndDate());
        vacationEntity.setType(vacationDto.getType());
        return vacationEntity;
    }

    public VacationDto fromEntityToDto(VacationEntity vacationEntity) {
        return VacationDto.builder()
                .id(vacationEntity.getId())
                .doctor(vacationEntity.getDoctor().getName())
                .startDate(vacationEntity.getStartDate())
                .endDate(vacationEntity.getEndDate())
                .type(vacationEntity.getType())
                .build();
    }
}
