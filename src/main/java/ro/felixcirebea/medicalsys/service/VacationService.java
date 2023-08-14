package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.VacationConverter;
import ro.felixcirebea.medicalsys.dto.VacationDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.VacationEntity;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.enums.VacationType;
import ro.felixcirebea.medicalsys.exception.ConcurrencyException;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.VacationRepository;
import ro.felixcirebea.medicalsys.util.Contributor;
import ro.felixcirebea.medicalsys.util.Validator;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class VacationService {

    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String LOG_UPDATE_MSG = "%s vacation starting %s canceled";
    public static final String LOG_INSERT_MSG = "Vacation for doctor %s was inserted";
    public static final String VACATION_DATE_ERROR_MSG = "Can't cancel vacations from the past";
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

    public Long insertVacation(VacationDto vacationDto)
            throws DataNotFoundException, DataMismatchException, ConcurrencyException {
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(vacationDto.getDoctor(), true)
                        .orElseThrow(() -> new DataNotFoundException(
                                String.format(NOT_FOUND_MSG, vacationDto.getDoctor())));
        if (vacationDto.getStartDate().isBefore(infoContributor.getCurrentDate())) {
            throw new ConcurrencyException(VACATION_DATE_ERROR_MSG);
        }
        log.info(String.format(LOG_INSERT_MSG, doctorEntity.getName()));
        return vacationRepository.save(
                vacationConverter.fromDtoToEntity(vacationDto, doctorEntity)).getId();
    }

    public Long cancelVacation(String doctorName, String startDate)
            throws DataNotFoundException, DataMismatchException, ConcurrencyException {

        LocalDate startDateValue = Validator.dateValidator(startDate);
        if (startDateValue.isBefore(infoContributor.getCurrentDate())) {
            throw new ConcurrencyException(VACATION_DATE_ERROR_MSG);
        }
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(doctorName, true)
                        .orElseThrow(() -> new DataNotFoundException(
                                String.format(NOT_FOUND_MSG, doctorName)));

        VacationEntity vacationEntity =
                vacationRepository.findByDoctorAndStartDate(doctorEntity, startDateValue)
                        .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));

        vacationEntity.setStatus(VacationStatus.CANCELED);

        log.info(String.format(LOG_UPDATE_MSG, doctorEntity.getName(), vacationEntity));
        return vacationRepository.save(vacationEntity).getId();
    }

    public List<VacationDto> getVacationByDoctorAndDates(
            String doctorName, String startDate, String endDate)
            throws DataNotFoundException, DataMismatchException {
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(doctorName, true)
                        .orElseThrow(() -> new DataNotFoundException(
                                String.format(NOT_FOUND_MSG, doctorName)));

        LocalDate startDateValue =
                StringUtils.isNotBlank(startDate) ? Validator.dateValidator(startDate) : null;
        LocalDate endDateValue =
                StringUtils.isNotBlank(endDate) ? Validator.dateValidator(endDate) : null;

        if (startDateValue != null && endDateValue != null) {
            return vacationRepository
                    .findAllByDoctorAndStartDateAfterAndEndDateBefore(
                            doctorEntity, startDateValue, endDateValue)
                    .stream()
                    .map(vacationConverter::fromEntityToDto)
                    .toList();
        } else if (startDateValue != null) {
            return vacationRepository.findAllByDoctorAndStartDateAfter(
                            doctorEntity, startDateValue)
                    .stream()
                    .map(vacationConverter::fromEntityToDto)
                    .toList();
        } else if (endDateValue != null) {
            return vacationRepository.findAllByDoctorAndEndDateBefore(
                            doctorEntity, endDateValue)
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
            DoctorEntity doctorEntity =
                    doctorRepository.findByNameAndIsActive(doctorName, true)
                    .orElseThrow(() -> new DataNotFoundException(
                            String.format(NOT_FOUND_MSG, doctorName)));
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

    public Boolean isDateVacation(String doctorName, LocalDate date)
            throws DataNotFoundException {
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(doctorName, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, doctorName)));
        return vacationRepository.isDateBetweenVacation(doctorEntity, date);
    }

    public List<VacationDto> getVacationByStatus(String doctorName, VacationStatus statusValue)
            throws DataNotFoundException {
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(doctorName, true)
                        .orElseThrow(() -> new DataNotFoundException(
                                String.format(NOT_FOUND_MSG, doctorName)));
        return vacationRepository.findAllByDoctorAndStatus(doctorEntity, statusValue)
                .stream()
                .map(vacationConverter::fromEntityToDto)
                .toList();
    }
}
