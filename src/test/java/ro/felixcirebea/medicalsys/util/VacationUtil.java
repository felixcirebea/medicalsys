package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.enums.VacationType;

import java.time.LocalDate;

public class VacationUtil {

    private static final String DOCTOR_NAME = "TestDoctor";
    private static final LocalDate START_DATE = LocalDate.of(2023, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2023, 1, 7);
    private static final VacationType TYPE = VacationType.VACATION;
    private static final DoctorEntity DOCTOR = DoctorUtil.createDoctorEntity(1L);

    public static VacationDto createVacationDto() {
        return VacationDto.builder()
                .doctor(DOCTOR_NAME)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .type(TYPE)
                .build();
    }

    public static VacationEntity createVacationEntity(Long id) {
        VacationEntity vacationEntity = new VacationEntity();
        vacationEntity.setId(id);
        vacationEntity.setDoctor(DOCTOR);
        vacationEntity.setStartDate(START_DATE);
        vacationEntity.setEndDate(END_DATE);
        vacationEntity.setType(TYPE);

        return vacationEntity;
    }
}
