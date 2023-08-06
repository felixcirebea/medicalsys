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
import ro.felixcirebea.medicalsys.util.Validator;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class VacationService {

    private final VacationRepository vacationRepository;
    private final DoctorRepository doctorRepository;
    private final VacationConverter vacationConverter;

    public VacationService(VacationRepository vacationRepository, DoctorRepository doctorRepository,
                           VacationConverter vacationConverter) {
        this.vacationRepository = vacationRepository;
        this.doctorRepository = doctorRepository;
        this.vacationConverter = vacationConverter;
    }


    public Long upsertVacation(VacationDto vacationDto) throws DataNotFoundException, DataMismatchException {
        DoctorEntity doctorEntity = doctorRepository.findByName(vacationDto.getDoctor())
                .orElseThrow(() -> new DataNotFoundException(String.format(
                        "Doctor %s not found", vacationDto.getDoctor())));
        if (vacationDto.getId() != null) {
            return updateVacation(vacationDto, doctorEntity);
        }
        log.info(String.format("Vacation for doctor %s was saved", doctorEntity.getName()));
        return vacationRepository.save(vacationConverter.fromDtoToEntity(vacationDto, doctorEntity)).getId();
    }

    private Long updateVacation(VacationDto vacationDto, DoctorEntity doctorEntity)
            throws DataNotFoundException {
        VacationEntity vacationEntity = vacationRepository.findById(vacationDto.getId())
                .orElseThrow(() -> new DataNotFoundException("Wrong ID"));

        vacationEntity.setDoctor(doctorEntity);
        vacationEntity.setStartDate(vacationDto.getStartDate());
        vacationEntity.setEndDate(vacationDto.getEndDate());
        vacationEntity.setType(vacationDto.getType());

        log.info(String.format("Vacation for doctor %s was updated", doctorEntity.getName()));
        return vacationRepository.save(vacationEntity).getId();
    }

    public List<VacationDto> getVacationByDoctorAndDates(String doctorName, String startDate, String endDate)
            throws DataNotFoundException, DataMismatchException {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName).orElseThrow(() ->
                new DataNotFoundException(String.format("Doctor with name %s not found", doctorName)));

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
            DoctorEntity doctorEntity = doctorRepository.findByName(doctorName).orElseThrow(() ->
                    new DataNotFoundException(String.format("Doctor with name %s not found", doctorName)));
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
}
