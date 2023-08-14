package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.AppointmentConverter;
import ro.felixcirebea.medicalsys.dto.AppointmentDto;
import ro.felixcirebea.medicalsys.entity.*;
import ro.felixcirebea.medicalsys.enums.AppointmentStatus;
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
import java.util.Optional;

@Service
@Slf4j
public class AppointmentService {

    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String NOT_FOUND_WH_MSG = "Working hours not found for %s";
    public static final String NOT_AVAILABLE_MSG = "%s not available, please select a different hour";
    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String LOG_SUCCESS_CANCEL_MSG = "Appointment: %s for %s canceled";
    public static final String LOG_FAIL_CANCEL_MSG = "Cancel appointment: %s for %s failed - not found";
    public static final String RETURN_SUCCESS_CANCEL_MSG = "Appointment successfully canceled";
    public static final String RETURN_FAIL_CANCEL_MSG = "No such appointments for %s";
    private static final String DATE_ERROR_MSG = "Can't create appointments for dates in the past";
    public static final String CANCEL_ALL_APPOINTMENTS_MSG = "Appointments for %s canceled";
    private final DoctorRepository doctorRepository;
    private final InvestigationRepository investigationRepository;
    private final AppointmentRepository appointmentRepository;
    private final HolidayService holidayService;
    private final AppointmentConverter appointmentConverter;
    private final Contributor infoContributor;

    public AppointmentService(DoctorRepository doctorRepository,
                              InvestigationRepository investigationRepository,
                              AppointmentRepository appointmentRepository,
                              HolidayService holidayService,
                              AppointmentConverter appointmentConverter,
                              Contributor infoContributor) {
        this.doctorRepository = doctorRepository;
        this.investigationRepository = investigationRepository;
        this.appointmentRepository = appointmentRepository;
        this.holidayService = holidayService;
        this.appointmentConverter = appointmentConverter;
        this.infoContributor = infoContributor;
    }

    public List<LocalTime> getAvailableHours(String doctorName,
                                             String investigation,
                                             LocalDate desiredDate)
            throws DataNotFoundException {
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(doctorName, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, doctorName)));

        InvestigationEntity investigationEntity =
                investigationRepository.findByNameAndIsActive(investigation, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, investigation)));

        WorkingHoursEntity workingHoursEntity = doctorEntity.getWorkingHours()
                .stream()
                .filter(workingHours ->
                        workingHours.getDayOfWeek() == desiredDate.getDayOfWeek())
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_WH_MSG, desiredDate)));

        Boolean isHoliday = holidayService.isDateHoliday(desiredDate);
        List<VacationEntity> vacation = doctorEntity.getVacation();
        boolean isVacation = vacation.stream()
                .anyMatch(vac ->
                        desiredDate.isAfter(vac.getStartDate()) &&
                                desiredDate.isBefore(vac.getEndDate()));

        if (isHoliday || isVacation) {
            return Collections.emptyList();
        }

        Integer investigationDuration = investigationEntity.getDuration();

        LocalTime startWorkingHour = workingHoursEntity.getStartHour();
        LocalTime endWorkingHour = workingHoursEntity.getEndHour();

        List<AppointmentEntity> appointments =
                appointmentRepository.findAllByDoctorAndDate(doctorEntity, desiredDate);

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

    public Long bookAppointment(AppointmentDto appointmentDto)
            throws DataNotFoundException, ConcurrencyException {
        if (appointmentDto.getDate().isBefore(infoContributor.getCurrentDate())) {
            throw new ConcurrencyException(DATE_ERROR_MSG);
        }

        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(appointmentDto.getDoctor(), true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, appointmentDto.getDoctor())));

        InvestigationEntity investigationEntity =
                investigationRepository.findByNameAndIsActive(appointmentDto.getInvestigation(), true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, appointmentDto.getInvestigation())));

        LocalTime clientStartHour = appointmentDto.getStartHour();
        LocalTime clientEndHour = clientStartHour.plusMinutes(investigationEntity.getDuration());

        Boolean notAvailable =
                appointmentRepository.existsByDoctorDateAndTimeRange(
                        doctorEntity, appointmentDto.getDate(), clientStartHour, clientEndHour);

        if (notAvailable) {
            throw new ConcurrencyException(
                    String.format(NOT_AVAILABLE_MSG, clientStartHour));
        }

        AppointmentEntity entity =
                appointmentConverter.fromDtoToEntity(
                        appointmentDto, doctorEntity, investigationEntity);
        return appointmentRepository.save(entity).getId();

    }

    public AppointmentDto getAppointmentById(Long appointmentIdValue)
            throws DataNotFoundException {
        AppointmentEntity appointmentEntity = appointmentRepository.findById(appointmentIdValue)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return appointmentConverter.fromEntityToDto(appointmentEntity);
    }

    public String cancelAppointmentByIdAndName(Long id, String clientName)
            throws DataNotFoundException {
        Optional<AppointmentEntity> appointmentEntityOptional =
                appointmentRepository.findByIdAndClientName(id, clientName);

        if (appointmentEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(LOG_FAIL_CANCEL_MSG, id, clientName));
            throw new DataNotFoundException(String.format(RETURN_FAIL_CANCEL_MSG, clientName));
        }

        appointmentEntityOptional.get().setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointmentEntityOptional.get());
        log.info(String.format(LOG_SUCCESS_CANCEL_MSG, id, clientName));
        return RETURN_SUCCESS_CANCEL_MSG;
    }

    public String cancelAllAppointmentForDoctor(DoctorEntity doctor) {
        List<AppointmentEntity> appointments = appointmentRepository.findAllByDoctor(doctor);
        appointments.forEach(book -> book.setStatus(AppointmentStatus.CANCELED));
        appointmentRepository.saveAll(appointments);
        return String.format(CANCEL_ALL_APPOINTMENTS_MSG, doctor.getName());
    }

}
