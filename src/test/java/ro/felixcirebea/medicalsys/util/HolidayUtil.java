package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.dto.HolidayDto;
import ro.felixcirebea.medicalsys.entity.HolidayEntity;

import java.time.LocalDate;

public class HolidayUtil {

    private static final LocalDate START_DATE = LocalDate.of(2023, 3, 1);
    private static final LocalDate END_DATE = LocalDate.of(2023, 3, 2);
    private static final String DESCRIPTION = "TestHoliday";

    public static HolidayDto createHolidayDto() {
        return HolidayDto.builder()
                .startDate(START_DATE)
                .endDate(END_DATE)
                .description(DESCRIPTION)
                .build();
    }

    public static HolidayEntity createHolidayEntity(Long expectedId) {
        HolidayEntity holidayEntity = new HolidayEntity();
        holidayEntity.setId(expectedId);
        holidayEntity.setStartDate(START_DATE);
        holidayEntity.setEndDate(END_DATE);
        holidayEntity.setDescription(DESCRIPTION);
        return holidayEntity;
    }

}
