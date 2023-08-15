package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.DoctorConverter;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.enums.VacationStatus;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;
import ro.felixcirebea.medicalsys.util.DeleteUtility;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DoctorService {

    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String LOG_UPDATE_MSG = "%s was updated as follows: %s";
    public static final String LOG_INSERT_MSG = "%s was inserted";
    public static final String LOG_FAIL_DELETE_MSG = "Delete of doctor: %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Doctor: %s deleted";
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorConverter doctorConverter;
    private final Contributor infoContributor;
    private final WorkingHoursService workingHoursService;
    private final AppointmentService appointmentService;
    private final DeleteUtility deleteUtility;

    public DoctorService(DoctorRepository doctorRepository,
                         SpecialtyRepository specialtyRepository,
                         DoctorConverter doctorConverter,
                         Contributor infoContributor,
                         WorkingHoursService workingHoursService,
                         AppointmentService appointmentService,
                         DeleteUtility deleteUtility) {
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorConverter = doctorConverter;
        this.infoContributor = infoContributor;
        this.workingHoursService = workingHoursService;
        this.appointmentService = appointmentService;
        this.deleteUtility = deleteUtility;
    }

    public Long upsertDoctor(DoctorDto doctorDto)
            throws DataNotFoundException {
        SpecialtyEntity specialtyEntity =
                specialtyRepository.findByNameAndIsActive(doctorDto.getSpecialty(), true)
                        .orElseThrow(() -> new DataNotFoundException(
                                String.format(NOT_FOUND_MSG, doctorDto.getSpecialty())));
        if (doctorDto.getId() != null) {
            return updateDoctor(doctorDto, specialtyEntity);
        }

        log.info(String.format(LOG_INSERT_MSG, doctorDto.getName()));
        return doctorRepository.save(doctorConverter.fromDtoToEntity(doctorDto, specialtyEntity)).getId();
    }

    private Long updateDoctor(DoctorDto doctorDto, SpecialtyEntity specialtyEntity)
            throws DataNotFoundException {
        DoctorEntity doctorEntity =
                doctorRepository.findByIdAndIsActive(doctorDto.getId(), true)
                        .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        String nameToLog = doctorEntity.getName();

        doctorEntity.setName(doctorDto.getName());
        doctorEntity.setSpecialty(specialtyEntity);
        doctorEntity.setPriceRate(doctorDto.getPriceRate());
        log.info(String.format(LOG_UPDATE_MSG, nameToLog, doctorDto));
        return doctorRepository.save(doctorEntity).getId();
    }

    public DoctorDto getDoctorById(Long doctorId)
            throws DataNotFoundException {
        DoctorEntity doctorEntity =
                doctorRepository.findByIdAndIsActive(doctorId, true)
                        .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return doctorConverter.fromEntityToDto(doctorEntity);
    }

    public DoctorDto getDoctorByName(String doctorName)
            throws DataNotFoundException {
        DoctorEntity doctorEntity =
                doctorRepository.findByNameAndIsActive(doctorName, true)
                        .orElseThrow(() -> new DataNotFoundException(
                                String.format(NOT_FOUND_MSG, doctorName)));
        return doctorConverter.fromEntityToDto(doctorEntity);
    }

    public List<DoctorDto> getDoctorsBySpecialty(String specialtyName)
            throws DataNotFoundException {
        SpecialtyEntity specialtyEntity =
                specialtyRepository.findByNameAndIsActive(specialtyName, true)
                        .orElseThrow(() -> new DataNotFoundException(
                                String.format(NOT_FOUND_MSG, specialtyName)));
        return specialtyEntity.getDoctors().stream()
                .filter(doc -> doc.getIsActive().equals(true))
                .map(doctorConverter::fromEntityToDto)
                .toList();
    }

    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAllByIsActive(true).stream()
                .map(doctorConverter::fromEntityToDto)
                .toList();
    }

    public Long deleteDoctorById(Long doctorId) {
        Optional<DoctorEntity> doctorEntityOptional =
                doctorRepository.findByIdAndIsActive(doctorId, true);

        DoctorEntity doctorEntity = deleteUtility.softDeleteById(
                doctorId, doctorEntityOptional,
                LOG_FAIL_DELETE_MSG, LOG_SUCCESS_DELETE_MSG, infoContributor);

        if (doctorEntity == null) {
            return doctorId;
        }

        cascadeSoftDelete(doctorEntity);

        return doctorRepository.save(doctorEntity).getId();
    }

    public Long deleteDoctorByName(String doctorName)
            throws DataNotFoundException {
        Optional<DoctorEntity> doctorEntityOptional =
                doctorRepository.findByNameAndIsActive(doctorName, true);

        DoctorEntity doctorEntity = deleteUtility.softDeleteByField(
                doctorName, doctorEntityOptional, doctorRepository,
                LOG_FAIL_DELETE_MSG, LOG_SUCCESS_DELETE_MSG, NOT_FOUND_MSG, infoContributor);

        cascadeSoftDelete(doctorEntity);

        return doctorRepository.save(doctorEntity).getId();
    }

    private void cascadeSoftDelete(DoctorEntity doctorEntity) {
        doctorEntity.getVacation().stream()
                .filter(vac -> !vac.getStatus().equals(VacationStatus.DONE))
                .forEach(undoneVac -> undoneVac.setStatus(VacationStatus.CANCELED));
        log.info(workingHoursService.deleteAllWorkingHoursForDoctor(doctorEntity));
        log.info(appointmentService.cancelAllAppointmentForDoctor(doctorEntity));
    }
}
