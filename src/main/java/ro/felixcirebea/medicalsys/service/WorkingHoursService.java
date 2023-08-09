package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.WorkingHoursConverter;
import ro.felixcirebea.medicalsys.dto.WorkingHoursDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.WorkingHoursEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.WorkingHoursRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class WorkingHoursService {

    private final DoctorRepository doctorRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final WorkingHoursConverter workingHoursConverter;
    private final Contributor infoContributor;

    public WorkingHoursService(DoctorRepository doctorRepository, WorkingHoursRepository workingHoursRepository,
                               WorkingHoursConverter workingHoursConverter, Contributor infoContributor) {
        this.doctorRepository = doctorRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.workingHoursConverter = workingHoursConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertWorkingHours(WorkingHoursDto workingHoursDto) throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findByName(workingHoursDto.getDoctor()).orElseThrow(() ->
                new DataNotFoundException(String.format("Doctor %s not found", workingHoursDto.getDoctor())));

        DayOfWeek dayOfWeek = DayOfWeek.of(workingHoursDto.getDayOfWeek());

        Boolean updateCondition = workingHoursRepository.existsByDoctorAndDayOfWeek(doctorEntity, dayOfWeek);
        if (updateCondition) {
            return updateWorkingHours(workingHoursDto, doctorEntity, dayOfWeek);
        }

        log.info(String.format("Working hours for %s were saved", doctorEntity.getName()));
        return workingHoursRepository
                .save(workingHoursConverter.fromDtoToEntity(workingHoursDto, doctorEntity)).getId();
    }

    private Long updateWorkingHours(WorkingHoursDto workingHoursDto, DoctorEntity doctorEntity, DayOfWeek dayOfWeek)
            throws DataNotFoundException {
        WorkingHoursEntity workingHoursEntity = workingHoursRepository.findByDoctorAndDayOfWeek(doctorEntity, dayOfWeek)
                .orElseThrow(() -> new DataNotFoundException("No suitable entry found"));
        workingHoursEntity.setDayOfWeek(dayOfWeek);
        workingHoursEntity.setStartHour(workingHoursDto.getStartHour());
        workingHoursEntity.setEndHour(workingHoursDto.getEndHour());
        log.info(String.format("Working hours for %s were updated", workingHoursEntity.getDoctor()));
        return workingHoursRepository.save(workingHoursEntity).getId();
    }

    public List<WorkingHoursDto> getWorkingHoursByDoctorAndDay(String doctorName, Integer dayOfWeek)
            throws DataNotFoundException {
        if (doctorName != null && dayOfWeek != null) {
            DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                    .orElseThrow(() -> new DataNotFoundException(String.format("Doctor %s not found", doctorName)));
            DayOfWeek dayOfWeekValue = DayOfWeek.of(dayOfWeek);
            return workingHoursRepository.findByDoctorAndDayOfWeek(doctorEntity, dayOfWeekValue).stream()
                    .map(workingHoursConverter::froEntityToDto).toList();
        } else if (doctorName != null) {
            DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                    .orElseThrow(() -> new DataNotFoundException(String.format("Doctor %s not found", doctorName)));
            return workingHoursRepository.findByDoctor(doctorEntity).stream()
                    .map(workingHoursConverter::froEntityToDto).toList();
        } else if (dayOfWeek != null) {
            DayOfWeek dayOfWeekValue = DayOfWeek.of(dayOfWeek);
            return workingHoursRepository.findByDayOfWeek(dayOfWeekValue).stream()
                    .map(workingHoursConverter::froEntityToDto).toList();
        } else {
            return StreamSupport.stream(workingHoursRepository.findAll().spliterator(), false)
                    .map(workingHoursConverter::froEntityToDto).toList();
        }
    }

    public Long deleteWorkingHoursByDoctorAndDay(String doctorName, Integer dayOfWeek)
            throws DataNotFoundException {
        Optional<DoctorEntity> doctorEntityOptional = doctorRepository.findByName(doctorName);
        if (doctorEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(
                    "Can't delete working hours for doctor with name %s. Doctor doesn't exist", doctorName));
            throw new DataNotFoundException(String.format("Doctor with name %s not found", doctorName));
        }

        if (dayOfWeek != null) {
            DayOfWeek dayOfWeekValue = DayOfWeek.of(dayOfWeek);
            workingHoursRepository.deleteByDoctorAndDayOfWeek(doctorEntityOptional.get(), dayOfWeekValue);
            return doctorEntityOptional.get().getId();
        }

        workingHoursRepository.deleteByDoctor(doctorEntityOptional.get());
        return doctorEntityOptional.get().getId();
    }
}
