package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.WorkingHoursEntity;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class WorkingHoursUtil {

    private static final DoctorEntity DOCTOR = DoctorUtil.createDoctorEntity(1L);
    public static final LocalTime START_HOUR = LocalTime.of(8, 0);
    public static final LocalTime END_HOUR = LocalTime.of(12, 0);

    public static WorkingHoursEntity createWorkingHoursEntity(Long id, Integer dayOfWeek) {
        WorkingHoursEntity workingHoursEntity = new WorkingHoursEntity();
        workingHoursEntity.setId(id);
        workingHoursEntity.setDoctor(DOCTOR);
        workingHoursEntity.setDayOfWeek(DayOfWeek.of(dayOfWeek));
        workingHoursEntity.setStartHour(START_HOUR);
        workingHoursEntity.setEndHour(END_HOUR);

        return workingHoursEntity;
    }

}
