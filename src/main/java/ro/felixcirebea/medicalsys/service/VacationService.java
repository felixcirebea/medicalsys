package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.VacationConverter;
import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.VacationRepository;
import ro.felixcirebea.medicalsys.util.Contributor;
import ro.felixcirebea.medicalsys.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class VacationService {

    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String LOG_UPDATE_MSG = "%s vacation was updated as follows: %s";
    public static final String LOG_INSERT_MSG = "Vacation for doctor %s was inserted";
    public static final String LOG_FAIL_ID_DELETE_MSG = "Delete of vacation id: %s failed - not found";
    public static final String LOG_FAIL_DOCTOR_DELETE_MSG = "Delete vacation for %s failed - doctor not found";
    public static final String LOG_SUCCESS_ID_DELETE_MSG = "Vacation id: %s deleted";
    public static final String LOG_SUCCESS_DOCTOR_DELETE_MSG = "Vacation for %s starting %s deleted";
    public static final String LOG_NOT_FOUND_MSG = "Vacation for %s starting %s not found";
    private final VacationRepository vacationRepository;
    private final DoctorRepository doctorRepository;
    private final VacationConverter vacationConverter;
    private final Contributor infoContributor;

    public VacationService(VacationRepository vacationRepository,
                           DoctorRepository doctorRepository,
                           VacationConverter vacationConverter,
                           Contributor infoContributor) {
        this.vacationRepository = vacationRepository;
        this.doctorRepository = doctorRepository;
        this.vacationConverter = vacationConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertVacation(VacationDto vacationDto) throws DataNotFoundException, DataMismatchException {
        DoctorEntity doctorEntity = doctorRepository.findByName(vacationDto.getDoctor())
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, vacationDto.getDoctor())));
        if (vacationDto.getId() != null) {
            return updateVacation(vacationDto, doctorEntity);
        }

        log.info(String.format(LOG_INSERT_MSG, doctorEntity.getName()));
        return vacationRepository.save(vacationConverter.fromDtoToEntity(vacationDto, doctorEntity)).getId();
    }

    private Long updateVacation(VacationDto vacationDto, DoctorEntity doctorEntity)
            throws DataNotFoundException {
        VacationEntity vacationEntity = vacationRepository.findById(vacationDto.getId())
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));

        vacationEntity.setDoctor(doctorEntity);
        vacationEntity.setStartDate(vacationDto.getStartDate());
        vacationEntity.setEndDate(vacationDto.getEndDate());
        vacationEntity.setType(vacationDto.getType());

        log.info(String.format(LOG_UPDATE_MSG, doctorEntity.getName(), vacationDto));
        return vacationRepository.save(vacationEntity).getId();
    }

    public List<VacationDto> getVacationByDoctorAndDates(String doctorName, String startDate, String endDate)
            throws DataNotFoundException, DataMismatchException {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorName)));

        LocalDate startDateValue = StringUtils.isNotBlank(startDate) ? Validator.dateValidator(startDate) : null;
        LocalDate endDateValue = StringUtils.isNotBlank(endDate) ? Validator.dateValidator(endDate) : null;

        if (startDateValue != null && endDateValue != null) {
            return vacationRepository
                    .findAllByDoctorAndStartDateAfterAndEndDateBefore(doctorEntity, startDateValue, endDateValue)
                    .stream()
                    .map(vacationConverter::fromEntityToDto)
                    .toList();
        } else if (startDateValue != null) {
            return vacationRepository.findAllByDoctorAndStartDateAfter(doctorEntity, startDateValue)
                    .stream()
                    .map(vacationConverter::fromEntityToDto)
                    .toList();
        } else if (endDateValue != null) {
            return vacationRepository.findAllByDoctorAndEndDateBefore(doctorEntity, endDateValue)
                    .stream()
                    .map(vacationConverter::fromEntityToDto)
                    .toList();
        } else {
            return doctorEntity.getVacation()
                    .stream()
                    .map(vacationConverter::fromEntityToDto)
                    .toList();
        }
    }

    public List<VacationDto> getVacationByDoctorAndType(String doctorName, VacationType type)
            throws DataNotFoundException {
        if (StringUtils.isNotBlank(doctorName)) {
            DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                    .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorName)));
            return vacationRepository.findAllByDoctorAndType(doctorEntity, type)
                    .stream()
                    .map(vacationConverter::fromEntityToDto)
                    .toList();
        } else {
            return vacationRepository.findAllByType(type)
                    .stream()
                    .map(vacationConverter::fromEntityToDto)
                    .toList();
        }
    }

    public Boolean isDateVacation(String doctorName, LocalDate date) throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorName)));
        return vacationRepository.isDateBetweenVacation(doctorEntity, date);
    }

    public Long deleteVacationById(Long vacationId) {
        boolean deleteCondition = vacationRepository.existsById(vacationId);
        if (!deleteCondition) {
            log.warn(String.format(LOG_FAIL_ID_DELETE_MSG, vacationId));
            infoContributor.incrementFailedDeleteOperations();
            return vacationId;
        }
        vacationRepository.deleteById(vacationId);
        log.info(String.format(LOG_SUCCESS_ID_DELETE_MSG, vacationId));
        return vacationId;
    }

    public Long deleteVacationByDoctorAndDate(String doctorName, LocalDate date)
            throws DataNotFoundException {
        Optional<DoctorEntity> doctorEntityOptional = doctorRepository.findByName(doctorName);
        if (doctorEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(VacationService.LOG_FAIL_DOCTOR_DELETE_MSG, doctorName));
            throw new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorName));
        }

        DoctorEntity doctorEntity = doctorEntityOptional.get();
        Boolean deleteCondition = vacationRepository.existsByDoctorAndStartDate(doctorEntity, date);
        if (deleteCondition) {
            vacationRepository.deleteByDoctorAndStartDate(doctorEntity, date);
            log.info(String.format(LOG_SUCCESS_DOCTOR_DELETE_MSG, doctorName, date));
            return doctorEntity.getId();
        }

        infoContributor.incrementFailedDeleteOperations();
        log.warn(String.format(LOG_NOT_FOUND_MSG, doctorName, date));
        return doctorEntity.getId();
    }
}
