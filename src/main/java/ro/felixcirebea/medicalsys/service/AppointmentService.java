package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.AppointmentConverter;
import ro.felixcirebea.medicalsys.dto.AppointmentDto;
import ro.felixcirebea.medicalsys.entity.*;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.AppointmentRepository;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class AppointmentService {

    private final DoctorRepository doctorRepository;
    private final InvestigationRepository investigationRepository;
    private final AppointmentRepository appointmentRepository;
    private final HolidayService holidayService;
    private final AppointmentConverter appointmentConverter;
    private final Contributor infoContributor;

    public AppointmentService(DoctorRepository doctorRepository, InvestigationRepository investigationRepository,
                              AppointmentRepository appointmentRepository, HolidayService holidayService,
                              AppointmentConverter appointmentConverter, Contributor infoContributor) {
        this.doctorRepository = doctorRepository;
        this.investigationRepository = investigationRepository;
        this.appointmentRepository = appointmentRepository;
        this.holidayService = holidayService;
        this.appointmentConverter = appointmentConverter;
        this.infoContributor = infoContributor;
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

    public Long bookAppointment(AppointmentDto appointmentDto) throws DataNotFoundException, ConcurrencyException {
        DoctorEntity doctorEntity = doctorRepository.findByName(appointmentDto.getDoctor())
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Doctor %s not found", appointmentDto.getDoctor())));

        InvestigationEntity investigationEntity = investigationRepository.findByName(appointmentDto.getInvestigation())
                .orElseThrow(() -> new DataNotFoundException(
                        String.format("Investigation with name %s not found", appointmentDto.getInvestigation())));

        LocalTime clientStartHour = appointmentDto.getStartHour();
        LocalTime clientEndHour = clientStartHour.plusMinutes(investigationEntity.getDuration());

        Boolean notAvailable = appointmentRepository
                .existsByDoctorDateAndTimeRange(doctorEntity, appointmentDto.getDate(), clientStartHour, clientEndHour);

        if (notAvailable) {
            throw new ConcurrencyException(
                    String.format("%s not available, please select a different hour", clientStartHour));
        }

        return appointmentRepository.save(
                appointmentConverter.fromDtoToEntity(appointmentDto, doctorEntity, investigationEntity)).getId();

    }

    public AppointmentDto getAppointmentById(Long appointmentIdValue) throws DataNotFoundException {
        AppointmentEntity appointmentEntity = appointmentRepository.findById(appointmentIdValue)
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));
        return appointmentConverter.fromEntityToDto(appointmentEntity);
    }

    public String deleteAppointmentByIdAndName(Long id, String clientName) throws DataNotFoundException {
        Boolean deleteCondition = appointmentRepository.existsByIdAndClientName(id, clientName);
        if (deleteCondition) {
            appointmentRepository.deleteByIdAndClientName(id, clientName);
            log.info(String.format("Appointment with id %s for %s deleted", id, clientName));
            return "Appointment successfully canceled";
        } else {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(
                    "Can't delete appointment with id %s for %s. Appointment doesn't exist", id, clientName));
            throw new DataNotFoundException(String.format("No such appointments for %s", clientName));
        }
    }


}
