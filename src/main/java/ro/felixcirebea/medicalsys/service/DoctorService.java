package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.DoctorConverter;
import ro.felixcirebea.medicalsys.dto.DoctorDto;
import ro.felixcirebea.medicalsys.entity.DoctorEntity;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.DoctorRepository;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class DoctorService {

    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String LOG_UPDATE_MSG = "%s was updated as follows: %s";
    public static final String LOG_INSERT_MSG = "%s was inserted";
    public static final String LOG_FAIL_DELETE_MSG = "Delete of doctor: %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Doctor id: %s deleted";
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorConverter doctorConverter;
    private final Contributor infoContributor;

    public DoctorService(DoctorRepository doctorRepository,
                         SpecialtyRepository specialtyRepository,
                         DoctorConverter doctorConverter,
                         Contributor infoContributor) {
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorConverter = doctorConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertDoctor(DoctorDto doctorDto) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(doctorDto.getSpecialty())
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorDto.getSpecialty())));
        if (doctorDto.getId() != null) {
            return updateDoctor(doctorDto, specialtyEntity);
        }

        log.info(String.format(LOG_INSERT_MSG, doctorDto.getName()));
        return doctorRepository.save(doctorConverter.fromDtoToEntity(doctorDto, specialtyEntity)).getId();
    }

    private Long updateDoctor(DoctorDto doctorDto, SpecialtyEntity specialtyEntity)
            throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findById(doctorDto.getId())
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));

        doctorEntity.setName(doctorDto.getName());
        doctorEntity.setSpecialty(specialtyEntity);
        doctorEntity.setPriceRate(doctorDto.getPriceRate());
        log.info(String.format(LOG_UPDATE_MSG, doctorDto.getName(), doctorDto));
        return doctorRepository.save(doctorEntity).getId();
    }

    public DoctorDto getDoctorById(Long doctorId) throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return doctorConverter.fromEntityToDto(doctorEntity);
    }

    public DoctorDto getDoctorByName(String doctorName) throws DataNotFoundException {
        DoctorEntity doctorEntity = doctorRepository.findByName(doctorName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorName)));
        return doctorConverter.fromEntityToDto(doctorEntity);
    }

    public List<DoctorDto> getDoctorsBySpecialty(String specialtyName) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(specialtyName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, specialtyName)));
        return specialtyEntity.getDoctors().stream()
                .map(doctorConverter::fromEntityToDto)
                .toList();
    }

    public List<DoctorDto> getAllDoctors() {
        return StreamSupport.stream(doctorRepository.findAll().spliterator(), false)
                .map(doctorConverter::fromEntityToDto)
                .toList();
    }

    public Long deleteDoctorById(Long doctorId) {
        boolean deleteCondition = doctorRepository.existsById(doctorId);
        if (!deleteCondition) {
            log.warn(String.format(LOG_FAIL_DELETE_MSG, doctorId));
            infoContributor.incrementFailedDeleteOperations();
            return doctorId;
        }
        doctorRepository.deleteById(doctorId);
        log.info(String.format(LOG_SUCCESS_DELETE_MSG, doctorId));
        return doctorId;
    }

    public Long deleteDoctorByName(String doctorName) throws DataNotFoundException {
        Optional<DoctorEntity> doctorEntityOptional = doctorRepository.findByName(doctorName);

        if (doctorEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(LOG_FAIL_DELETE_MSG, doctorName));
            throw new DataNotFoundException(String.format(NOT_FOUND_MSG, doctorName));
        }

        Long doctorId = doctorEntityOptional.get().getId();
        doctorRepository.deleteById(doctorId);
        return doctorId;
    }
}
