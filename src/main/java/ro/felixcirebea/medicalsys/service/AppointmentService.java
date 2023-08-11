package ro.felixcirebea.medicalsys.service;

import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.entity.*;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.AppointmentRepository;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AppointmentService {

    private final DoctorRepository doctorRepository;
    private final InvestigationRepository investigationRepository;
    private final AppointmentRepository appointmentRepository;
    private final HolidayService holidayService;

    public AppointmentService(DoctorRepository doctorRepository, InvestigationRepository investigationRepository,
                              AppointmentRepository appointmentRepository, HolidayService holidayService) {
        this.doctorRepository = doctorRepository;
        this.investigationRepository = investigationRepository;
        this.appointmentRepository = appointmentRepository;
        this.holidayService = holidayService;
    }

    public List<LocalTime> getAvailableHours(String doctorName, String investigation, LocalDate desiredDate)
            throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new DataNotFoundException(String.format("Doctor %s not found", doctorName)));

        InvestigationEntity investigationEntity = investigationRepository.findByName(investigation)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Investigation with name %s not found", investigation)));

        WorkingHoursEntity workingHoursEntity = doctorEntity.getWorkingHours().stream()
                .filter(workingHours -> workingHours.getDayOfWeek() == desiredDate.getDayOfWeek())
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(String.format("Not working hours for %s", desiredDate)));

        Boolean isHoliday = holidayService.isDateHoliday(desiredDate);
        List<VacationEntity> vacation = doctorEntity.getVacation();
        boolean isVacation = vacation.stream()
                .anyMatch(vac -> desiredDate.isAfter(vac.getStartDate()) && desiredDate.isBefore(vac.getEndDate()));

        if (isHoliday || isVacation) {
            return Collections.emptyList();
        }

        Integer investigationDuration = investigationEntity.getDuration();

        LocalTime startWorkingHour = workingHoursEntity.getStartHour();
        LocalTime endWorkingHour = workingHoursEntity.getEndHour();

        List<AppointmentEntity> appointments = appointmentRepository.findAllByDoctorAndDate(doctorEntity, desiredDate);

        List<LocalTime> availableHours = new ArrayList<>();
        LocalTime currentTime = startWorkingHour;

        while (currentTime.isBefore(endWorkingHour.minusMinutes(investigationDuration))) {
            boolean slotAvailable = true;

            for (AppointmentEntity appointment : appointments) {
                LocalTime appointmentStart = appointment.getStartTime();
                LocalTime appointmentEnd = appointment.getEndTime();

                if (currentTime.isAfter(appointmentStart.minusMinutes(investigationDuration)) &&
                        currentTime.isBefore(appointmentEnd)) {
                    slotAvailable = false;
                    break;
                }
            }

            if (slotAvailable) {
                availableHours.add(currentTime);
            }

            currentTime = currentTime.plusMinutes(30);
        }

        return availableHours;
    }
}
