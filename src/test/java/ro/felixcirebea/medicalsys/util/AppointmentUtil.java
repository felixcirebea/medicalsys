package ro.felixcirebea.medicalsys.util;

import ro.felixcirebea.medicalsys.dto.AppointmentDto;
import ro.felixcirebea.medicalsys.entity.AppointmentEntity;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.InvestigationEntity;
import ro.felixcirebea.medicalsys.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentUtil {

    private static final DoctorEntity DOCTOR = DoctorUtil.createDoctorEntity(1L);
    private static final InvestigationEntity INVESTIGATION = InvestigationUtil.createInvestigationEntity(1L);
    public static final AppointmentStatus STATUS = AppointmentStatus.NEW;
    public static final String CLIENT = "TestClient";

    public static AppointmentEntity createAppointmentEntity(Long id, LocalDate date,
                                                            LocalTime startTime, LocalTime endTime) {
        AppointmentEntity appointmentEntity = new AppointmentEntity();
        appointmentEntity.setId(id);
        appointmentEntity.setClientName(CLIENT);
        appointmentEntity.setDoctor(DOCTOR);
        appointmentEntity.setInvestigation(INVESTIGATION);
        appointmentEntity.setDate(date);
        appointmentEntity.setStartTime(startTime);
        appointmentEntity.setEndTime(endTime);
        Double price = ((DOCTOR.getPriceRate() / 100) * INVESTIGATION.getBasePrice()) + INVESTIGATION.getBasePrice();
        appointmentEntity.setPrice(price);
        appointmentEntity.setStatus(STATUS);

        return appointmentEntity;
    }

    public static AppointmentDto createAppointmentDto(LocalDate date, LocalTime startTime) {
        return AppointmentDto.builder()
                .clientName(CLIENT)
                .doctor(DOCTOR.getName())
                .investigation(INVESTIGATION.getName())
                .date(date)
                .startHour(startTime)
                .build();
    }

}
