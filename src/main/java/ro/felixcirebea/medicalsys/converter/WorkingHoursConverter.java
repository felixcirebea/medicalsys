package ro.felixcirebea.medicalsys.converter;

import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.dto.WorkingHoursDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.WorkingHoursEntity;

import java.time.DayOfWeek;

@Component
public class WorkingHoursConverter {


    public WorkingHoursEntity fromDtoToEntity(WorkingHoursDto workingHoursDto, DoctorEntity doctorEntity) {
        WorkingHoursEntity workingHoursEntity = new WorkingHoursEntity();
        workingHoursEntity.setDoctor(doctorEntity);
        workingHoursEntity.setDayOfWeek(DayOfWeek.of(workingHoursDto.getDayOfWeek()));
        workingHoursEntity.setStartHour(workingHoursDto.getStartHour());
        workingHoursEntity.setEndHour(workingHoursDto.getEndHour());
        return workingHoursEntity;
    }

    public WorkingHoursDto froEntityToDto(WorkingHoursEntity workingHoursEntity) {
        return WorkingHoursDto.builder()
                .id(workingHoursEntity.getId())
                .doctor(workingHoursEntity.getDoctor().getName())
                .dayOfWeek(workingHoursEntity.getDayOfWeek().getValue())
                .startHour(workingHoursEntity.getStartHour())
                .endHour(workingHoursEntity.getEndHour())
                .build();
    }
}
