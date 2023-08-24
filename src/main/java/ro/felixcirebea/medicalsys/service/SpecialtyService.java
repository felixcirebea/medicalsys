package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.SpecialtyConverter;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.InvestigationRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.helper.Contributor;
import ro.felixcirebea.medicalsys.helper.DeleteUtility;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SpecialtyService {

    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String LOG_INSERT_MSG = "%s was inserted";
    public static final String LOG_UPDATE_MSG = "%s was updated as follows: %s";
    public static final String LOG_FAIL_DELETE_MSG = "Delete of specialty: %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Specialty: %s deleted";
    public static final String LOG_SUCCESS_CASCADE_DELETE_MSG = "Deleted: %s";
    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyConverter specialtyConverter;
    private final DoctorRepository doctorRepository;
    private final InvestigationRepository investigationRepository;
    private final WorkingHoursService workingHoursService;
    private final AppointmentService appointmentService;
    private final Contributor infoContributor;
    private final DeleteUtility deleteUtility;

    public SpecialtyService(SpecialtyRepository specialtyRepository,
                            SpecialtyConverter specialtyConverter,
                            DoctorRepository doctorRepository,
                            InvestigationRepository investigationRepository,
                            WorkingHoursService workingHoursService,
                            AppointmentService appointmentService,
                            Contributor infoContributor,
                            DeleteUtility deleteUtility) {
        this.specialtyRepository = specialtyRepository;
        this.specialtyConverter = specialtyConverter;
        this.doctorRepository = doctorRepository;
        this.investigationRepository = investigationRepository;
        this.workingHoursService = workingHoursService;
        this.appointmentService = appointmentService;
        this.infoContributor = infoContributor;
        this.deleteUtility = deleteUtility;
    }

    public Long upsertSpecialty(SpecialtyDto specialtyDto)
            throws DataNotFoundException {
        if (specialtyDto.getId() != null) {
            return updateSpecialty(specialtyDto);
        }

        log.info(String.format(LOG_INSERT_MSG, specialtyDto.getName()));
        return specialtyRepository.save(specialtyConverter.fromDtoToEntity(specialtyDto)).getId();
    }

    private Long updateSpecialty(SpecialtyDto specialtyDto)
            throws DataNotFoundException {
        SpecialtyEntity specialtyEntity =
                specialtyRepository.findByIdAndIsActive(specialtyDto.getId(), true)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        String nameToLog = specialtyEntity.getName();

        specialtyEntity.setName(specialtyDto.getName());
        log.info(String.format(LOG_UPDATE_MSG, nameToLog, specialtyDto));
        return specialtyRepository.save(specialtyEntity).getId();
    }

    public String getSpecialtyById(Long specialtyId)
            throws DataNotFoundException {
        SpecialtyEntity specialtyEntity =
                specialtyRepository.findByIdAndIsActive(specialtyId, true)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return specialtyEntity.getName();
    }

    public String getSpecialtyByName(String specialtyName)
            throws DataNotFoundException {
        SpecialtyEntity specialtyEntity =
                specialtyRepository.findByNameAndIsActive(specialtyName, true)
                .orElseThrow(() -> new DataNotFoundException(
                        String.format(NOT_FOUND_MSG, specialtyName)));
        return specialtyEntity.getName();
    }

    public List<SpecialtyDto> getAllSpecialties() {
        return specialtyRepository.findAllByIsActive(true).stream()
                .map(specialtyConverter::fromEntityToDto)
                .toList();
    }

    public Long deleteSpecialtyById(Long specialtyId) {
        Optional<SpecialtyEntity> specialtyEntityOptional =
                specialtyRepository.findByIdAndIsActive(specialtyId, true);

        SpecialtyEntity specialtyEntity = deleteUtility.softDeleteById(
                specialtyId, specialtyEntityOptional,
                LOG_FAIL_DELETE_MSG, LOG_SUCCESS_DELETE_MSG, infoContributor);

        if (specialtyEntity == null) {
            return specialtyId;
        }

        deleteUtility.softCascadeDelete(specialtyEntity, workingHoursService, appointmentService,
                doctorRepository, investigationRepository, LOG_SUCCESS_CASCADE_DELETE_MSG);

        return specialtyRepository.save(specialtyEntity).getId();
    }

    public Long deleteSpecialtyByName(String specialtyName)
            throws DataNotFoundException {
        Optional<SpecialtyEntity> specialtyEntityOptional =
                specialtyRepository.findByNameAndIsActive(specialtyName, true);

        SpecialtyEntity specialtyEntity = deleteUtility.softDeleteByField(
                specialtyName, specialtyEntityOptional, specialtyRepository,
                LOG_FAIL_DELETE_MSG, LOG_SUCCESS_DELETE_MSG, NOT_FOUND_MSG, infoContributor);

        deleteUtility.softCascadeDelete(specialtyEntity, workingHoursService, appointmentService,
                doctorRepository, investigationRepository, LOG_SUCCESS_CASCADE_DELETE_MSG);

        return specialtyRepository.save(specialtyEntity).getId();
    }

}
