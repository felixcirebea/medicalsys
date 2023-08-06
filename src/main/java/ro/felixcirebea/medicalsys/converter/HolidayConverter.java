package ro.felixcirebea.medicalsys.converter;

import org.springframework.stereotype.Component;
import ro.felixcirebea.medicalsys.dto.HolidayDto;
import ro.felixcirebea.medicalsys.entity.HolidayEntity;

@Component
public class HolidayConverter {

    public HolidayEntity fromDtoToEntity(HolidayDto holidayDto) {
        HolidayEntity holidayEntity = new HolidayEntity();
        holidayEntity.setStartDate(holidayDto.getStartDate());
        holidayEntity.setEndDate(holidayDto.getEndDate());
        holidayEntity.setDescription(holidayDto.getDescription());
        return holidayEntity;
    }

    public HolidayDto fromEntityToDto(HolidayEntity holidayEntity) {
        return HolidayDto.builder()
                .id(holidayEntity.getId())
                .startDate(holidayEntity.getStartDate())
                .endDate(holidayEntity.getEndDate())
                .description(holidayEntity.getDescription())
                .build();
    }
}
