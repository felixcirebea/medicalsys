package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.WorkingHoursConverter;
import ro.felixcirebea.medicalsys.dto.WorkingHoursDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.WorkingHoursEntity;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.WorkingHoursRepository;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.helper.Validator;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class WorkingHoursService {

    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String NO_ENTRY_MSG = "No suitable entry found";
    public static final String LOG_UPDATE_MSG = "Working hours for %s were updated as follows: %s";
    public static final String LOG_INSERT_MSG = "Working hours for %s were inserted";
    public static final String LOG_FAIL_DELETE_MSG = "Can't delete working hours for %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Working hours for %s deleted";
    private final DoctorRepository doctorRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final WorkingHoursConverter workingHoursConverter;
    private final Contributor infoContributor;

    public WorkingHoursService(DoctorRepository doctorRepository,
                               WorkingHoursRepository workingHoursRepository,
                               WorkingHoursConverter workingHoursConverter,
                               Contributor infoContributor) {
        this.doctorRepository = doctorRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.workingHoursConverter = workingHoursConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertWorkingHours(WorkingHoursDto workingHoursDto)
            throws DataNotFoundException, DataMismatchException {
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(workingHoursDto.getDoctor(), true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, workingHoursDto.getDoctor())));

        DayOfWeek dayOfWeek = Validator.dayOfWeekValidator(workingHoursDto.getDayOfWeek());
        Boolean updateCondition =
                workingHoursRepository.existsByDoctorAndDayOfWeek(doctorEntity, dayOfWeek);
        if (updateCondition) {
            return updateWorkingHours(workingHoursDto, doctorEntity, dayOfWeek);
        }

        log.info(String.format(LOG_INSERT_MSG, doctorEntity.getName()));
        WorkingHoursEntity workingHoursEntity =
                workingHoursConverter.fromDtoToEntity(workingHoursDto, doctorEntity);
        return workingHoursRepository.save(workingHoursEntity).getId();
    }

    private Long updateWorkingHours(WorkingHoursDto workingHoursDto,
                                    DoctorEntity doctorEntity,
                                    DayOfWeek dayOfWeek)
            throws DataNotFoundException {
        WorkingHoursEntity workingHoursEntity =
                workingHoursRepository.findByDoctorAndDayOfWeek(doctorEntity, dayOfWeek)
                .orElseThrow(() -> new DataNotFoundException(NO_ENTRY_MSG));

        workingHoursEntity.setDayOfWeek(dayOfWeek);
        workingHoursEntity.setStartHour(workingHoursDto.getStartHour());
        workingHoursEntity.setEndHour(workingHoursDto.getEndHour());
        log.info(String.format(LOG_UPDATE_MSG, workingHoursEntity.getDoctor(), workingHoursDto));
        return workingHoursRepository.save(workingHoursEntity).getId();
    }

    public List<WorkingHoursDto> getWorkingHoursByDoctorAndDay(String doctorName, Integer dayOfWeek)
            throws DataNotFoundException, DataMismatchException {
        if (doctorName != null && dayOfWeek != null) {
            DoctorEntity doctorEntity =
                    doctorRepository.findByNameAndIsActive(doctorName, true)
                    .orElseThrow(() -> new DataNotFoundException(
                            String.format(NOT_FOUND_MSG, doctorName)));
            DayOfWeek dayOfWeekValue = Validator.dayOfWeekValidator(dayOfWeek);

            return workingHoursRepository.findByDoctorAndDayOfWeek(doctorEntity, dayOfWeekValue)
                    .stream()
                    .map(workingHoursConverter::fromEntityToDto)
                    .toList();
        } else if (doctorName != null) {
            DoctorEntity doctorEntity =
                    doctorRepository.findByNameAndIsActive(doctorName, true)
                    .orElseThrow(() -> new DataNotFoundException(
                            String.format(NOT_FOUND_MSG, doctorName)));
            return workingHoursRepository.findByDoctor(doctorEntity)
                    .stream()
                    .map(workingHoursConverter::fromEntityToDto)
                    .toList();
        } else if (dayOfWeek != null) {
            DayOfWeek dayOfWeekValue = Validator.dayOfWeekValidator(dayOfWeek);
            return workingHoursRepository.findByDayOfWeek(dayOfWeekValue)
                    .stream()
                    .map(workingHoursConverter::fromEntityToDto)
                    .toList();
        } else {
            return StreamSupport.stream(workingHoursRepository.findAll().spliterator(), false)
                    .map(workingHoursConverter::fromEntityToDto)
                    .toList();
        }
    }

    public Long deleteWorkingHoursByDoctorAndDay(String doctorName, Integer dayOfWeek)
            throws DataNotFoundException, DataMismatchException {
        Optional<DoctorEntity> doctorEntityOptional =
                doctorRepository.findByNameAndIsActive(doctorName, true);

        if (doctorEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(LOG_FAIL_DELETE_MSG, doctorName));
            throw new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorName));
        }

        DoctorEntity doctorEntity = doctorEntityOptional.get();
        if (dayOfWeek != null) {
            DayOfWeek dayOfWeekValue = Validator.dayOfWeekValidator(dayOfWeek);
            workingHoursRepository.deleteByDoctorAndDayOfWeek(doctorEntity, dayOfWeekValue);
            log.info(String.format(LOG_SUCCESS_DELETE_MSG, doctorEntity.getName()));
            return doctorEntity.getId();
        }

        workingHoursRepository.deleteByDoctor(doctorEntity);
        return doctorEntity.getId();
    }

    public String deleteAllWorkingHoursForDoctor(DoctorEntity doctor) {
        workingHoursRepository.deleteByDoctor(doctor);
        return String.format("Working hours for %s deleted", doctor.getName());
    }
}
