package ro.felixcirebea.medicalsys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.felixcirebea.medicalsys.converter.SpecialtyConverter;
import ro.felixcirebea.medicalsys.dto.SpecialtyDto;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;
import ro.felixcirebea.medicalsys.exception.DataNotFoundException;
import ro.felixcirebea.medicalsys.repository.SpecialtyRepository;
import ro.felixcirebea.medicalsys.util.Contributor;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class SpecialtyService {

    public static final String WRONG_ID_MSG = "Wrong ID";
    public static final String NOT_FOUND_MSG = "%s not found";
    public static final String LOG_INSERT_MSG = "%s was inserted";
    public static final String LOG_UPDATE_MSG = "%s was updated as follows: %s";
    public static final String LOG_FAIL_DELETE_MSG = "Delete of specialty: %s failed - not found";
    public static final String LOG_SUCCESS_DELETE_MSG = "Specialty id: %s deleted";
    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyConverter specialtyConverter;
    private final Contributor infoContributor;

    public SpecialtyService(SpecialtyRepository specialtyRepository,
                            SpecialtyConverter specialtyConverter,
                            Contributor infoContributor) {
        this.specialtyRepository = specialtyRepository;
        this.specialtyConverter = specialtyConverter;
        this.infoContributor = infoContributor;
    }

    public Long upsertSpecialty(SpecialtyDto specialtyDto) throws DataNotFoundException {
        if (specialtyDto.getId() != null) {
            return updateSpecialty(specialtyDto);
        }

        log.info(String.format(LOG_INSERT_MSG, specialtyDto.getName()));
        return specialtyRepository.save(specialtyConverter.fromDtoToEntity(specialtyDto)).getId();
    }

    private Long updateSpecialty(SpecialtyDto specialtyDto) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(specialtyDto.getId())
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));

        specialtyEntity.setName(specialtyDto.getName());
        log.info(String.format(LOG_UPDATE_MSG, specialtyDto.getName(), specialtyDto));
        return specialtyRepository.save(specialtyEntity).getId();
    }

    public String getSpecialtyById(Long specialtyId) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new DataNotFoundException(WRONG_ID_MSG));
        return specialtyEntity.getName();
    }

    public String getSpecialtyByName(String specialtyName) throws DataNotFoundException {
        SpecialtyEntity specialtyEntity = specialtyRepository.findByName(specialtyName)
                .orElseThrow(() -> new DataNotFoundException(String.format(NOT_FOUND_MSG, specialtyName)));
        return specialtyEntity.getName();
    }

    public List<SpecialtyDto> getAllSpecialties() {
        return StreamSupport.stream(specialtyRepository.findAll().spliterator(), false)
                .map(specialtyConverter::fromEntityToDto)
                .toList();
    }

    public Long deleteSpecialtyById(Long specialtyId) {
        boolean deleteCondition = specialtyRepository.existsById(specialtyId);
        if (!deleteCondition) {
            log.warn(String.format(LOG_FAIL_DELETE_MSG, specialtyId));
            infoContributor.incrementFailedDeleteOperations();
            return specialtyId;
        }

        specialtyRepository.deleteById(specialtyId);
        log.info(String.format(LOG_SUCCESS_DELETE_MSG, specialtyId));
        return specialtyId;
    }

    public Long deleteSpecialtyByName(String specialtyName) throws DataNotFoundException {
        Optional<SpecialtyEntity> specialtyEntityOptional = specialtyRepository.findByName(specialtyName);

        if (specialtyEntityOptional.isEmpty()) {
            infoContributor.incrementFailedDeleteOperations();
            log.warn(String.format(LOG_FAIL_DELETE_MSG, specialtyName));
            throw new DataNotFoundException(String.format(NOT_FOUND_MSG, specialtyName));
        }

        Long specialtyId = specialtyEntityOptional.get().getId();
        specialtyRepository.deleteById(specialtyId);
        return specialtyId;
    }

}
